package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 客服表
 * 参考 PHP: eb_chat_service
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_chat_service")
public class ChatServiceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    /**
     * 客服所属租户的 appid（用于多租户隔离）
     */
    private String appid;

    /**
     * 商户ID
     */
    private Integer merId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 客服组ID
     */
    private Integer groupId;

    /**
     * 在线状态：0-离线，1-在线
     */
    private Integer online;

    /**
     * 客服账号
     */
    private String account;

    /**
     * 客服密码（BCrypt加密）
     */
    private String password;

    /**
     * 客服头像
     */
    private String avatar;

    /**
     * 客服昵称
     */
    private String nickname;

    /**
     * 客服电话
     */
    private String phone;

    /**
     * 添加时间（Unix时间戳）
     */
    private Integer addTime;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 通知开关：0-关闭，1-开启
     */
    private Integer notify;

    /**
     * 客户开关：0-关闭，1-开启
     */
    private Integer customer;

    /**
     * 唯一ID
     */
    private String uniqid;

    /**
     * 是否APP：0-否，1-是
     */
    private Integer isApp;

    /**
     * 是否后台运行：0-否，1-是
     */
    private Integer isBackstage;

    /**
     * 自动回复：0-关闭，1-开启
     */
    private Integer autoReply;

    /**
     * 欢迎语
     */
    private String welcomeWords;

    /**
     * 更新时间（Unix时间戳）
     */
    private Integer updateTime;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 客户端ID（WebSocket连接标识）
     */
    private String clientId;
}
