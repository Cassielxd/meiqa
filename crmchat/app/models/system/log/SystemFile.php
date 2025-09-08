<?php

namespace app\models\system\log;

use crmeb\basic\BaseModel;
use crmeb\traits\ModelTrait;

/**
 * 文件校验模型
 * Class SystemFile
 * @package app\models\system\log
 */
class SystemFile extends BaseModel
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
    protected $name = 'system_file';

}
