<?php

namespace app\dao\other;


use app\models\other\SiteStatistics;
use crmeb\basic\BaseDao;

/**
 * Class SiteStatisticsDao
 * @package app\dao\other
 */
class SiteStatisticsDao extends BaseDao
{

    /**
     * @return string
     */
    protected function setModel(): string
    {
        return SiteStatistics::class;
    }

    /**
     * @param array $where
     * @return \crmeb\basic\BaseModel|mixed|\think\Model
     */
    protected function search(array $where = [])
    {
        return parent::search($where)->when(isset($where['create_time']) && $where['create_time'], function ($query) use ($where) {
            time_model($query, $where, 'create_time');
        });
    }
}
