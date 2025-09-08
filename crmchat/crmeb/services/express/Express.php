<?php

namespace crmeb\services\express;


use crmeb\basic\BaseManager;
use crmeb\services\AccessTokenServeService;
use think\Container;
use think\facade\Config;

/**
 * Class Express
 * @package crmeb\services\express
 * @mixin \crmeb\services\express\storage\Express
 */
class Express extends BaseManager
{
    /**
     * 空间名
     * @var string
     */
    protected $namespace = '\\crmeb\\services\\express\\storage\\';

    /**
     * 默认驱动
     * @return mixed
     */
    protected function getDefaultDriver()
    {
        return Config::get('express.default', 'express');
    }

    /**
     * 获取类的实例
     * @param $class
     * @return mixed|void
     */
    protected function invokeClass($class)
    {
        if (!class_exists($class)) {
            throw new \RuntimeException('class not exists: ' . $class);
        }
        $this->getConfigFile();

        if (!$this->config) {
            $this->config = Config::get($this->configFile . '.stores.' . $this->name, []);
        }
        $handleAccessToken = new AccessTokenServeService($this->config['account'] ?? '', $this->config['secret'] ?? '', app()->cache);
        $handle = Container::getInstance()->invokeClass($class, [$this->name, $handleAccessToken, $this->configFile]);
        $this->config = [];
        return $handle;
    }
}
