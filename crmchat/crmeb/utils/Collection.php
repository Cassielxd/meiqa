<?php

namespace crmeb\utils;

/**
 * 数据集管理类
 * Class Collection
 * @package crmeb\utils
 */
class Collection extends \think\Collection
{

    /**
     * Get an item from the collection by key.
     *
     * @param mixed $key
     * @param mixed $default
     * @return mixed
     */
    public function get($key, $default = null)
    {
        if (strstr($key, '.') !== false) {
            $keys  = explode('.', $key);
            $value = $this->items;
            foreach ($keys as $k) {
                if (isset($value[$k])) {
                    $value = $value[$k];
                }
            }
            return $value;
        }
        if (array_key_exists($key, $this->items)) {
            return $this->items[$key];
        }

        return value($default);
    }

    /**
     * @param $name
     * @return mixed
     */
    public function __get($name)
    {
        return $this->get($name);
    }
}
