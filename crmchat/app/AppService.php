<?php

namespace app;

use crmeb\services\SystemConfigService;
use crmeb\services\GroupDataService;
use crmeb\utils\Encrypter;
use crmeb\utils\Json;
use think\helper\Str;
use think\Service;
use crmeb\exceptions\MissingAppKeyException;

/**
 * Class AppService
 * @package app
 */
class AppService extends Service
{

    /**
     * 绑定实例
     * @var string[]
     */
    public $bind = [
        'json'         => Json::class,
        'sysConfig'    => SystemConfigService::class,
        'sysGroupData' => GroupDataService::class
    ];

    /**
     * 注入服务
     */
    public function register()
    {
        $this->app->bind(Encrypter::class, function () {
            $config = $this->app->make('config')->get('app');

            return new Encrypter($this->parseKey($config), $config['cipher']);
        });
    }

    public function boot()
    {
        defined('DS') || define('DS', DIRECTORY_SEPARATOR);
    }

    /**
     * @param array $config
     * @return false|mixed|string
     */
    protected function parseKey(array $config)
    {
        if (Str::startsWith($key = $this->key($config), $prefix = 'base64:')) {
            $key = base64_decode(\crmeb\utils\Str::after($key, $prefix));
        }

        return $key;
    }

    /**
     * @param array $config
     * @return mixed
     */
    protected function key(array $config)
    {
        return tap($config['key'], function ($key) {
            if (empty($key)) {
                throw new MissingAppKeyException('Missing app key');
            }
        });
    }
}
