# Chat API Mapping - 聊天功能API映射

## 概述

客服聊天系统使用两张核心表存储数据：
- **eb_chat_service_record**: 存储聊天用户/会话列表（左侧列表）
- **eb_chat_service_dialogue_record**: 存储聊天消息内容（右侧消息）

## 数据表映射

### 1. eb_chat_service_record - 聊天用户列表

**表作用**: 存储客服与用户的会话记录（左侧用户列表）

**关键字段**:
```sql
id              int           # 主键
appid           varchar(32)   # 应用ID
user_id         int           # 发送方用户ID（游客ID）
to_user_id      int           # 接收方用户ID（客服ID）
nickname        varchar(50)   # 用户昵称
avatar          varchar(255)  # 用户头像
is_tourist      tinyint(1)    # 是否游客
online          tinyint(1)    # 是否在线
type            tinyint(1)    # 类型
add_time        int           # 创建时间
update_time     int           # 更新时间
mssage_num      int           # 未读消息数
message         text          # 最后一条消息内容
message_type    tinyint(1)    # 最后一条消息类型
```

**对应API**: `GET /api/kefu/user/record`

**Controller**: `KefuUserController.getRecordList()`
**Service**: `KefuUserService.getRecordList()`
**Mapper**: `ChatServiceRecordMapper`

**请求参数**:
```
nickname    - 昵称搜索（可选）
is_tourist  - 是否游客（可选）
label_id    - 标签ID（可选）
group_id    - 分组ID（可选）
page        - 页码（可选，默认1）
limit       - 每页数量（可选，默认15）
```

**响应格式**:
```json
{
  "status": 200,
  "msg": "ok",
  "data": {
    "count": 10,
    "list": [
      {
        "id": 1,
        "user_id": 874,
        "to_user_id": 1,
        "nickname": "Guest2025473756",
        "avatar": "https://...",
        "is_tourist": 1,
        "add_time": 1234567890,
        "msn": "最后一条消息",
        "message_type": 1,
        "num": 3
      }
    ]
  }
}
```

### 2. eb_chat_service_dialogue_record - 聊天消息列表

**表作用**: 存储聊天消息内容（右侧消息列表）

**关键字段**:
```sql
id              int           # 主键
appid           varchar(32)   # 应用ID
mer_id          int           # 商户ID
msn             text          # 消息内容
user_id         int           # 发送方用户ID
to_user_id      int           # 接收方用户ID
is_tourist      tinyint(1)    # 是否游客
add_time        int           # 发送时间
type            tinyint(1)    # 消息来源类型(0-用户发送, 1-客服发送)
remind          tinyint(1)    # 提醒
msn_type        tinyint(1)    # 消息类型(1-文字, 2-表情, 3-图片, 4-语音)
is_send         tinyint(1)    # 是否已发送
other           varchar(2000) # 其他信息(JSON)
guid            varchar(100)  # 消息唯一ID
```

**对应API**: `GET /api/kefu/service/list`

**Controller**: `KefuServiceExtensionController.getServiceList()`
**Service**: `KefuServiceExtensionService.getChatList()`
**Mapper**: `ChatServiceDialogueRecordMapper`

**请求参数**:
```
user_id     - 用户ID（必需，选中的聊天用户）
upperId     - 消息ID（分页用，默认0）
is_tourist  - 是否游客（可选）
limit       - 每页数量（可选，默认20）
```

**响应格式**:
```json
{
  "status": 200,
  "msg": "ok",
  "data": {
    "count": 50,
    "list": [
      {
        "id": 123,
        "msn": "你好",
        "user_id": 874,
        "to_user_id": 1,
        "is_tourist": 1,
        "add_time": 1234567890,
        "type": 0,
        "msn_type": 1,
        "guid": "unique-id",
        "other": "{}"
      }
    ]
  }
}
```

## API调用流程

### 前端加载流程

```
1. 客服登录成功
   ↓
2. 调用 GET /api/kefu/user/record
   - 获取聊天用户列表（eb_chat_service_record）
   - 显示在左侧列表
   ↓
3. 用户点击某个聊天用户
   ↓
4. 调用 GET /api/kefu/service/list?user_id=874
   - 获取与该用户的聊天消息（eb_chat_service_dialogue_record）
   - 显示在右侧消息区域
```

### 数据依赖关系

```
eb_chat_service_record (会话表)
  ├─ user_id: 游客/用户ID
  ├─ to_user_id: 客服ID
  └─ 用于左侧用户列表

        ↓ 选择用户后

eb_chat_service_dialogue_record (消息表)
  ├─ user_id + to_user_id 过滤消息
  └─ 用于右侧消息列表
```

## 当前问题诊断

### 问题现象
- 左侧用户列表为空
- API `/api/kefu/user/record` 返回 `{count: 0, list: []}`

### 可能原因

1. **数据库无数据**
   - `eb_chat_service_record` 表为空
   - 没有创建会话记录

2. **查询条件错误**
   - `to_user_id` 字段使用错误
   - appid 不匹配
   - 租户隔离问题

3. **业务逻辑问题**
   - 会话记录创建时机不对
   - 未正确保存到 `eb_chat_service_record`

### 调试建议

1. **检查数据库**
```sql
-- 查看是否有会话记录
SELECT * FROM eb_chat_service_record LIMIT 10;

-- 查看当前客服相关记录
SELECT * FROM eb_chat_service_record
WHERE to_user_id = 1  -- 客服ID
ORDER BY update_time DESC;
```

2. **检查API日志**
```bash
# 查看后端日志
tail -f backend/security-enterprise-tenant/crmchat/logs/crmchat.log

# 检查SQL查询
# 应该看到类似：
SELECT * FROM eb_chat_service_record
WHERE appid = '202509251953426878'
  AND to_user_id = 1
```

3. **检查Service层逻辑**
- 文件: `KefuUserService.java:getRecordList()`
- 确认查询条件是否正确
- 检查租户隔离逻辑

## API测试

### 测试会话列表API
```bash
curl -X GET "http://localhost:20108/api/kefu/user/record?page=1&limit=15" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 测试消息列表API
```bash
curl -X GET "http://localhost:20108/api/kefu/service/list?user_id=874&limit=20&upperId=0&is_tourist=0" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 相关文件位置

### Backend (Java)
```
/backend/security-enterprise-tenant/crmchat/src/main/java/io/renren/crmchat/
├── controller/kefu/
│   ├── KefuUserController.java              # 用户列表API
│   └── KefuServiceExtensionController.java  # 消息列表API
├── service/
│   ├── KefuUserService.java                 # 用户列表业务逻辑
│   └── KefuServiceExtensionService.java     # 消息列表业务逻辑
└── dao/
    ├── ChatServiceRecordMapper.java         # 会话表Mapper
    └── ChatServiceDialogueRecordMapper.java # 消息表Mapper
```

### Frontend
```
/template/admin/src/pages/kefu/
├── pc/
│   ├── index.vue          # 客服工作台主页面
│   └── components/
│       └── chatList.vue   # 左侧用户列表组件
```

## 问题修复记录

### 问题原因
前端API调用错误：
- 文件：`template/admin/src/api/kefu.js:258`
- 问题：`serviceList()` 函数调用 `/service/list` 端点（用于客服转接列表）
- 应该：调用 `/service/chat/history` 端点（用于聊天消息列表）

### 修复方案
更新 `template/admin/src/api/kefu.js:260` 的 URL：
```javascript
// 修复前
url: `service/list`,

// 修复后
url: `service/chat/history`,
```

### 验证结果
✅ 修复完成并验证成功
- 前端成功调用 `/api/kefu/service/chat/history`
- API返回聊天消息数据：`{status: 200, msg: ok, data: Array(1)}`
- 聊天消息正常显示在中间面板
- 截图保存：`/Volumes/ORICO/project/kefu/.playwright-mcp/page-2025-10-09T12-40-27-015Z.png`

## 下一步行动

1. ✅ 确认API映射关系
2. ✅ 检查前端API调用
3. ✅ 修复API端点错误
4. ✅ 验证前端能正常显示聊天消息
5. ⏳ （可选）创建更多测试数据验证完整功能
