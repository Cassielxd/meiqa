<?php

namespace app\validate\chat;


use think\Validate;

class ChatServiceFeedbackValidate extends Validate
{

    /**
     * @var string[]
     */
    protected $regex = ['phone' => '/^1[3456789]\d{9}$/'];

    /**
     * @var string[]
     */
    protected $rule = [
        'phone'     => 'require|regex:phone',
        'rela_name' => 'require',
        'content'   => 'require',
    ];

    /**
     * @var string[]
     */
    protected $message = [
        'phone.require'     => '请输入手机号',
        'phone.regex'       => '手机号格式错误',
        'content.require'   => '请填写反馈内容',
        'rela_name.require' => '请填写真实姓名',
    ];
}
