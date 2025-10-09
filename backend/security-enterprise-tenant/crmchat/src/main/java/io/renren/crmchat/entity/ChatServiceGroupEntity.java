package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 客服分组表
 * 参考 PHP: eb_chat_service_group
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_chat_service_group")
public class ChatServiceGroupEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 排序（数字越大越靠前）
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 更新时间
     */
    private Timestamp updateTime;

    /**
     * 所属租户的 appid（用于多租户隔离）
     */
    private String appid;
}
