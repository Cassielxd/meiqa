<?php

namespace app\models\other;


use crmeb\basic\BaseModel;
use crmeb\traits\ModelTrait;

/**
 * 辅助表
 * Class Auxiliary
 * @package app\models\other
 */
class Auxiliary extends BaseModel
{

    use ModelTrait;

    /**
     * 表明
     * @var string
     */
    protected $name = 'auxiliary';

    protected $insert = ['add_time'];

    protected $autoWriteTimestamp = false;

    /**
     * 主键
     * @var string
     */
    protected $pk = 'id';

    /**
     * 类型搜索器
     * @param $query
     * @param $value
     */
    public function searchTypeAttr($query, $value)
    {
        $query->where('type', $value);
    }

    /**
     * 类型绑定id搜索器
     * @param $query
     * @param $value
     */
    public function searchBindingIdAttr($query, $value)
    {
        $query->where('binding_id', $value);
    }

    /**
     * 类型状态搜索器
     * @param $query
     * @param $value
     */
    public function searchStatusAttr($query, $value)
    {
        $query->whereIn('status', $value);
    }

    /**
     * 类型关联id搜索器
     * @param $query
     * @param $value
     */
    public function searchRelationIdAttr($query, $value)
    {
        $query->where('relation_id', $value);
    }
}
