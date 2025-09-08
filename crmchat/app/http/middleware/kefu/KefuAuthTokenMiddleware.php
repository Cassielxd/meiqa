<?php
namespace app\http\middleware\kefu;


use app\Request;
use app\services\kefu\LoginServices;
use crmeb\interfaces\MiddlewareInterface;
use think\facade\Config;

/**
 * Class KefuAuthTokenMiddleware
 * @package app\kefu\middleware
 */
class KefuAuthTokenMiddleware implements MiddlewareInterface
{

    /**
     * @param Request $request
     * @param \Closure $next
     * @throws \Psr\SimpleCache\InvalidArgumentException
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function handle(Request $request, \Closure $next)
    {
        $authInfo = null;
        $token = trim(ltrim($request->header(Config::get('cookie.token_name', 'Authori-zation')), 'Bearer'));
        /** @var LoginServices $services */
        $services = app()->make(LoginServices::class);
        $kefuInfo = $services->parseToken($token);

        Request::macro('kefuId', function () use (&$kefuInfo) {
            return (int)$kefuInfo['id'];
        });

        Request::macro('kefuInfo', function () use (&$kefuInfo) {
            return $kefuInfo;
        });

        return $next($request);
    }
}
