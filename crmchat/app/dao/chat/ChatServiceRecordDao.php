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

namespace app\dao\chat;


use app\models\chat\ChatServiceRecord;
use crmeb\basic\BaseDao;
use crmeb\basic\BaseModel;
use think\db\exception\DataNotFoundException;
use think\db\exception\DbException;
use think\db\exception\ModelNotFoundException;
use think\Model;

/**
 * 聊天记录
 * Class ChatServiceRecordDao
 * @package app\dao\chat
 */
class ChatServiceRecordDao extends BaseDao
{

    /**
     * ChatServiceRecordDao constructor.
     */
    public function __construct()
    {

    }

    /**
     * 获取当前模型
     * @return string
     */
    protected function setModel(): string
    {
        return ChatServiceRecord::class;
    }

    /**
     * 删除上周游客记录
     */
    protected function deleteWeekRecord()
    {
        $this->search(['time' => 'last week', 'timeKey' => 'update_time', 'is_tourist' => 1])->delete();
    }


    /**
     *
     * @param array $where
     * @param array $data
     * @return BaseModel
     */
    public function updateOnline(array $where, array $data)
    {
        return $this->getModel()->whereNotIn('to_user_id', $where['notUid'])->update($data);
    }

    /**
     * 获取客服聊天用户列表
     * @param array $where
     * @param int $page
     * @param int $limit
     * @param array $with
     * @return array
     * @throws DataNotFoundException
     * @throws DbException
     * @throws ModelNotFoundException
     */
    public function getServiceList(array $where, int $page, int $limit, array $with = [], array $field = ['*'])
    {
        $labelId = isset($where['label_id']) ? $where['label_id'] : [];
        unset($where['label_id']);
        return $this->search($where)->when($page && $limit, function ($query) use ($page, $limit) {
            $query->page($page, $limit);
        })->when(count($with), function ($query) use ($with) {
            $query->with($with);
        })->when($labelId, function ($query) use ($labelId) {
            $query->whereIn('to_user_id', function ($query) use ($labelId) {
                $query->name('chat_user_label_assist')->whereIn('label_id', $labelId)->field(['user_id']);
            });
        })->when(isset($where['group_id']) && $where['group_id'], function ($query) use ($where) {
            $query->whereIn('to_user_id', function ($query) use ($where) {
                $query->name('chat_user')->whereIn('group_id', $where['group_id'])->field(['id']);
            });
        })->when(isset($where['delete']), function ($query) {
            $query->whereNull('delete_time');
        })->field($field)->order('update_time desc')
            ->select()->toArray();
    }

    /**
     * @param array $where
     * @param int $page
     * @param int $limit
     * @param array $with
     * @return BaseModel|mixed|Model
     */
    public function recordModel(array $where, int $page = 0, int $limit = 0, array $with = [])
    {
        $labelId = isset($where['label_id']) ? $where['label_id'] : [];
        unset($where['label_id']);
        return $this->search($where)->when($page && $limit, function ($query) use ($page, $limit) {
            $query->page($page, $limit);
        })->when(count($with), function ($query) use ($with) {
            $query->with($with);
        })->when($labelId, function ($query) use ($labelId) {
            $query->whereIn('to_user_id', function ($query) use ($labelId) {
                $query->name('chat_user_label_assist')->whereIn('label_id', $labelId)->field(['user_id']);
            });
        })->when(isset($where['group_id']) && $where['group_id'], function ($query) use ($where) {
            $query->whereIn('to_user_id', function ($query) use ($where) {
                $query->name('chat_user')->whereIn('group_id', $where['group_id'])->field(['id']);
            });
        })->when(isset($where['delete']), function ($query) {
            $query->whereNull('delete_time');
        });
    }

    /**
     * 查询最近和用户聊天的uid用户
     * @param array $where
     * @param string $key
     * @return array|Model|null
     * @throws DataNotFoundException
     * @throws DbException
     * @throws ModelNotFoundException
     */
    public function getLatelyMsgUid(array $where, string $key)
    {
        return $this->search($where)->order('update_time DESC')->value($key);
    }

    /**
     * @param array $where
     * @return array
     * @throws DataNotFoundException
     * @throws DbException
     * @throws ModelNotFoundException
     */
    public function kefuStatistics(array $where)
    {
        return $this->search()
            ->when(isset($where['user_id']) && $where['user_id'], function ($query) use ($where) {
                $query->where('user_id', $where['user_id']);
            })->whereBetweenTime('add_time', $where['startTime'], $where['endTime'])
            ->field(["FROM_UNIXTIME(add_time,'%Y-%m') as month", 'count(*) as number'])
            ->group('month')
            ->select()->toArray();
    }
}
