<?php

namespace app\controller\tenant\chat;


use app\controller\tenant\AuthController;
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
        $appid = $this->request->tenantAppid();
        return $this->success($services->getColumn(['status' => 1,'appid' => $appid], 'appid,id,nickname'));
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
        $appid = $this->request->tenantAppid();
        $where['appid']=$appid;
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
        $appid = $this->request->tenantAppid();
        $where['appid']=$appid;
        return $this->success($this->services->getDialogueRecord($where));
    }
}
