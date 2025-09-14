<?php
declare (strict_types = 1);

namespace app\http\middleware\tenant;

use app\Request;
use crmeb\interfaces\MiddlewareInterface;
use crmeb\exceptions\AuthException;

/**
 * 租户权限检查中间件
 * Class TenantCheckRoleMiddleware
 * @package app\http\middleware\tenant
 */
class TenantCheckRoleMiddleware implements MiddlewareInterface
{
    /**
     * 处理请求
     * @param Request $request
     * @param \Closure $next
     * @return mixed
     */
    public function handle(Request $request, \Closure $next)
    {
        if (!$request->isTenantLogin()) {
            throw new AuthException('请先登录');
        }

        $tenantInfo = $request->tenantInfo();
        
        // 检查租户状态
        if ($tenantInfo['status'] !== 1) {
            throw new AuthException('租户状态异常，无法访问');
        }

        // 检查是否过期
        if ($tenantInfo['is_expired']) {
            throw new AuthException('租户已过期，请联系管理员');
        }

        return $next($request);
    }
}