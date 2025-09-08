<?php
namespace app\http\middleware\admin;


use app\Request;
use app\services\system\log\SystemLogServices;
use crmeb\interfaces\MiddlewareInterface;

/**
 * 日志中間件
 * Class AdminLogMiddleware
 * @package app\http\middleware\admin
 */
class AdminLogMiddleware implements MiddlewareInterface
{
    /**
     * @param Request $request
     * @param \Closure $next
     * @return mixed
     */
    public function handle(Request $request, \Closure $next)
    {
        try {
            /** @var SystemLogServices $services */
            $services = app()->make(SystemLogServices::class);
            $services->recordAdminLog($request->adminId(), $request->adminInfo()['account'], 'system');
        } catch (\Throwable $e) {
        }
        return $next($request);
    }

}
