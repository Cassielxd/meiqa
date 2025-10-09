package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 用户投诉表
 * 对应PHP: chat_complain表
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_chat_complain")
public class ChatComplainEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Integer id;

    /**
     * 投诉内容
     */
    private String content;

    /**
     * 用户表ID（被投诉的用户）
     */
    private Integer userId;

    /**
     * 分类ID（投诉分类，可能是多级，PHP中使用"/"分隔）
     * PHP中: implode('/', cate_id数组)
     * 例如: "1/3" 表示一级分类1下的二级分类3
     */
    private String cateId;

    /**
     * 创建时间
     */
    private Timestamp createTime;
}
