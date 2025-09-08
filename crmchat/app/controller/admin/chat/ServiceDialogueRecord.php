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
use app\services\chat\ChatServiceDialogueRecordServices;
use app\services\chat\ChatServiceRecordServices;
use app\services\chat\ChatServiceServices;

/**
 * Class ServiceDialogueRecord
 * @package app\controller\admin\chat
 */
class ServiceDialogueRecord extends AuthController
{

    /**
     * ServiceDialogueRecord constructor.
     * @param ChatServiceDialogueRecordServices $services
     */
    public function __construct(ChatServiceDialogueRecordServices $services)
    {
        parent::__construct();
        $this->services = $services;
    }

    /**
     * @param ChatServiceServices $services
     * @return mixed
     */
    public function kefu(ChatServiceServices $services)
    {
        return $this->success($services->getColumn(['status' => 1], 'appid,id,nickname'));
    }

    /**
     * @param ChatServiceRecordServices $services
     * @return mixed
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function record(ChatServiceRecordServices $services)
    {
        $where = $this->request->getMore([
            ['title', ''],
            ['time', '']
        ]);
        $where['delete'] = 1;
        return $this->success($services->getAdminUserRecodeList($where));
    }

    /**
     * @return mixed
     */
    public function index()
    {
        $where = $this->request->getMore([
            ['kefu_id', ''],
            ['msn', ''],
            ['time', ''],
            ['appid', ''],
            ['user_id', 0]
        ]);
        if ((int)$where['kefu_id'] === 0) {
            $where['kefu_id'] = '';
        }
        if ($where['kefu_id']) {
            /** @var ChatServiceServices $make */
            $make = app()->make(ChatServiceServices::class);
            $where['kefu_id'] = $make->value($where['kefu_id'], 'user_id');
        }
        return $this->success($this->services->getDialogueRecord($where));
    }
}
