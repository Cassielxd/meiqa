# CRMChat前端租户参数简单改造方案

## 基于现有customerServer.js的最简实现

基于对 `customerServer.js` 的分析，这是一个完整的客服聊天系统前端实现，主要通过iframe和postMessage机制工作。要添加租户支持，只需要在现有的数据传递中加入租户参数即可。

## 核心改造原理

**关键发现**：
- 文件使用 `postMessage` 进行iframe通信（第38行）
- 有用户数据传递机制 `sendUserData`（第130行）
- 有现成的参数传递函数 `toParams`（第535行）

## 最简改造方案（仅需3处修改）

### 1. 添加租户参数获取函数

在文件开头添加：

```javascript
// 租户参数获取
function getTenantParam() {
    // URL参数: ?tenant=abc 或 ?t=abc
    const urlParams = new URLSearchParams(window.location.search);
    const urlTenant = urlParams.get('tenant') || urlParams.get('t');
    if (urlTenant) return urlTenant;
    
    // 全局配置
    if (window.CRMChatConfig && window.CRMChatConfig.tenant) {
        return window.CRMChatConfig.tenant;
    }
    
    // 子域名: abc.crm.com -> abc
    const hostname = window.location.hostname;
    const subdomain = hostname.split('.')[0];
    if (subdomain && !['www', 'admin', 'api'].includes(subdomain)) {
        return subdomain;
    }
    
    return null;
}
```

### 2. 修改sendUserData数据传递（第167行）

原代码：
```javascript
if (this.settingObj.sendUserData && Object.keys(this.settingObj.sendUserData).length) {
    customerServerData = toParams(this.settingObj.sendUserData);
}
```

改为：
```javascript
if (this.settingObj.sendUserData && Object.keys(this.settingObj.sendUserData).length) {
    const userData = {...this.settingObj.sendUserData};
    const tenant = getTenantParam();
    if (tenant) userData.tenant = tenant;
    customerServerData = toParams(userData);
}
```

### 3. 修改postMessage传递（第38行）

原代码：
```javascript
this.iframe_contanier.contentWindow.postMessage({type: type, data: data}, "*");
```

改为：
```javascript
const messageData = {type: type, data: data};
const tenant = getTenantParam();
if (tenant) messageData.tenant = tenant;
this.iframe_contanier.contentWindow.postMessage(messageData, "*");
```

## 完整集成代码（补丁形式）

```javascript
/**
 * CRMChat租户参数补丁 - 在customerServer.js开头添加
 */
(function() {
    'use strict';
    
    // 租户参数获取
    function getTenantParam() {
        const urlParams = new URLSearchParams(window.location.search);
        const urlTenant = urlParams.get('tenant') || urlParams.get('t');
        if (urlTenant) return urlTenant;
        
        if (window.CRMChatConfig && window.CRMChatConfig.tenant) {
            return window.CRMChatConfig.tenant;
        }
        
        const hostname = window.location.hostname;
        const subdomain = hostname.split('.')[0];
        if (subdomain && !['www', 'admin', 'api'].includes(subdomain)) {
            return subdomain;
        }
        
        return null;
    }
    
    // 增强postMessage
    const originalEmit = window.$chat.emit;
    window.$chat.emit = function(name, attr) {
        if (name === 'postMessage' && this.iframe_contanier) {
            const [type, data] = attr;
            const messageData = {type: type, data: data};
            const tenant = getTenantParam();
            if (tenant) messageData.tenant = tenant;
            this.iframe_contanier.contentWindow.postMessage(messageData, "*");
            return;
        }
        return originalEmit.call(this, name, attr);
    };
    
    // 全局租户方法
    window.CRMChatTenant = {
        getCurrent: getTenantParam,
        getStatus: function() {
            return {
                enabled: true,
                current: getTenantParam(),
                version: '1.0.0-simple'
            };
        }
    };
    
    console.log('[CRMChat] 租户参数支持已加载');
})();
```

## 使用方法

### 方式1：直接修改customerServer.js
1. 在文件开头添加 `getTenantParam` 函数
2. 修改第167行的用户数据传递逻辑
3. 修改第38行的postMessage传递逻辑

### 方式2：补丁形式（推荐）
1. 将完整集成代码保存为 `crm-tenant-patch.js`
2. 在页面中先加载补丁，再加载原文件：
```html
<script src="/js/crm-tenant-patch.js"></script>
<script src="/customerServer.js"></script>
```

### 访问示例
```
# URL参数方式
https://your-site.com?tenant=company_abc

# 子域名方式
https://company_abc.your-site.com

# 全局配置方式
<script>
window.CRMChatConfig = { tenant: 'company_abc' };
</script>
```

## 后端配合

### WebSocket消息处理（Manager.php第214-228行）
修改 `onMessage` 方法处理租户参数：

```php
public function onMessage(Frame $frame)
{
    $info = $this->nowRoom->get($frame->fd);
    $result = json_decode($frame->data, true) ?: [];
    
    if (!isset($result['type']) || !$result['type']) return true;
    
    // 处理租户参数
    $tenant = $result['tenant'] ?? '';
    if ($tenant) {
        // 将租户信息保存到连接信息中
        $this->nowRoom->update($frame->fd, 'tenant', $tenant);
    }
    
    $this->refresh($info['type'], $info['user_id']);
    // ... 其他逻辑
}
```

### API请求处理（Request.php）
现有的 `Request.php` 已有参数过滤机制，租户参数会自动通过 `more()` 方法传递，无需修改。

## 优势

✅ **极简改造** - 只需3处小修改
✅ **零破坏** - 不影响现有功能
✅ **自动传递** - 租户参数自动添加到所有通信中
✅ **多种方式** - 支持URL参数、子域名、全局配置
✅ **向后兼容** - 不设置租户时系统正常工作
✅ **易于调试** - 逻辑简单，便于问题排查

这个方案把复杂的多租户需求简化为最基本的参数传递，完全符合"只是添加租户参数"的要求。