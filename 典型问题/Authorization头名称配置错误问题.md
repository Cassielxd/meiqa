# Authorization HTTP头名称配置错误问题

## 问题描述

CRMChat项目中存在HTTP认证头名称配置错误，导致API调用时需要使用非标准的头名称。

## 问题详情

### 错误配置
- **配置文件**: `/config/cookie.php`
- **错误配置**: `'token_name' => 'Authori-zation'` (带连字符)
- **跨域头配置**: 同时包含 `'Authori-zation,Authorization'` 重复配置

### 问题表现
1. **API调用失败**: 使用标准 `Authorization: Bearer <token>` 头时返回"请登录"错误
2. **需要非标准头**: 必须使用 `Authori-zation: Bearer <token>` 才能正常工作
3. **不符合HTTP标准**: RFC 7235规范中认证头应为 `Authorization`

### 受影响的模块
- Admin API认证 (`AdminAuthTokenMiddleware`)
- Kefu API认证 (`KefuAuthTokenMiddleware`) 
- Mobile API认证 (`MobileAuthTokenMiddleware`)
- Tenant API认证 (`TenantAuthTokenMiddleware`)

## 解决方案

### 1. 修正配置文件
```php
// config/cookie.php
return [
    // ...
    'header' => [
        'Access-Control-Allow-Origin'       => '*',
        'Access-Control-Allow-Headers'      => 'Authorization, Content-Type, If-Match, If-Modified-Since, If-None-Match, If-Unmodified-Since, X-Requested-With, Form-type',
        // 移除重复的 Authori-zation
        // ...
    ],
    // 修正token名称
    'token_name' => 'Authorization', // 正确的标准HTTP头
];
```

### 2. 重启服务
修改配置后需要重启Swoole服务使配置生效：
```bash
php think swoole stop
php think swoole
```

### 3. 测试验证
```bash
# 使用标准Authorization头测试
curl -X GET "http://localhost:20108/api/admin/tenant/list" \
  -H "Authorization: Bearer <token>" \
  | jq
```

## 影响范围

### 修复前
- 前端和API调用需要使用非标准 `Authori-zation` 头
- 与标准HTTP客户端和工具不兼容
- 开发调试困难

### 修复后  
- 使用标准 `Authorization: Bearer <token>` 格式
- 兼容所有标准HTTP客户端
- 符合RESTful API最佳实践

## 预防措施

1. **代码审查**: 配置文件修改需要仔细审查拼写
2. **API测试**: 使用标准HTTP客户端工具测试
3. **文档更新**: 确保API文档使用正确的头名称
4. **自动化测试**: 添加API认证相关的自动化测试

## 历史记录

- **发现时间**: 2025-09-14
- **修复时间**: 2025-09-14  
- **影响版本**: CRMChat 1.2.0及之前版本
- **修复状态**: ✅ 已修复

## 相关文件

- `/config/cookie.php` - 主要配置文件
- `/app/http/middleware/admin/AdminAuthTokenMiddleware.php` - Admin认证中间件
- `/app/http/middleware/kefu/KefuAuthTokenMiddleware.php` - 客服认证中间件
- `/app/http/middleware/mobile/MobileAuthTokenMiddleware.php` - 移动端认证中间件
- `/app/http/middleware/tenant/TenantAuthTokenMiddleware.php` - 租户认证中间件

## 注意事项

⚠️ **重要**: 此修改会影响现有的前端应用和API客户端，需要同步更新所有使用认证的代码。

✅ **兼容性**: 修复后与标准HTTP认证规范完全兼容。