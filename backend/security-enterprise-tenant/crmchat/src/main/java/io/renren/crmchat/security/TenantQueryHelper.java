package io.renren.crmchat.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * Helper for applying tenant-aware conditions in queries when the tenant interceptor is bypassed.
 */
public final class TenantQueryHelper {

    private TenantQueryHelper() {
    }

    /**
     * Apply an appid condition to the wrapper only when tenant isolation is disabled and a candidate value is provided.
     */
    public static <T> void applyAppid(QueryWrapper<T> wrapper, String candidateAppid) {
        if (wrapper == null || candidateAppid == null || candidateAppid.trim().isEmpty()) {
            return;
        }
        if (!TenantContextUtils.isTenantIsolationEnabled()) {
            wrapper.eq("appid", candidateAppid);
        }
    }
}
