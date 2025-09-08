<?php

namespace app\controller\mobile;


use app\services\other\SiteStatisticsServices;

/**
 * Class Statistics
 * @package app\controller\mobile
 */
class Statistics extends AuthController
{

    /**
     * Statistics constructor.
     * @param SiteStatisticsServices $services
     */
    public function __construct(SiteStatisticsServices $services)
    {
        parent::__construct();
        $this->services = $services;
    }

    /**
     * @return mixed
     * @throws \Psr\SimpleCache\InvalidArgumentException
     */
    public function save()
    {
        $data = $this->request->postMore([
            ['ip', ''],
            ['path', ''],
            ['source', ''],
            ['browser', ''],
        ]);
        if (!$data['ip'] || !$data['path']) {
            return $this->fail('缺少参数');
        }
        $this->services->saveSite($data);
        return $this->success('添加成功');
    }
}
