<?php

namespace crmeb\services\serve;


use crmeb\basic\BaseManager;
use crmeb\services\AccessTokenServeService;
use crmeb\services\serve\storage\Crmeb;
use think\facade\Config;
use think\Container;

/**
 * Class Serve
 * @package crmeb\services\serve
 * @mixin Crmeb
 */
class Serve extends BaseManager
{
    /**
     * 空间名
     * @var string
     */
    protected $namespace = '\\crmeb\\services\\serve\\storage\\';

    /**
     * 默认驱动
     * @return mixed
     */
    protected function getDefaultDriver()
    {
        return Config::get('serve.default', 'crmeb');
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

        $handleAccessToken = new AccessTokenServeService($this->config['account'] ?? '', $this->config['secret'] ?? '');
        $handle = Container::getInstance()->invokeClass($class, [$this->name, $handleAccessToken, $this->configFile]);
        $this->config = [];
        return $handle;
    }
}
