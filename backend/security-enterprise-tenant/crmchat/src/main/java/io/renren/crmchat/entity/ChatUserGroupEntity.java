package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户分组表
 * 参考 PHP: chat_user_group
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_chat_user_group")
public class ChatUserGroupEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    /**
     * 分组名称
     */
    @TableField("group_name")
    @JsonProperty("group_name")
    private String groupName;

    /**
     * 租户appid（多租户隔离）
     */
    private String appid;
}
