<?php

namespace app\dao;


use app\models\Application;
use crmeb\basic\BaseDao;

/**
 * Class ApplicationDao
 * @package app\dao
 */
class ApplicationDao extends BaseDao
{

    /**
     * 获取当前模型
     * @return string
     */
    protected function setModel(): string
    {
        return Application::class;
    }

}
