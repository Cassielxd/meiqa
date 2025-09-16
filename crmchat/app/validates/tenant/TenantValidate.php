<?php
declare (strict_types = 1);

namespace app\validates\tenant;

use think\Validate;

/**
 * 租户验证器
 * Class TenantValidate
 * @package app\validates\tenant
 */
class TenantValidate extends Validate
{
    /**
     * 定义验证规则
     * @var array
     */
    protected $rule = [
        'appid' => 'require|alphaNum|length:6,32',
        'tenant_name' => 'require|max:100',
        'tenant_code' => 'require|alphaNum|length:3,50',
        // 管理端创建/编辑：账号为邮箱
        'account' => 'require|email|max:100',
        'pwd' => 'require|length:6,32',
        'domain' => 'max:100|url',
        'logo' => 'max:255',
        'contact_name' => 'max:50',
        'contact_phone' => 'mobile',
        'contact_email' => 'email',
        'status' => 'in:0,1,2,3',
        'max_users' => 'number|egt:1',
        'max_services' => 'number|egt:1',
        'expire_at' => 'date',
    ];

    /**
     * 定义错误信息
     * @var array
     */
    protected $message = [
        'appid.require' => '应用ID必须填写',
        'appid.alphaNum' => '应用ID只能包含字母和数字',
        'appid.length' => '应用ID长度必须在6-32个字符之间',
        'tenant_name.require' => '租户名称必须填写',
        'tenant_name.max' => '租户名称最多100个字符',
        'tenant_code.require' => '租户编码必须填写',
        'tenant_code.alphaNum' => '租户编码只能包含字母和数字',
        'tenant_code.length' => '租户编码长度必须在3-50个字符之间',
        'account.require' => '管理员账号（邮箱）必须填写',
        'account.email' => '管理员账号必须为有效邮箱',
        'account.max' => '管理员账号长度最多100个字符',
        'pwd.require' => '管理员密码必须填写',
        'pwd.length' => '管理员密码长度必须在6-32个字符之间',
        'domain.max' => '租户域名最多100个字符',
        'domain.url' => '租户域名格式不正确',
        'logo.max' => 'Logo地址最多255个字符',
        'contact_name.max' => '联系人姓名最多50个字符',
        'contact_phone.mobile' => '联系人电话格式不正确',
        'contact_email.email' => '联系人邮箱格式不正确',
        'status.in' => '状态值不正确',
        'max_users.number' => '最大用户数必须是数字',
        'max_users.egt' => '最大用户数至少为1',
        'max_services.number' => '最大客服数必须是数字',
        'max_services.egt' => '最大客服数至少为1',
        'expire_at.date' => '到期时间格式不正确',
    ];

    /**
     * 定义验证场景
     * @var array
     */
    protected $scene = [
        'update' => [
            'appid' => 'alphaNum|length:6,32',
            'tenant_name' => 'max:100',
            'tenant_code' => 'alphaNum|length:3,50',
            'account' => 'email|max:100',
            'pwd' => 'length:6,32',
            'domain' => 'max:100|url',
            'logo' => 'max:255',
            'contact_name' => 'max:50',
            'contact_phone' => 'mobile',
            'contact_email' => 'email',
            'status' => 'in:0,1,2,3',
            'max_users' => 'number|egt:1',
            'max_services' => 'number|egt:1',
            'expire_at' => 'date',
        ],
    ];
}
