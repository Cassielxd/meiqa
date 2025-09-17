<?php

namespace app\controller\tenant\chat;


use app\controller\tenant\AuthController;
use app\services\chat\ChatServiceGroupServices;
use app\services\chat\ChatServiceServices;

/**
 * Class ServiceGroup
 * @package app\controller\admin\chat
 */
class ServiceGroup extends AuthController
{

    /**
     * ServiceGroup constructor.
     * @param ChatServiceGroupServices $services
     */
    public function __construct(ChatServiceGroupServices $services)
    {
        parent::__construct();
        $this->services = $services;
    }

    /**
     * @return mixed
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function index()
    {
        $appid = $this->request->tenantAppid();
        return $this->success($this->services->getGroupList(["appid"=>$appid]));
    }

    /**
     * @param $id
     * @return mixed
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function create($id)
    {

        return $this->success($this->services->from((int)$id));
    }

    public function save($id)
    {
        $data = $this->request->postMore([
            ['name', ''],
            ['sort', 0],
        ]);

        if (!$data['name']) {
            return $this->fail('缺少分组名称');
        }
        $appid = $this->request->tenantAppid();
        $data["appid"] = $appid;
        if ($id) {
            $this->services->update($id, $data);
        } else {
            $this->services->save($data);
        }

        return $this->success($id ? '修改成功' : '添加成功');
    }

    /**
     * 删除
     * @param ChatServiceServices $services
     * @param $id
     * @return mixed
     */
    public function delete(ChatServiceServices $services, $id)
    {
        if (!$id) {
            return $this->fail('缺少参数');
        }
        if ($services->count(['group_id' => $id])) {
            return $this->fail('请先解除客服关联');
        }
        if ($this->services->delete($id)) {
            return $this->success('删除成功');
        } else {
            return $this->fail('删除失败');
        }
    }
}
