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

class Qrcode extends BaseModel
{

    /**
     * @var string
     */
    protected $name = 'qrcode';

    /**
     * @var string
     */
    protected $pk = 'id';

    /**
     * @param $value
     * @return array|mixed
     */
    public function getUserIdsAttr($value)
    {
        return $value ? array_map('intval', json_decode($value, true)) : [];
    }

    /**
     * @param $value
     * @return false|string
     */
    public function setUserIdsAttr($value)
    {
        return json_encode($value);
    }

    /**
     * @param $query
     * @param $value
     */
    public function searchNameAttr($query, $value)
    {
        if ($value !== '') {
            $query->whereLike('name', '%' . $value . '%');
        }
    }
}
