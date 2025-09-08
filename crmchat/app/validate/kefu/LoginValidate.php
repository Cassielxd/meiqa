<?php

namespace app\validate\kefu;


use think\Validate;

/**
 * Class LoginValidate
 * @package app\validate\kefu
 */
class LoginValidate extends Validate
{
    protected $regex = ['account' => '/^[a-zA-Z0-9]{4,30}$/'];

    /**
     * @var string[]
     */
    protected $rule = [
        'account'  => 'require|account',
        'password' => 'require',
    ];

    /**
     * @var string[]
     */
    protected $message = [
        'account.require' => '请填写账号',
        'account.account' => '请填写正确的账号',
        'password.regex'  => '请填写密码',
    ];
}
