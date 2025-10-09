package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 辅助表（客服转接等辅助信息）。
 * 对应表：eb_auxiliary
 */
@Data
@TableName("eb_auxiliary")
public class AuxiliaryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    /**
     * 绑定ID（chat_user.id）
     */
    private Integer bindingId;

    /**
     * 租户appid
     */
    private String appid;

    /**
     * 关联ID（通常为客服user_id）
     */
    private Integer relationId;

    /**
     * 类型：0=客服转接辅助
     */
    private Integer type;

    /**
     * 其他扩展信息（JSON）
     */
    private String other;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 更新时间（Unix时间戳）
     */
    private Integer updateTime;

    /**
     * 添加时间（Unix时间戳）
     */
    private Integer addTime;
}
