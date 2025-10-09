package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户标签关联表
 * 参考 PHP: eb_chat_user_label_assist
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_chat_user_label_assist")
public class ChatUserLabelAssistEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 标签ID
     */
    private Integer labelId;

    /**
     * 租户appid（多租户隔离）
     */
    private String appid;
}
