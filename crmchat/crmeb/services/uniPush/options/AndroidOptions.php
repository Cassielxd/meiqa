<?php

namespace crmeb\services\uniPush\options;


use crmeb\services\uniPush\OptionsBase;
use think\helper\Str;

/**
 * Class AndroidOptions
 * @package crmeb\services\uniPush\options
 */
class AndroidOptions extends OptionsBase
{

    public $title;

    public $body;

    public $clickType = 'payload';

    public $payload = '';

    /**
     * 华为角标
     * @var int
     */
    public $HWbadgeNum = 0;

    /**
     * @return array
     */
    public function toArray()
    {
        $publicData = get_object_vars($this);
        $data = [];
        foreach ($publicData as $key => $value) {
            $data[Str::snake($key)] = $value;
        }
        return [
            'ups' => [
                'notification' => $data,
                'options' => [
                    'VV' => ['classification' => 1],
                    'HW' => [
                        '/message/android/notification/importance' => 'HIGH',
                        '/message/android/notification/big_title' => $data['title'],
                        '/message/android/notification/big_body' => $data['body'],
                        '/message/android/notification/badge/set_num' => $this->HWbadgeNum
                    ],
                ]
            ],
        ];
    }
}
