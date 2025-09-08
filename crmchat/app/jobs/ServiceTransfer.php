<?php
namespace app\jobs;


use app\services\chat\ChatServiceDialogueRecordServices;
use crmeb\basic\BaseJobs;
use crmeb\traits\QueueTrait;

/**
 * 转接同步聊天记录
 * Class ServiceTransfer
 * @package app\jobs
 */
class ServiceTransfer extends BaseJobs
{

    use QueueTrait;

    /**
     * @param array $where
     * @param int $kfuUserId
     * @param int $kefuToUserId
     * @param int $page
     * @param int $limit
     * @return bool
     */
    public function doJob(array $where, int $kfuUserId, int $kefuToUserId, int $page, int $limit)
    {
        /** @var ChatServiceDialogueRecordServices $service */
        $service = app()->make(ChatServiceDialogueRecordServices::class);
        $list = $service->getMessageList($where, $page, $limit);
        foreach ($list as $item) {
            if ($item['to_user_id'] == $kfuUserId) {
                $item['to_user_id'] = $kefuToUserId;
            }
            if ($item['user_id'] == $kfuUserId) {
                $item['user_id'] = $kefuToUserId;
            }
            $service->update($item['id'], [
                'to_user_id' => $item['to_user_id'],
                'user_id' => $item['user_id']
            ]);
        }

        return true;
    }

}
