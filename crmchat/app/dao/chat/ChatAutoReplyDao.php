<?php

namespace app\dao\chat;


use app\models\chat\ChatAutoReply;
use crmeb\basic\BaseDao;

/**
 * Class ChatAutoReplyDao
 * @package app\dao\chat
 */
class ChatAutoReplyDao extends BaseDao
{
    /**
     * @return string
     */
    protected function setModel(): string
    {
        return ChatAutoReply::class;
    }

    /**
     * @param array $where
     * @param int $page
     * @param int $limit
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getReply(array $where, int $page, int $limit)
    {
        return $this->search($where)->page($page, $limit)->select()->toArray();
    }

    /**
     * 获取回复列表
     * @param array $where
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getReplyList(array $where)
    {
        return $this->getModel()->when(isset($where['keyword']), function ($query) use ($where) {
            $field = 'keyword';
            $query->where(function ($q) use ($where, $field) {
                foreach ($where['keyword'] as $k => $v) {
                    if ($k === 0) {
                        $q->where($field, 'like', "%" . trim($v) . "%");
                    } else {
                        $q->whereOr($field, 'like', "%" . trim($v) . "%");
                    }
                }
            });
        })->when(isset($where['appid']), function ($query) use ($where) {
            $query->where('appid', $where['appid']);
        })->when(isset($where['user_id']), function ($query) use ($where) {
            $query->where('user_id', $where['user_id']);
        })->limit(5)->order('sort desc,id desc')->select()->toArray();
    }
}
