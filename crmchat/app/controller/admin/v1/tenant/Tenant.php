<?php
declare (strict_types = 1);

namespace app\controller\admin\v1\tenant;

use app\controller\admin\AuthController;
use app\services\tenant\TenantServices;
use app\validates\tenant\TenantValidate;
use think\Request;

/**
 * 租户管理控制器
 * Class Tenant
 * @package app\controller\admin\v1\tenant
 */
class Tenant extends AuthController
{
    /**
     * @var TenantServices
     */
    protected $services;

    /**
     * Tenant constructor.
     * @param TenantServices $services
     */
    public function __construct(TenantServices $services)
    {
        parent::__construct();
        $this->services = $services;
    }

    /**
     * 租户列表
     * @return mixed
     */
    public function index()
    {
        $where = $this->request->getMore([
            ['tenant_name', ''],
            ['tenant_code', ''],
            ['contact_name', ''],
            ['contact_phone', ''],
            ['status', ''],
            ['is_expired', ''],
            ['date', ''],
        ]);
        
        return $this->success($this->services->getTenantList($where));
    }

    /**
     * 租户详情
     * @param int $id
     * @return mixed
     */
    public function read($id)
    {
        if (!$id) {
            return $this->fail('参数错误');
        }
        
        return $this->success($this->services->getTenantInfo((int)$id));
    }

    /**
     * 创建租户
     * @return mixed
     */
    public function save()
    {
        $data = $this->request->postMore([
            'appid',
            'tenant_name',
            'tenant_code',
            'account',
            'pwd',
            'domain',
            'logo',
            'contact_name',
            'contact_phone',
            'contact_email',
            'status',
            'max_users',
            'max_services',
            'expire_at',
        ]);
        
        // 验证数据
        $this->validate($data, TenantValidate::class);
        
        try {
            $tenant = $this->services->createTenant($data);
            return $this->success('创建成功', $tenant);
        } catch (\Exception $e) {
            return $this->fail($e->getMessage());
        }
    }

    /**
     * 更新租户
     * @param int $id
     * @return mixed
     */
    public function update($id)
    {
        if (!$id) {
            return $this->fail('参数错误');
        }
        
        $data = $this->request->postMore([
            ['appid', ''],
            ['tenant_name', ''],
            ['tenant_code', ''],
            ['account', ''],
            ['pwd', ''],
            ['domain', ''],
            ['logo', ''],
            ['contact_name', ''],
            ['contact_phone', ''],
            ['contact_email', ''],
            ['status', ''],
            ['max_users', ''],
            ['max_services', ''],
            ['expire_at', ''],
        ]);
        
        // 过滤空值
        $data = array_filter($data, function($value) {
            return $value !== '';
        });
        
        // 验证数据（更新时密码可选）
        $this->validate($data, TenantValidate::class . '.update');
        
        try {
            $result = $this->services->updateTenant((int)$id, $data);
            return $this->success('更新成功');
        } catch (\Exception $e) {
            return $this->fail($e->getMessage());
        }
    }

    /**
     * 删除租户
     * @param int $id
     * @return mixed
     */
    public function delete($id)
    {
        if (!$id) {
            return $this->fail('参数错误');
        }
        
        try {
            $result = $this->services->deleteTenant((int)$id);
            return $this->success('删除成功');
        } catch (\Exception $e) {
            return $this->fail($e->getMessage());
        }
    }

    /**
     * 更新租户状态
     * @param int $id
     * @return mixed
     */
    public function updateStatus($id)
    {
        if (!$id) {
            return $this->fail('参数错误');
        }
        
        $status = $this->request->post('status/d', 0);
        
        try {
            $result = $this->services->updateStatus((int)$id, $status);
            return $this->success('操作成功');
        } catch (\Exception $e) {
            return $this->fail($e->getMessage());
        }
    }

    /**
     * 批量更新状态
     * @return mixed
     */
    public function batchUpdateStatus()
    {
        $ids = $this->request->post('ids/a', []);
        $status = $this->request->post('status/d', 0);
        
        try {
            $result = $this->services->batchUpdateStatus($ids, $status);
            return $this->success('操作成功');
        } catch (\Exception $e) {
            return $this->fail($e->getMessage());
        }
    }

    /**
     * 获取统计信息
     * @return mixed
     */
    public function statistics()
    {
        return $this->success($this->services->getStatistics());
    }

    /**
     * 获取即将过期的租户
     * @return mixed
     */
    public function expiring()
    {
        $days = $this->request->get('days/d', 7);
        return $this->success($this->services->getExpiringTenants($days));
    }

    /**
     * 获取状态选项
     * @return mixed
     */
    public function statusOptions()
    {
        $options = [];
        foreach (\app\models\tenant\Tenant::$statusMap as $value => $label) {
            $options[] = [
                'value' => $value,
                'label' => $label,
            ];
        }
        return $this->success($options);
    }

    /**
     * 验证唯一性
     * @return mixed
     */
    public function checkUnique()
    {
        $field = $this->request->get('field', '');
        $value = $this->request->get('value', '');
        $excludeId = $this->request->get('exclude_id/d', 0);
        
        if (!in_array($field, ['appid', 'tenant_code', 'account'])) {
            return $this->fail('参数错误');
        }
        
        $exists = false;
        switch ($field) {
            case 'appid':
                $exists = $this->services->checkAppidExists($value, $excludeId);
                break;
            case 'tenant_code':
                $exists = $this->services->checkTenantCodeExists($value, $excludeId);
                break;
            case 'account':
                $exists = $this->services->checkAccountExists($value, $excludeId);
                break;
        }
        
        return $this->success(['exists' => $exists]);
    }
}