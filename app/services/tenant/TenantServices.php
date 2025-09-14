<?php
declare (strict_types = 1);

namespace app\services\tenant;

use app\dao\tenant\TenantDao;
use crmeb\basic\BaseServices;
use app\services\other\UploadService;
use crmeb\exceptions\AdminException;
use crmeb\services\CacheService;
use think\exception\ValidateException;

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
        // 验证唯一性
        if ($this->dao->checkAppidExists($data['appid'])) {
            throw new AdminException('应用ID已存在');
        }
        if ($this->dao->checkTenantCodeExists($data['tenant_code'])) {
            throw new AdminException('租户编码已存在');
        }
        if ($this->dao->checkAccountExists($data['account'])) {
            throw new AdminException('管理员账号已存在');
        }
        
        // 密码加密
        $data['pwd'] = password_hash($data['pwd'], PASSWORD_DEFAULT);
        
        // 设置默认值
        $data['status'] = $data['status'] ?? 0;
        $data['max_users'] = $data['max_users'] ?? 1000;
        $data['max_services'] = $data['max_services'] ?? 10;
        
        // 创建租户
        $tenant = $this->dao->save($data);
        
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
        
        // 更新数据
        $result = $this->dao->update($id, $data);
        
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
}