<?php

namespace app\models\system\config;

use crmeb\basic\BaseModel;
use crmeb\traits\ModelTrait;
use think\Model;

/**
 * 组合数据数据列表模型
 * Class SystemGroupData
 * @package app\models\system\config
 */
class SystemGroupData extends BaseModel
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
    protected $name = 'system_group_data';

    /**
     * 状态搜索器
     * @param $query
     * @param $value
     */
    public function searchStatusAttr($query, $value)
    {
        if ($value != '') {
            $query->where('status', $value);
        }
    }

    /**
     * Gid搜索器
     * @param Model $query
     * @param $value
     */
    public function searchGidAttr($query, $value)
    {
        if (is_array($value)) {
            $query->whereIn('gid', $value);
        } else {
            $query->where('gid', $value);
        }
    }
}
