# CRMChat 数据库备份

## 文件说明

### crmeb_complete.sql
**完整数据库备份** - 包含结构和数据

- **数据库**: crmeb
- **导出时间**: 2025-10-10
- **包含内容**:
  - 所有表结构（CREATE TABLE）
  - 所有表数据（INSERT）
  - 存储过程（ROUTINES）
  - 触发器（TRIGGERS）
  - 事件（EVENTS）
- **特性**:
  - `--add-drop-table`: 包含DROP TABLE语句
  - `--complete-insert`: 完整的INSERT语句（包含列名）
  - `--single-transaction`: 事务一致性快照

## 使用方法

### 恢复整个数据库

```bash
# 方式1: 通过Docker恢复
docker exec -i srt-mysql mysql -uroot -proot123 crmeb < crmeb_complete.sql

# 方式2: 直接恢复（如果MySQL在本地）
mysql -uroot -proot123 crmeb < crmeb_complete.sql

# 方式3: 恢复到新数据库
docker exec -i srt-mysql mysql -uroot -proot123 -e "CREATE DATABASE crmeb_new CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
docker exec -i srt-mysql mysql -uroot -proot123 crmeb_new < crmeb_complete.sql
```

### 仅查看表结构

```bash
# 查看所有CREATE TABLE语句
grep -A 20 "CREATE TABLE" crmeb_complete.sql | less
```

### 导出特定表

```bash
# 仅导出指定表
docker exec srt-mysql mysqldump -uroot -proot123 crmeb eb_chat_service eb_chat_user > specific_tables.sql
```

## 核心数据表

### 租户相关
- `eb_tenants` - 租户主表

### 用户相关
- `eb_chat_user` - 用户/访客表（游客和正式用户）
- `eb_chat_service` - 客服账号表

### 消息相关
- `eb_chat_service_dialogue_record` - 聊天消息记录
- `eb_chat_service_record` - 会话记录（用户列表）

### 系统配置
- `eb_system_config` - 系统配置表
- `eb_auxiliary` - 辅助表（转接关系等）

## 数据库连接信息

```yaml
Host: localhost
Port: 3306
Database: crmeb
Username: root
Password: root123
Docker容器: srt-mysql
```

## 备份策略

### 建议备份频率
- **开发环境**: 每天备份一次
- **测试环境**: 每次重大变更前备份
- **生产环境**: 每小时增量备份 + 每天全量备份

### 备份命令（定时任务）

```bash
#!/bin/bash
# 每日备份脚本
BACKUP_DIR="/Volumes/ORICO/project/kefu/php/crmchat/backend/security-enterprise-tenant/crmchat/db/backups"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

docker exec srt-mysql mysqldump -uroot -proot123 \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  --add-drop-table \
  --complete-insert \
  crmeb 2>&1 | grep -v "Using a password" > $BACKUP_DIR/crmeb_$DATE.sql

# 只保留最近7天的备份
find $BACKUP_DIR -name "crmeb_*.sql" -mtime +7 -delete
```

## 数据验证

### 验证备份文件

```bash
# 检查文件大小
ls -lh crmeb_complete.sql

# 验证SQL语法
docker exec -i srt-mysql mysql -uroot -proot123 --force -e "source /path/to/crmeb_complete.sql" 2>&1 | grep -i error

# 统计表数量
grep -c "CREATE TABLE" crmeb_complete.sql

# 统计数据行数
grep -c "^INSERT INTO" crmeb_complete.sql
```

## 注意事项

⚠️ **重要提醒**:
1. 恢复数据库前务必备份现有数据
2. 确认目标数据库的字符集为 `utf8mb4`
3. 检查MySQL版本兼容性（当前备份来自MySQL 8.0.43）
4. 生产环境恢复前应先在测试环境验证
5. 大数据量恢复可能需要较长时间

## 最后更新

- **备份时间**: 2025-10-10 10:48
- **数据库版本**: MySQL 8.0.43
- **备份大小**: 293KB
- **维护人**: Claude Code Assistant
