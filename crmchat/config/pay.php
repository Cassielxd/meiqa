<?php

return [
    //默认支付模式
    'default' => 'wechat',
    //支付方式
    'payType' => ['weixin' => '微信支付', 'yue' => '余额支付', 'offline' => '线下支付'],
    //提现方式
    'extractType' => ['alipay', 'bank', 'weixin'],
    //配送方式
    'deliveryType' => ['send' => '商家配送', 'express' => '快递配送'],
    //驱动模式
    'stores' => [
        //微信支付
        'wechat' => [],
        //小程序支付
        'routine' => [],
        //微信h5支付
        'wechath5' => [],
        //支付宝支付
        'alipay' => [],
        //余额支付
        'yue' => [],
    ]
];
