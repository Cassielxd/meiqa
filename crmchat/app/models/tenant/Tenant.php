<?php
declare (strict_types = 1);

namespace app\models\tenant;

use crmeb\basic\BaseModel;
use crmeb\traits\ModelTrait;
use think\model\concern\SoftDelete;

/**
 * 租户模型
 * Class Tenant
 * @package app\models\tenant
 */
class Tenant extends BaseModel
{
    use ModelTrait;

    /**
     * 数据表主键
     * @var string
     */
    protected $pk = 'id';

    /**
     * 模型名称
     * @var string
     */
    protected $name = 'tenants';

    /**
     * 状态常量定义
     */
    const STATUS_PENDING = 0;    // 待审核
    const STATUS_APPROVED = 1;   // 已批准
    const STATUS_REJECTED = 2;   // 已拒绝
    const STATUS_DISABLED = 3;   // 已禁用

    /**
     * 状态文本映射
     * @var array
     */
    public static $statusMap = [
        self::STATUS_PENDING => '待审核',
        self::STATUS_APPROVED => '已批准',
        self::STATUS_REJECTED => '已拒绝',
        self::STATUS_DISABLED => '已禁用',
    ];

    /**
     * 创建时间
     * @param $value
     * @return false|string
     */
    public function getCreatedAtAttr($value)
    {
        if ($value) return date('Y-m-d H:i:s', strtotime($value));
        return '';
    }

    /**
     * 更新时间
     * @param $value
     * @return false|string
     */
    public function getUpdatedAtAttr($value)
    {
        if ($value) return date('Y-m-d H:i:s', strtotime($value));
        return '';
    }

    /**
     * 到期时间
     * @param $value
     * @return false|string
     */
    public function getExpireAtAttr($value)
    {
        if ($value) return date('Y-m-d H:i:s', strtotime($value));
        return '';
    }

    /**
     * 状态文本
     * @param $value
     * @return string
     */
    public function getStatusTextAttr($value, $data)
    {
        return self::$statusMap[$data['status']] ?? '未知';
    }

    /**
     * 搜索器：租户名称
     * @param $query
     * @param $value
     */
    public function searchTenantNameAttr($query, $value)
    {
        if ($value !== '') {
            $query->where('tenant_name', 'like', '%' . $value . '%');
        }
    }

    /**
     * 搜索器：租户编码
     * @param $query
     * @param $value
     */
    public function searchTenantCodeAttr($query, $value)
    {
        if ($value !== '') {
            $query->where('tenant_code', 'like', '%' . $value . '%');
        }
    }

    /**
     * 搜索器：联系人姓名
     * @param $query
     * @param $value
     */
    public function searchContactNameAttr($query, $value)
    {
        if ($value !== '') {
            $query->where('contact_name', 'like', '%' . $value . '%');
        }
    }

    /**
     * 搜索器：联系人电话
     * @param $query
     * @param $value
     */
    public function searchContactPhoneAttr($query, $value)
    {
        if ($value !== '') {
            $query->where('contact_phone', 'like', '%' . $value . '%');
        }
    }

    /**
     * 搜索器：状态
     * @param $query
     * @param $value
     */
    public function searchStatusAttr($query, $value)
    {
        if ($value !== '') {
            $query->where('status', $value);
        }
    }

    /**
     * 搜索器：appid
     * @param $query
     * @param $value
     */
    public function searchAppidAttr($query, $value)
    {
        if ($value !== '') {
            $query->where('appid', $value);
        }
    }

    /**
     * 搜索器：是否过期
     * @param $query
     * @param $value
     */
    public function searchIsExpiredAttr($query, $value)
    {
        if ($value == 1) {
            // 已过期
            $query->whereNotNull('expire_at')->where('expire_at', '<', date('Y-m-d H:i:s'));
        } elseif ($value == 0) {
            // 未过期
            $query->where(function($q) {
                $q->whereNull('expire_at')->whereOr('expire_at', '>=', date('Y-m-d H:i:s'));
            });
        }
    }

    /**
     * 是否已过期
     * @return bool
     */
    public function getIsExpiredAttr($value, $data)
    {
        if (empty($data['expire_at'])) {
            return false;
        }
        return strtotime($data['expire_at']) < time();
    }

    /**
     * 剩余天数
     * @return int
     */
    public function getRemainingDaysAttr($value, $data)
    {
        if (empty($data['expire_at'])) {
            return -1; // 永久有效
        }
        $days = (strtotime($data['expire_at']) - time()) / 86400;
        return max(0, (int)$days);
    }
}