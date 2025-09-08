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
