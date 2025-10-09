package io.renren.modules.ai.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * AI聊天消息
 *
 * @author Mark sunlightcs@gmail.com
 */

@Data
@TableName("ai_chat_message")
public class AiChatMessageEntity {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 回答内容
     */
    private String content;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 模型标识
     */
    private String model;

    /**
     * 对话ID
     */
    private Long conversationId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;
}