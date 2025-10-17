# 退出登录WebSocket关闭和离线标记修复

## 问题描述

用户反馈：客服点击退出登录后，仍然会收到消息推送，说明：
1. **WebSocket连接未正确关闭** - 连接仍然保持活跃状态
2. **客服在线状态未标记为离线** - 数据库中online字段未更新为0
3. **自动重连机制干扰** - 关闭连接后立即触发自动重连

## 根本原因分析

### 1. 后端问题：缺少logout消息处理

**问题位置**: `ChatWebSocketServer.java:onMessage()`

原代码的`switch`语句只处理了这些消息类型：
- `ping` - 心跳检测
- `user` - 用户信息同步
- `to_chat` - 切换聊天对象
- `open` - 打开聊天
- `set_form_type` - 设置表单类型

**缺少的处理**:
- ❌ `logout` - 退出登录（标记离线并关闭连接）
- ❌ `online` - 在线状态变更

**后果**:
- 前端发送`{type: "logout", data: {...}}`消息后，后端不处理
- 客服的`eb_chat_service.online`字段不会更新为0
- WebSocket连接不会被服务器主动关闭

### 2. 前端问题：自动重连机制

**问题位置**: `socket.js:onClose()`

原代码：
```javascript
onClose() {
    this.connectLing = false;
    this.timer && clearInterval(this.timer);
    this.timer = null;
    this.opt.close && this.opt.close();
    this.socketStatus = false;
    this.reconne();  // ⚠️ 无条件自动重连！
}
```

**问题流程**:
```
1. 用户点击退出 → logoutKefu()
2. 发送logout消息 → ws.send({type: 'logout'})
3. 调用ws.onClose()关闭连接
4. onClose()触发 → 调用reconne()
5. ⚠️ 2秒后自动重连成功（Token和Cookie还未清除）
6. 客服继续在线，收到消息推送
```

### 3. 前端逻辑问题：先关闭后清理

**问题位置**: `kefu.js:logoutKefu()`

原代码执行顺序：
```javascript
1. 调用后端API: AccountLogoutKefu()
2. 发送logout消息: ws.send({type: 'logout'})
3. 立即关闭连接: ws.onClose()
4. 清理认证信息: localStorage.clear() + cookie清除
5. 跳转登录页
```

**问题**:
- 第3步关闭连接后，`onClose()`立即触发自动重连
- 第4步清理认证信息时，重连可能已经成功（使用旧Token）
- 时序竞争：关闭和清理之间有时间窗口

## 完整解决方案

### 1. 后端修复：添加logout和online消息处理

**文件**: `ChatWebSocketServer.java`
**位置**: Lines 138-180

#### 添加logout消息处理

```java
case "logout" -> {
    // 处理客服退出登录
    WebSocketSessionRegistry.SessionHolder holder = WebSocketSessionRegistry.get(session);
    if (holder != null) {
        log.info("Processing logout request: appid={}, userId={}, type={}",
                holder.getAppid(), holder.getUserId(), holder.getUserType());

        // 标记用户离线
        runWithUserContext(holder.getAppid(), holder.getUserId(), holder.getUserType(),
                () -> markUserOnline(holder, false));

        // 移除session注册
        WebSocketSessionRegistry.remove(session);

        // 发送logout确认并关闭连接
        sendEnvelope(session, "logout", Map.of("status", "success"));
        closeSilently(session);
    }
}
```

**功能**:
1. ✅ 获取session holder信息
2. ✅ 调用`markUserOnline(holder, false)`标记离线
   - 更新`eb_chat_user.online = 0`
   - 更新`eb_chat_service.online = 0`
   - 更新`eb_chat_service_record.online = 0`
3. ✅ 从注册表移除session
4. ✅ 发送logout确认消息
5. ✅ 服务器主动关闭连接

#### 添加online消息处理

```java
case "online" -> {
    // 处理客服在线状态变更
    Integer onlineStatus = parseIntNullable(dataNode.path("online").asText(null));
    if (onlineStatus != null) {
        WebSocketSessionRegistry.SessionHolder holder = WebSocketSessionRegistry.get(session);
        if (holder != null && "kefu".equals(holder.getUserType())) {
            ChatServiceMapper serviceMapper = SpringContextUtils.getBean(ChatServiceMapper.class);
            if (serviceMapper != null) {
                UpdateWrapper<ChatServiceEntity> wrapper = new UpdateWrapper<>();
                if (holder.getServiceId() != null && holder.getServiceId() > 0) {
                    wrapper.eq("id", holder.getServiceId()).eq("appid", holder.getAppid());
                } else {
                    wrapper.eq("appid", holder.getAppid()).eq("user_id", holder.getUserId());
                }
                ChatServiceEntity update = new ChatServiceEntity();
                update.setOnline(onlineStatus);
                update.setIsBackstage(onlineStatus);
                update.setUpdateTime((int) (System.currentTimeMillis() / 1000));
                serviceMapper.update(update, wrapper);
                log.info("Updated kefu online status: userId={}, status={}", holder.getUserId(), onlineStatus);
            }
        }
    }
}
```

**功能**:
- 处理客服点击"Online/Offline"切换在线状态
- 更新`eb_chat_service.online`字段
- 不关闭WebSocket连接（仅状态变更）

### 2. 前端修复：防止自动重连

#### 步骤1：添加manualClose标志

**文件**: `socket.js`
**位置**: Line 22

```javascript
class wsSocket {
    constructor(opt) {
        this.vm = new Vue;
        this.ws = null;
        this.opt = opt || {};
        this.networkStatus = true;
        this.reconneMax = 100;
        this.connectLing = false;
        this.manualClose = false; // ✅ 新增：标记是否为手动关闭
        // ...
    }
}
```

#### 步骤2：onClose检查标志

**文件**: `socket.js`
**位置**: Lines 201-214

```javascript
onClose() {
    this.connectLing = false;
    this.timer && clearInterval(this.timer);
    this.timer = null;
    this.opt.close && this.opt.close();
    this.socketStatus = false;

    // ✅ 如果是手动关闭（退出登录），则不自动重连
    if (!this.manualClose) {
        this.reconne();
    } else {
        console.log('[WebSocket] 手动关闭，不进行重连');
    }
}
```

**逻辑**:
- 正常断网/异常断开 → `manualClose = false` → 自动重连 ✅
- 退出登录 → `manualClose = true` → 不重连 ✅

#### 步骤3：添加manuallyClose方法

**文件**: `socket.js`
**位置**: Lines 233-244

```javascript
// 手动关闭WebSocket连接（退出登录时使用，不自动重连）
manuallyClose() {
    this.manualClose = true;
    if (this.timer) {
        clearInterval(this.timer);
        this.timer = null;
    }
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.ws.close();
    }
    console.log('[WebSocket] 手动关闭连接，已禁用自动重连');
}
```

**功能**:
1. 设置`manualClose = true`
2. 清除心跳定时器
3. 检查连接状态后关闭
4. 输出日志便于调试

### 3. 前端修复：优化退出流程

**文件**: `kefu.js:logoutKefu()`
**位置**: Lines 24-70

#### 优化后的执行顺序

```javascript
logoutKefu({ commit, dispatch }, { confirm = false, vm } = {}) {
    async function logout() {
        // 1️⃣ 先发送logout消息到服务器，标记离线
        Socket(false).then(ws => {
            ws.send({
                type: 'logout',
                data: { uid: getCookies('kefu_uuid') }
            }).then(() => {
                console.log('[退出登录] 已发送logout消息到服务器');
            }).catch(err => {
                console.warn('[退出登录] 发送logout消息失败:', err);
            });

            // 2️⃣ 等待100ms让服务器处理logout消息，然后手动关闭WebSocket（不重连）
            setTimeout(() => {
                ws.manuallyClose();
            }, 100);
        });

        // 3️⃣ 调用后端API退出登录
        AccountLogoutKefu().then(() => {
            console.log('[退出登录] 后端API退出成功');
        }).catch(err => {
            console.warn('[退出登录] 后端API退出失败:', err);
        });

        // 4️⃣ 清理Token检查定时器
        if (vm && typeof vm.cleanupTokenTimer === 'function') {
            vm.cleanupTokenTimer();
        }

        // 5️⃣ 清理所有本地认证信息
        localStorage.clear();
        document.cookie.split(";").forEach(c => {
            document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
        });

        // 6️⃣ 清空 vuex 用户信息
        commit('setInfo', null);

        // 7️⃣ 跳转到登录页（使用replace避免返回）
        router.replace({ path: '/kefu' });
    }
    logout();
}
```

#### 关键改进点

| 步骤 | 改进 | 目的 |
|-----|------|------|
| 1️⃣ | 先发送logout消息 | 让服务器标记离线、关闭连接 |
| 2️⃣ | 使用`manuallyClose()`代替`onClose()` | 禁用自动重连 |
| 2️⃣ | 延迟100ms关闭 | 确保logout消息已发送 |
| 3️⃣ | API调用不阻塞 | 快速响应用户操作 |
| 4️⃣ | 清理Token定时器 | 防止内存泄漏 |
| 5️⃣ | 完整清理认证信息 | 安全退出 |
| 6️⃣ | 清空Vuex状态 | 状态一致性 |
| 7️⃣ | router.replace | 防止返回 |

## 完整流程对比

### 修复前的流程（❌ 有问题）

```
用户点击退出
    ↓
前端发送logout消息
    ↓
前端调用ws.onClose()
    ↓ (立即触发)
onClose()自动调用reconne()
    ↓ (2秒后)
自动重连成功 ← Token和Cookie还未清除
    ↓
❌ 客服仍然在线，继续收到消息
```

**问题**:
- 后端不处理logout消息，online字段不更新
- 前端自动重连，连接未真正断开
- 时序竞争导致清理失效

### 修复后的流程（✅ 正确）

```
用户点击退出
    ↓
1. 前端发送logout消息
    ↓
2. 后端处理logout消息:
   - 更新online=0
   - 移除session注册
   - 发送logout确认
   - 服务器关闭连接
    ↓
3. 前端延迟100ms后调用manuallyClose()
   - 设置manualClose=true
   - 清除心跳定时器
   - 客户端关闭连接
    ↓
4. onClose()触发检查manualClose
   - manualClose=true → 不自动重连 ✅
    ↓
5. 清理本地数据
   - Token定时器
   - localStorage
   - Cookie
    ↓
6. 跳转登录页
```

**改进**:
- ✅ 后端正确标记离线
- ✅ 服务器主动关闭连接
- ✅ 前端禁用自动重连
- ✅ 清理顺序正确
- ✅ 不再收到消息推送

## 测试验证

### 测试1：正常退出登录

**步骤**:
```
1. 登录客服端
2. 打开浏览器控制台（F12）
3. 点击右上角头像 → "Logout"
4. 确认退出对话框
```

**预期结果**:
```
✅ Console输出:
   [退出登录] 已发送logout消息到服务器
   [WebSocket] 手动关闭连接，已禁用自动重连
   [Token管理] 定时器已清理

✅ 后端日志:
   Processing logout request: appid=xxx, userId=xxx, type=kefu
   Updated kefu online status: userId=xxx, status=0

✅ 数据库验证:
   eb_chat_service.online = 0
   eb_chat_user.online = 0
   eb_chat_service_record.online = 0

✅ 行为验证:
   - 不再收到消息推送
   - WebSocket连接已断开
   - 游客发消息看不到该客服在线
```

### 测试2：退出后不自动重连

**步骤**:
```
1. 登录客服端
2. 打开Network标签，过滤WS
3. 点击退出登录
4. 观察5分钟
```

**预期结果**:
```
✅ WebSocket连接显示：
   - 状态: Finished (101 Switching Protocols → Close)
   - 没有新的WS连接建立
   - 不显示重连尝试

✅ Console没有输出：
   - 重新连接
   - reconne相关日志

✅ 网络活动：
   - 无心跳ping/pong消息
   - 无自动重连请求
```

### 测试3：退出后发送消息

**步骤**:
```
1. 客服A登录并在线
2. 游客发送消息给客服A
3. 客服A点击退出登录
4. 游客再次发送消息
```

**预期结果**:
```
步骤2后:
✅ 客服A收到消息推送
✅ 消息列表实时更新

步骤4后:
✅ 客服A不再收到消息
✅ 游客看到"客服离线"提示
✅ 消息分配给其他在线客服（如果有）
```

### 测试4：数据库验证

**SQL查询**:
```sql
-- 退出前
SELECT id, nickname, online FROM eb_chat_service
WHERE id = 客服ID;
-- 结果: online = 1

-- 点击退出后
SELECT id, nickname, online FROM eb_chat_service
WHERE id = 客服ID;
-- 结果: online = 0

-- 验证所有相关表
SELECT * FROM eb_chat_user WHERE id = user_id;
-- online = 0

SELECT * FROM eb_chat_service_record WHERE user_id = user_id;
-- online = 0
```

## 文件修改清单

### 后端修改

| 文件 | 修改内容 | 行数 |
|-----|---------|------|
| `ChatWebSocketServer.java` | 添加`logout`消息处理 | 138-156 |
| `ChatWebSocketServer.java` | 添加`online`消息处理 | 157-180 |

**构建命令**:
```bash
cd /Volumes/ORICO/project/kefu/php/crmchat/backend/security-enterprise-tenant/crmchat
mvn clean package -DskipTests
java -jar target/crmchat.jar
```

### 前端修改

| 文件 | 修改内容 | 行数 |
|-----|---------|------|
| `socket.js` | 添加`manualClose`标志 | 22 |
| `socket.js` | `onClose()`检查标志 | 201-214 |
| `socket.js` | 添加`manuallyClose()`方法 | 233-244 |
| `kefu.js` | 重构`logoutKefu()`逻辑 | 24-70 |

**无需重启前端**: 浏览器刷新页面即可

## 技术要点

### 1. 为什么需要延迟100ms关闭？

```javascript
setTimeout(() => {
    ws.manuallyClose();
}, 100);
```

**原因**:
- WebSocket消息发送是**异步**的
- `ws.send()`立即返回，但消息可能还在发送缓冲区
- 如果立即关闭连接，logout消息可能未发送完成
- 延迟100ms确保消息已发送到服务器

**替代方案**:
```javascript
// 可以监听logout响应后再关闭
ws.$on('logout', (data) => {
    if (data.status === 'success') {
        ws.manuallyClose();
    }
});
```

### 2. 为什么使用manualClose标志而不是参数？

**不好的设计**:
```javascript
onClose(isManual) {
    if (!isManual) {
        this.reconne();
    }
}

// 调用时
ws.close(); // 如何传参？浏览器原生close()不接受参数
```

**好的设计**:
```javascript
this.manualClose = true; // 先设置标志
ws.close(); // 触发onClose回调
// onClose内部检查this.manualClose
```

**优点**:
- 不依赖方法参数
- 状态持久化
- 兼容浏览器原生close()

### 3. 为什么需要同时处理logout和online？

| 消息类型 | 用途 | WebSocket连接 |
|---------|------|--------------|
| `logout` | 退出登录 | 关闭连接 |
| `online` | 在线状态切换 | 保持连接 |

**场景区分**:
- 退出登录 → 发送`logout` → 断开连接 → 跳转登录页
- 切换状态 → 发送`online(0/1)` → 保持连接 → 仅更新状态

## 后续优化建议

### 1. 添加心跳超时检测

当前问题：如果网络异常，客服可能显示在线但实际无法收消息

**解决方案**:
```java
// 后端：超时自动标记离线
@Scheduled(fixedRate = 60000) // 每分钟检查
public void checkHeartbeatTimeout() {
    long now = System.currentTimeMillis();
    for (SessionHolder holder : getAllSessions()) {
        long lastPing = holder.getLastPingTime();
        if (now - lastPing > 120000) { // 2分钟无心跳
            markOffline(holder);
            closeSilently(holder.getSession());
        }
    }
}
```

### 2. 添加退出登录埋点

**前端埋点**:
```javascript
logoutKefu() {
    // 记录退出时间、原因
    this.trackEvent('kefu_logout', {
        userId: this.userId,
        duration: Date.now() - this.loginTime,
        reason: 'manual' // manual | token_expired | network_error
    });

    // 原有退出逻辑...
}
```

### 3. 添加强制下线功能

**应用场景**:
- 管理员踢掉某个客服
- 检测到异常登录行为
- 账号被禁用

**实现思路**:
```java
// 后端：管理员API
@PostMapping("/admin/kefu/kick")
public void kickKefu(Long kefuId) {
    // 1. 查找该客服的所有WebSocket连接
    Set<SessionHolder> sessions = WebSocketSessionRegistry.getSessionsByServiceId(kefuId);

    // 2. 向每个连接发送强制下线消息
    for (SessionHolder holder : sessions) {
        sendEnvelope(holder.getSession(), "force_logout",
            Map.of("reason", "admin_kick", "message", "您已被管理员强制下线"));
        closeSilently(holder.getSession());
    }

    // 3. 更新数据库
    updateKefuOnlineStatus(kefuId, 0);
}
```

```javascript
// 前端：监听force_logout消息
ws.$on('force_logout', (data) => {
    this.$Modal.warning({
        title: '强制下线',
        content: data.message,
        onOk: () => {
            // 执行退出流程
            this.cleanupAndRedirect();
        }
    });
});
```

## 相关文档

- [客服端Token自动过期检查功能说明.md](./客服端Token自动过期检查功能说明.md)
- [客服退出登录与Token清理增强.md](./客服退出登录与Token清理增强.md)
- [客服长时间在线与Token过期分析.md](./客服长时间在线与Token过期分析.md)

## 修改日期
2025-10-17

## 修改人员
Claude Code
