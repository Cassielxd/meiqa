<?php
declare (strict_types = 1);

namespace app\controller\admin\tenant;

use app\controller\admin\AuthController;
use app\services\tenant\TenantServices;
use think\facade\App;
use crmeb\services\CacheService;
use think\facade\Log;

/**
 * 管理员的租户管理控制器
 * Class Tenant
 * @package app\controller\admin\tenant
 */
class Tenant extends AuthController
{
    /**
     * Tenant constructor.
     * @param App $app
     * @param TenantServices $services
     */
    public function __construct(App $app, TenantServices $services)
    {
        parent::__construct($app);
        $this->services = $services;
    }

    /**
     * 获取租户列表
     * @return mixed
     */
    public function list()
    {
        Log::info('Admin tenant list called');
        
        $where = $this->request->getMore([
            ['keyword', ''],
            ['status', ''],
            ['date', ''],
        ]);
        
        Log::info('Admin tenant list where', $where);
        
        try {
            $result = $this->services->getTenantList($where);
            Log::info('Admin tenant list result', $result);
            return $this->success($result);
        } catch (\Exception $e) {
            Log::error('Admin tenant list error: ' . $e->getMessage());
            return $this->fail($e->getMessage());
        }
    }
    /**
     * 获取租户详情
     * @param int $id
     * @return mixed
     */
    public function read($id)
    {
        if (!$id) {
            return $this->fail('参数错误');
        }

        $info = $this->services->getTenantInfo((int)$id);
        if (!$info) {
            return $this->fail('租户不存在');
        }

        return $this->success($info);
    }

    /**
     * 创建租户
     * @return mixed
     */
    public function save()
    {
        $data = $this->request->postMore([
            ['tenant_name', ''],
            ['account', ''],
            ['pwd', ''],
            ['contact_email', ''],
            ['contact_phone', ''],
            ['user_limit', 100],
            ['service_limit', 10],
            ['expire_at', ''],
            ['auto_renew', 0],
            ['status', 1],
            ['remark', ''],
        ]);

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
            ['tenant_name', ''],
            ['contact_email', ''],
            ['contact_phone', ''],
            ['user_limit', ''],
            ['service_limit', ''],
            ['expire_at', ''],
            ['auto_renew', ''],
            ['status', ''],
            ['remark', ''],
        ]);

        // 过滤空值
        $data = array_filter($data, function($value) {
            return $value !== '';
        });

        try {
            $this->services->updateTenant((int)$id, $data);
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
            $this->services->deleteTenant((int)$id);
            // 清除相关缓存
            CacheService::clear();
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

        $status = $this->request->post('status', 0);
        
        try {
            $this->services->updateTenantStatus((int)$id, (int)$status);
            return $this->success('状态更新成功');
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
        $ids = $this->request->post('ids', []);
        $status = $this->request->post('status', 0);
        
        if (empty($ids)) {
            return $this->fail('请选择要操作的租户');
        }

        try {
            $this->services->batchUpdateStatus($ids, (int)$status);
            return $this->success('批量更新成功');
        } catch (\Exception $e) {
            return $this->fail($e->getMessage());
        }
    }

    /**
     * 获取租户统计信息
     * @return mixed
     */
    public function statistics()
    {
        $stats = $this->services->getTenantStatistics();
        return $this->success($stats);
    }

    /**
     * 获取即将过期的租户
     * @return mixed
     */
    public function expiring()
    {
        $days = $this->request->get('days', 30);
        $list = $this->services->getExpiringTenants((int)$days);
        return $this->success($list);
    }

    /**
     * 重置租户密码
     * @param int $id
     * @return mixed
     */
    public function resetPassword($id)
    {
        if (!$id) {
            return $this->fail('参数错误');
        }

        $password = $this->request->post('password', '');
        if (!$password) {
            return $this->fail('请输入新密码');
        }

        try {
            $this->services->resetTenantPassword((int)$id, $password);
            return $this->success('密码重置成功');
        } catch (\Exception $e) {
            return $this->fail($e->getMessage());
        }
    }
}