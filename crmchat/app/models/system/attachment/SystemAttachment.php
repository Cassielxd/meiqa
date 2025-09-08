<?php

namespace app\models\system\attachment;

use crmeb\basic\BaseModel;
use crmeb\traits\ModelTrait;
use think\Model;

/**
 * 附件管理模型
 * Class SystemAttachment
 * @package app\models\system\attachment
 */
class SystemAttachment extends BaseModel
{
    use ModelTrait;

    /**
     * 数据表主键
     * @var string
     */
    protected $pk = 'att_id';

    /**
     * 模型名称
     * @var string
     */
    protected $name = 'system_attachment';

    /**
     * 图片类型搜索器
     * @param Model $query
     * @param $value
     */
    public function searchModuleTypeAttr($query, $value)
    {
        $query->where('module_type', $value ?: 1);
    }

    /**
     * pid搜索器
     * @param Model $query
     * @param $value
     */
    public function searchPidAttr($query, $value)
    {
        if ($value) $query->where('pid', $value);
    }

    /**
     * name模糊搜索
     * @param Model $query
     * @param $value
     */
    public function searchLikeNameAttr($query, $value)
    {
        if ($value) $query->where('name','LIKE' ,"$value%");
    }
}
