package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 快捷回复分类表
 * 参考 PHP: chat_service_speechcraft_cate
 *
 * 注意事项：
 * - 数据库字段使用蛇形命名（add_time, owner_id）
 * - MyBatis-Plus自动映射驼峰到蛇形
 * - JSON序列化使用蛇形命名以保持与PHP兼容
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_chat_service_speechcraft_cate")
public class ChatServiceSpeechcraftCateEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 类型：0-其他，1-快捷回复分类
     */
    private Integer type;

    /**
     * 添加时间（时间戳）
     * 数据库字段: add_time
     */
    @TableField("add_time")
    @JsonProperty("add_time")
    private Integer addTime;

    /**
     * 所属者ID：0-系统分类
     * 数据库字段: owner_id
     */
    @TableField("owner_id")
    @JsonProperty("owner_id")
    private Integer ownerId;

    /**
     * 租户appid（多租户隔离）
     */
    private String appid;
}
