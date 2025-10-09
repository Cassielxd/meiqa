package io.renren.crmchat.security;

import io.renren.crmchat.exception.CrmChatException;

import java.util.Objects;

/**
 * Helper to enforce tenant ownership rules after fetching an entity.
 */
public final class TenantGuard {

    private TenantGuard() {
    }

    /**
     * Ensure that the given resource belongs to the current tenant when isolation is enabled.
     * Super administrators or contexts without tenant isolation are allowed to pass through.
     *
     * @param resourceAppid appid stored on the resource (may be null)
     * @param notFoundMessage message thrown when mismatch occurs
     */
    public static void ensureOwnedByCurrentTenant(String resourceAppid, String notFoundMessage) {
        if (!TenantContextUtils.isTenantIsolationEnabled()) {
            return;
        }
        String current = TenantContextUtils.currentAppid();
        if (resourceAppid == null || !Objects.equals(resourceAppid, current)) {
            throw new CrmChatException(notFoundMessage);
        }
    }
}
