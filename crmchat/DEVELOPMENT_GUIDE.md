# CRM-CHAT Development Guide

## Development Environment Setup

### System Requirements
- **Operating System**: Linux/Unix (Windows not supported)
- **PHP**: 7.1 - 7.4
- **Database**: MySQL 5.7+ / MariaDB 10.2+
- **Web Server**: Nginx (recommended) / Apache
- **Memory**: Minimum 512MB, Recommended 2GB+

### Required PHP Extensions
```bash
# Essential Extensions
php-json      # JSON processing
php-curl      # HTTP client support
php-bcmath    # Arbitrary precision mathematics
php-mbstring  # Multibyte string handling
php-swoole    # Async network communication

# Additional Extensions  
php-mysql     # MySQL database driver
php-redis     # Redis caching support
php-gd        # Image processing
php-zip       # Archive handling
```

---

## 🚀 Quick Start

### 1. Installation Process

#### Clone Repository
```bash
git clone <repository-url> crmchat
cd crmchat
```

#### Install Dependencies
```bash
composer install
```

#### Environment Configuration
```bash
cp .example.env .env
# Edit .env with your database and configuration settings
```

#### Database Setup
```bash
# Import initial database structure
mysql -u root -p database_name < database/install.sql

# Apply updates if needed
mysql -u root -p database_name < update.sql
mysql -u root -p database_name < update_v1.1.sql
```

#### Generate Application Key
```bash
php think key:generate
```

### 2. Development Server

#### Start Swoole Server
```bash
# Development mode
php think swoole:server start

# Production mode  
php think swoole:server start --daemon

# Stop server
php think swoole:server stop
```

#### Queue Processing
```bash
# Start queue worker
php think queue:work

# Process specific queue
php think queue:work --queue=high,default
```

---

## 🏗️ Architecture Patterns

### MVC + Service Layer Architecture

```
Request → Controller → Service → DAO → Model → Database
                    ↓
                Response ← View/JSON
```

#### Layer Responsibilities
- **Controller**: 处理HTTP请求，参数验证，调用Service
- **Service**: 业务逻辑处理，事务管理，数据组合
- **DAO**: 数据访问抽象，SQL查询，缓存处理
- **Model**: 数据模型定义，关联关系，访问器/修改器

### Service-Oriented Design
```php
<?php
// app/services/ChatServiceService.php
namespace app\services\chat;

use crmeb\basic\BaseServices;
use app\dao\chat\ChatServiceDao;

class ChatServiceService extends BaseServices
{
    public function __construct(ChatServiceDao $dao)
    {
        $this->dao = $dao;
    }
    
    /**
     * 获取客服列表
     */
    public function getServiceList(array $where): array
    {
        $list = $this->dao->getList($where);
        return $this->buildResult($list);
    }
    
    /**
     * 数据处理
     */
    private function buildResult(array $data): array
    {
        // 业务逻辑处理
        return $data;
    }
}
```

---

## 📝 Coding Standards

### PSR Compliance
项目遵循 PSR-2 编码标准和 PSR-4 自动加载规范。

#### Class Naming
```php
<?php
// Controllers: PascalCase
class ChatServiceController extends AuthController

// Services: PascalCase + Service suffix
class ChatServiceService extends BaseServices

// Models: PascalCase
class ChatService extends BaseModel

// DAO: PascalCase + Dao suffix  
class ChatServiceDao extends BaseDao
```

#### Method Naming
```php
<?php
class ExampleController 
{
    // Controller methods: snake_case
    public function get_service_list() 
    {
        
    }
    
    // Service methods: camelCase
    public function getServiceList(): array
    {
        
    }
    
    // Private methods: camelCase
    private function buildServiceData(array $data): array
    {
        
    }
}
```

#### Database Conventions
```sql
-- Tables: lowercase + underscore
CREATE TABLE chat_service (
    id int PRIMARY KEY AUTO_INCREMENT,
    -- Fields: lowercase + underscore
    service_name varchar(50),
    create_time timestamp,
    update_time timestamp
);
```

### Code Quality Standards

#### Documentation Requirements
```php
<?php
/**
 * 客服服务管理类
 * @package app\services\chat
 */
class ChatServiceService extends BaseServices
{
    /**
     * 获取客服列表
     * @param array $where 查询条件
     * @return array
     * @throws \Exception
     */
    public function getServiceList(array $where): array
    {
        // 实现逻辑
    }
}
```

#### Input Validation
```php
<?php
// 使用 Validate 类进行数据验证
public function create(Request $request)
{
    // 获取并验证数据
    $data = $request->getMore([
        ['service_name', ''],
        ['phone', ''],
        ['status', 1]
    ]);
    
    // 数据验证
    $this->validate($data, [
        'service_name|客服名称' => 'require|max:50',
        'phone|手机号' => 'require|mobile',
        'status|状态' => 'require|in:0,1'
    ]);
    
    // 调用 Service
    return $this->success($this->services->create($data));
}
```

#### Error Handling
```php
<?php
// 统一异常处理
try {
    $result = $this->services->processData($data);
    return $this->success($result);
} catch (\Exception $e) {
    return $this->fail($e->getMessage());
}

// 或者直接抛出业务异常
if (!$this->checkPermission()) {
    throw new AuthException('权限不足', 403);
}
```

---

## 🗃️ Database Development

### Model Definition
```php
<?php
// app/models/chat/ChatService.php
namespace app\models\chat;

use crmeb\basic\BaseModel;
use crmeb\traits\ModelTrait;

class ChatService extends BaseModel
{
    use ModelTrait;
    
    protected $name = 'chat_service';
    protected $pk = 'id';
    
    // 字段类型转换
    protected $type = [
        'id' => 'integer',
        'status' => 'boolean',
        'create_time' => 'timestamp',
        'update_time' => 'timestamp'
    ];
    
    // 关联定义
    public function dialogues()
    {
        return $this->hasMany(ChatServiceDialogueRecord::class, 'service_id', 'id');
    }
    
    // 搜索器
    public function searchStatusAttr($query, $value)
    {
        if ($value !== '') {
            $query->where('status', $value);
        }
    }
}
```

### DAO Implementation
```php
<?php
// app/dao/chat/ChatServiceDao.php
namespace app\dao\chat;

use crmeb\basic\BaseDao;
use app\models\chat\ChatService;

class ChatServiceDao extends BaseDao
{
    protected function setModel(): string
    {
        return ChatService::class;
    }
    
    /**
     * 获取客服列表
     */
    public function getServiceList(array $where, int $page = 1, int $limit = 20): array
    {
        return $this->search($where)
            ->with(['dialogues'])
            ->order('sort desc,id desc')
            ->paginate([
                'page' => $page,
                'list_rows' => $limit
            ])
            ->toArray();
    }
    
    /**
     * 统计在线客服数量
     */
    public function getOnlineServiceCount(): int
    {
        return $this->count(['status' => 1, 'online' => 1]);
    }
}
```

### Migration Example
```php
<?php
// database/migrations/create_chat_service_table.php
use think\migration\Migrator;

class CreateChatServiceTable extends Migrator
{
    public function up()
    {
        $table = $this->table('chat_service', [
            'engine' => 'InnoDB',
            'charset' => 'utf8mb4',
            'collation' => 'utf8mb4_unicode_ci'
        ]);
        
        $table->addColumn('service_name', 'string', ['limit' => 50, 'comment' => '客服名称'])
              ->addColumn('avatar', 'string', ['limit' => 200, 'default' => '', 'comment' => '头像'])
              ->addColumn('phone', 'string', ['limit' => 20, 'default' => '', 'comment' => '手机号'])
              ->addColumn('status', 'boolean', ['default' => 1, 'comment' => '状态:0=禁用,1=启用'])
              ->addColumn('online', 'boolean', ['default' => 0, 'comment' => '在线状态'])
              ->addColumn('sort', 'integer', ['default' => 0, 'comment' => '排序'])
              ->addTimestamps()
              ->addIndex(['status', 'online'])
              ->create();
    }
    
    public function down()
    {
        $this->dropTable('chat_service');
    }
}
```

---

## 🌐 API Development

### Controller Structure
```php
<?php
// app/controller/admin/chat/Service.php
namespace app\controller\admin\chat;

use app\controller\admin\AuthController;
use app\services\chat\ChatServiceService;
use app\Request;

class Service extends AuthController
{
    public function __construct(ChatServiceService $services)
    {
        parent::__construct();
        $this->services = $services;
    }
    
    /**
     * 客服列表
     */
    public function index(Request $request)
    {
        $where = $request->getMore([
            ['keyword', ''],
            ['status', '']
        ]);
        
        return $this->success($this->services->getList($where));
    }
    
    /**
     * 新增客服
     */
    public function save(Request $request)
    {
        $data = $request->postMore([
            ['service_name', ''],
            ['phone', ''],
            ['avatar', ''],
            ['status', 1]
        ]);
        
        $this->validate($data, \app\validates\admin\chat\ServiceValidate::class);
        
        return $this->success('添加成功', $this->services->save($data));
    }
}
```

### Response Formatting
```php
<?php
// 使用基类的响应方法
return $this->success('操作成功', $data);           // 成功响应
return $this->fail('操作失败', 400);                // 失败响应  
return $this->successList($list, $count);          // 列表响应

// 自定义响应格式
return json([
    'status' => 200,
    'msg' => '操作成功',
    'data' => $data,
    'timestamp' => time()
]);
```

### Validation Rules
```php
<?php
// app/validates/admin/chat/ServiceValidate.php
namespace app\validates\admin\chat;

use think\Validate;

class ServiceValidate extends Validate
{
    protected $rule = [
        'service_name|客服名称' => 'require|max:50',
        'phone|手机号' => 'require|mobile',
        'status|状态' => 'require|in:0,1'
    ];
    
    protected $message = [
        'service_name.require' => '客服名称不能为空',
        'service_name.max' => '客服名称不能超过50个字符',
        'phone.mobile' => '请输入正确的手机号码'
    ];
    
    protected $scene = [
        'save' => ['service_name', 'phone', 'status'],
        'update' => ['service_name', 'phone']
    ];
}
```

---

## 🔄 Queue & Job Development

### Job Definition
```php
<?php
// app/jobs/ServiceTransfer.php
namespace app\jobs;

use crmeb\basic\BaseJobs;
use crmeb\traits\QueueTrait;

class ServiceTransfer extends BaseJobs
{
    use QueueTrait;
    
    protected $data;
    
    public function __construct($data)
    {
        $this->data = $data;
    }
    
    public function doJob()
    {
        try {
            // 执行转接逻辑
            $this->processTransfer($this->data);
            
            return true;
        } catch (\Exception $e) {
            // 记录错误日志
            \Log::error('Service transfer failed: ' . $e->getMessage());
            return false;
        }
    }
    
    private function processTransfer(array $data)
    {
        // 转接业务逻辑
    }
}
```

### Queue Usage
```php
<?php
// 推送任务到队列
use app\jobs\ServiceTransfer;
use crmeb\utils\Queue;

// 立即执行
Queue::instance()->do('serviceTransfer')->job(ServiceTransfer::class)->data($data)->push();

// 延迟执行 (60秒后)
Queue::instance()->do('serviceTransfer')->job(ServiceTransfer::class)->data($data)->delay(60)->push();
```

---

## 🔌 WebSocket Development

### WebSocket Server Configuration
```php
<?php
// config/swoole.php
return [
    'http' => [
        'enable' => true,
        'host' => '0.0.0.0',
        'port' => 9501,
    ],
    'websocket' => [
        'enable' => true,  
        'host' => '0.0.0.0',
        'port' => 9502,
    ],
    'options' => [
        'worker_num' => 4,
        'task_worker_num' => 2,
        'max_request' => 10000,
        'dispatch_mode' => 2,
    ]
];
```

### WebSocket Event Handling
```php
<?php
// app/listeners/SwooleWebSocketListen.php
use Swoole\WebSocket\Server;
use Swoole\Http\Request;
use Swoole\WebSocket\Frame;

class SwooleWebSocketListen
{
    public function onOpen(Server $server, Request $request)
    {
        // 连接建立时处理
        $token = $request->get['token'] ?? '';
        $userInfo = $this->validateToken($token);
        
        if ($userInfo) {
            $server->push($request->fd, json_encode([
                'type' => 'connected',
                'message' => '连接成功'
            ]));
        } else {
            $server->close($request->fd);
        }
    }
    
    public function onMessage(Server $server, Frame $frame)
    {
        // 接收消息处理
        $data = json_decode($frame->data, true);
        
        switch ($data['type']) {
            case 'message':
                $this->handleMessage($server, $frame, $data);
                break;
            case 'typing':
                $this->handleTyping($server, $frame, $data);
                break;
        }
    }
    
    private function handleMessage(Server $server, Frame $frame, array $data)
    {
        // 处理聊天消息
        $message = [
            'type' => 'message',
            'from_id' => $data['from_id'],
            'to_id' => $data['to_id'],
            'message' => $data['message'],
            'time' => time()
        ];
        
        // 广播消息给目标用户
        $server->push($data['to_fd'], json_encode($message));
    }
}
```

---

## 🧪 Testing

### Unit Testing Setup
```php
<?php
// tests/Unit/Services/ChatServiceServiceTest.php
namespace tests\Unit\Services;

use PHPUnit\Framework\TestCase;
use app\services\chat\ChatServiceService;

class ChatServiceServiceTest extends TestCase
{
    protected $service;
    
    protected function setUp(): void
    {
        parent::setUp();
        $this->service = app(ChatServiceService::class);
    }
    
    public function testGetServiceList()
    {
        $result = $this->service->getList([]);
        
        $this->assertIsArray($result);
        $this->assertArrayHasKey('list', $result);
        $this->assertArrayHasKey('count', $result);
    }
    
    public function testCreateService()
    {
        $data = [
            'service_name' => '测试客服',
            'phone' => '13800138000',
            'status' => 1
        ];
        
        $result = $this->service->save($data);
        
        $this->assertIsArray($result);
        $this->assertArrayHasKey('id', $result);
    }
}
```

### API Testing
```php
<?php
// tests/Feature/Api/ServiceTest.php
namespace tests\Feature\Api;

use Tests\TestCase;

class ServiceTest extends TestCase
{
    public function testServiceList()
    {
        $response = $this->withHeaders([
            'Authorization' => 'Bearer ' . $this->getAdminToken(),
        ])->get('/admin/chat/service');
        
        $response->assertStatus(200)
                ->assertJsonStructure([
                    'status',
                    'data' => [
                        'list',
                        'count'
                    ]
                ]);
    }
    
    private function getAdminToken(): string
    {
        // 获取测试用的管理员 token
        return 'test_admin_token';
    }
}
```

---

## 📦 Deployment Guide

### Production Configuration

#### Environment Variables
```bash
# .env
APP_ENV=production
APP_DEBUG=false
APP_KEY=your_32_character_secret_key

DATABASE_HOSTNAME=localhost
DATABASE_DATABASE=crmchat_prod
DATABASE_USERNAME=crmchat_user
DATABASE_PASSWORD=secure_password

CACHE_DRIVER=redis
QUEUE_DRIVER=redis

REDIS_HOSTNAME=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=redis_password
```

#### Nginx Configuration
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /var/www/crmchat/public;
    index index.php index.html;
    
    location / {
        try_files $uri $uri/ /index.php?$query_string;
    }
    
    location ~ \.php$ {
        fastcgi_pass 127.0.0.1:9000;
        fastcgi_index index.php;
        fastcgi_param SCRIPT_FILENAME $document_root$fastcgi_script_name;
        include fastcgi_params;
    }
    
    # WebSocket 代理
    location /ws {
        proxy_pass http://127.0.0.1:9502;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### Service Management
```bash
# 创建 systemd 服务文件
sudo nano /etc/systemd/system/crmchat-swoole.service

[Unit]
Description=CRMChat Swoole Server
After=network.target

[Service]
Type=forking
User=www-data
Group=www-data
WorkingDirectory=/var/www/crmchat
ExecStart=/usr/bin/php think swoole:server start --daemon
ExecReload=/bin/kill -USR1 $MAINPID
ExecStop=/usr/bin/php think swoole:server stop
Restart=always
RestartSec=3

[Install]
WantedBy=multi-user.target

# 启用服务
sudo systemctl enable crmchat-swoole
sudo systemctl start crmchat-swoole
```

---

## 🐛 Debugging & Logging

### Debug Configuration
```php
<?php
// config/app.php
return [
    'app_debug' => env('APP_DEBUG', false),
    'app_trace' => env('APP_TRACE', false),
    
    // 异常页面的模板文件
    'exception_tmpl' => app()->getRootPath() . 'public/error.html',
    
    // 错误显示信息,非调试模式有效
    'error_message' => '页面错误！请稍后再试～',
];
```

### Logging Usage
```php
<?php
use think\facade\Log;

// 不同级别的日志
Log::debug('Debug information');
Log::info('Informational message');
Log::notice('Normal but significant');
Log::warning('Warning message');
Log::error('Error occurred', ['context' => $data]);
Log::critical('Critical condition');
Log::alert('Action must be taken');
Log::emergency('System is unusable');

// 写入到特定日志文件
Log::channel('sql')->info('SQL query executed', ['sql' => $sql, 'time' => $time]);
```

---

*Generated with CRM-CHAT Development Guide System*