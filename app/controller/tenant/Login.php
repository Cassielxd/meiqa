<?php
declare (strict_types = 1);

namespace app\controller\tenant;

use app\Request;
use app\services\tenant\TenantLoginServices;
use app\validates\tenant\TenantLoginValidate;
use think\Response;

/**
 * 租户登录
 * Class Login
 * @package app\controller\tenant
 */
class Login
{
    /**
     * @var Request
     */
    protected $request;

    /**
     * @var TenantLoginServices
     */
    protected $services;

    /**
     * Login constructor.
     * @param TenantLoginServices $services
     */
    public function __construct(TenantLoginServices $services)
    {
        $this->services = $services;
        $this->request = app()->request;
    }

    /**
     * 租户登录
     * @return mixed
     */
    public function login()
    {
        [$account, $password] = $this->request->postMore([
            'account',
            'pwd',
        ], true);

        validate(TenantLoginValidate::class)->check(['account' => $account, 'pwd' => $password]);

        return app('json')->success($this->services->login($account, $password));
    }

    /**
     * 获取租户信息
     * @return mixed
     */
    public function info()
    {
        $tenantInfo = $this->request->tenantInfo();
        if (!$tenantInfo) {
            return app('json')->fail('请先登录');
        }

        return app('json')->success([
            'tenant_info' => [
                'id' => $tenantInfo['id'],
                'appid' => $tenantInfo['appid'],
                'tenant_name' => $tenantInfo['tenant_name'],
                'tenant_code' => $tenantInfo['tenant_code'],
                'account' => $tenantInfo['account'],
                'domain' => $tenantInfo['domain'],
                'logo' => $tenantInfo['logo'],
                'contact_name' => $tenantInfo['contact_name'],
                'contact_phone' => $tenantInfo['contact_phone'],
                'contact_email' => $tenantInfo['contact_email'],
                'status' => $tenantInfo['status'],
                'max_users' => $tenantInfo['max_users'],
                'max_services' => $tenantInfo['max_services'],
                'expire_at' => $tenantInfo['expire_at'],
                'is_expired' => $tenantInfo['is_expired'] ?? false,
                'remaining_days' => $tenantInfo['remaining_days'] ?? 0,
            ]
        ]);
    }

    /**
     * 退出登录
     * @return mixed
     */
    public function logout()
    {
        return app('json')->success('退出成功');
    }

    /**
     * 修改密码
     * @return mixed
     */
    public function changePassword()
    {
        [$oldPassword, $newPassword, $confirmPassword] = $this->request->postMore([
            'old_password',
            'new_password',
            'confirm_password',
        ], true);

        if ($newPassword !== $confirmPassword) {
            return app('json')->fail('两次密码输入不一致');
        }

        validate(TenantLoginValidate::class)->scene('change_password')->check([
            'old_password' => $oldPassword,
            'new_password' => $newPassword,
        ]);

        $tenantInfo = $this->request->tenantInfo();
        if (!$tenantInfo) {
            return app('json')->fail('请先登录');
        }

        try {
            $this->services->changePassword((int)$tenantInfo['id'], $oldPassword, $newPassword);
            return app('json')->success('密码修改成功，请重新登录');
        } catch (\Exception $e) {
            return app('json')->fail($e->getMessage());
        }
    }
}