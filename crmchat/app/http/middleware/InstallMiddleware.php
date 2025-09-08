<?php

namespace app\http\middleware;


use app\Request;
use crmeb\interfaces\MiddlewareInterface;

/**
 * Class InstallMiddleware
 * @package app\http\middleware
 */
class InstallMiddleware implements MiddlewareInterface
{

    public function handle(Request $request, \Closure $next)
    {
        //检测是否已安装CRMEB系统
        if (file_exists(root_path() . "public/install/") && !file_exists(root_path() . "public/install/install.lock")) {
            return redirect('/install/index');
        }

        return $next($request);
    }
}
