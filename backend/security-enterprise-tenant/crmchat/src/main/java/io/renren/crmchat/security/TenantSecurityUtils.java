package io.renren.crmchat.security;

import io.renren.crmchat.exception.CrmChatException;

/**
 * 租户安全上下文工具类
 *
 * 负责从当前线程的用户上下文中提取租户身份信息。
 */
public final class TenantSecurityUtils {

    private TenantSecurityUtils() {
    }

    /**
     * 获取当前租户的 appid，若缺失则抛出业务异常。
     *
     * @return 当前租户的 appid
     */
    public static String requireAppid() {
        String appid = UserContext.getAppid();
        if (appid == null || appid.trim().isEmpty()) {
            throw new CrmChatException("Invalid tenant authentication, please login again");
        }
        return appid;
    }

    /**
     * 如果调用方未显式传递 appid，则回退到当前上下文的 appid。
     *
     * @param appid 调用方传入的 appid，可为空
     * @return 非空 appid
     */
    public static String resolveAppid(String appid) {
        if (appid == null || appid.trim().isEmpty()) {
            return requireAppid();
        }
        return appid;
    }

    /**
     * 获取当前租户的 ID（token 中的 userId），若缺失则抛出业务异常。
     *
     * @return 当前租户 ID
     */
    public static Integer requireTenantId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new CrmChatException("Invalid tenant authentication, please login again");
        }
        return userId.intValue();
    }
}
