<?php

use app\http\middleware\AllowOriginMiddleware;
use app\http\middleware\InstallMiddleware;
use app\http\middleware\tenant\TenantAuthTokenMiddleware;
use app\http\middleware\tenant\TenantCheckRoleMiddleware;
use think\facade\Route;

Route::group('api', function () {
    
    Route::group('tenant', function () {
        
        /**
         * 无需授权的接口
         */
        Route::group(function () {
            // 租户登录
            Route::post('login', 'Login/login')->name('TenantLogin')->option(['real_name' => '租户登录']);
            
            // 租户注册相关接口
            Route::post('send_captcha', 'Login/sendCaptcha')->name('TenantSendCaptcha')->option(['real_name' => '发送注册验证码']);
            Route::post('register', 'Login/register')->name('TenantRegister')->option(['real_name' => '租户注册']);
        });

        /**
         * 需要授权的接口
         */
        Route::group(function () {
            // 获取租户信息
            Route::get('info', 'Login/info')->option(['real_name' => '获取租户信息']);
            
            // 退出登录
            Route::post('logout', 'Login/logout')->option(['real_name' => '退出登录']);
            
            // 修改密码
            Route::post('change_password', 'Login/changePassword')->option(['real_name' => '修改密码']);
            
            /**
             * 用户管理相关路由
             */
            Route::group('user', function () {
                // 用户列表
                Route::get('list', 'User/index')->option(['real_name' => '获取用户列表']);
                // 用户详情
                Route::get('info/:id', 'User/read')->option(['real_name' => '获取用户详情']);
                // 创建用户
                Route::post('save', 'User/save')->option(['real_name' => '创建用户']);
                // 更新用户
                Route::put('update/:id', 'User/update')->option(['real_name' => '更新用户']);
                // 删除用户
                Route::delete('delete/:id', 'User/delete')->option(['real_name' => '删除用户']);
                // 更新用户状态
                Route::put('status/:id', 'User/updateStatus')->option(['real_name' => '更新用户状态']);
            });
            
            /**
             * 客服管理相关路由
             */
            Route::group('service', function () {
                // 客服列表
                Route::get('list', 'Service/list')->option(['real_name' => '获取客服列表']);
                // 客服详情
                Route::get('info/:id', 'Service/read')->option(['real_name' => '获取客服详情']);
                // 创建客服
                Route::post('save', 'Service/save')->option(['real_name' => '创建客服']);
                // 更新客服
                Route::put('update/:id', 'Service/update')->option(['real_name' => '更新客服']);
                // 删除客服
                Route::delete('delete/:id', 'Service/delete')->option(['real_name' => '删除客服']);
                // 更新客服状态
                Route::put('status/:id', 'Service/updateStatus')->option(['real_name' => '更新客服状态']);
                // 获取客服分组
                Route::get('groups', 'Service/groups')->option(['real_name' => '获取客服分组列表']);
            });
            
            /**
             * 统计相关路由
             */
            Route::group('statistics', function () {
                // 概览统计
                Route::get('overview', 'Statistics/overview')->option(['real_name' => '获取概览统计']);
                // 用户统计
                Route::get('users', 'Statistics/users')->option(['real_name' => '获取用户统计']);
                // 客服统计
                Route::get('services', 'Statistics/services')->option(['real_name' => '获取客服统计']);
            });
            
        })->middleware([
            TenantAuthTokenMiddleware::class,
            TenantCheckRoleMiddleware::class,
        ]);
        
        /**
         * 租户自助管理相关路由
         * 只允许租户管理自己的信息
         */
        Route::group(function () {
            // 获取当前租户信息
            Route::get('info', 'Tenant/info')->option(['real_name' => '获取当前租户信息']);
            // 更新当前租户信息
            Route::put('update', 'Tenant/update')->option(['real_name' => '更新当前租户信息']);
            // 修改密码
            Route::put('change_password', 'Tenant/changePassword')->option(['real_name' => '修改密码']);
            // 获取当前租户状态
            Route::get('status', 'Tenant/status')->option(['real_name' => '获取当前租户状态']);
        })->middleware([
            TenantAuthTokenMiddleware::class,
        ]);
        
    })->prefix('tenant.');
    
})->middleware([
    AllowOriginMiddleware::class,
    InstallMiddleware::class
]);