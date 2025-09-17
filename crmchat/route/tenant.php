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
            Route::get('captcha_pro', 'Login/captcha')->name('')->option(['real_name' => '获取验证码']);
            Route::get('login/info', 'Login/info')->option(['real_name' => '获取登录信息']);
            //获取AJ验证码
            Route::get('ajcaptcha', 'Login/ajcaptcha')->name('ajcaptcha')->option(['real_name' => '获取AJ验证码']);
            //验证码验证
            Route::post('ajcheck', 'Login/ajcheck')->name('ajcheck')->option(['real_name' => '验证码验证']);
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
            Route::get('logo', 'Index/logo')->option(['real_name' => '获取logo']);
            // 退出登录
            Route::post('logout', 'Login/logout')->option(['real_name' => '退出登录']);
            
            // 修改密码
            Route::post('change_password', 'Login/changePassword')->option(['real_name' => '修改密码']);

            Route::get('chart/sum', 'Index/sum')->option(['real_name' => '客户统计']);
            Route::get('chart', 'Index/index')->option(['real_name' => '客户首页统计']);
            Route::put('app/reset/:id', 'Application/reset')->option(['real_name' => '重置token']);

            Route::resource('app', 'Application')->option(['real_name' => [
                'index' => '获取应用列表接口',
                'create' => '获取应用创建接口',
                'save' => '保存应用接口',
                'edit' => '获取修改应用接口',
                'update' => '修改应用接口',
                'delete' => '删除应用接口'
            ]])->except(['read']);

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
        Route::group('chat', function () {
            //站点统计
            Route::get('statistics', 'SiteStatistics/index')->option(['real_name' => '站点统计']);
            //获取随机客服二维码
            Route::get('qrcode', 'Qrcode/index')->option(['real_name' => '获取随机客服二维码']);
            //获取随机客服二维码表单
            Route::get('qrcode/:id', 'Qrcode/create')->option(['real_name' => '获取随机客服二维码表单']);
            //保存随机客服二维码
            Route::post('qrcode/:id', 'Qrcode/save')->option(['real_name' => '保存随机客服二维码']);
            //删除随机客服二维码
            Route::delete('qrcode/:id', 'Qrcode/delete')->option(['real_name' => '删除随机客服二维码']);
            //客服列表
            Route::get('kefu', 'Service/index')->option(['real_name' => '客服列表']);
            //客服组列表
            Route::get('group', 'ServiceGroup/index')->option(['real_name' => '客服组列表']);
            //获取客服组表单
            Route::get('group/create/:id', 'ServiceGroup/create')->option(['real_name' => '获取客服组表单']);
            //保存客服组
            Route::post('group/:id', 'ServiceGroup/save')->option(['real_name' => '保存客服组']);
            //删除客服组
            Route::delete('group/:id', 'ServiceGroup/delete')->option(['real_name' => '删除客服组']);
            //自动回复列表
            Route::get('reply', 'AutoReply/index')->option(['real_name' => '自动回复列表']);
            //获取自动回复表单
            Route::get('reply/:id', 'AutoReply/create')->option(['real_name' => '获取自动回复表单']);
            //保存自动回复
            Route::post('reply/:id', 'AutoReply/save')->option(['real_name' => '保存自动回复']);
            //删除自动回复
            Route::delete('reply/:id', 'AutoReply/delete')->option(['real_name' => '删除自动回复']);
            //客服登录
            Route::get('kefu/login/:id', 'Service/keufLogin')->option(['real_name' => '客服登录']);
            //添加客服表单
            Route::get('kefu/add', 'Service/add')->option(['real_name' => '添加客服表单']);
            //添加客服
            Route::post('kefu', 'Service/save')->option(['real_name' => '添加客服']);
            //修改客服表单
            Route::get('kefu/:id/edit', 'Service/edit')->option(['real_name' => '修改客服表单']);
            //修改客服
            Route::put('kefu/:id', 'Service/update')->option(['real_name' => '修改客服']);
            //删除客服
            Route::delete('kefu/:id', 'Service/delete')->option(['real_name' => '删除客服']);
            //修改客服状态
            Route::put('kefu/set_status/:id/:status', 'Service/set_status')->option(['real_name' => '修改客服状态']);
            //聊天记录
            Route::get('kefu/record/:id', 'Service/chat_user')->option(['real_name' => '聊天记录']);
            //查看对话
            Route::get('kefu/chat_list', 'Service/chat_list')->option(['real_name' => '查看对话']);
            //查看所有聊天记录
            Route::get('record', 'ServiceDialogueRecord/index')->option(['real_name' => '查看所有聊天记录']);
            Route::get('record/list', 'ServiceDialogueRecord/record')->option(['real_name' => '查看所有聊天用户列表']);
            //获取所有客服
            Route::get('record_kefu', 'ServiceDialogueRecord/kefu')->option(['real_name' => '获取所有客服']);
            //客服话术资源路由
            Route::resource('speechcraft', 'ServiceSpeechcraft')->option(['real_name' => [
                'index' => '获取话术列表接口',
                'create' => '获取话术创建接口',
                'read' => '获取话术详情接口',
                'save' => '保存话术接口',
                'edit' => '获取修改话术接口',
                'update' => '修改话术接口',
                'delete' => '删除话术接口'
            ]]);
            //客服话术分类资源路由
            Route::resource('speechcraftcate', 'ServiceSpeechcraftCate')->option(['real_name' => [
                'index' => '获取话术分类列表接口',
                'create' => '获取话术分类创建接口',
                'read' => '获取话术分类详情接口',
                'save' => '保存话术分类接口',
                'edit' => '获取修改话术分类接口',
                'update' => '修改话术分类接口',
                'delete' => '删除话术分类接口'
            ]]);
            //用户反馈资源路由
            Route::resource('feedback', 'ServiceFeedback')->only(['index', 'delete', 'update', 'edit'])->option(['real_name' => [
                'index' => '获取用户反馈列表接口',
                'edit' => '获取修改用户反馈接口',
                'update' => '修改用户反馈接口',
                'delete' => '删除用户反馈接口'
            ]])->except(['save', 'create', 'read']);

        })->middleware([
            TenantAuthTokenMiddleware::class,
            TenantCheckRoleMiddleware::class,
        ])->prefix('tenant.chat.');
        Route::group('user', function () {

            Route::get('/index', 'user.User/index')->option(['real_name' => '用户列表']);
            Route::get('/user_label', 'user.User/getLavelAll')->option(['real_name' => '用户标签搜索列表']);
            Route::get('/edit/:id', 'user.User/edit')->option(['real_name' => '获取修改用户表单']);
            Route::put('/update/:id', 'user.User/update')->option(['real_name' => '修改用户']);
            Route::put('/batch/label', 'user.User/batchLabel')->option(['real_name' => '批量修改用户标签']);
            Route::put('/batch/group', 'user.User/batchGroup')->option(['real_name' => '批量修改用户分组']);
            Route::get('/label/all', 'user.User/getLabelAll')->option(['real_name' => '获取全部标签']);
            Route::get('/group/all', 'user.User/getGroupAll')->option(['real_name' => '获取全部分组']);
            Route::post('/label/move', 'user.Label/move')->option(['real_name' => '标签移动排序']);
            Route::post('/label/move_cate', 'user.LabelCate/move')->option(['real_name' => '标签分类移动排序']);

            Route::resource('label/cate', 'user.LabelCate')->option(['real_name' => [
                'index' => '获取标签分类列表接口',
                'create' => '获取标签分类创建接口',
                'save' => '保存标签分类接口',
                'edit' => '获取修改标签分类接口',
                'update' => '修改标签分类接口',
                'delete' => '删除标签分类接口'
            ]])->except(['read']);

            Route::resource('label', 'user.Label')->option(['real_name' => [
                'index' => '获取标签列表接口',
                'create' => '获取标签创建接口',
                'save' => '保存标签接口',
                'edit' => '获取修改标签接口',
                'update' => '修改标签接口',
                'delete' => '删除标签接口'
            ]])->except(['read']);

            Route::resource('group', 'user.Group')->option(['real_name' => [
                'index' => '获取分组列表接口',
                'create' => '获取分组创建接口',
                'save' => '保存分组接口',
                'edit' => '获取修分组签接口',
                'update' => '修改分组接口',
                'delete' => '删除分组接口'
            ]])->except(['read']);

        })->middleware([
            TenantAuthTokenMiddleware::class,
            TenantCheckRoleMiddleware::class,
        ]);
        /**
         * 附件相关路由
         */
        Route::group('file', function () {
            //图片附件列表
            Route::get('file', 'Attachment/index')->option(['real_name' => '图片附件列表']);
            //删除图片
            Route::post('file/delete', 'Attachment/delete')->option(['real_name' => '删除图片']);
            //移动图片分类表单
            Route::get('file/move', 'Attachment/move')->option(['real_name' => '移动图片分类表单']);
            //移动图片分类
            Route::put('file/do_move', 'Attachment/moveImageCate')->option(['real_name' => '移动图片分类']);
            //修改图片名称
            Route::put('file/update/:id', 'Attachment/update')->option(['real_name' => '修改图片名称']);
            //上传图片
            Route::post('upload/[:upload_type]', 'Attachment/upload')->option(['real_name' => '上传图片']);
            //附件分类管理资源路由
            Route::resource('category', 'AttachmentCategory')->option([
                'real_name' => [
                    'index' => '获取附件分类列表接口',
                    'create' => '获取附件分类创建接口',
                    'read' => '获取附件分类详情接口',
                    'save' => '保存附件分类接口',
                    'edit' => '获取修改附件分类接口',
                    'update' => '修改附件分类接口',
                    'delete' => '删除附件分类接口'
                ]
            ]);

        })->middleware([
            TenantAuthTokenMiddleware::class,
            TenantCheckRoleMiddleware::class,
        ])->prefix('tenant.file.');
        Route::group('setting', function () {

            Route::get('admin/logout', 'system.Admin/logout')->name('SystemAdminLogout')->option(['real_name' => '退出登陆']);

        })->middleware([
            TenantAuthTokenMiddleware::class,
            TenantCheckRoleMiddleware::class,
        ]);
        /**
         * 租户自助管理相关路由
         * 只允许租户管理自己的信息
         */
        /*Route::group(function () {
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
        ]);*/

    })->prefix('tenant.');
    
})->middleware([
    AllowOriginMiddleware::class,
    InstallMiddleware::class
]);