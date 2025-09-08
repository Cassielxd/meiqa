<?php

namespace app\dao\other;


use app\models\other\Qrcode;
use crmeb\basic\BaseDao;

/**
 * Class QrcodeDao
 * @package app\dao\other
 */
class QrcodeDao extends BaseDao
{

    /**
     * @return string
     */
    protected function setModel(): string
    {
        return Qrcode::class;
    }
}
