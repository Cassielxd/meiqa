<?php

namespace app\models\system\config;

use crmeb\basic\BaseModel;
use crmeb\traits\ModelTrait;
use think\Model;

/**
 * 组合数据配置模型
 * Class SystemGroup
 * @package app\models\system\config
 */
class SystemGroup extends BaseModel
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
    protected $name = 'system_group';

    /**
     * 配置名搜索器
     * @param Model $query
     * @param $value
     */
    public function searchConfigNameAttr($query, $value)
    {
        $query->where('config_name', $value);
    }

    /**
     * @param Model $query
     * @param $value
     */
    public function searchTitleAttr($query, $value)
    {
        if ($value != '') {
            $query->whereLIke('id|name|info|config_name', "%$value%");
        }
    }

    /**
     * 查询分类
     * @param Model $query
     * @param $value
     */
    public function searchCateIdAttr($query, $value)
    {
        $query->where('cate_id', $value);
    }
}
