<?php

namespace crmeb\services\uniPush;


use think\helper\Str;

/**
 * Class OptionsBase
 * @package crmeb\services\uniPush
 */
abstract class OptionsBase
{

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
        return $data;
    }

    /**
     * 获取参数
     * @param string $key
     * @return mixed|null
     */
    public function get(string $key)
    {
        return $this->{$key} ?? null;
    }

    /**
     * 设置参数
     * @param string $key
     * @param $value
     * @return $this|mixed
     */
    public function set(string $key, $value)
    {
        $this->{$key} = $value;
        return $this;
    }
}
