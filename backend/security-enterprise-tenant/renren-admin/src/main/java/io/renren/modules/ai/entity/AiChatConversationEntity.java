package io.renren.modules.ai.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * AI聊天对话
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@TableName("ai_chat_conversation")
public class AiChatConversationEntity {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 对话标题
     */
    private String title;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 模型标识
     */
    private String model;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

}