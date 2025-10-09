package io.renren.crmchat.security;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 用户上下文 - 使用ThreadLocal存储当前用户信息
 *
 * @author CRMChat Team
 */
public class UserContext {

    private static final TransmittableThreadLocal<CrmChatUser> userHolder = new TransmittableThreadLocal<>();

    /**
     * 设置当前用户
     */
    public static void setUser(CrmChatUser user) {
        userHolder.set(user);
    }

    /**
     * 获取当前用户
     */
    public static CrmChatUser getUser() {
        return userHolder.get();
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        CrmChatUser user = getUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        CrmChatUser user = getUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 获取当前租户appid
     */
    public static String getAppid() {
        CrmChatUser user = getUser();
        return user != null ? user.getAppid() : null;
    }

    /**
     * 获取当前部门ID
     */
    public static Long getDeptId() {
        CrmChatUser user = getUser();
        return user != null ? user.getDeptId() : null;
    }

    /**
     * 获取当前用户角色（逗号分隔的角色ID）
     */
    public static String getRoles() {
        CrmChatUser user = getUser();
        return user != null ? user.getRoles() : null;
    }

    /**
     * 获取当前用户等级
     */
    public static Integer getLevel() {
        CrmChatUser user = getUser();
        return user != null ? user.getLevel() : null;
    }

    /**
     * 清除当前用户
     */
    public static void clear() {
        userHolder.remove();
    }
}
