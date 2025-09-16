<?php
declare (strict_types = 1);

namespace app\validates\tenant;

use think\Validate;

/**
 * 租户注册验证器
 * Class TenantRegisterValidate
 * @package app\validates\tenant
 */
class TenantRegisterValidate extends Validate
{
    /**
     * 定义验证规则
     * @var array
     */
    protected $rule = [
        'tenant_name' => 'require|length:2,50|unique:tenants',
        'contact_name' => 'require|length:2,20',
        'contact_phone' => 'max:20',
        'contact_email' => 'require|email|unique:tenants',
        'captcha' => 'require|length:4,6',
        'pwd' => 'require|length:6,32',
        'confirm_pwd' => 'require|confirm:pwd',
    ];

    /**
     * 定义错误信息
     * @var array
     */
    protected $message = [
        'tenant_name.require' => '请输入租户名称',
        'tenant_name.length' => '租户名称长度必须在2-50个字符之间',
        'tenant_name.unique' => '租户名称已存在',
        'contact_name.require' => '请输入联系人姓名',
        'contact_name.length' => '联系人姓名长度必须在2-20个字符之间',
        'contact_phone.max' => '联系电话长度不能超过20个字符',
        'contact_email.require' => '请输入联系邮箱',
        'contact_email.email' => '请输入正确的邮箱地址',
        'contact_email.unique' => '该邮箱已被注册',
        'captcha.require' => '请输入验证码',
        'captcha.length' => '验证码长度错误',
        'pwd.require' => '请输入密码',
        'pwd.length' => '密码长度必须在6-32位之间',
        'confirm_pwd.require' => '请输入确认密码',
        'confirm_pwd.confirm' => '两次输入的密码不一致',
    ];

    /**
     * 定义验证场景
     * @var array
     */
    protected $scene = [
        'register' => ['tenant_name', 'contact_name', 'contact_phone', 'contact_email', 'captcha', 'pwd', 'confirm_pwd'],
    ];
}
