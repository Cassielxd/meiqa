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
    public function getGroupList()
    {
        return $this->dao->getDataList([], ['*'], 'sort');
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
