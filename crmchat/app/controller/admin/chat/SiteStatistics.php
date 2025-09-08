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

namespace app\controller\admin\chat;


use app\controller\admin\AuthController;
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

        return $this->success($this->services->getList($where));
    }
}
