# 租户端API重构测试文档

## 重构概述

根据业务需求，对租户端API进行了重构，确保租户只能管理自己的信息，不能管理其他租户。

## 重构前后对比

### 重构前（不合适的API）
- `GET /api/tenant/list` - 租户列表（租户不应该看到其他租户）
- `GET /api/tenant/info/:id` - 租户详情（可以查看任意租户信息）
- `POST /api/tenant/save` - 创建租户（租户不应该创建其他租户）
- `PUT /api/tenant/update/:id` - 更新租户（可以更新任意租户）
- `DELETE /api/tenant/delete/:id` - 删除租户（可以删除任意租户）
- `PUT /api/tenant/status/:id` - 更新租户状态（可以修改任意租户状态）
- `PUT /api/tenant/batch/status` - 批量更新状态（可以批量修改任意租户状态）
- `GET /api/tenant/statistics` - 获取统计信息（可以看到所有租户统计）
- `GET /api/tenant/expiring` - 获取即将过期的租户（可以看到其他租户信息）

### 重构后（合适的API）
- `GET /api/tenant/info` - 获取当前租户信息（只能查看自己的信息）
- `PUT /api/tenant/update` - 更新当前租户信息（只能更新自己的信息）
- `PUT /api/tenant/change_password` - 修改密码（只能修改自己的密码）
- `GET /api/tenant/status` - 获取当前租户状态（只能查看自己的状态）

## 安全特性

### 1. 基于Token的身份验证
- 所有API都需要通过 `TenantAuthTokenMiddleware` 验证
- 从token中提取租户ID，确保只能操作自己的数据
- 使用 `$request->tenantId()` 获取当前租户ID
- 使用 `$request->tenantInfo()` 获取当前租户完整信息

### 2. 数据隔离
- 租户不能指定ID参数，所有操作都基于token中的租户ID
- 移除了所有可以操作其他租户的接口
- 租户只能修改非敏感字段（如联系信息、域名、logo等）
- 敏感字段（如appid、tenant_code、status、max_users等）只能由管理员修改

### 3. 权限控制
- 租户不能创建、删除其他租户
- 租户不能修改其他租户的状态
- 租户不能查看其他租户的统计信息
- 租户不能重置其他租户的密码

## API测试用例

### 1. 获取当前租户信息
```bash
curl -H "Authorization: Bearer <tenant_token>" \
     http://localhost:20108/api/tenant/info
```

### 2. 更新当前租户信息
```bash
curl -X PUT \
     -H "Authorization: Bearer <tenant_token>" \
     -H "Content-Type: application/json" \
     -d '{
       "tenant_name": "新租户名称",
       "contact_name": "新联系人",
       "contact_phone": "13800138000",
       "contact_email": "new@example.com",
       "domain": "new.example.com",
       "logo": "new_logo.png"
     }' \
     http://localhost:20108/api/tenant/update
```

### 3. 修改密码
```bash
curl -X PUT \
     -H "Authorization: Bearer <tenant_token>" \
     -H "Content-Type: application/json" \
     -d '{
       "old_password": "old_password",
       "new_password": "new_password",
       "confirm_password": "new_password"
     }' \
     http://localhost:20108/api/tenant/change_password
```

### 4. 获取当前租户状态
```bash
curl -H "Authorization: Bearer <tenant_token>" \
     http://localhost:20108/api/tenant/status
```

## 预期响应

### 成功响应示例
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "tenant_name": "测试租户",
    "tenant_code": "tenant_20241201_1234",
    "appid": "app_20241201_ABC123",
    "status": 1,
    "status_text": "已激活",
    "is_expired": false,
    "remaining_days": 30,
    "expire_at": "2025-01-01 00:00:00",
    "contact_name": "张三",
    "contact_phone": "13800138000",
    "contact_email": "test@example.com"
  }
}
```

### 错误响应示例
```json
{
  "code": 400,
  "msg": "未登录或登录已过期",
  "data": null
}
```

## 注意事项

1. **Token验证**: 所有API都需要有效的租户token
2. **数据隔离**: 租户只能操作自己的数据，不能访问其他租户信息
3. **字段限制**: 租户只能修改非敏感字段，敏感字段需要管理员权限
4. **安全性**: 密码修改需要验证原密码，确保安全性
5. **状态检查**: 中间件会检查租户状态和过期时间

## 管理员API保持不变

管理员端的租户管理API (`/api/admin/tenant/...`) 保持不变，仍然具有完整的租户管理权限：
- 可以查看所有租户列表
- 可以创建、更新、删除任意租户
- 可以修改租户状态和敏感信息
- 可以重置租户密码
- 可以查看租户统计信息

这样的设计确保了：
- 租户只能管理自己的信息（自助管理）
- 管理员可以管理所有租户（系统管理）
- 权限边界清晰，安全性得到保障
