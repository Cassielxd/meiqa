<?php

namespace app\models\other;


use crmeb\basic\BaseModel;
use think\model\concern\SoftDelete;

/**
 * Class AppVersion
 * @package app\models\other
 */
class AppVersion extends BaseModel
{

    use SoftDelete;

    /**
     * @var string
     */
    protected $name = 'app_version';

    /**
     * @var string
     */
    protected $pk = 'id';

    /**
     * 自动写入时间戳
     * @var bool
     */
    protected $autoWriteTimestamp = true;

    /**
     * @var string
     */
    protected $defaultSoftDelete = 'delete_time';
}
