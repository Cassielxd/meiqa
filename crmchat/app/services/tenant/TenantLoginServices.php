<?php
declare (strict_types = 1);

namespace app\services\tenant;

use app\dao\tenant\TenantDao;
use app\models\tenant\Tenant;
use crmeb\basic\BaseServices;
use crmeb\exceptions\AdminException;
use crmeb\services\CacheService;
use think\exception\ValidateException;

/**
 * 租户登录服务
 * Class TenantLoginServices
 * @package app\services\tenant
 * @mixin TenantDao
 */
class TenantLoginServices extends BaseServices
{
    /**
     * TenantLoginServices constructor.
     * @param TenantDao $dao
     */
    public function __construct(TenantDao $dao)
    {
        $this->dao = $dao;
    }

    public function getLoginInfo()
    {
        return [
            'slide' => sys_data('admin_login_slide') ?? [],
            'logo_square' => sys_config('site_logo_square'),//透明
            'logo_rectangle' => sys_config('site_logo'),//方形
            'login_logo' => sys_config('login_logo'),//登陆
            'site_name' => sys_config('site_name')
        ];
    }

    /**
     * 租户登录
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
        if ($tenant['status'] !== Tenant::STATUS_APPROVED) {
            $statusText = Tenant::$statusMap[$tenant['status']] ?? '未知状态';
            throw new AdminException('租户状态异常：' . $statusText);
        }
        
        // 检查是否过期
        if (!empty($tenant['expire_at']) && strtotime($tenant['expire_at']) < time()) {
            throw new AdminException('租户已过期，请联系管理员');
        }
        
        // 创建token
        $tokenInfo = $this->createToken($tenant['id'], 'tenant');
        
        // 更新最后登录时间
        $this->dao->update($tenant['id'], [
            'updated_at' => date('Y-m-d H:i:s')
        ]);
        
        // 返回登录信息
        return [
            'token' => $tokenInfo['token'],
            'expires_time' => $tokenInfo['params']['exp'],
            'tenant_info' => [
                'id' => $tenant['id'],
                'appid' => $tenant['appid'],
                'tenant_name' => $tenant['tenant_name'],
                'tenant_code' => $tenant['tenant_code'],
                'account' => $tenant['account'],
                'domain' => $tenant['domain'],
                'logo' => $tenant['logo'],
                'contact_name' => $tenant['contact_name'],
                'contact_phone' => $tenant['contact_phone'],
                'contact_email' => $tenant['contact_email'],
                'status' => $tenant['status'],
                'max_users' => $tenant['max_users'],
                'max_services' => $tenant['max_services'],
                'expire_at' => $tenant['expire_at'],
                'is_expired' => !empty($tenant['expire_at']) && strtotime($tenant['expire_at']) < time(),
                'remaining_days' => $this->calculateRemainingDays($tenant['expire_at']),
            ]
        ];
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
     * 修改密码
     * @param int $id
     * @param string $oldPassword
     * @param string $newPassword
     * @return bool
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function changePassword(int $id, string $oldPassword, string $newPassword)
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