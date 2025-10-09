package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户标签表
 * 参考 PHP: chat_user_label
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_chat_user_label")
public class ChatUserLabelEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    /**
     * 标签名称
     */
    private String label;

    /**
     * 分类ID
     */
    private Integer cateId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 用户ID（0=系统标签）
     */
    private Integer userId;

    /**
     * 租户appid（多租户隔离）
     */
    private String appid;
}
