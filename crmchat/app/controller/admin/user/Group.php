<?php

namespace app\controller\admin\user;


use app\controller\admin\AuthController;
use app\services\chat\user\ChatUserGroupServices;

/**
 * Class Group
 * @package app\controller\admin\user
 */
class Group extends AuthController
{

    /**
     * Group constructor.
     * @param ChatUserGroupServices $services
     */
    public function __construct(ChatUserGroupServices $services)
    {
        parent::__construct();
        $this->services = $services;
    }


    /**
     * 分组列表
     */
    public function index()
    {
        return $this->success($this->services->getGroupList(['*'], true));
    }

    /**
     * @return mixed
     * @throws \FormBuilder\Exception\FormBuilderException
     */
    public function create()
    {
        return $this->success($this->services->add());
    }

    /**
     *
     * @param int $id
     * @return mixed
     */
    public function save()
    {
        $data = $this->request->postMore([
            ['group_name', ''],
        ]);
        if (!$data['group_name']) {
            return $this->fail('请输入分组名称');
        }
        $this->services->save(0, $data);
        return $this->success('提交成功！');
    }

    /**
     * 添加/修改分组页面
     * @param int $id
     * @return string
     */
    public function edit($id)
    {
        return $this->success($this->services->add((int)$id));
    }

    /**
     * @param $id
     * @return mixed
     */
    public function update($id)
    {
        $data = $this->request->postMore([
            ['group_name', ''],
        ]);
        if (!$data['group_name']) {
            return $this->fail('请输入分组名称');
        }
        $this->services->save((int)$id, $data);
        return $this->success('提交成功！');
    }

    /**
     * 删除
     * @param $id
     * @throws \Exception
     */
    public function delete()
    {
        $data = $this->request->getMore([
            ['id', 0],
        ]);
        if (!$data['id']) return $this->fail('数据不存在');
        $this->services->delGroup((int)$data['id']);
        return $this->success();
    }
}
