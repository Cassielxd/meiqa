<?php

namespace app\controller\tenant\chat;


use app\controller\tenant\AuthController;
use app\services\other\SiteStatisticsServices;

/**
 * Class SiteStatistics
 * @package app\controller\admin\system
 */
class SiteStatistics extends AuthController
{
    /**
     * SiteStatistics constructor.
     * @param SiteStatisticsServices $services
     */
    public function __construct(SiteStatisticsServices $services)
    {
        parent::__construct();
        $this->services = $services;
    }

    /**
     * @return mixed
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function index()
    {
        $where = $this->request->getMore([
            ['province', ''],
            ['create_time', '']
        ]);
        $appid = $this->request->tenantAppid();
        $where['appid'] = $appid;
        return $this->success($this->services->getList($where));
    }
}
