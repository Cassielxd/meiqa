package io.renren.crmchat.security;

import lombok.Data;

import java.io.Serializable;

/**
 * 当前登录用户信息
 *
 * @author CRMChat Team
 */
@Data
public class CrmChatUser implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 租户appid
     */
    private String appid;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 用户类型: admin, tenant, kefu, mobile
     */
    private String userType;

    /**
     * Token
     */
    private String token;

    /**
     * 角色IDs（逗号分隔）- 用于权限控制
     */
    private String roles;

    /**
     * 管理员等级 - 用于权限控制
     */
    private Integer level;
}
