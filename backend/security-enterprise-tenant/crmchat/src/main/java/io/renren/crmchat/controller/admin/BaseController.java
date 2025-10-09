package io.renren.crmchat.controller.admin;

import io.renren.crmchat.security.TenantContextUtils;
import io.renren.crmchat.security.TenantSecurityUtils;

/**
 * 公共后台控制器，封装租户上下文相关的辅助方法。
 */
public abstract class BaseController {

    /**
     * 获取当前租户的 appid，若缺失会抛出业务异常。
     */
    protected static String currentAppid() {
        return TenantSecurityUtils.requireAppid();
    }

    /**
     * 在允许外部指定appid时使用，若开启租户隔离则自动回退到当前租户。
     */
    protected static String resolveAppid(String candidate) {
        String resolved = TenantContextUtils.resolveAppid(candidate);
        if (resolved == null || resolved.trim().isEmpty()) {
            return currentAppid();
        }
        return resolved;
    }

    /**
     * 判断当前登录主体是否为平台超级管理员。
     */
    protected static boolean isSuperAdmin() {
        return TenantContextUtils.isSuperAdmin();
    }
}
