<?php

namespace app\models;


use crmeb\basic\BaseModel;
use think\Model;

/**
 * Class ApplicationDao
 * @package app\models
 */
class Application extends BaseModel
{

    /**
     * 表名
     * @var string
     */
    protected $name = 'application';

    /**
     * 主键
     * @var string
     */
    protected $key = 'id';

    /**
     * name搜索
     * @param Model $query
     * @param $value
     */
    public function searchNameLikeAttr($query, $value)
    {
        if ($value) {
            $query->whereLike('name|appid', '%' . $value . '%');
        }
    }

    /**
     * name搜索
     * @param Model $query
     * @param $value
     */
    public function searchNameAttr($query, $value)
    {
        if ($value) {
            $query->where('name', $value);
        }
    }

    /**
     * @param $query
     * @param $value
     */
    public function searchIsDeleteAttr($query, $value)
    {
        $query->where('is_delete', $value);
    }
}
