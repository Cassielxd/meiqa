<?php
declare (strict_types = 1);

namespace app\dao\tenant;

use crmeb\basic\BaseDao;
use app\models\tenant\Tenant;
use think\db\Query;

/**
 * 租户数据访问层
 * Class TenantDao
 * @package app\dao\tenant
 */
class TenantDao extends BaseDao
{
    /**
     * 设置模型
     * @return string
     */
    protected function setModel(): string
    {
        return Tenant::class;
    }

    /**
     * 搜索器
     * @param array $where
     * @return \crmeb\basic\BaseModel
     */
    public function search(array $where = [])
    {
        return parent::search($where)->when(isset($where['date']) && $where['date'] !== '', function ($query) use ($where) {
            [$startTime, $endTime] = explode(' - ', $where['date']);
            $query->where('created_at', '>=', $startTime . ' 00:00:00')
                  ->where('created_at', '<=', $endTime . ' 23:59:59');
        });
    }

    /**
     * 获取租户列表
     * @param array $where
     * @param int $page
     * @param int $limit
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getTenantsList(array $where, int $page = 0, int $limit = 0): array
    {
        return $this->search($where)
            ->when($page && $limit, function ($query) use ($page, $limit) {
                $query->page($page, $limit);
            })
            ->order('id desc')
            ->select()
            ->toArray();
    }

    /**
     * 获取租户总数
     * @param array $where
     * @return int
     */
    public function getTenantsCount(array $where): int
    {
        return $this->search($where)->count();
    }

    /**
     * 根据appid获取租户信息
     * @param string $appid
     * @return array|\think\Model|null
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getTenantByAppid(string $appid)
    {
        return $this->getModel()->where('appid', $appid)->find();
    }

    /**
     * 根据租户编码获取租户信息
     * @param string $tenantCode
     * @return array|\think\Model|null
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getTenantByCode(string $tenantCode)
    {
        return $this->getModel()->where('tenant_code', $tenantCode)->find();
    }

    /**
     * 根据管理员账号获取租户信息
     * @param string $account
     * @return array|\think\Model|null
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getTenantByAccount(string $account)
    {
        return $this->getModel()->where('account', $account)->find();
    }

    /**
     * 检查appid是否存在
     * @param string $appid
     * @param int $excludeId
     * @return bool
     */
    public function checkAppidExists(string $appid, int $excludeId = 0): bool
    {
        return $this->getModel()
            ->where('appid', $appid)
            ->when($excludeId > 0, function ($query) use ($excludeId) {
                $query->where('id', '<>', $excludeId);
            })
            ->count() > 0;
    }

    /**
     * 检查租户编码是否存在
     * @param string $tenantCode
     * @param int $excludeId
     * @return bool
     */
    public function checkTenantCodeExists(string $tenantCode, int $excludeId = 0): bool
    {
        return $this->getModel()
            ->where('tenant_code', $tenantCode)
            ->when($excludeId > 0, function ($query) use ($excludeId) {
                $query->where('id', '<>', $excludeId);
            })
            ->count() > 0;
    }

    /**
     * 检查管理员账号是否存在
     * @param string $account
     * @param int $excludeId
     * @return bool
     */
    public function checkAccountExists(string $account, int $excludeId = 0): bool
    {
        return $this->getModel()
            ->where('account', $account)
            ->when($excludeId > 0, function ($query) use ($excludeId) {
                $query->where('id', '<>', $excludeId);
            })
            ->count() > 0;
    }

    /**
     * 获取即将过期的租户列表
     * @param int $days 天数
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getExpiringTenants(int $days = 7): array
    {
        $endDate = date('Y-m-d H:i:s', strtotime("+{$days} days"));
        return $this->getModel()
            ->whereNotNull('expire_at')
            ->where('expire_at', '<=', $endDate)
            ->where('expire_at', '>', date('Y-m-d H:i:s'))
            ->where('status', Tenant::STATUS_APPROVED)
            ->select()
            ->toArray();
    }

    /**
     * 获取已过期的租户列表
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getExpiredTenants(): array
    {
        return $this->getModel()
            ->whereNotNull('expire_at')
            ->where('expire_at', '<', date('Y-m-d H:i:s'))
            ->where('status', Tenant::STATUS_APPROVED)
            ->select()
            ->toArray();
    }

    /**
     * 统计各状态租户数量
     * @return array
     */
    public function getStatusStatistics(): array
    {
        $result = $this->getModel()
            ->field('status, count(*) as count')
            ->group('status')
            ->select()
            ->toArray();
        
        $statistics = [];
        foreach (Tenant::$statusMap as $status => $name) {
            $statistics[$status] = 0;
        }
        
        foreach ($result as $item) {
            $statistics[$item['status']] = $item['count'];
        }
        
        return $statistics;
    }

    /**
     * 更新租户状态
     * @param int $id
     * @param int $status
     * @return bool
     */
    public function updateTenantStatus(int $id, int $status): bool
    {
        return $this->update($id, ['status' => $status, 'updated_at' => date('Y-m-d H:i:s')]) !== false;
    }

    /**
     * 批量更新租户状态
     * @param array $ids
     * @param int $status
     * @return bool
     */
    public function batchUpdateStatus(array $ids, int $status): bool
    {
        return $this->getModel()
            ->whereIn('id', $ids)
            ->update(['status' => $status, 'updated_at' => date('Y-m-d H:i:s')]) !== false;
    }
}