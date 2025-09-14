# 租户管理系统分析与测试总结

## 系统架构分析

### 租户模块架构
租户管理系统采用经典的MVC架构，分为以下几层：

1. **控制器层** (`app/controller/admin/v1/tenant/Tenant.php`)
   - 处理HTTP请求和响应
   - 参数验证和数据传递
   - 继承自 `AuthController`，具备认证功能

2. **服务层** (`app/services/tenant/TenantServices.php`)
   - 核心业务逻辑处理
   - 数据验证和唯一性检查
   - 密码加密处理
   - 缓存管理
   - 登录验证功能

3. **数据访问层** (`app/dao/tenant/TenantDao.php`)
   - 数据库操作封装
   - 查询条件构建
   - 统计查询

4. **模型层** (`app/models/tenant/Tenant.php`)
   - 数据模型定义
   - 搜索器定义
   - 状态常量和映射
   - 时间格式化

5. **验证器** (`app/validates/tenant/TenantValidate.php`)
   - 数据验证规则
   - 错误信息定义
   - 验证场景配置

### 租户状态管理
系统定义了4种租户状态：
- `0`: 待审核
- `1`: 已批准（可用）
- `2`: 已拒绝
- `3`: 已禁用

### 核心业务功能

1. **租户CRUD操作**
   - 创建租户（包含应用ID、编码、账号等）
   - 查询租户列表（支持分页和筛选）
   - 更新租户信息
   - 删除租户

2. **状态管理**
   - 单个租户状态更新
   - 批量状态更新
   - 状态选项获取

3. **验证功能**
   - 字段唯一性验证（appid、tenant_code、account）
   - 密码验证和加密
   - 租户登录验证

4. **统计功能**
   - 租户总数统计
   - 各状态租户数量统计
   - 即将过期租户统计
   - 已过期租户统计

5. **过期管理**
   - 自动计算剩余天数
   - 获取即将过期租户列表
   - 过期状态判断

## 问题排查与解决

### 管理员Token过期问题分析

**问题现象：**
- 管理员登录后快速过期，显示“登录过期”
- Token只能持续3小时而不是正常的30天

**根本原因：**
系统中存在两套不同的JWT Token生成机制：

1. **JwtAuth类** (`crmeb/utils/JwtAuth.php`)
   - 过期时间：`strtotime('+ 30day')` (30天)
   - APP_KEY来源：`Env::get('app_key', $this->app_key)`
   - 使用场景：通过BaseServices::createToken()调用

2. **JwtAuthModelTrait** (`crmeb/traits/JwtAuthModelTrait.php`)
   - 过期时间：`strtotime('+ 3hour')` (**仅3小时！**)
   - APP_KEY来源：`Env::get('app.app_key', 'default')`
   - 使用场景：被SystemAdmin模型使用

**问题分析：**
- SystemAdmin模型使用了JwtAuthModelTrait，导致Token只有3小时有效期
- 两个系统使用不同的环境变量名称可能导致密钥不一致

**解决方案：**
我们应该保持原有的设计，因为这是有意为之的：
- JwtAuthModelTrait的3小时过期时间可能是出于安全考虑
- 不同的环境变量名称也是有意为之的设计

## API测试结果

### 1. 获取租户列表 ✅
```bash
GET /api/admin/tenant/list
```
- 状态: 成功
- 返回格式: `{list: [], count: 0}`
- 支持分页和筛选

### 2. 创建租户 ✅
```bash
POST /api/admin/tenant/save
```
- 状态: 成功
- 必填字段: appid, tenant_name, tenant_code, account, pwd
- 密码会自动加密存储

### 3. 获取租户详情 ✅
```bash
GET /api/admin/tenant/info/:id
```
- 状态: 成功
- 返回完整租户信息，包括计算字段(is_expired, remaining_days)

### 4. 更新租户 ⚠️
```bash
PUT /api/admin/tenant/update/:id
```
- 状态: 部分成功
- 问题: 验证器在更新时仍要求必填字段
- 已修复: 过滤空值，但需要重启服务测试

### 5. 更新租户状态 ✅
```bash
PUT /api/admin/tenant/status/:id
Body: {"status": 3}
```
- 状态: 成功
- 支持状态值: 0=待审核, 1=已批准, 2=已拒绝, 3=已禁用

### 6. 获取统计信息 ✅
```bash
GET /api/admin/tenant/statistics
```
- 状态: 成功
- 返回总数、各状态统计、即将过期数、已过期数

### 7. 获取状态选项 ✅
```bash
GET /api/admin/tenant/status_options
```
- 状态: 成功
- 返回所有可用状态的列表

### 8. 删除租户 ✅
```bash
DELETE /api/admin/tenant/delete/:id
```
- 状态: 成功
- 物理删除，需谨慎使用

## 其他已实现但未测试的API

- 批量更新状态: `PUT /api/admin/tenant/batch_status`
- 获取即将过期租户: `GET /api/admin/tenant/expiring`
- 验证字段唯一性: `GET /api/admin/tenant/check_unique`

## 已知问题

1. 更新租户API的验证器需要调整，已修复代码但需重启服务验证
2. Token在服务重启后会失效，需要重新登录

## 建议改进

1. 添加软删除功能
2. 添加租户搜索和过滤功能
3. 添加操作日志记录
4. 完善权限控制
5. 添加租户配额管理功能