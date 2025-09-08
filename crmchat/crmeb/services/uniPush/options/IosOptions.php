<?php

namespace crmeb\services\uniPush\options;


use crmeb\services\uniPush\OptionsBase;
use think\helper\Str;

/**
 * Class IosOptions
 * @package crmeb\services\uniPush\options
 */
class IosOptions extends OptionsBase
{


    public $title;

    public $body;

    public $payload;

    public $autoBadge;

    /**
     * @return array
     */
    public function toArray()
    {
        $publicData = get_object_vars($this);
        $data       = [];
        foreach ($publicData as $key => $value) {
            $data[Str::snake($key)] = $value;
        }

        return [
            'type'       => 'notify',
            'aps'        => [
                'alert' => $data,
                'sound' => 'sound'
            ],
            'payload'    => json_encode($this->payload),
            'auto_badge' => $this->autoBadge
        ];
    }
}
