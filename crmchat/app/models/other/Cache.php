<?php
namespace app\models\other;

use crmeb\traits\ModelTrait;
use crmeb\basic\BaseModel;
use think\Model;

/**
 *  缓存Model
 * Class Cache
 * @package app\models\other
 */
class Cache extends BaseModel
{
    use ModelTrait;

    /**
     * 模型名称
     * @var string
     */
    protected $name = 'cache';

    /**
     * 缓存KEY搜索器
     * @param Model $query
     * @param $value
     * @param $data
     */
    public function searchKeyAttr($query, $value, $data)
    {
        $query->where('key', $value);
    }
}
