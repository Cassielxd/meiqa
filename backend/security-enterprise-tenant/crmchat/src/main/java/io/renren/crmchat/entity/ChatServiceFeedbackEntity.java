package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 服务反馈/留言表
 * 参考 PHP: eb_chat_service_feedback
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_chat_service_feedback")
public class ChatServiceFeedbackEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 联系人姓名
     */
    private String relaName;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 内容
     */
    private String content;

    /**
     * 备注（处理说明）
     */
    private String make;

    /**
     * 状态：0-未处理，1-已处理
     */
    private Integer status;

    /**
     * 添加时间（时间戳）
     */
    private Integer addTime;

    /**
     * 租户appid（多租户隔离）
     */
    private String appid;
}
