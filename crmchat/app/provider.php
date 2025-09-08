<?php

use app\ExceptionHandle;
use app\Request;
use app\Route;

// 容器Provider定义文件
return [
    'think\Request'          => Request::class,
    'think\Route'            => Route::class,
    'think\exception\Handle' => ExceptionHandle::class,
];
