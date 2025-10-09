package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 对话记录表
 * 参考 PHP: chat_service_dialogue_record
 *
 * 消息类型:
 * 1 = 文字
 * 2 = 表情
 * 3 = 图片
 * 4 = 语音
 * 5 = 商品链接
 * 6 = 订单类型
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_chat_service_dialogue_record")
public class ChatServiceDialogueRecordEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    /**
     * 发送者用户ID
     */
    private Integer userId;

    /**
     * 接收者用户ID
     */
    private Integer toUserId;

    /**
     * 消息内容
     */
    private String msn;

    /**
     * 消息类型：1-文字，2-表情，3-图片，4-语音，5-商品链接，6-订单类型
     */
    private Integer type;

    /**
     * 其他数据（JSON格式）
     */
    private String other;

    /**
     * 添加时间（时间戳）
     */
    private Integer addTime;

    /**
     * 租户appid（多租户隔离）
     */
    private String appid;

    /**
     * 是否游客：0-否，1-是
     */
    private Integer isTourist;

    /**
     * 消息类型：1-文字，2-表情，3-图片，4-语音
     * 注意：type字段在表中表示"是否已读"，msn_type才是消息类型
     */
    private Integer msnType;

    /**
     * 是否提醒过：0-否，1-是
     */
    private Integer remind;

    /**
     * GUID（唯一值）
     */
    private String guid;

    /**
     * 商户ID
     */
    private Integer merId;
}
