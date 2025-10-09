package io.renren.crmchat.security;

import io.renren.crmchat.common.constant.TenantConstants;

/**
 * 租户上下文辅助工具。
 */
public final class TenantContextUtils {

    private TenantContextUtils() {
    }

    /**
     * 当前上下文是否处于租户隔离模式（即存在有效 appid 且不是超级管理员）。
     */
    public static boolean isTenantIsolationEnabled() {
        String appid = currentAppid();
        if (appid == null || appid.trim().isEmpty()) {
            return false;
        }
        return !TenantConstants.SUPER_APPID.equals(appid);
    }

    /**
     * 当前用户是否超级管理员。
     */
    public static boolean isSuperAdmin() {
        String appid = currentAppid();
        return TenantConstants.SUPER_APPID.equals(appid);
    }

    /**
     * 获取当前上下文的 appid。
     */
    public static String currentAppid() {
        return UserContext.getAppid();
    }

    /**
     * Resolve an appid value: prefer the provided one when isolation is disabled, otherwise use the current context.
     */
    public static String resolveAppid(String candidate) {
        if (isTenantIsolationEnabled()) {
            return currentAppid();
        }
        return candidate;
    }
}
