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

namespace app\services\other;


use app\dao\other\AppVersionDao;
use crmeb\basic\BaseServices;
use crmeb\services\FormBuilder;
use think\exception\ValidateException;

/**
 * Class AppVersionServices
 * @package app\services\other
 * @mixin AppVersionDao
 */
class AppVersionServices extends BaseServices
{

    /**
     * AppVersionServices constructor.
     * @param AppVersionDao $dao
     */
    public function __construct(AppVersionDao $dao)
    {
        $this->dao = $dao;
    }

    /**
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getList()
    {
        [$page, $limit] = $this->getPageValue();
        $list  = $this->dao->getDataList([], ['*'], 'id', $page, $limit);
        $count = $this->dao->count();

        return compact('list', 'count');
    }

    /**
     * @param int $id
     * @return array
     * @throws \FormBuilder\Exception\FormBuilderException
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getForm(int $id)
    {
        $rule = [
            FormBuilder::input('name', '更新摘要')->required(),
            FormBuilder::input('verisons_num', '版本号')->required(),
            FormBuilder::uploadFile('url', '压缩包', $this->url('/api/admin/file/upload/1', ['type' => 1], false, false))->headers([
                'Authori-zation' => app()->request->header('Authori-zation'),
            ])->required(),
            FormBuilder::textarea('info', '更新详情')->required(),
        ];
        $data = [];
        if ($id) {
            $data = $this->dao->get($id);
            if (!$data) {
                throw new ValidateException('修改的升级包信息不存在');
            }
            $data = $data->toArray();
        }
        return create_form($id ? '修改APP升级包' : '添加APP升级包', $rule, '/setting/verison/save/' . $id, 'POST', $data);
    }
}
