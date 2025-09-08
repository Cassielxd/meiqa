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
        return $this->success($this->services->getGroupList());
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
