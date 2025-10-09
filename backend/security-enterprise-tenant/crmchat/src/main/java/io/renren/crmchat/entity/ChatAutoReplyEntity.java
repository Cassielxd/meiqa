package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 自动回复表
 * 参考 PHP: eb_chat_auto_reply
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_chat_auto_reply")
public class ChatAutoReplyEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    /**
     * 关键字
     */
    private String keyword;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 用户ID：0-系统级别
     */
    private Integer userId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 添加时间（时间戳）
     */
    private Integer addTime;

    /**
     * 租户appid（多租户隔离）
     */
    private String appid;
}
