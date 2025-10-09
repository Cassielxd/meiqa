package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 管理员表 eb_system_admin
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_system_admin")
public class SystemAdminEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 管理员ID
     */
    @TableId
    private Integer id;

    /**
     * 账号
     */
    private String account;

    /**
     * 头像
     */
    private String headPic;

    /**
     * 密码
     */
    private String pwd;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色IDs（逗号分隔）
     */
    private String roles;

    /**
     * 最后登录IP
     */
    private String lastIp;

    /**
     * 最后登录时间（时间戳）
     */
    private Integer lastTime;

    /**
     * 添加时间（时间戳）
     */
    private Integer addTime;

    /**
     * 登录次数
     */
    private Integer loginCount;

    /**
     * 管理员等级
     */
    private Integer level;

    /**
     * 状态：0=禁用，1=启用
     */
    private Integer status;

    /**
     * 是否删除：0=未删除，1=已删除
     */
    private Integer isDel;
}
