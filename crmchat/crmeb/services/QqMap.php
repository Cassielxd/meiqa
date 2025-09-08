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

namespace crmeb\services;


use crmeb\utils\Collection;
use think\exception\ValidateException;

/**
 * 腾讯地理位置服务
 * Class QqMap
 * @package crmeb\services
 */
class QqMap
{

    const MAP_LOCATION = 'https://apis.map.qq.com/ws/location/v1/ip';

    protected $key = '';

    public function __construct()
    {
        $this->key = sys_config('qq_map_key');
    }

    /**
     * @param string $ip
     * @return \think\Collection
     */
    public function getMapLocationInfo(string $ip)
    {
        $res = HttpService::getRequest(self::MAP_LOCATION, [
            'ip'  => $ip,
            'key' => $this->key,
        ], false, 10, true);

        $res = json_decode($res, true);

        if (json_last_error() !== JSON_ERROR_NONE) {
            throw new ValidateException(json_last_error_msg());
        }

        if ($res['status'] !== 0) {
            throw new ValidateException($res['message']);
        }

        return new Collection($res);
    }
}
