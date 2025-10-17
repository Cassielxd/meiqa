package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 聊天记录表（客服与用户会话记录）
 * 参考 PHP: eb_chat_service_record
 *
 * 用途: 记录客服与用户之间的最新会话状态，用于会话列表展示
 * 区别: chat_service_dialogue_record 是详细的聊天消息记录
 *       chat_service_record 是会话摘要记录
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_chat_service_record")
public class ChatServiceRecordEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 租户APPID
     */
    private String appid;

    /**
     * 发送人的user_id
     */
    private Integer userId;

    /**
     * 接收人的user_id
     */
    private Integer toUserId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 是否游客：0-否，1-是
     */
    private Integer isTourist;

    /**
     * 是否在线：0-离线，1-在线
     */
    private Integer online;

    /**
     * 来源类型：0-pc，1-微信，2-小程序，3-H5
     */
    private Integer type;

    /**
     * 添加时间（Unix时间戳）
     */
    private Integer addTime;

    /**
     * 更新时间（Unix时间戳）
     */
    private Integer updateTime;

    /**
     * 未读消息条数（注意PHP表中字段名拼写错误：mssage_num）
     */
    @TableField("mssage_num")
    private Integer num;

    /**
     * 软删除时间（Unix时间戳）
     */
    private Integer deleteTime;

    /**
     * 最新消息内容（text类型）
     * PHP中字段名: message
     */
    @TableField("message")
    private String msn;

    /**
     * 消息类型：0-文本，1-图片，2-语音，3-视频，4-文件，5-商品，6-订单
     */
    private Integer messageType;
}
