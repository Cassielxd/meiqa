<?php

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
