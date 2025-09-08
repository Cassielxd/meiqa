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

namespace app\controller\mobile;


use app\Request;
use app\services\chat\ChatServiceDialogueRecordServices;
use app\services\chat\ChatServiceServices;
use app\services\other\CacheServices;
use app\services\kefu\KefuServices;
use app\services\system\attachment\SystemAttachmentServices;
use crmeb\services\CacheService;
use crmeb\services\UploadService;
use think\db\exception\DataNotFoundException;
use think\db\exception\DbException;
use think\db\exception\ModelNotFoundException;

/**
 * Class Service
 * @package app\controller\mobile
 */
class Service extends AuthController
{
    /**
     * Service constructor.
     * @param ChatServiceServices $services
     */
    public function __construct(ChatServiceServices $services)
    {
        parent::__construct();
        $this->services = $services;
    }

    /**
     * 获取聊天记录
     * @return mixed
     * @throws DataNotFoundException
     * @throws DbException
     * @throws ModelNotFoundException
     */
    public function getRecordList()
    {
        [$idTo, $limit, $toUserId, $cookieUid, $kefuUd, $kefuRand] = $this->request->getMore([
            ['idTo', 0],
            ['limit', 10],
            ['toUserId', 0],
            ['cookieUid', 0],
            ['kefu_id', 0],
            ['kefu_rand', 0],
        ], true);

        $user = $this->request->getMore([
            ['uid', ''],
            ['nickname', ''],
            ['phone', ''],
            ['sex', ''],
            ['avatar', ''],
            ['openid', ''],
            ['type', ''],
        ]);
        //优先使用指定客服
        if ($kefuUd && $toUserId) {
            $toUserId = 0;
        }
        //指定客服id和随机客服id有限使用客服id
        if ($kefuUd && $kefuRand) {
            $kefuRand = 0;
        }
        //指定客服后，随机客服取消
        if ($toUserId && $kefuRand) {
            $kefuRand = 0;
            $kefuUd = 0;
        }
        return app('json')->successful($this->services->getRecord($this->appId, $user, $idTo, $limit, $toUserId, (int)$cookieUid, (int)$kefuUd, (int)$kefuRand));
    }

    /**
     * 获取客服页面广告内容
     * @return mixed
     */
    public function getKfAdv()
    {
        /** @var CacheServices $cache */
        $cache = app()->make(CacheServices::class);
        $content = $cache->getDbCache('kf_adv', '');
        return $this->success(compact('content'));
    }

    /**
     * @param $key
     * @return mixed
     */
    public function getCache($key)
    {
        /** @var CacheServices $cache */
        $cache = app()->make(CacheServices::class);
        $value = $cache->getDbCache($key, []);
        return $this->success(compact('value'));
    }

    /**
     * @param $key
     * @return mixed
     */
    public function setCache()
    {
        [$key, $value] = $this->request->postMore([
            ['key', ''],
            ['value', []],
        ]);
        if (!$key) {
            return $this->fail('key必须存在');
        }
        /** @var CacheServices $cache */
        $cache = app()->make(CacheServices::class);
        $cache->setDbCache($key, $value, 600);
        return $this->success('ok');
    }


    /**
     * 图片上传
     * @param Request $request
     * @return mixed
     * @throws \Psr\SimpleCache\InvalidArgumentException
     */
    public function upload(Request $request, SystemAttachmentServices $services)
    {
        $data = $request->postMore([
            ['filename', 'file'],
        ]);
        if (!$data['filename']) return $this->fail('参数有误');
        if (CacheService::has('start_uploads_' . $request->appId()) && CacheService::get('start_uploads_' . $request->appId()) >= 500) return $this->fail('非法操作');
        $upload = UploadService::init();
        $info = $upload->to('store/comment')->validate()->move($data['filename']);
        if ($info === false) {
            return $this->fail($upload->getError());
        }
        $res = $upload->getUploadInfo();
        $services->attachmentAdd($res['name'], $res['size'], $res['type'], $res['dir'], $res['thumb_path'], 1, (int)sys_config('upload_type', 1), $res['time'], 2);
        if (CacheService::has('start_uploads_' . $request->appId()))
            $start_uploads = (int)CacheService::get('start_uploads_' . $request->appId());
        else
            $start_uploads = 0;
        $start_uploads++;
        CacheService::set('start_uploads_' . $request->appId(), $start_uploads, 86400);
        $res['dir'] = path_to_url($res['dir']);
        if (strpos($res['dir'], 'http') === false) $res['dir'] = $request->domain() . $res['dir'];
        return $this->success('图片上传成功!', ['name' => $res['name'], 'url' => $res['dir']]);
    }

    /**
     * @return mixed
     */
    public function getKefuConfig()
    {
        $type = sys_config('kefu_icon_type');
        $icon = sys_config('kefu_icon_url' . $type);
        return $this->success(['icon' => $icon, 'type' => $type]);
    }

    /**
     * 获取消息id
     * @param ChatServiceDialogueRecordServices $services
     * @return mixed
     * @throws \Psr\SimpleCache\InvalidArgumentException
     */
    public function getSendId(ChatServiceDialogueRecordServices $services)
    {
        $sendId = $services->getSendId();
        CacheService::redisHandler()->set($sendId, 1);
        return $this->success(['send_id' => $sendId]);
    }

    /**
     * 发送消息
     * @return mixed
     * @throws \Psr\SimpleCache\InvalidArgumentException
     */
    public function sendMessage(KefuServices $services)
    {
        $data = $this->request->postMore([
            ['to_user_id', 0],
            ['msn_type', 0],
            ['msn', ''],
            ['other', ''],
            ['guid', ''],
            ['is_tourist', ''],
            ['user_id', ''],
        ]);

        if (!$data['guid']) {
            return $this->fail('消息ID不存在！');
        }
        if (!$data['user_id']) {
            return $this->fail('缺少user_id');
        }

        $userId = $data['user_id'];
        unset($data['user_id']);

        if (!$data['to_user_id']) {
            return $this->fail('用户不存在');
        }
        if ($data['to_user_id'] == $userId) {
            return $this->fail('不能和自己聊天');
        }

        $res = $services->sendMessage($data, $userId, $this->appId, 'kefu');

        $res['guid'] = $data['guid'];

        return $this->success('发送成功', $res);
    }



}
