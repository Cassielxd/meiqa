package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统角色实体
 * PHP Reference: /app/models/system/SystemRole.php
 *
 * 表: eb_system_role
 * 用途: 管理员角色/身份管理
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_system_role")
public class SystemRoleEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @TableId
    private Integer id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 权限规则IDs（逗号分隔）
     */
    private String rules;

    /**
     * 角色级别
     */
    private Integer level;

    /**

 * 状态（0-禁用，1-启用）
     */
    private Integer status;
}
