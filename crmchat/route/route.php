<?php

use app\http\middleware\InstallMiddleware;
use think\facade\Route;

Route::get('install/index', 'InstallController/index');//安装程序
Route::post('install/index', 'InstallController/index');//安装程序
Route::get('upgrade/index', 'UpgradeController/index');
Route::get('upgrade/upgrade', 'UpgradeController/upgrade');

// 引入其他路由文件
require __DIR__ . '/admin.php';
require __DIR__ . '/kefu.php';
require __DIR__ . '/mobile.php';
require __DIR__ . '/tenant.php';

Route::group('/', function () {
    Route::miss(function () {
      $name=  app()->request->pathinfo();
            if(str_starts_with($name,"tenant")){
                return view(app()->getRootPath() . 'public' . DS . 'tenant' . DS . 'index.html');
            }else{
                return view(app()->getRootPath() . 'public' . DS . 'admin' . DS . 'index.html');
            }
    });
})->middleware(InstallMiddleware::class);
