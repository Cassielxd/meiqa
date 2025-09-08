# CRM-CHAT Project Structure Documentation

## Project Overview

**CRM-CHAT** is a customer service chat system built with ThinkPHP 6.0 and Swoole 4, providing real-time communication capabilities for businesses. The system features a Vue.js frontend and follows a service-oriented architecture.

---

## 🏗️ System Architecture

### Core Technologies
- **Backend Framework**: ThinkPHP 6.0 + Swoole 4
- **Frontend Framework**: Vue CLI
- **Database**: MySQL (with ORM)
- **Real-time Communication**: WebSocket (via Swoole)
- **Dependency Management**: Composer
- **Authentication**: JWT Token-based

### Key Requirements
- **PHP Version**: 7.1 ~ 7.4
- **Extensions**: JSON, cURL, BCMath, Mbstring
- **Environment**: Unix/Linux (Windows not supported)

---

## 📁 Directory Structure

```
crmchat/
├── app/                          # 应用核心目录
│   ├── controller/              # 控制器层
│   │   ├── admin/              # 后台管理控制器
│   │   ├── kefu/               # 客服端控制器
│   │   └── mobile/             # 移动端控制器
│   ├── dao/                    # 数据访问层
│   │   ├── chat/              # 聊天相关 DAO
│   │   ├── other/             # 其他功能 DAO
│   │   └── system/            # 系统功能 DAO
│   ├── models/                 # 数据模型层
│   ├── services/              # 业务逻辑服务层
│   ├── validates/             # 数据验证层
│   ├── http/middleware/       # 中间件
│   ├── jobs/                  # 队列任务
│   └── listeners/             # 事件监听器
├── config/                     # 配置文件
├── crmeb/                      # 核心框架扩展
│   ├── basic/                 # 基础类库
│   ├── services/              # 核心服务
│   ├── utils/                 # 工具类
│   └── traits/                # 特性类
├── public/                     # 公共资源目录
│   └── admin/                 # 后台前端资源
├── vendor/                     # 第三方依赖
└── database/                   # 数据库相关
```

---

## 🎯 Core Components

### 1. Application Layer (`app/`)

#### Controllers (`app/controller/`)
- **Admin Controllers** (`admin/`): 后台管理功能
  - `AuthController.php` - 认证基类
  - `chat/` - 聊天管理相关
  - `system/` - 系统管理相关
  - `user/` - 用户管理相关

- **Kefu Controllers** (`kefu/`): 客服端功能
  - `Service.php` - 客服服务管理
  - `Statistics.php` - 统计功能

- **Mobile Controllers** (`mobile/`): 移动端API
  - `Service.php` - 客户端聊天服务

#### Data Access Layer (`app/dao/`)
遵循数据访问对象模式，将数据库操作封装：
- `chat/` - 聊天系统数据访问
- `system/` - 系统管理数据访问
- `other/` - 其他功能数据访问

#### Services Layer (`app/services/`)
业务逻辑处理层，负责复杂业务逻辑：
- 遵循单一职责原则
- 处理数据组合和业务规则
- 与 DAO 层交互获取数据

### 2. CRMEB Framework (`crmeb/`)

#### Basic Components (`crmeb/basic/`)
- `BaseDao.php` - DAO基类
- `BaseServices.php` - 服务基类
- `BaseModel.php` - 模型基类
- `BaseManager.php` - 管理器基类

#### Core Services (`crmeb/services/`)
- `CacheService.php` - 缓存服务
- `FormBuilder.php` - 表单构建器
- `UploadService.php` - 文件上传服务
- `SystemConfigService.php` - 系统配置服务

#### Utilities (`crmeb/utils/`)
- `JwtAuth.php` - JWT认证工具
- `QRcode.php` - 二维码生成
- `Queue.php` - 队列管理
- `Arr.php` - 数组处理工具

### 3. Configuration (`config/`)

重要配置文件：
- `app.php` - 应用基础配置
- `database.php` - 数据库配置
- `swoole.php` - Swoole服务配置
- `queue.php` - 队列配置
- `cache.php` - 缓存配置

---

## 🔐 Authentication System

### JWT Token Authentication
- **Admin Token**: 后台管理员认证
- **Kefu Token**: 客服端认证  
- **Mobile Token**: 移动端用户认证

### Middleware Chain
1. `AdminAuthTokenMiddleware` - 管理员token验证
2. `AdminCkeckRoleMiddleware` - 权限检查
3. `AdminLogMiddleware` - 操作日志记录

---

## 💬 Chat System Architecture

### Real-time Communication
- **WebSocket Server**: 基于 Swoole WebSocket
- **Message Queue**: 异步消息处理
- **Connection Pool**: 长连接管理

### Chat Components
- **Service Management**: 客服分配与管理
- **Dialogue Records**: 会话记录管理
- **Auto Reply**: 自动回复系统
- **Feedback System**: 用户反馈处理

---

## 📋 Development Standards

### Naming Conventions
- **Classes**: PascalCase (e.g., `UserService`)
- **Methods**: camelCase (e.g., `getUserData`)
- **Properties**: camelCase (e.g., `userName`)
- **Files**: PascalCase for classes, lowercase+underscore for others
- **Database**: lowercase+underscore (e.g., `chat_service`)

### Code Structure
- **Controllers**: 处理HTTP请求，调用Services
- **Services**: 业务逻辑处理，调用DAO
- **DAO**: 数据库操作，继承BaseDao
- **Models**: 数据模型定义，使用ORM
- **Validates**: 数据验证规则

### Error Handling
```php
// 统一异常处理
throw new AuthException('错误信息', 400);
```

---

## 🚀 Deployment & Operations

### Access Points
- **Admin Panel**: `http://domain/admin`
- **Kefu Panel**: `http://domain/kefu`
- **Mobile API**: RESTful API endpoints

### Command Line Tools
```bash
php think swoole:server start    # 启动Swoole服务
php think queue:work             # 启动队列处理
php think install               # 系统安装
```

### File Upload Support
- **Local Storage**: 本地文件系统
- **Cloud Storage**: 阿里云OSS, 腾讯云COS, 七牛云

---

## 🔧 Third-party Integrations

### Payment Systems
- **Alipay**: Easy SDK integration
- **WeChat Pay**: Native API support

### Cloud Services  
- **File Storage**: OSS, COS, Qiniu
- **SMS Service**: Multiple provider support
- **Push Notifications**: UniPush integration

### Development Tools
- **Form Builder**: Backend form generation
- **QR Code**: Dynamic QR code generation
- **JWT**: Secure token authentication
- **Image Processing**: Intervention/Image

---

## 📊 Database Design

### Core Tables
- `chat_service` - 客服信息表
- `chat_service_dialogue_record` - 会话记录表
- `chat_service_feedback` - 用户反馈表
- `chat_auto_reply` - 自动回复规则表
- `system_admin` - 管理员表
- `system_role` - 角色权限表

---

## 🔄 Queue System

### Job Processing
- **ServiceTransfer**: 客服转接处理
- **AutoBadge**: 自动徽章分配
- **UniPush**: 消息推送处理

### Queue Configuration
支持多种队列驱动：
- Redis
- Database  
- Sync (同步处理)

---

*Generated with CRM-CHAT Project Documentation System*