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


use app\dao\other\QrcodeDao;
use app\services\chat\ChatServiceServices;
use crmeb\basic\BaseServices;
use crmeb\services\FormBuilder;
use think\exception\ValidateException;


/**
 * Class QrcodeServices
 * @package app\services\other
 */
class QrcodeServices extends BaseServices
{

    /**
     * QrcodeServices constructor.
     * @param QrcodeDao $dao
     */
    public function __construct(QrcodeDao $dao)
    {
        $this->dao = $dao;
    }

    /**
     * @param array $where
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getList(array $where)
    {
        [$page, $limit] = $this->getPageValue();
        $list    = $this->dao->getDataList($where, ['*'], 'id', $page, $limit);
        $userIds = [];
        foreach ($list as &$item) {
            if ($item['user_ids']) {
                $userIds = array_merge($userIds, $item['user_ids']);
            }
        }
        $userIds = array_merge(array_unique($userIds));
        if ($userIds) {
            /** @var ChatServiceServices $service */
            $service  = app()->make(ChatServiceServices::class);
            $kefuList = $service->getColumn(['id' => $userIds], 'account', 'id');
            foreach ($list as &$item) {
                $item['user_account'] = [];
                foreach ($kefuList as $id => $account) {
                    if (in_array($id, $item['user_ids'])) {
                        $item['user_account'][] = $account;
                    }
                }
            }
        }
        $count = $this->dao->count($where);
        return compact('list', 'count');
    }

    /**
     * 获取修改和创建表单
     * @param string $appid
     * @param int $id
     * @return array
     * @throws \FormBuilder\Exception\FormBuilderException
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getForm(string $appid, int $id = 0)
    {
        $codeInfo = [];
        if ($id) {
            $codeInfo = $this->dao->get($id);
            if (!$codeInfo) {
                throw new ValidateException('修改的二维码不存在');
            }
            $codeInfo = $codeInfo->toArray();
        }
        /** @var ChatServiceServices $service */
        $service = app()->make(ChatServiceServices::class);
        $data    = $service->getKefuSelect(['appid' => $appid]);
        $rule    = [
            FormBuilder::input('name', '二维码名称', $codeInfo['name'] ?? '')->required(),
            FormBuilder::select('user_ids', '选择客服', $codeInfo['user_ids'] ?? [])->required()->options($data)->multiple(true),
            FormBuilder::number('sort', '排序', $codeInfo['sort'] ?? 0),
        ];
        return create_form($id ? '编辑二维码' : '添加二维码', $rule, '/chat/qrcode/' . $id);
    }

    /**
     * @param array $data
     * @param int $id
     * @return bool
     */
    public function saveQrcode(array $data, int $id = 0)
    {
        if ($id) {
            $this->dao->update($id, $data);
        } else {
            $this->dao->save($data);
        }
        return true;
    }
}
