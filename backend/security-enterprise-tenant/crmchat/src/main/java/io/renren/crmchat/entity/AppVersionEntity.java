package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * APP版本管理表
 * PHP Reference: eb_app_version
 */
@Data
@TableName("eb_app_version")
public class AppVersionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;
    private String name;
    private String verisonsNum;
    private String url;
    private String info;
    private Date createTime;
    private Date updateTime;
    private Date deleteTime;
}
