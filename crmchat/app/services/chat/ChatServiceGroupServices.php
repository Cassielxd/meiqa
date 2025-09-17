<?php

namespace app\services\chat;


use app\dao\chat\ChatServiceGroupDao;
use crmeb\basic\BaseServices;
use crmeb\services\FormBuilder;
use think\exception\ValidateException;

/**
 * Class ChatServiceGroupServices
 * @package app\services\chat
 */
class ChatServiceGroupServices extends BaseServices
{

    /**
     * ChatServiceGroupServices constructor.
     * @param ChatServiceGroupDao $dao
     */
    public function __construct(ChatServiceGroupDao $dao)
    {
        $this->dao = $dao;
    }

    /**
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getGroupList($where=[])
    {
        return $this->dao->getDataList($where, ['*'], 'sort');
    }

    /**
     * @param int $id
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function from(int $id)
    {
        $data = [];
        if ($id) {
            $data = $this->dao->get($id);
            if (!$data) {
                throw new ValidateException('修改的组不存在');
            }
            $data = $data->toArray();
        }
        $rule = [
            FormBuilder::input('name', '组名', $data['name'] ?? ''),
            FormBuilder::number('sort', '排序', $data['sort'] ?? 0),
        ];

        return create_form($id ? '修改组名' : '添加分组', $rule, '/chat/group/' . $id);
    }

    /**
     * @return array
     */
    public function getOptions()
    {
        return $this->dao->getDataList([], ['name as label', 'id as value'],'create_time');
    }

}
