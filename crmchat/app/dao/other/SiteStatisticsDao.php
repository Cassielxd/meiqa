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
