<?php
declare (strict_types = 1);

namespace app\validates\tenant;

use think\Validate;

/**
 * 租户登录验证器
 * Class TenantLoginValidate
 * @package app\validates\tenant
 */
class TenantLoginValidate extends Validate
{
    /**
     * 定义验证规则
     * @var array
     */
    protected $rule = [
        'pwd' => 'require|length:6,32',
        'old_password' => 'require|length:6,32',
        'new_password' => 'require|length:6,32',
    ];

    /**
     * 定义错误信息
     * @var array
     */
    protected $message = [
        'account.require' => '请输入邮箱账号',
        'account.max' => '邮箱长度最多100个字符',
        'pwd.require' => '请输入密码',
        'pwd.length' => '密码长度必须在6-32个字符之间',
        'old_password.require' => '请输入原密码',
        'old_password.length' => '原密码长度必须在6-32个字符之间',
        'new_password.require' => '请输入新密码',
        'new_password.length' => '新密码长度必须在6-32个字符之间',
    ];

    /**
     * 定义验证场景
     * @var array
     */
    protected $scene = [
        'login' => ['account', 'pwd'],
        'change_password' => ['old_password', 'new_password'],
    ];
}
