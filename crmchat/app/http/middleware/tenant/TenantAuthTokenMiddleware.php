<?php
declare (strict_types = 1);

namespace app\http\middleware\tenant;

use app\Request;
use app\services\tenant\TenantAuthServices;
use crmeb\interfaces\MiddlewareInterface;
use think\facade\Config;

/**
 * 租户登录验证中间件
 * Class TenantAuthTokenMiddleware
 * @package app\http\middleware\tenant
 */
class TenantAuthTokenMiddleware implements MiddlewareInterface
{
    /**
     * 处理请求
     * @param Request $request
     * @param \Closure $next
     * @return mixed
     */
    public function handle(Request $request, \Closure $next)
    {
        $tenantInfo = null;
        $token = trim(ltrim($request->header(Config::get('cookie.token_name', 'Authori-zation')), 'Bearer'));

        // 解析tenant token
        try {
            /** @var TenantAuthServices $tenantService */
            $tenantService = app()->make(TenantAuthServices::class);
            $tenantInfo = $tenantService->parseToken($token);
        } catch (\Throwable $e) {
            // 记录错误日志
            // Log::error('认证失败: ' . $e->getMessage());
        }

        // 注入租户相关的宏方法
        Request::macro('isTenantLogin', function () use (&$tenantInfo) {
            return !is_null($tenantInfo);
        });

        Request::macro('tenantId', function () use (&$tenantInfo) {
            return $tenantInfo ? $tenantInfo['id'] : null;
        });

        Request::macro('tenantInfo', function () use (&$tenantInfo) {
            return $tenantInfo;
        });

        Request::macro('tenantAppid', function () use (&$tenantInfo) {
            return $tenantInfo ? $tenantInfo['appid'] : null;
        });

        Request::macro('tenantCode', function () use (&$tenantInfo) {
            return $tenantInfo ? $tenantInfo['tenant_code'] : null;
        });

        return $next($request);
    }
}