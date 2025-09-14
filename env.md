# CRMChat 环境配置信息

## 🛠 Docker 服务配置

### 1. 宝塔面板 (BaoTa Panel - 官方最新版)
```
访问地址: http://localhost:8888/btpanel
用户名: admin123
密码: admin123456
容器名: baota
镜像: btpanel/baota:latest (官方最新版)
端口映射: 
  - 8888:8888 (面板端口)
  - 80:80 (HTTP)
  - 443:443 (HTTPS)
  - 21:21 (FTP)
  - 20:20 (FTP数据)
  - 20108:20108 (Swoole WebSocket)
  - 9000:9000 (PHP-FPM)
  - 39000-39999:39000-39999 (FTP被动模式)
数据挂载:
  - /Users/tony/Documents/saasChat/crmchat/crmchat:/www/wwwroot/crmchat
  - /Users/tony/Documents/saasChat/baota_data:/www/server/data
重启策略: --restart=always

启动命令:
docker run -tid --name baota --privileged=true --shm-size=1g --restart=always \
  -p 8888:8888 -p 80:80 -p 443:443 -p 21:21 -p 20:20 -p 20108:20108 -p 9000:9000 -p 39000-39999:39000-39999 \
  -v /Users/tony/Documents/saasChat/crmchat/crmchat:/www/wwwroot/crmchat \
  -v /Users/tony/Documents/saasChat/baota_data:/www/server/data \
  btpanel/baota:latest
```

### 2. MySQL 5.7 数据库
```
主机: localhost
端口: 3306
用户名: root
密码: 123456
默认数据库: crmchat
容器名: mysql57
镜像: mysql:5.7
数据目录: /Users/tony/Documents/docker/mysql

连接命令:
mysql -h localhost -P 3306 -u root -p123456

Docker连接:
docker exec -it mysql57 mysql -uroot -p123456
```

### 3. Redis 6.2 缓存
```
主机: localhost
端口: 6379
密码: 无
AOF持久化: 已开启
容器名: redis62
镜像: redis:6.2
数据目录: /Users/tony/Documents/docker/redis

连接命令:
redis-cli -h localhost -p 6379

Docker连接:
docker exec -it redis62 redis-cli
```

## 📁 项目目录结构

```
/Users/tony/Documents/saasChat/crmchat/
├── crmchat/                    # CRMChat项目源码
├── baota_data/                 # 宝塔面板数据
├── env.md                      # 本配置文件
└── CLAUDE.md                   # Claude开发指南

/Users/tony/Documents/docker/
├── mysql/                      # MySQL数据文件
└── redis/                      # Redis数据文件
```

## 🔧 CRMChat 应用配置

### ThinkPHP 数据库配置 (config/database.php)
```php
'mysql' => [
    'hostname'        => '127.0.0.1',
    'database'        => 'crmchat',
    'username'        => 'root',
    'password'        => '123456',
    'hostport'        => '3306',
],
```

### Redis 配置 (config/cache.php)
```php
'redis' => [
    'host'          => '127.0.0.1',
    'port'          => '6379',
    'password'      => '',
    'select'        => 0,
],
```

### Swoole WebSocket 配置 (config/swoole.php)
```php
'server' => [
    'host'      => '0.0.0.0',
    'port'      => 20108,
],
```

## 🚀 启动命令

### 启动所有服务
```bash
# 启动宝塔面板
docker start baota

# 启动MySQL
docker start mysql57

# 启动Redis
docker start redis62

# 查看所有运行状态
docker ps
```

### 停止所有服务
```bash
docker stop baota mysql57 redis62
```

### 重建服务（如需要）
```bash
# 宝塔面板 (官方最新版)
docker run -tid --name baota --privileged=true --shm-size=1g --restart=always \
  -p 8888:8888 -p 80:80 -p 443:443 -p 21:21 -p 20:20 -p 20108:20108 -p 9000:9000 -p 39000-39999:39000-39999 \
  -v /Users/tony/Documents/saasChat/crmchat/crmchat:/www/wwwroot/crmchat \
  -v /Users/tony/Documents/saasChat/baota_data:/www/server/data \
  btpanel/baota:latest

# MySQL 5.7
docker run -d --name mysql57 --restart=always \
  -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 -e MYSQL_DATABASE=crmchat \
  -v /Users/tony/Documents/docker/mysql:/var/lib/mysql \
  mysql:5.7

# Redis 6.2
docker run -d --name redis62 --restart=always \
  -p 6379:6379 -v /Users/tony/Documents/docker/redis:/data \
  redis:6.2 redis-server --appendonly yes
```

## 🔐 安全提示

- 生产环境请修改默认密码
- 考虑启用Redis密码保护
- 配置防火墙规则限制数据库访问
- 定期备份数据目录

## 📝 注意事项

1. **宝塔面板官方镜像**：使用 btpanel/baota:latest 官方最新版，软件商店可正常使用
2. 数据文件已挂载到本地，删除容器不会丢失数据  
3. CRMChat项目已挂载到宝塔的 `/www/wwwroot/crmchat` 目录
4. Swoole WebSocket 服务端口为 20108
5. 宝塔面板访问地址需要加 `/btpanel` 后缀

## 🎯 宝塔面板操作指南

登录后可通过软件商店安装：
- PHP 7.4 (需要安装 redis、swoole、fileinfo、bcmath、gd 等扩展)
- Nginx 或 Apache
- 其他必需组件

---
*最后更新: 2025-09-14*