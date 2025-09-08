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

namespace app\controller\admin\system;


use app\controller\admin\AuthController;
use app\services\other\AppVersionServices;

/**
 * app版本管理
 * Class AppVersion
 * @package app\controller\admin\system
 */
class AppVersion extends AuthController
{

    /**
     * AppVersion constructor.
     * @param AppVersionServices $services
     */
    public function __construct(AppVersionServices $services)
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
        return $this->success($this->services->getList());
    }

    /**
     * @param int $id
     * @return mixed
     * @throws \FormBuilder\Exception\FormBuilderException
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function create($id = 0)
    {
        return $this->success($this->services->getForm((int)$id));
    }

    /**
     * 保存
     * @param $id
     * @return mixed
     */
    public function save($id)
    {
        $data = $this->request->postMore([
            ['name', ''],
            ['verisons_num', ''],
            ['url', ''],
            ['info', ''],
        ]);

        $verison = $this->services->max();
        if ($data['verisons_num'] < $verison) {
            return $this->fail('您输入的版本号必须大于：' . $verison . '版本号');
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
     * @param $id
     * @return mixed
     */
    public function delete($id)
    {
        if (!$id) {
            return $this->fail('缺少参数');
        }
        if ($this->services->update($id, ['delete_time' => date('Y-m-d H:i:s')])) {
            return $this->success('删除成功');
        } else {
            return $this->fail('删除失败');
        }
    }
}
