<?php

namespace app\controller\mobile;


use app\Request;
use app\services\chat\ChatServiceFeedbackServices;
use app\validate\chat\ChatServiceFeedbackValidate;

/**
 * Class Feedback
 * @package app\controller\mobile
 */
class Feedback extends AuthController
{

    /**
     * Feedback constructor.
     * @param ChatServiceFeedbackServices $services
     */
    public function __construct(ChatServiceFeedbackServices $services)
    {
        parent::__construct();
        $this->services = $services;
    }

    /**
     * 保存反馈信息
     * @param Request $request
     * @param ChatServiceFeedbackServices $services
     * @return mixed
     */
    public function saveFeedback(Request $request, ChatServiceFeedbackServices $services)
    {
        $data = $request->postMore([
            ['rela_name', ''],
            ['phone', ''],
            ['content', ''],
        ]);

        validate(ChatServiceFeedbackValidate::class)->check($data);

        $data['content']  = htmlspecialchars($data['content']);
        $data['add_time'] = time();
        $services->save($data);
        return $this->success('保存成功');
    }

    /**
     * 客服反馈页面头部文字
     * @return mixed
     */
    public function getFeedbackInfo()
    {
        return $this->success(['feedback' => sys_config('service_feedback')]);
    }
}
