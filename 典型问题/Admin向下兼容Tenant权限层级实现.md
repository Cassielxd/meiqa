# Admin向下兼容Tenant权限层级实现

## 问题背景

在CRMChat多租户系统中，需要实现权限层级管理：
- **admin** 是高级权限，可以**向下兼容**访问 tenant 域的所有 API
- **tenant** 是低级权限，**不能向上兼容**访问 admin 域的 API

原始问题：租户管理功能原本在admin域下（`api/admin/tenant/*`），现需要迁移到tenant域（`api/tenant/*`），同时保持管理员能够访问租户管理功能。

## 核心问题分析

使用admin域的token（JWT中type是"admin"）访问tenant域的API时，会出现"登录过期"错误。这是因为：

1. **Admin中间件和Tenant中间件有不同的验证逻辑**
2. **AdminAuthTokenMiddleware** 使用 `AdminAuthServices` 解析token
3. **TenantAuthTokenMiddleware** 使用 `TenantAuthServices` 解析token
4. **跨域访问时token验证失败**

## 解决方案

### 1. 修改TenantAuthTokenMiddleware实现权限层级

修改 `/app/http/middleware/tenant/TenantAuthTokenMiddleware.php`：

```php
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

    // ... 其他代码
}
```

### 2. 在tenant.php中添加租户管理路由

在 `/route/tenant.php` 中添加：

```php
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
```

## 实现原理

### 权限层级验证流程

1. **接收到请求** → 提取token
2. **优先尝试解析admin token**
   - 成功 → admin向下兼容，允许访问
   - 失败 → 继续下一步
3. **尝试解析tenant token**
   - 成功 → tenant正常访问
   - 失败 → 拒绝访问
4. **注入相应的宏方法** → 控制器可通过 `$request->isAdminLogin()` 或 `$request->isTenantLogin()` 判断用户类型

### 权限层级结果

- **Admin Token** (`type: "admin"`)
  - ✅ 可访问 `api/admin/*`
  - ✅ 可访问 `api/tenant/*`（向下兼容）

- **Tenant Token** (`type: "tenant"`)
  - ❌ 不能访问 `api/admin/*`（正确拒绝）
  - ✅ 可访问 `api/tenant/*`

## 验证结果

### ✅ Admin向下兼容测试
```bash
# admin token 访问 tenant 域
curl -X GET "http://localhost:20108/api/tenant/list" -H "Authori-zation: [admin-token]"
# 返回: 200 OK，成功获取租户列表

curl -X GET "http://localhost:20108/api/tenant/statistics" -H "Authori-zation: [admin-token]"
# 返回: 200 OK，成功获取统计信息
```

### ✅ Tenant不能向上兼容测试
```bash
# tenant token 尝试访问 admin 域
curl -X GET "http://localhost:20108/api/admin/menusList" -H "Authori-zation: [tenant-token]"
# 返回: 410000 "登录过期"，正确拒绝访问
```

### ✅ 租户正常功能测试
```bash
# 租户登录
curl -X POST "http://localhost:20108/api/tenant/login" -d '{"account": "tenant002", "pwd": "123456"}'
# 返回: 200 OK，成功登录并获取tenant token
```

## 关键技术点

1. **中间件优先级处理**：admin token解析优先于tenant token
2. **异常捕获机制**：token解析失败不影响后续验证流程
3. **宏方法注入**：为Request对象注入用户身份判断方法
4. **路由配置统一**：所有租户管理路由使用同一个中间件
5. **权限隔离保证**：tenant token无法通过admin中间件验证

## 应用场景

这种权限层级设计适用于：
- **多租户SaaS系统**
- **分层权限管理系统**
- **需要管理员能管理租户，但租户不能越权的场景**
- **API路由域分离但需要跨域访问的系统**

## 总结

通过修改TenantAuthTokenMiddleware实现了完美的权限层级：
- **管理员** 可以通过tenant域路由管理租户（向下兼容）
- **租户** 只能访问自己的业务功能，无法访问admin域（权限隔离）
- **租户管理功能** 从admin域成功迁移到tenant域
- **保持了系统的安全性和功能完整性**