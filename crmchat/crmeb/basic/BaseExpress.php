<?php

namespace crmeb\basic;

use crmeb\services\AccessTokenServeService;
use crmeb\services\HttpService;
use think\exception\ValidateException;
use think\facade\Config;
use crmeb\services\CacheService;

/**
 * Class BaseExpress
 * @package crmeb\basic
 */
abstract class BaseExpress extends BaseStorage
{

    /**
     * access_token
     * @var null
     */
    protected $accessToken = NULL;


    public function __construct(string $name, AccessTokenServeService $accessTokenServeService, string $configFile)
    {
        parent::__construct($name, [], $configFile);
        $this->accessToken = $accessTokenServeService;
    }

    /**
     * 初始化
     * @param array $config
     * @return mixed|void
     */
    protected function initialize(array $config = [])
    {
//        parent::initialize($config);
    }


    /**
     * 开通服务
     * @return mixed
     */
    abstract public function open();

    /**物流追踪
     * @return mixed
     */
    abstract public function query(string $num, string $com = '');

    /**电子面单
     * @return mixed
     */
    abstract public function dump($data);

    /**快递公司
     * @return mixed
     */
    //abstract public function express($type, $page, $limit);

    /**面单模板
     * @return mixed
     */
    abstract public function temp(string $com);
}
