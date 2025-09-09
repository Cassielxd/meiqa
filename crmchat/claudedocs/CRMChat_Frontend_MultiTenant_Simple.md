# CRMChat前端多租户简化方案

## 核心理念

**客户端不需要复杂的租户管理！** 只需要在API请求和WebSocket消息中自动添加租户参数即可。

---

## 1. 租户参数获取

```javascript
/**
 * 获取当前租户参数
 * 简单的函数，无需复杂的管理器
 */
function getCurrentTenant() {
    // 优先级：URL参数 > 全局配置 > 本地存储 > 子域名
    
    // 1. URL参数: ?tenant=abc 或 ?t=abc
    const urlParams = new URLSearchParams(window.location.search);
    const urlTenant = urlParams.get('tenant') || urlParams.get('t');
    if (urlTenant) return urlTenant;
    
    // 2. 全局配置
    const configTenant = window.CRMChatConfig?.tenant;
    if (configTenant) return configTenant;
    
    // 3. 本地存储
    const storageTenant = localStorage.getItem('crm_current_tenant');
    if (storageTenant) return storageTenant;
    
    // 4. 子域名: abc.crm.com -> abc
    const hostname = window.location.hostname;
    const subdomain = hostname.split('.')[0];
    if (subdomain && !['www', 'admin', 'api'].includes(subdomain)) {
        return subdomain;
    }
    
    return null; // 无租户或默认租户
}

/**
 * 设置租户（可选）
 */
function setCurrentTenant(tenant) {
    if (tenant) {
        localStorage.setItem('crm_current_tenant', tenant);
    } else {
        localStorage.removeItem('crm_current_tenant');
    }
}
```

## 2. WebSocket增强

```javascript
/**
 * 在页面加载时执行，增强WebSocket自动添加租户参数
 */
(function enhanceWebSocket() {
    const originalSend = WebSocket.prototype.send;
    
    WebSocket.prototype.send = function(data) {
        try {
            if (typeof data === 'string') {
                let messageData = JSON.parse(data);
                const tenant = getCurrentTenant();
                
                // 自动添加租户参数
                if (tenant && typeof messageData === 'object') {
                    messageData.tenant = tenant;
                    data = JSON.stringify(messageData);
                }
            }
        } catch (e) {
            // JSON解析失败，使用原始数据
        }
        
        return originalSend.call(this, data);
    };
})();
```

## 3. API请求增强

```javascript
/**
 * 增强fetch请求，自动添加租户头部
 */
(function enhanceAPI() {
    const originalFetch = window.fetch;
    
    window.fetch = function(url, options = {}) {
        const tenant = getCurrentTenant();
        
        if (tenant) {
            options.headers = options.headers || {};
            // 方式1：添加到请求头
            options.headers['X-Tenant-Code'] = tenant;
            
            // 方式2：添加到URL参数（可选）
            if (typeof url === 'string' && !url.includes('tenant=')) {
                const separator = url.includes('?') ? '&' : '?';
                url = url + separator + 'tenant=' + encodeURIComponent(tenant);
            }
        }
        
        return originalFetch.call(this, url, options);
    };
})();
```

## 4. CRMChat对象兼容扩展

```javascript
/**
 * 扩展现有CRMChat对象，保持100%兼容
 */
(function extendCRMChat() {
    // 保存原有CRMChat对象
    const OriginalCRMChat = window.CRMChat || {};
    
    // 扩展CRMChat
    window.CRMChat = {
        // 保留所有原有功能
        ...OriginalCRMChat,
        
        // 新增租户相关方法（可选使用）
        tenant: {
            getCurrent: getCurrentTenant,
            setCurrent: setCurrentTenant
        },
        
        // WebSocket方法保持不变（已自动增强）
        socket: {
            ...OriginalCRMChat.socket,
            // 原有方法继续工作，已自动添加租户参数
        },
        
        // API方法保持不变（已自动增强）
        api: {
            ...OriginalCRMChat.api,
            // 原有方法继续工作，已自动添加租户头部
        }
    };
})();
```

## 5. 完整集成代码

```javascript
/**
 * CRMChat多租户简化版集成
 * 只需要在页面中引入这个脚本即可
 */
(function() {
    'use strict';
    
    // 1. 租户参数获取
    function getCurrentTenant() {
        const urlParams = new URLSearchParams(window.location.search);
        const urlTenant = urlParams.get('tenant') || urlParams.get('t');
        if (urlTenant) return urlTenant;
        
        const configTenant = window.CRMChatConfig?.tenant;
        if (configTenant) return configTenant;
        
        const storageTenant = localStorage.getItem('crm_current_tenant');
        if (storageTenant) return storageTenant;
        
        const hostname = window.location.hostname;
        const subdomain = hostname.split('.')[0];
        if (subdomain && !['www', 'admin', 'api'].includes(subdomain)) {
            return subdomain;
        }
        
        return null;
    }
    
    function setCurrentTenant(tenant) {
        if (tenant) {
            localStorage.setItem('crm_current_tenant', tenant);
        } else {
            localStorage.removeItem('crm_current_tenant');
        }
    }
    
    // 2. 增强WebSocket
    const originalSend = WebSocket.prototype.send;
    WebSocket.prototype.send = function(data) {
        try {
            if (typeof data === 'string') {
                let messageData = JSON.parse(data);
                const tenant = getCurrentTenant();
                
                if (tenant && typeof messageData === 'object') {
                    messageData.tenant = tenant;
                    data = JSON.stringify(messageData);
                }
            }
        } catch (e) {
            // 忽略JSON解析错误
        }
        
        return originalSend.call(this, data);
    };
    
    // 3. 增强API请求
    const originalFetch = window.fetch;
    window.fetch = function(url, options = {}) {
        const tenant = getCurrentTenant();
        
        if (tenant) {
            options.headers = options.headers || {};
            options.headers['X-Tenant-Code'] = tenant;
        }
        
        return originalFetch.call(this, url, options);
    };
    
    // 4. 扩展CRMChat对象
    const OriginalCRMChat = window.CRMChat || {};
    window.CRMChat = {
        ...OriginalCRMChat,
        
        // 新增租户方法
        tenant: {
            getCurrent: getCurrentTenant,
            setCurrent: setCurrentTenant
        },
        
        // 获取多租户状态
        getMultiTenantStatus() {
            return {
                enabled: true,
                currentTenant: getCurrentTenant(),
                version: '1.0.0-simple'
            };
        }
    };
    
    console.log('[CRMChat] 多租户简化版已加载');
})();
```

## 6. 使用方法

### 引入脚本
```html
<!-- 在现有脚本之前加载 -->
<script src="/js/crm-chat-multitenant-simple.js"></script>

<!-- 然后加载原有脚本 -->
<script src="/customerServer.js"></script>
```

### 现有代码无需修改
```javascript
// 原有代码继续工作，自动支持多租户
CRMChat.socket.connect();
CRMChat.socket.send({ type: 'message', content: 'Hello' });
CRMChat.api.get('/services');

// 可选：使用新增的租户方法
console.log('当前租户:', CRMChat.tenant.getCurrent());
CRMChat.tenant.setCurrent('company_abc');
```

### URL访问示例
```
# 通过URL参数指定租户
https://crm.example.com?tenant=company_abc

# 通过子域名指定租户  
https://company_abc.crm.example.com

# 通过全局配置指定租户
<script>
window.CRMChatConfig = { tenant: 'company_abc' };
</script>
```

## 7. 优势

✅ **极简设计** - 只有不到100行代码
✅ **零侵入** - 现有代码无需任何修改
✅ **自动化** - WebSocket和API请求自动添加租户参数
✅ **灵活性** - 支持多种租户识别方式
✅ **兼容性** - 100%向后兼容
✅ **性能好** - 几乎无性能开销
✅ **易调试** - 逻辑简单清晰

这就是多租户前端的完整解决方案！简单、高效、实用。