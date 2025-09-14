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
        $adminInfo = null;
        $token = trim(ltrim($request->header(Config::get('cookie.token_name', 'Authori-zation')), 'Bearer'));

        // 优先尝试解析admin token（向下兼容）
        try {
            /** @var \app\services\system\admin\AdminAuthServices $adminService */
            $adminService = app()->make(\app\services\system\admin\AdminAuthServices::class);
            $adminInfo = $adminService->parseToken($token);
        } catch (\Throwable $e) {
            // admin token解析失败，尝试解析tenant token
            try {
                /** @var TenantAuthServices $tenantService */
                $tenantService = app()->make(TenantAuthServices::class);
                $tenantInfo = $tenantService->parseToken($token);
            } catch (\Throwable $e2) {
                // 记录错误日志
                // Log::error('认证失败: ' . $e2->getMessage());
            }
        }

        // 注入租户相关的宏方法（支持admin向下兼容）
        Request::macro('isTenantLogin', function () use (&$tenantInfo) {
            return !is_null($tenantInfo);
        });

        Request::macro('isAdminLogin', function () use (&$adminInfo) {
            return !is_null($adminInfo);
        });

        Request::macro('tenantId', function () use (&$tenantInfo) {
            return $tenantInfo ? $tenantInfo['id'] : null;
        });

        Request::macro('adminId', function () use (&$adminInfo) {
            return $adminInfo ? $adminInfo['id'] : null;
        });

        Request::macro('tenantInfo', function () use (&$tenantInfo) {
            return $tenantInfo;
        });

        Request::macro('adminInfo', function () use (&$adminInfo) {
            return $adminInfo;
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