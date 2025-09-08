<?php

namespace app\validate\kefu;


use think\Validate;

class SpeechcraftValidate extends Validate
{
    /**
     * @var string[]
     */
    protected $rule = [
        'title'   => 'length:0,50',
        'cate_id' => 'require|number',
        'message' => 'require|length:0,500',
        'sort'    => 'number',
    ];

    /**
     * @var string[]
     */
    protected $message = [
        'title.chsAlphaNum' => '请填汉字字母或者数字',
        'title.length'      => '标题长度不能超过50个字',
        'cate_id.require'   => '请选择分类',
        'cate_id.number'    => '分类必须为数字',
        'message.require'   => '请填写话术内容',
        'message.length'    => '话术长度不能超过500个字',
        'sort.number'       => '排序必须为数字',
    ];
}
