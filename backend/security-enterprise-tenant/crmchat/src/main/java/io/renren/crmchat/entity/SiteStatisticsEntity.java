package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 站点统计表
 * PHP Reference: eb_site_statistics
 * 用于记录网站访问来源、IP地理位置等统计信息
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_site_statistics")
public class SiteStatisticsEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Integer id;

    /**
     * 网站来源
     */
    private String source;

    /**
     * 来源网址
     */
    private String path;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 地区（城市）
     */
    private String region;

    /**
     * 省份
     */
    private String province;

    /**
     * 浏览器信息
     */
    private String browser;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 更新时间
     */
    private Timestamp updateTime;
}
