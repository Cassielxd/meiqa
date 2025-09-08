<?php

namespace app\dao\other;


use app\models\other\Auxiliary;
use crmeb\basic\BaseDao;

/**
 * 辅助表
 * Class AuxiliaryDao
 * @package app\dao\other
 */
class AuxiliaryDao extends BaseDao
{

    /**
     * 获取当前模型
     * @return string
     */
    protected function setModel(): string
    {
        return Auxiliary::class;
    }
}
