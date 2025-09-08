<?php

namespace app\validate;


use think\Validate;

class Test extends Validate
{
    protected $rule = [
        'real_name' => 'require|test_api',
    ];

    protected $message = [
        'real_name.require'  => '名称必须填写',
        'real_name.test_api' => '名称最多不能超过25个字符',
    ];
}
