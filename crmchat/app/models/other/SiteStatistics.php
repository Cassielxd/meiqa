<?php

namespace app\models\other;


use crmeb\basic\BaseModel;

class SiteStatistics extends BaseModel
{

    /**
     * @var string
     */
    protected $name = 'site_statistics';

    /**
     * @var string
     */
    protected $pk = 'id';

    /**
     * @param $query
     * @param $value
     */
    public function searchProvinceAttr($query, $value)
    {
        if ($value !== '') {
            $query->whereLike('province', '%' . $value . '%');
        }
    }

}
