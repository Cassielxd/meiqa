# CRM-CHAT API Documentation

## API Overview

CRM-CHAT 提供三套完整的 RESTful API 接口，支持后台管理、客服工作台和移动端应用的全功能访问。所有 API 采用 JSON 格式数据交换，使用 JWT Token 进行身份认证。

---

## 🔐 Authentication System

### Token Types
- **Admin Token**: 后台管理员访问令牌
- **Kefu Token**: 客服端访问令牌  
- **Mobile Token**: 移动端用户访问令牌

### Authentication Headers
```http
Authorization: Bearer <jwt_token>
Form-type: admin|kefu|h5|wechat|routine|app|pc
Content-Type: application/json
```

### Token Refresh
所有 token 支持自动刷新机制，在过期前会自动获取新的访问令牌。

---

## 🏢 Admin API (`/admin/*`)

### Authentication Endpoints

#### Admin Login
```http
POST /admin/login
```

**Request Body:**
```json
{
  "account": "admin",
  "pwd": "password",
  "imgcode": "captcha_code"
}
```

**Response:**
```json
{
  "status": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGci...",
    "expires_time": 86400,
    "admin": {
      "admin_id": 1,
      "account": "admin",
      "real_name": "管理员",
      "roles": ["super_admin"]
    }
  }
}
```

#### Admin Logout
```http
POST /admin/logout
Authorization: Bearer <token>
```

### Chat Management API

#### Service Management

##### Get Service List
```http
GET /admin/chat/service
Authorization: Bearer <token>
```

**Query Parameters:**
- `page`: 页码 (default: 1)
- `limit`: 每页数量 (default: 20)
- `keyword`: 搜索关键词
- `status`: 状态筛选 (0:禁用, 1:启用)

**Response:**
```json
{
  "status": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "nickname": "客服小王",
        "avatar": "/uploads/avatar/1.jpg",
        "status": 1,
        "online": 1,
        "customer_count": 15,
        "create_time": "2023-01-01 10:00:00"
      }
    ],
    "count": 50,
    "page": 1,
    "limit": 20
  }
}
```

##### Create Service
```http
POST /admin/chat/service
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "account": "kefu001",
  "pwd": "password123",
  "nickname": "客服小李",
  "avatar": "/uploads/avatar/2.jpg",
  "phone": "13800138000",
  "status": 1
}
```

##### Update Service
```http
PUT /admin/chat/service/{id}
Authorization: Bearer <token>
```

##### Delete Service
```http
DELETE /admin/chat/service/{id}
Authorization: Bearer <token>
```

#### Auto Reply Management

##### Get Auto Reply List
```http
GET /admin/chat/auto_reply
Authorization: Bearer <token>
```

##### Create Auto Reply Rule
```http
POST /admin/chat/auto_reply
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "keyword": "你好",
  "reply_type": 1,
  "reply_content": "欢迎咨询，请问有什么可以帮助您的？",
  "status": 1,
  "sort": 10
}
```

#### Dialogue Records

##### Get Dialogue Records
```http
GET /admin/chat/service_dialogue_record
Authorization: Bearer <token>
```

**Query Parameters:**
- `service_id`: 客服ID
- `user_id`: 用户ID
- `date`: 日期范围
- `keyword`: 消息内容搜索

### User Management API

#### Get User List
```http
GET /admin/user/user
Authorization: Bearer <token>
```

#### Get User Labels
```http
GET /admin/user/label
Authorization: Bearer <token>
```

#### Update User Label
```http
PUT /admin/user/user/{id}/label
Authorization: Bearer <token>
```

### System Management API

#### System Configuration
```http
GET /admin/system/config/{key}
PUT /admin/system/config/{key}
Authorization: Bearer <token>
```

#### Menu Management
```http
GET /admin/system/menus
POST /admin/system/menus
PUT /admin/system/menus/{id}
DELETE /admin/system/menus/{id}
Authorization: Bearer <token>
```

#### Role & Permission Management
```http
GET /admin/system/role
POST /admin/system/role
PUT /admin/system/role/{id}
DELETE /admin/system/role/{id}
Authorization: Bearer <token>
```

---

## 👨‍💼 Kefu API (`/kefu/*`)

### Authentication

#### Kefu Login
```http
POST /kefu/login
```

**Request Body:**
```json
{
  "account": "kefu001",
  "pwd": "password"
}
```

### Service Operations

#### Get Service Statistics
```http
GET /kefu/statistics
Authorization: Bearer <kefu_token>
```

**Response:**
```json
{
  "status": 200,
  "data": {
    "today_msg_count": 128,
    "today_user_count": 45,
    "online_user_count": 12,
    "avg_response_time": 30,
    "satisfaction_rate": 95.2
  }
}
```

#### Get Customer List
```http
GET /kefu/user
Authorization: Bearer <kefu_token>
```

#### Send Message
```http
POST /kefu/service/reply
Authorization: Bearer <kefu_token>
```

**Request Body:**
```json
{
  "to_user_id": 123,
  "message_type": 1,
  "message": "您好，请问有什么可以帮助您的？",
  "attachment": null
}
```

#### Transfer Service
```http
POST /kefu/service/transfer
Authorization: Bearer <kefu_token>
```

**Request Body:**
```json
{
  "user_id": 123,
  "to_service_id": 5,
  "remark": "用户需要技术支持"
}
```

---

## 📱 Mobile API (`/mobile/*`)

### User Authentication

#### User Registration (Optional)
```http
POST /mobile/register
```

#### Get Service List
```http
GET /mobile/service
Form-type: h5|wechat|routine|app
```

**Response:**
```json
{
  "status": 200,
  "data": {
    "service_list": [
      {
        "id": 1,
        "nickname": "客服小王",
        "avatar": "/uploads/avatar/1.jpg",
        "online": 1,
        "avg_response_time": 25
      }
    ],
    "auto_service": {
      "id": 2,
      "nickname": "智能客服",
      "avatar": "/static/images/robot.png"
    }
  }
}
```

### Chat Operations

#### Connect to Service
```http
POST /mobile/service/connect
Form-type: h5|wechat|routine|app
```

**Request Body:**
```json
{
  "service_id": 1,
  "user_info": {
    "nickname": "张三",
    "avatar": "/uploads/user/avatar.jpg",
    "phone": "13800138000"
  }
}
```

#### Send Message
```http
POST /mobile/service/send
Form-type: h5|wechat|routine|app
```

**Request Body:**
```json
{
  "service_id": 1,
  "message_type": 1,
  "message": "你好，我想咨询一下产品信息",
  "attachment": null
}
```

#### Upload File
```http
POST /mobile/service/upload
Form-type: h5|wechat|routine|app
Content-Type: multipart/form-data
```

**Form Data:**
- `file`: 文件内容
- `type`: 文件类型 (image|file|video)

#### Get Chat History
```http
GET /mobile/service/history
Form-type: h5|wechat|routine|app
```

**Query Parameters:**
- `service_id`: 客服ID
- `page`: 页码
- `limit`: 每页数量

### Feedback System

#### Submit Feedback
```http
POST /mobile/feedback
Form-type: h5|wechat|routine|app
```

**Request Body:**
```json
{
  "service_id": 1,
  "content": "服务很好，解决了我的问题",
  "score": 5,
  "tags": ["专业", "及时", "友好"]
}
```

---

## 🔗 WebSocket API

### Connection
```javascript
// WebSocket 连接地址
ws://your-domain.com:9502

// 连接参数
{
  "type": "login",
  "token": "jwt_token_here",
  "user_type": "admin|kefu|mobile"
}
```

### Message Format
```json
{
  "type": "message|system|transfer|typing",
  "from_id": 123,
  "to_id": 456,
  "user_type": "admin|kefu|mobile",
  "message_type": 1,
  "message": "消息内容",
  "time": 1640995200,
  "msg_id": "unique_message_id"
}
```

### Event Types
- `message`: 普通消息
- `system`: 系统通知
- `transfer`: 转接通知
- `typing`: 正在输入
- `read`: 消息已读
- `online`: 上线通知
- `offline`: 离线通知

---

## 📊 Response Format

### Success Response
```json
{
  "status": 200,
  "msg": "操作成功",
  "data": {
    // 返回数据
  }
}
```

### Error Response
```json
{
  "status": 400,
  "msg": "错误信息",
  "data": null
}
```

### Pagination Response
```json
{
  "status": 200,
  "data": {
    "list": [],
    "count": 100,
    "page": 1,
    "limit": 20,
    "totalPages": 5
  }
}
```

---

## ⚠️ Error Codes

| Code | Description |
|------|-------------|
| 200  | 操作成功 |
| 400  | 请求参数错误 |
| 401  | 未授权访问 |
| 403  | 权限不足 |
| 404  | 资源不存在 |
| 422  | 数据验证失败 |
| 429  | 请求频率超限 |
| 500  | 服务器内部错误 |

---

## 🔧 Rate Limiting

| Endpoint | Limit | Window |
|----------|-------|---------|
| `/admin/*` | 1000/hour | 管理后台 |
| `/kefu/*` | 2000/hour | 客服工作台 |
| `/mobile/*` | 500/hour | 移动端API |

---

## 📝 Request/Response Examples

### File Upload Example
```http
POST /admin/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

--boundary
Content-Disposition: form-data; name="file"; filename="image.jpg"
Content-Type: image/jpeg

[binary data]
```

**Response:**
```json
{
  "status": 200,
  "msg": "上传成功",
  "data": {
    "url": "/uploads/images/2023/01/01/image_123456.jpg",
    "size": 245760,
    "type": "image/jpeg"
  }
}
```

---

*Generated with CRM-CHAT API Documentation System*