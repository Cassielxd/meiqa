package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 租户表
 * 参考 PHP: eb_tenants
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_tenants")
public class TenantsEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    /**
     * 租户唯一标识（appid，用于多租户隔离）
     */
    private String appid;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 租户代码
     */
    private String tenantCode;

    /**
     * 租户管理员账号
     */
    private String account;

    /**
     * 租户管理员密码（BCrypt加密）
     */
    private String pwd;

    /**
     * 租户域名
     */
    private String domain;

    /**
     * 租户Logo
     */
    private String logo;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 联系人邮箱
     */
    private String contactEmail;

    /**
     * 状态：0-待审核，1-已批准，2-已拒绝，3-已禁用
     */
    private Integer status;

    /**
     * 最大用户数
     */
    private Integer maxUsers;

    /**
     * 最大客服数
     */
    private Integer maxServices;

    /**
     * 过期时间
     */
    private Timestamp expireAt;

    /**
     * 创建时间
     */
    private Timestamp createdAt;

    /**
     * 更新时间
     */
    private Timestamp updatedAt;
}
