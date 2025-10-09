package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 快捷回复表
 * 参考 PHP: chat_service_speechcraft
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_chat_service_speechcraft")
public class ChatServiceSpeechcraftEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    /**
     * 标题
     */
    private String title;

    /**
     * 话术内容（回复内容）
     */
    private String message;

    /**
     * 分类ID
     */
    private Integer cateId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 添加时间（时间戳）
     */
    private Integer addTime;

    /**
     * 客服ID：0-系统话术
     */
    private Integer kefuId;

    /**
     * 租户appid（多租户隔离）
     */
    private String appid;
}
