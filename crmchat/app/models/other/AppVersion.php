<?php
// +----------------------------------------------------------------------
// | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2016~2020 https://www.crmeb.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
// +----------------------------------------------------------------------
// | Author: CRMEB Team <admin@crmeb.com>
// +----------------------------------------------------------------------

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
