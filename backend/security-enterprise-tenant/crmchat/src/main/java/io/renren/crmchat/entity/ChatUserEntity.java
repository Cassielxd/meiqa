package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 客户/用户表
 * 参考 PHP: eb_chat_user
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_chat_user")
public class ChatUserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    /**
     * 用户UID（用于和业务系统关联）
     */
    private Integer uid;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * openid（微信公众号/小程序用户唯一标识）
     */
    private String openid;

    /**
     * 分组ID
     */
    private Integer groupId;

    /**
     * 租户appid（多租户隔离）
     */
    private String appid;

    /**
     * 最后访问IP
     */
    @TableField("last_ip")
    private String lastIp;

    /**
     * 用户类型：0-PC，1-微信公众号，2-小程序，3-H5，4-APP
     */
    private Integer type;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer sex;

    /**
     * 是否游客：0-否，1-是
     */
    private Integer isTourist;

    /**
     * 是否删除
     */
    @TableField("is_delete")
    private Integer isDelete;

    /**
     * 是否客服
     */
    @TableField("is_kefu")
    private Integer isKefu;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 备注昵称
     */
    private String remarkNickname;

    /**
     * 在线状态：0-离线，1-在线
     */
    private Integer online;

    /**
     * 客户端版本号
     */
    private String version;

    /**
     * 国家
     */
    private String country;

    /**
     * 省份/地区
     */
    private String region;

    /**
     * 城市
     */
    private String city;

    /**
     * 运营商/ISP
     */
    private String isp;

    /**
     * 完整地理信息JSON
     */
    @TableField("geo_info")
    private String geoInfo;

    /**
     * 地理信息更新时间（Unix时间戳）
     */
    @TableField("geo_updated_time")
    private Integer geoUpdatedTime;

    /**
     * 来源页面（HTTP Referer 头）
     */
    @TableField("referer")
    private String referer;

    /**
     * Referer 更新时间（Unix时间戳）
     */
    @TableField("referer_updated_time")
    private Integer refererUpdatedTime;

    /**
     * 当前请求的完整URL（包含参数）
     */
    @TableField("request_url")
    private String requestUrl;

    /**
     * Request URL 更新时间（Unix时间戳）
     */
    @TableField("request_url_updated_time")
    private Integer requestUrlUpdatedTime;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updateTime;
}
