<?php
declare (strict_types = 1);

namespace app\services\tenant;

use app\dao\tenant\TenantDao;
use app\models\tenant\Tenant;
use app\services\ApplicationServices;
use crmeb\basic\BaseServices;
use app\services\other\UploadService;
use crmeb\exceptions\AdminException;
use crmeb\services\CacheService;
use think\exception\ValidateException;
use think\facade\Cache;

/**
 * 租户业务逻辑层
 * Class TenantServices
 * @package app\services\tenant
 * @mixin TenantDao
 */
class TenantServices extends BaseServices
{
    /**
     * TenantServices constructor.
     * @param TenantDao $dao
     */
    public function __construct(TenantDao $dao)
    {
        $this->dao = $dao;
    }

    /**
     * 获取租户列表
     * @param array $where
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getTenantList(array $where)
    {
        [$page, $limit] = $this->getPageValue();
        $list = $this->dao->getTenantsList($where, $page, $limit);
        $count = $this->dao->getTenantsCount($where);
        
        // 处理数据
        foreach ($list as &$item) {
            $item['is_expired'] = !empty($item['expire_at']) && strtotime($item['expire_at']) < time();
            $item['remaining_days'] = $this->calculateRemainingDays($item['expire_at']);
        }
        
        return compact('list', 'count');
    }

    /**
     * 计算剩余天数
     * @param string|null $expireAt
     * @return int
     */
    private function calculateRemainingDays($expireAt): int
    {
        if (empty($expireAt)) {
            return -1; // 永久有效
        }
        $days = (strtotime($expireAt) - time()) / 86400;
        return max(0, (int)$days);
    }

    /**
     * 获取租户详情
     * @param int $id
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getTenantInfo(int $id)
    {
        $info = $this->dao->get($id);
        if (!$info) {
            throw new AdminException('租户不存在');
        }
        
        $info = $info->toArray();
        $info['is_expired'] = !empty($info['expire_at']) && strtotime($info['expire_at']) < time();
        $info['remaining_days'] = $this->calculateRemainingDays($info['expire_at']);
        
        return $info;
    }

    /**
     * 创建租户
     * @param array $data
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function createTenant(array $data)
    {
        // 验证账号唯一性
        if ($this->dao->checkAccountExists($data['account'])) {
            throw new AdminException('管理员账号已存在');
        }

        // 生成唯一的租户编码
        $tenantCode = $this->generateTenantCode();

        // 使用ApplicationServices生成应用信息
        /** @var ApplicationServices $applicationServices */
        $applicationServices = app()->make(ApplicationServices::class);
        $appInfo = $applicationServices->generateAppInfo();
        $appId = $appInfo['appid'];

        // 密码加密
        if (!empty($data['pwd'])) {
            $data['pwd'] = password_hash($data['pwd'], PASSWORD_DEFAULT);
        }

        // 设置必要字段
        $data['tenant_code'] = $tenantCode;
        $data['appid'] = $appId;
        $data['tenant_name'] =  $data['tenant_name'];
        $data['contact_name'] =  $data['tenant_name'];
        $data['contact_email'] = $data['account'] ?? '';
        $data['contact_phone'] = $data['contact_phone'] ?? '';

        // 设置默认值
        $data['status'] = $data['status'] ?? 0; // 管理员创建默认为启用状态
        $data['max_users'] = $data['user_limit'] ?? 1000;
        $data['max_services'] = $data['service_limit'] ?? 10;
        $data['created_at'] = date('Y-m-d H:i:s');
        $data['updated_at'] = date('Y-m-d H:i:s');

        // 处理到期时间
        if (!empty($data['expire_time']) && $data['expire_time'] !== '') {
            $data['expire_at'] = $data['expire_time'];
        } else {
            $data['expire_at'] = null; // 空值设置为null
        }
        unset($data['expire_time']);

        // 创建租户
        $tenant = $this->dao->save($data);

        // 如果租户状态为启用（1），自动创建应用数据
        if ($data['status'] == 1) {
            $this->createTenantApplication($tenant, $appInfo);
        }

        // 清除缓存
        $this->clearTenantCache();

        return $tenant->toArray();
    }

    /**
     * 更新租户
     * @param int $id
     * @param array $data
     * @return bool
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function updateTenant(int $id, array $data)
    {
        $tenant = $this->dao->get($id);
        if (!$tenant) {
            throw new AdminException('租户不存在');
        }

        // 验证唯一性
        if (isset($data['appid']) && $this->dao->checkAppidExists($data['appid'], $id)) {
            throw new AdminException('应用ID已存在');
        }
        if (isset($data['tenant_code']) && $this->dao->checkTenantCodeExists($data['tenant_code'], $id)) {
            throw new AdminException('租户编码已存在');
        }
        if (isset($data['account']) && $this->dao->checkAccountExists($data['account'], $id)) {
            throw new AdminException('管理员账号已存在');
        }

        // 密码处理
        if (!empty($data['pwd'])) {
            $data['pwd'] = password_hash($data['pwd'], PASSWORD_DEFAULT);
        } else {
            unset($data['pwd']);
        }

        // 检查状态变化：如果更新为启用状态(1)，需要检查应用信息
        $oldStatus = $tenant['status'];
        $newStatus = isset($data['status']) ? $data['status'] : $oldStatus;

        // 更新数据
        $result = $this->dao->update($id, $data);

        // 如果状态更新为启用(1)，检查并生成应用信息
        if ($result && $newStatus == 1) {
            $this->checkAndCreateApplication($tenant, $data);
        }

        // 清除缓存
        $this->clearTenantCache($tenant['appid']);
        if (isset($data['appid']) && $data['appid'] != $tenant['appid']) {
            $this->clearTenantCache($data['appid']);
        }

        return $result !== false;
    }

    /**
     * 删除租户
     * @param int $id
     * @return bool
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function deleteTenant(int $id)
    {
        $tenant = $this->dao->get($id);
        if (!$tenant) {
            throw new AdminException('租户不存在');
        }
        
        // TODO: 检查是否有关联数据，如有则不允许删除
        
        $result = $this->dao->delete($id);
        
        // 清除缓存
        $this->clearTenantCache($tenant['appid']);
        
        return $result !== false;
    }

    /**
     * 更新租户状态
     * @param int $id
     * @param int $status
     * @return bool
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function updateStatus(int $id, int $status)
    {
        $tenant = $this->dao->get($id);
        if (!$tenant) {
            throw new AdminException('租户不存在');
        }
        
        $result = $this->dao->update($id, ['status' => $status]);
        
        // 清除缓存
        $this->clearTenantCache($tenant['appid']);
        
        return $result !== false;
    }

    /**
     * 更新租户状态（别名方法，用于Admin控制器）
     * @param int $id
     * @param int $status
     * @return bool
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function updateTenantStatus(int $id, int $status)
    {
        return $this->updateStatus($id, $status);
    }

     /**
     * 批量更新租户状态
     * @param array $ids
     * @param int $status
     * @return bool
     */
    public function batchUpdateStatus(array $ids, int $status)
    {
        if (empty($ids)) {
            throw new AdminException('请选择要操作的租户');
        }
        
        // 获取所有租户的appid用于清除缓存
        $tenants = $this->dao->getColumn(['id' => $ids], 'appid');
        
        $result = $this->dao->batchUpdateStatus($ids, $status);
        
        // 清除缓存
        foreach ($tenants as $appid) {
            $this->clearTenantCache($appid);
        }
        
        return $result;
    }

    /**
     * 获取租户统计信息
     * @return array
     */
    public function getStatistics()
    {
        $statusStats = $this->dao->getStatusStatistics();
        $total = array_sum($statusStats);
        
        // 获取即将过期的租户数量
        $expiringCount = count($this->dao->getExpiringTenants(7));
        
        // 获取已过期的租户数量
        $expiredCount = count($this->dao->getExpiredTenants());
        
        return [
            'total' => $total,
            'status_stats' => $statusStats,
            'expiring_count' => $expiringCount,
            'expired_count' => $expiredCount,
        ];
    }

    /**
     * 获取租户统计信息（别名方法，用于Admin控制器）
     * @return array
     */
    public function getTenantStatistics()
    {
        return $this->getStatistics();
    }

    /**
     * 获取即将过期的租户列表
     * @param int $days
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getExpiringTenants(int $days = 7)
    {
        return $this->dao->getExpiringTenants($days);
    }

    /**
     * 根据appid获取租户信息（带缓存）
     * @param string $appid
     * @return array|null
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getTenantByAppid(string $appid)
    {
        $cacheKey = 'tenant:appid:' . $appid;
        
        return CacheService::redisHandler()->remember($cacheKey, function () use ($appid) {
            $tenant = $this->dao->getTenantByAppid($appid);
            return $tenant ? $tenant->toArray() : null;
        }, 3600); // 缓存1小时
    }

    /**
     * 验证租户是否可用
     * @param string $appid
     * @return bool
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function validateTenant(string $appid): bool
    {
        $tenant = $this->getTenantByAppid($appid);
        
        if (!$tenant) {
            return false;
        }
        
        // 检查状态
        if ($tenant['status'] !== 1) {
            return false;
        }
        
        // 检查是否过期
        if (!empty($tenant['expire_at']) && strtotime($tenant['expire_at']) < time()) {
            return false;
        }
        
        return true;
    }

    /**
     * 清除租户缓存
     * @param string|null $appid
     */
    private function clearTenantCache(string $appid = null)
    {
        if ($appid) {
            CacheService::redisHandler()->delete('tenant:appid:' . $appid);
        } else {
            // 清除所有租户缓存
            $keys = CacheService::redisHandler()->keys('tenant:appid:*');
            if ($keys) {
                CacheService::redisHandler()->delete(...$keys);
            }
        }
    }

    /**
     * 登录验证
     * @param string $account
     * @param string $password
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function login(string $account, string $password)
    {
        $tenant = $this->dao->getTenantByAccount($account);
        
        if (!$tenant) {
            throw new AdminException('账号或密码错误');
        }
        
        if (!password_verify($password, $tenant['pwd'])) {
            throw new AdminException('账号或密码错误');
        }
        
        // 检查状态
        if ($tenant['status'] !== 1) {
            throw new AdminException('租户未激活或已禁用');
        }
        
        // 检查是否过期
        if (!empty($tenant['expire_at']) && strtotime($tenant['expire_at']) < time()) {
            throw new AdminException('租户已过期，请联系管理员');
        }
        
        return $tenant->toArray();
    }

    /**
     * 更新密码
     * @param int $id
     * @param string $oldPassword
     * @param string $newPassword
     * @return bool
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function updatePassword(int $id, string $oldPassword, string $newPassword)
    {
        $tenant = $this->dao->get($id);
        if (!$tenant) {
            throw new AdminException('租户不存在');
        }
        
        if (!password_verify($oldPassword, $tenant['pwd'])) {
            throw new AdminException('原密码错误');
        }
        
        return $this->dao->update($id, [
            'pwd' => password_hash($newPassword, PASSWORD_DEFAULT)
        ]) !== false;
    }

    /**
     * 租户注册
     * @param array $data
     * @return array
     * @throws ValidateException
     */
    public function register(array $data): array
    {
        // 验证密码确认
        if (!isset($data['pwd']) || !isset($data['confirm_pwd'])) {
            throw new ValidateException('请输入密码和确认密码');
        }
        
        if ($data['pwd'] !== $data['confirm_pwd']) {
            throw new ValidateException('两次输入的密码不一致');
        }
        
        // 验证验证码
        $this->validateCaptcha($data['captcha'], $data['contact_email']);
        
        // 生成唯一的租户编码
        $tenantCode = $this->generateTenantCode();

        // 使用ApplicationServices生成应用信息
        /** @var ApplicationServices $applicationServices */
        $applicationServices = app()->make(ApplicationServices::class);
        $appInfo = $applicationServices->generateAppInfo();
        $appId = $appInfo['appid'];
        
        // 准备租户数据
        $tenantData = [
            'tenant_name' => $data['tenant_name'],
            'tenant_code' => $tenantCode,
            'appid' => $appId,
            // 使用邮箱作为账号
            'account' => $data['contact_email'],
            'pwd' => password_hash($data['pwd'], PASSWORD_DEFAULT), // 注册时设置密码
            'contact_name' => $data['contact_name'],
            'contact_phone' => $data['contact_phone'] ?? '',
            'contact_email' => $data['contact_email'],
            'status' => Tenant::STATUS_PENDING, // 待审核状态
            'max_users' => 100, // 默认最大用户数
            'max_services' => 5, // 默认最大客服数
            'created_at' => date('Y-m-d H:i:s'),
            'updated_at' => date('Y-m-d H:i:s'),
        ];
        
        // 创建租户
        $tenant = $this->dao->save($tenantData);
        if (!$tenant) {
            throw new ValidateException('注册失败，请重试');
        }
        
        // 清除验证码缓存
        $this->clearCaptcha($data['contact_email']);
        
        return [
            'tenant_id' => $tenant->id,
            'tenant_code' => $tenantCode,
            'status' => '待审核',
            'message' => '注册成功，请等待管理员审核'
        ];
    }
    
    /**
     * 验证验证码
     * @param string $captcha
     * @param string $email
     * @throws ValidateException
     */
    private function validateCaptcha(string $captcha, string $email = ''): void
    {
        // 从缓存中获取验证码
        $cacheKey = 'tenant_register_captcha_' . md5($email);
        $cachedCaptcha = Cache::get($cacheKey);
        
        if (!$cachedCaptcha) {
            throw new ValidateException('验证码已过期，请重新获取');
        }
        
        if (strtolower($captcha) !== strtolower($cachedCaptcha)) {
            throw new ValidateException('验证码错误');
        }
    }
    
    /**
     * 清除验证码缓存
     * @param string $email
     */
    private function clearCaptcha(string $email = ''): void
    {
        $cacheKey = 'tenant_register_captcha_' . md5($email);
        Cache::delete($cacheKey);
    }
    
    /**
     * 生成唯一的租户编码
     * @return string
     */
    private function generateTenantCode(): string
    {
        do {
            $code = 'tenant_' . date('Ymd') . '_' . mt_rand(1000, 9999);
            $exists = $this->dao->getOne(['tenant_code' => $code]);
        } while ($exists);
        
        return $code;
    }
    
    
    /**
     * 发送注册验证码
     * @param string $email
     * @return array
     */
    public function sendRegisterCaptcha(string $email): array
    {
        // 检查邮箱格式
        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            throw new ValidateException('请输入正确的邮箱地址');
        }
        
        // 检查是否已注册
        $exists = $this->dao->getOne(['contact_email' => $email]);
        if ($exists) {
            throw new ValidateException('该邮箱已注册');
        }
        
        // 生成验证码
        $captcha = (string)mt_rand(100000, 999999);
        
        // 缓存验证码，10分钟有效
        $cacheKey = 'tenant_register_captcha_' . md5($email);
        Cache::set($cacheKey, $captcha, 600);
        
        // TODO: 这里集成真实的邮件发送服务
        // 开发环境直接返回验证码用于测试
        return [
            'status' => 'success',
            'captcha' => $captcha,
            'message' => '验证码发送成功，开发环境验证码：' . $captcha
        ];
    }

    /**
     * 为租户创建应用数据
     * @param $tenant 租户数据
     * @param array $appInfo 应用信息
     * @return bool
     */
    private function createTenantApplication($tenant, array $appInfo)
    {
        try {
            // 获取APP_KEY用于加密token
            $appKey = config('app.key');

            // 使用Encrypter生成token（参考InstallController的逻辑）
            $encrypter = new \crmeb\utils\Encrypter($this->parseKey($appKey), 'AES-256-CBC');
            $token = $encrypter->encrypt(json_encode([
                'appid' => $appInfo['appid'],
                'app_secret' => $appInfo['app_secret'],
                'rand' => $appInfo['rand'],
                'timestamp' => $appInfo['timestamp'],
            ]));
            $tokenMd5 = md5($token);

            // 准备应用数据
            $applicationData = [
                'appid' => $appInfo['appid'],
                'name' => $tenant['tenant_name'] . '的客服应用',
                'icon' => '', // 可以设置默认图标
                'introduce' => '由系统自动为租户 ' . $tenant['tenant_name'] . ' 创建的客服应用',
                'app_secret' => $appInfo['app_secret'],
                'timestamp' => $appInfo['timestamp'],
                'rand' => $appInfo['rand'],
                'token' => $token,
                'token_md5' => $tokenMd5,
                'status' => 1, // 启用状态
                'is_delete' => 0,
                'add_time' => time(),
            ];

            // 获取ApplicationDao直接保存
            $applicationDao = app()->make(\app\dao\ApplicationDao::class);
            $result = $applicationDao->save($applicationData);

            return $result !== false;
        } catch (\Exception $e) {
            // 记录错误日志，但不阻断租户创建流程
            \think\facade\Log::error('创建租户应用失败: ' . $e->getMessage());
            return false;
        }
    }

    /**
     * 检查并创建应用信息（用于更新租户时）
     * @param $tenant 租户数据
     * @param array $updateData 更新的数据
     * @return bool
     */
    private function checkAndCreateApplication($tenant, array $updateData = [])
    {
        try {
            // 获取租户的appid
            $appid = $tenant['appid'];
            if (!$appid) {
                // 如果租户没有appid，先生成一个
                /** @var ApplicationServices $applicationServices */
                $applicationServices = app()->make(\app\services\ApplicationServices::class);
                $appInfo = $applicationServices->generateAppInfo();
                $appid = $appInfo['appid'];

                // 更新租户的appid
                $this->dao->update($tenant['id'], ['appid' => $appid]);
            } else {
                // 检查应用记录是否存在
                $applicationDao = app()->make(\app\dao\ApplicationDao::class);
                $existingApp = $applicationDao->get(['appid' => $appid, 'is_delete' => 0]);

                if ($existingApp) {
                    // 应用已存在，无需重新创建
                    return true;
                }

                // 应用不存在，有两种选择：
                // 1. 使用现有appid重新生成应用信息
                // 2. 重新生成appid和应用信息（如果现有appid有问题）

                /** @var ApplicationServices $applicationServices */
                $applicationServices = app()->make(\app\services\ApplicationServices::class);

                // 尝试使用现有appid生成应用信息
                try {
                    $appInfo = $applicationServices->generateAppSecret($appid);
                    $appInfo['appid'] = $appid;
                    $this->dao->update($tenant['id'], ['appid' => $appid]);
                } catch (\Exception $e) {
                    // 如果现有appid有问题，重新生成
                    \think\facade\Log::warning('现有appid可能有问题，重新生成: ' . $e->getMessage());
                    $appInfo = $applicationServices->generateAppInfo();
                    $appid = $appInfo['appid'];

                    // 更新租户表中的appid
                    $this->dao->update($tenant['id'], ['appid' => $appid]);
                }
            }

            // 合并租户信息和更新数据
            $tenantData = array_merge($tenant->toArray(), $updateData);

            // 确保租户数据包含正确的appid
            $tenantData['appid'] = $appid;

            // 创建应用记录
            return $this->createTenantApplication($tenantData, $appInfo);

        } catch (\Exception $e) {
            // 记录错误日志，但不阻断租户更新流程
            \think\facade\Log::error('检查并创建租户应用失败: ' . $e->getMessage());
            return false;
        }
    }

    /**
     * 解析APP_KEY（参考InstallController）
     * @param string $key
     * @return false|mixed|string
     */
    private function parseKey(string $key)
    {
        if (\think\helper\Str::startsWith($key, $prefix = 'base64:')) {
            $key = base64_decode(\crmeb\utils\Str::after($key, $prefix));
        }
        return $key;
    }
}
