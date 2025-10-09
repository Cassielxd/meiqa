package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统日志实体
 * PHP Reference: /app/models/system/log/SystemLog.php
 *
 * 表: eb_system_log
 * 用途: 记录管理员操作日志
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_system_log")
public class SystemLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId
    private Long id;

    /**
     * 管理员ID
     */
    private Integer adminId;

    /**
     * 管理员账号
     */
    private String adminName;

    /**
     * 访问方式/模块 (admin/api/etc)
     */
    private String method;

    /**
     * 访问路径 (API路径)
     */
    private String path;

    /**
     * 访问页面描述
     */
    private String page;

    /**
     * 访问IP地址
     */
    private String ip;

    /**
     * 操作类型 (登录/添加/删除/修改等)
     */
    private String type;

    /**
     * 添加时间 (Unix timestamp)
     */
    private Long addTime;
}
