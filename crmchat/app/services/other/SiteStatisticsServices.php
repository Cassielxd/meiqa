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

namespace app\services\other;


use app\dao\other\SiteStatisticsDao;
use crmeb\basic\BaseServices;
use crmeb\services\QqMap;
use think\db\exception\DataNotFoundException;
use think\db\exception\ModelNotFoundException;
use think\facade\Cache;

/**
 * Class SiteStatisticsServices
 * @package app\services\other
 */
class SiteStatisticsServices extends BaseServices
{

    /**
     * SiteStatisticsServices constructor.
     * @param SiteStatisticsDao $dao
     */
    public function __construct(SiteStatisticsDao $dao)
    {
        $this->dao = $dao;
    }


    /**
     * 获取站点统计列表
     * @param array $where
     * @return array
     * @throws DataNotFoundException
     * @throws DbException
     * @throws ModelNotFoundException
     */
    public function getList(array $where = [], array $field = ['*'])
    {
        [$page, $limit] = $this->getPageValue();
        $data = $this->dao->getDataList($where, $field, null, $page, $limit);
        $count = $this->dao->count($where);
        return compact('data', 'count');
    }

    /**
     * ip获取地区位置
     * @param string $ip
     * @return array|bool
     */
    public function ipByCity(string $ip)
    {
        $url = "https://ip.taobao.com/service/getIpInfo.php?ip=" . $ip;
        $ip = json_decode(file_get_contents($url));
        if ((string)$ip->code == '1') {
            return false;
        }
        return (array)$ip->data;
    }

    /**
     * 保存网站来源
     * @param array $data
     * @return bool
     * @throws \Psr\SimpleCache\InvalidArgumentException
     */
    public function saveSite(array $data)
    {
        $res = Cache::has($data['ip']);
        if ($res) {
            return true;
        }
        try {
            $map = new QqMap();
            $city = $map->getMapLocationInfo($data['ip']);
            $data['province'] = $city->get('result.ad_info.province');
            $data['region'] = $city->get('result.ad_info.city');
        } catch (\Throwable $e) {
        }
        $data['create_time'] = date('Y-m-d H:i:s');
        $res = $this->dao->save($data);
        if ($res) {
            Cache::set($data['ip'], 1, 600);
        }
        return !!$res;
    }
}
