package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统日志实体
 * PHP Reference: /app/models/system/log/SystemLog.php
 *
 * 表: eb_system_log
 * 用途: 记录管理员操作日志
 *
 * 注意：JSON 返回字段使用下划线格式（snake_case）以匹配 PHP 前端期望
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_system_log")
public class SystemLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     * JSON: id
     */
    @TableId
    @JsonProperty("id")
    private Long id;

    /**
     * 管理员ID
     * JSON: admin_id
     */
    @JsonProperty("admin_id")
    private Integer adminId;

    /**
     * 管理员账号
     * JSON: admin_name
     */
    @JsonProperty("admin_name")
    private String adminName;

    /**
     * 访问方式/模块 (admin/api/etc)
     * JSON: method
     */
    @JsonProperty("method")
    private String method;

    /**
     * 访问路径 (API路径)
     * JSON: path
     */
    @JsonProperty("path")
    private String path;

    /**
     * 访问页面描述
     * JSON: page
     */
    @JsonProperty("page")
    private String page;

    /**
     * 访问IP地址
     * JSON: ip
     */
    @JsonProperty("ip")
    private String ip;

    /**
     * 操作类型 (登录/添加/删除/修改等)
     * JSON: type
     */
    @JsonProperty("type")
    private String type;

    /**
     * 添加时间 (Unix timestamp秒数)
     * JSON: add_time
     */
    @JsonProperty("add_time")
    private Long addTime;
}
