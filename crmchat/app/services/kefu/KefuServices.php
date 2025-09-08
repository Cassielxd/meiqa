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

namespace app\services\kefu;


use app\dao\chat\ChatServiceDao;
use app\jobs\AutoBadge;
use app\jobs\ServiceTransfer;
use app\jobs\UniPush;
use app\services\chat\ChatServiceAuxiliaryServices;
use app\services\chat\ChatServiceDialogueRecordServices;
use app\services\chat\ChatServiceRecordServices;
use app\services\chat\ChatServiceServices;
use app\services\chat\ChatUserServices;
use app\webscoket\Manager;
use app\webscoket\Response;
use app\webscoket\Room;
use crmeb\basic\BaseServices;
use crmeb\services\SwooleTaskService;
use think\db\exception\DataNotFoundException;
use think\db\exception\DbException;
use think\db\exception\ModelNotFoundException;
use think\exception\ValidateException;

/**
 * Class KefuServices
 * @package app\services\kefu
 */
class KefuServices extends BaseServices
{

    /**
     * KefuServices constructor.
     * @param ChatServiceDao $dao
     */
    public function __construct(ChatServiceDao $dao)
    {
        $this->dao = $dao;
    }

    /**
     * 获取客服列表
     * @param array $where
     * @return array
     * @throws DataNotFoundException
     * @throws DbException
     * @throws ModelNotFoundException
     */
    public function getServiceList(array $where, array $noId)
    {
        $where['status'] = 1;
        $where['noId'] = $noId;
        $where['online'] = 1;
        [$page, $limit] = $this->getPageValue();
        $list = $this->dao->getServiceList($where, $page, $limit);
        $count = $this->dao->count($where);
        return compact('list', 'count');
    }

    /**
     * 获取聊天记录
     * @param int $userId
     * @param int $toUserId
     * @param int $isUp
     * @return array
     * @throws DataNotFoundException
     * @throws DbException
     * @throws ModelNotFoundException
     */
    public function getChatList(int $userId, int $toUserId, int $upperId, string $appId = '')
    {
        /** @var ChatServiceDialogueRecordServices $service */
        $service = app()->make(ChatServiceDialogueRecordServices::class);
        [$page, $limit] = $this->getPageValue();
        AutoBadge::dispatch([$userId, $toUserId, $appId]);
        return array_reverse($service->tidyChat($service->getServiceChatList(['appid' => $appId, 'to_user_id' => $toUserId], $limit, $upperId)));
    }

    /**
     * 转移客服
     * @param string $appid
     * @param int $kfuUserId
     * @param int $userId
     * @param int $kefuToUserId
     * @return bool
     */
    public function setTransfer(string $appid, int $kfuUserId, int $userId, int $kefuToUserId)
    {
        if ($userId === $kefuToUserId) {
            throw new ValidateException('自己不能转接给自己');
        }
        /** @var ChatServiceDialogueRecordServices $service */
        $service = app()->make(ChatServiceDialogueRecordServices::class);
        /** @var ChatServiceAuxiliaryServices $transfeerService */
        $transfeerService = app()->make(ChatServiceAuxiliaryServices::class);
        $where = ['chat' => [$kfuUserId, $userId]];
        $messageData = $service->getMessageOne($where);
        $messageData = $messageData ? $messageData->toArray() : [];
        $record = $this->transaction(function () use ($transfeerService, $where, $messageData, $appid, $service, $kfuUserId, $userId, $kefuToUserId) {
            /** @var ChatServiceRecordServices $serviceRecord */
            $serviceRecord = app()->make(ChatServiceRecordServices::class);
            $info = $serviceRecord->get(['user_id' => $kfuUserId, 'to_user_id' => $userId, 'appid' => $appid], ['id', 'type', 'message_type', 'is_tourist', 'avatar', 'nickname']);
            $record = $serviceRecord->saveRecord(
                $appid,
                $userId,
                $kefuToUserId,
                $messageData['msn'] ?? '',
                $info['type'] ?? 1,
                $messageData['message_type'] ?? 1,
                0,
                (int)($info['is_tourist'] ?? 0),
                $info['nickname'] ?? "",
                $info['avatar'] ?? ''
            );
            $res = $serviceRecord->delete(['user_id' => $kfuUserId, 'to_user_id' => $userId, 'appid' => $appid]);
            $res = $res && $serviceRecord->delete(['user_id' => $userId, 'to_user_id' => $kfuUserId, 'appid' => $appid]);
            $transfeerService->saveAuxliary([
                'binding_id' => $userId,
                'relation_id' => $kefuToUserId,
                'appid' => $appid
            ]);
            if (!$record && !$res) {
                throw new ValidateException('转接客服失败');
            }
            return $record;
        });
        try {
            $keufInfo = $this->dao->get(['user_id' => $kfuUserId], ['avatar', 'nickname']);
            if ($keufInfo) {
                $keufInfo = $keufInfo->toArray();
            } else {
                $keufInfo = (object)[];
            }

            /** @var ChatUserServices $services */
            $services = app()->make(ChatUserServices::class);
            $version = $services->value(['id' => $userId], 'version');
            if ($version) {
                $record['nickname'] = '[' . $version . ']' . $record['nickname'];
            }

            //给转接的客服发送消息通知
            SwooleTaskService::kefu()->type('transfer')->to($kefuToUserId)->data(['recored' => $record, 'kefuInfo' => $keufInfo])->push();
            //告知用户对接此用户聊天
            $keufToInfo = $this->dao->get(['user_id' => $kefuToUserId], ['avatar', 'nickname']);
            SwooleTaskService::user()->type('to_transfer')->to($userId)->data(['toUid' => $kefuToUserId, 'avatar' => $keufToInfo['avatar'] ?? '', 'nickname' => $keufToInfo['nickname'] ?? ''])->push();
        } catch (\Exception $e) {
        }
        return true;
    }

    /**
     * 发送消息
     * @param array $data
     * @param int $userId
     * @param string $appId
     * @param string $type
     * @return array
     */
    public function sendMessage(array $data, int $userId, string $appId, string $type = 'kefu')
    {

        $isCcli = (bool)preg_match("/cli/i", php_sapi_name());
        if (!$isCcli) {
            throw new ValidateException('请在CLI模式下运行');
        }

        $msnType = $data['msn_type'];//消息类型
        $msn = $data['msn'];//消息内容
        $guid = $data['guid'];//消息唯一id
        $toUserId = $data['to_user_id'];//送达人id
        $other = $data['other'];

        if (!in_array($msnType, ChatServiceDialogueRecordServices::MSN_TYPE)) {
            throw new ValidateException('格式错误');
        }

        $msn = trim(strip_tags(str_replace(["\n", "\t", "\r", "&nbsp;"], '', htmlspecialchars_decode($msn))));
        $saveData['to_user_id'] = $toUserId;
        $saveData['msn'] = $msn;
        $saveData['msn_type'] = $msnType;
        $saveData['add_time'] = time();
        $saveData['appid'] = $appId;
        $saveData['user_id'] = $userId;
        $saveData['guid'] = $guid;
        $saveData['is_send'] = 1;


        $online = false;
        $newUserInfo = [];
        //查到送达人是否和当前用户聊天
        $fds = Manager::userFd($type, $toUserId);
        if ($fds) {
            /** @var Room $room */
            $room = app()->make(Room::class);
            $userInfo = [];
            foreach ($fds as $fd) {
                if ($room->exist($fd)) {
                    $userInfo[] = $room->get($fd);
                }
            }
            foreach ($userInfo as $item) {
                if (isset($item['to_user_id']) && $item['to_user_id'] === $userId) {
                    $online = true;
                    $newUserInfo = $item;
                    break;
                }
            }
        }

        /** @var ChatServiceDialogueRecordServices $logServices */
        $logServices = app()->make(ChatServiceDialogueRecordServices::class);

        $saveData['type'] = $online ? 1 : 0;
        if (in_array($msnType, [5, 6])) {
            $saveData['other'] = json_encode($other);
        } else {
            $saveData['other'] = '';
        }

        $data = $logServices->save($saveData);
        $data = $data->toArray();
        $data['_add_time'] = $data['add_time'];
        $data['add_time'] = strtotime($data['add_time']);

        /** @var ChatUserServices $userService */
        $userService = app()->make(ChatUserServices::class);
        $_userInfo = $userService->getUserInfo($data['user_id'], ['nickname', 'avatar', 'version', 'is_tourist', 'online']);
        $isTourist = $_userInfo['is_tourist'];
        $data['nickname'] = $_userInfo['nickname'] ?? '';
        $data['avatar'] = $_userInfo['avatar'] ?? '';

        //用户向客服发送消息，判断当前客服是否在登录中
        /** @var ChatServiceRecordServices $serviceRecored */
        $serviceRecored = app()->make(ChatServiceRecordServices::class);
        $unMessagesCount = $logServices->getMessageNum(['user_id' => $userId, 'to_user_id' => $toUserId, 'type' => 0]);
        //记录当前用户和他人聊天记录
        $data['recored'] = $serviceRecored->saveRecord(
            $appId,
            $userId,
            $toUserId,
            $msn,
            $formType ?? 0,
            $msnType,
            $unMessagesCount,
            (int)$isTourist,
            $data['nickname'],
            $data['avatar'],
            isset($userInfo) ? 1 : 0
        );
        $data['recored']['nickname'] = isset($_userInfo['version']) && $_userInfo['version'] ? '[' . $_userInfo['version'] . ']' . $data['recored']['nickname'] : $data['recored']['nickname'];
        $data['recored']['_update_time'] = date('Y-m-d H:i', $data['recored']['update_time']);
        /** @var ChatServiceServices $services */
        $services = app()->make(ChatServiceServices::class);
        $kefuInfo = $services->get(['user_id' => $toUserId, 'appid' => $appId], ['is_backstage', 'online', 'client_id', 'auto_reply']);
        if (!$kefuInfo) {
            $clientId = '';
            $autoReply = false;
            $kefuOnline = false;
            $isBackstage = false;
        } else {
            $clientId = $kefuInfo->client_id;
            $autoReply = !!$kefuInfo->auto_reply;
            $kefuOnline = !!$kefuInfo['online'];
            $isBackstage = !!$kefuInfo['is_backstage'];
        }

        $toUserOnline = !!$userService->value(['id' => $toUserId], 'online');

        //自动回复
        if ($autoReply && $msn) {
            //如果自动回复的内容没,那么消息不会反回
            $autoReplyData = $services->autoReply(app(), $appId, $toUserId, $userId, $msn, $msnType, $other);
            if ($autoReplyData !== false) {
                //发送给当前客服
                if ($online && $toUserOnline) {
                    SwooleTaskService::kefu()->type('chat_auth')->to($toUserId)->data([$data, $autoReplyData])->push();
                }
                return compact('autoReply', 'autoReplyData');
            } else {
                $autoReply = false;
            }
        }

        //如果在线
        if ($online && $toUserOnline) {
            SwooleTaskService::serve($type)->type('reply')->to($toUserId)->data($data)->push();
        } else {
            //用户在线，可是没有和当前用户进行聊天，给当前用户发送未读条数
            if ($type === 'kefu') {
                //用户给客服发送消息需要查看客服是否在线
                $res = $isBackstage && $kefuOnline;
            } else {
                $res = true;
            }
            if ($fds && ($newUserInfo['to_user_id'] ?? 0) != $userId && $res) {

                $data['recored']['nickname'] = $_userInfo['nickname'];
                $data['recored']['avatar'] = $_userInfo['avatar'];
                $data['recored']['online'] = isset($userInfo) ? 1 : 0;

                $allUnMessagesCount = $logServices->getMessageNum([
                    'appid' => $appId,
                    'to_user_id' => $toUserId,
                    'type' => 0
                ]);


                SwooleTaskService::serve($type)->type('mssage_num')->to($toUserId)->data([
                    'user_id' => $userId,
                    'num' => $unMessagesCount,//某个用户的未读条数
                    'allNum' => $allUnMessagesCount,//总未读条数
                    'recored' => $data['recored']
                ])->push();

            } elseif ($kefuOnline && $clientId && $kefuInfo) {
                //客服不在线,但是客服在app登录了,状态保持在线,发送app推送消息

                UniPush::dispatch([
                    ['nickname' => $data['nickname'], 'to_user_id' => $toUserId, 'user_id' => $userId, 'appid' => $appId],
                    $clientId,
                    [
                        'content' => $msn,
                        'msn_type' => $data['msn_type'],
                        'other' => is_string($data['other']) ?
                            json_decode($data['other'], true) :
                            $data['other'],
                    ]
                ]);

            } elseif (!$kefuOnline && $kefuInfo) {
                //客服不在线,app端也不在线,自动转接给在线的客服
                $this->authTransfer($appId, $userId, $toUserId);
            }
        }

        return compact('autoReply');
    }

    /**
     * 聊天自动转接
     * @param Response $response
     * @param string $appid
     * @param $userId
     * @param $kfuUserId
     * @return void
     */
    protected function authTransfer(string $appid, $userId, $kfuUserId)
    {
        /** @var ChatServiceServices $services */
        $services = app()->make(ChatServiceServices::class);
        //客服不在线,app端也不在线,自动转接给在线的客服
        $kefuUserInfo = $services->getColumn(['online' => 1, 'appid' => $appid], 'user_id,id');
        if (!$kefuUserInfo) {
            SwooleTaskService::kefu()->type('kefu_logout')->to($userId)->data([
                'user_id' => $kfuUserId,
                'online' => 0
            ])->push();
            return;
        }

        $userIds = array_column($kefuUserInfo, 'user_id');
        mt_srand();
        $kefuToUserId = $userIds[array_rand($userIds)] ?? 0;

        /** @var ChatServiceDialogueRecordServices $service */
        $service = app()->make(ChatServiceDialogueRecordServices::class);
        $where = ['chat' => [$kfuUserId, $userId]];
        $messageData = $service->getMessageOne($where);
        $messageData = $messageData ? $messageData->toArray() : [];

        try {
            /** @var ChatServiceRecordServices $serviceRecord */
            $serviceRecord = app()->make(ChatServiceRecordServices::class);
            $info = $serviceRecord->get(['user_id' => $kfuUserId, 'to_user_id' => $userId, 'appid' => $appid], ['id', 'user_id', 'to_user_id', 'type', 'message_type', 'is_tourist', 'avatar', 'nickname']);
            /** @var ChatServiceAuxiliaryServices $transfeerService */
            $transfeerService = app()->make(ChatServiceAuxiliaryServices::class);
            $record = $service->transaction(function () use ($info, $serviceRecord, $messageData, $appid, $transfeerService, $service, $kfuUserId, $userId, $kefuToUserId) {
                $record = $serviceRecord->saveRecord(
                    $appid,
                    $userId,
                    $kefuToUserId,
                    $messageData['msn'] ?? '',
                    $info['type'] ?? 1,
                    $messageData['message_type'] ?? 1,
                    0,
                    (int)($info['is_tourist'] ?? 0),
                    $info['nickname'] ?? "",
                    $info['avatar'] ?? ''
                );
                $res = $serviceRecord->delete(['user_id' => $kfuUserId, 'to_user_id' => $userId, 'appid' => $appid]);
                $res = $res && $serviceRecord->delete(['user_id' => $userId, 'to_user_id' => $kfuUserId, 'appid' => $appid]);
                $transfeerService->saveAuxliary([
                    'binding_id' => $userId,
                    'relation_id' => $kefuToUserId,
                    'appid' => $appid
                ]);
                if (!$record && !$res) {
                    throw new ValidateException('转接客服失败');
                }
                return $record;
            });

            $keufInfo = $services->get(['user_id' => $kfuUserId], ['avatar', 'nickname']);
            if ($keufInfo) {
                $keufInfo = $keufInfo->toArray();
            } else {
                $keufInfo = (object)[];
            }
            /** @var ChatUserServices $userService */
            $userService = app()->make(ChatUserServices::class);
            $version = $userService->value(['id' => $userId], 'version');
            if ($version) {
                $record['nickname'] = '[' . $version . ']' . $record['nickname'];
            }

            //给转接的客服发送消息通知
            SwooleTaskService::kefu()->type('transfer')->to($kefuToUserId)->data([
                'recored' => $record,
                'kefuInfo' => $keufInfo
            ])->push();

            //给当前客服发送此用户已被转接走的消息通知
            SwooleTaskService::kefu()->type('transfer')->to($kfuUserId)->data([
                'recored' => $info->toArray()
            ])->push();

            //告知用户对接此用户聊天
            $keufToInfo = $services->get(['user_id' => $kefuToUserId], ['avatar', 'nickname']);
            SwooleTaskService::user()->type('to_transfer')->to($userId)->data([
                'toUid' => $kefuToUserId,
                'avatar' => $keufToInfo['avatar'] ?? '',
                'nickname' => $keufToInfo['nickname'] ?? ''
            ])->push();

        } catch (\Exception $e) {
            Log::error('自动转接客服失败:' . $e->getMessage());
        }

    }
}
