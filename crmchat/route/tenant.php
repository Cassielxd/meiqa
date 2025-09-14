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
                Route::get('list', 'Service/index')->option(['real_name' => '获取客服列表']);
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
         * 租户管理相关路由（支持admin向下兼容访问）
         */
        Route::group(function () {
            // 租户列表
            Route::get('list', 'Tenant/index')->option(['real_name' => '租户列表']);
            // 租户详情
            Route::get('info/:id', 'Tenant/read')->option(['real_name' => '租户详情']);
            // 创建租户
            Route::post('save', 'Tenant/save')->option(['real_name' => '创建租户']);
            // 更新租户
            Route::put('update/:id', 'Tenant/update')->option(['real_name' => '更新租户']);
            // 删除租户
            Route::delete('delete/:id', 'Tenant/delete')->option(['real_name' => '删除租户']);
            // 更新租户状态
            Route::put('status/:id', 'Tenant/updateStatus')->option(['real_name' => '更新租户状态']);
            // 批量更新状态
            Route::put('batch/status', 'Tenant/batchUpdateStatus')->option(['real_name' => '批量更新状态']);
            // 获取统计信息
            Route::get('statistics', 'Tenant/statistics')->option(['real_name' => '获取统计信息']);
            // 获取即将过期的租户
            Route::get('expiring', 'Tenant/expiring')->option(['real_name' => '获取即将过期的租户']);
            // 获取状态选项
            Route::get('status/options', 'Tenant/statusOptions')->option(['real_name' => '获取状态选项']);
            // 验证唯一性
            Route::get('check/unique', 'Tenant/checkUnique')->option(['real_name' => '验证唯一性']);
        })->middleware([
            TenantAuthTokenMiddleware::class,
        ]);
        
    })->prefix('tenant.');
    
})->middleware([
    AllowOriginMiddleware::class,
    InstallMiddleware::class
]);