<?php
declare (strict_types = 1);

namespace app\controller\tenant;

use app\services\tenant\TenantServices;
use app\validates\tenant\TenantValidate;
use app\Request;
use crmeb\traits\Help;
use crmeb\exceptions\AdminException;

/**
 * 租户自助管理控制器
 * 只允许租户管理自己的信息，不允许管理其他租户
 * Class Tenant
 * @package app\controller\tenant
 */
class Tenant
{
    use Help;
    
    /**
     * @var TenantServices
     */
    protected $services;
    
    /**
     * @var Request
     */
    protected $request;

    /**
     * Tenant constructor.
     * @param TenantServices $services
     */
    public function __construct(TenantServices $services)
    {
        $this->services = $services;
        $this->request = app()->request;
    }

    /**
     * 获取当前租户信息
     * @return mixed
     */
    public function info()
    {
        // 从token中获取当前租户ID
        $tenantId = $this->request->tenantId();
        if (!$tenantId) {
            return $this->fail('未登录或登录已过期');
        }
        
        try {
            $info = $this->services->getTenantInfo($tenantId);
            return $this->success($info);
        } catch (\Exception $e) {
            return $this->fail($e->getMessage());
        }
    }

    /**
     * 更新当前租户信息
     * @return mixed
     */
    public function update()
    {
        // 从token中获取当前租户ID
        $tenantId = $this->request->tenantId();
        if (!$tenantId) {
            return $this->fail('未登录或登录已过期');
        }
        
        $data = $this->request->postMore([
            ['tenant_name', ''],
            ['contact_name', ''],
            ['contact_phone', ''],
            ['domain', ''],
            ['logo', ''],
        ]);
        
        // 过滤空值
        $data = array_filter($data, function($value) {
            return $value !== '';
        });
        
        // 租户不能修改敏感字段，如appid、tenant_code、status、max_users、contact_email等
        // 这些字段只能由管理员修改
        // contact_email是登录账号，不允许租户修改
        
        try {
            $result = $this->services->updateTenant($tenantId, $data);
            return $this->success('更新成功');
        } catch (\Exception $e) {
            return $this->fail($e->getMessage());
        }
    }

    /**
     * 修改密码
     * @return mixed
     */
    public function changePassword()
    {
        // 从token中获取当前租户ID
        $tenantId = $this->request->tenantId();
        if (!$tenantId) {
            return $this->fail('未登录或登录已过期');
        }
        
        $data = $this->request->postMore([
            ['old_password', ''],
            ['new_password', ''],
            ['confirm_password', ''],
        ]);
        
        // 验证参数
        if (empty($data['old_password']) || empty($data['new_password']) || empty($data['confirm_password'])) {
            return $this->fail('请填写完整信息');
        }
        
        if ($data['new_password'] !== $data['confirm_password']) {
            return $this->fail('两次输入的新密码不一致');
        }
        
        if (strlen($data['new_password']) < 6) {
            return $this->fail('新密码长度不能少于6位');
        }
        
        try {
            $result = $this->services->updatePassword($tenantId, $data['old_password'], $data['new_password']);
            return $this->success('密码修改成功');
        } catch (\Exception $e) {
            return $this->fail($e->getMessage());
        }
    }

    /**
     * 获取当前租户状态信息
     * @return mixed
     */
    public function status()
    {
        // 从token中获取当前租户信息
        $tenantInfo = $this->request->tenantInfo();
        if (!$tenantInfo) {
            return $this->fail('未登录或登录已过期');
        }
        
        return $this->success([
            'status' => $tenantInfo['status'],
            'status_text' => \app\models\tenant\Tenant::$statusMap[$tenantInfo['status']] ?? '未知',
            'is_expired' => $tenantInfo['is_expired'] ?? false,
            'remaining_days' => $tenantInfo['remaining_days'] ?? -1,
            'expire_at' => $tenantInfo['expire_at'] ?? null,
        ]);
    }
}