package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应用管理表
 * 参考 PHP: eb_application
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_application")
public class ApplicationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    /**
     * 应用ID（唯一标识）
     */
    private String appid;

    /**
     * 应用图标
     */
    private String icon;

    /**
     * 应用名称
     */
    private String name;

    /**
     * 应用介绍
     */
    private String introduce;

    /**
     * 随机数
     */
    private Integer rand;

    /**
     * 时间戳
     */
    private Integer timestamp;

    /**
     * 应用密钥
     */
    private String appSecret;

    /**
     * Token
     */
    private String token;

    /**
     * Token MD5
     */
    private String tokenMd5;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private Integer isDelete;

    /*
    * 应用域名
    */
    private String domain;
}
