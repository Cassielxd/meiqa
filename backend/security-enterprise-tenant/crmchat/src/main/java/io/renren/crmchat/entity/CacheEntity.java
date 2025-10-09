package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 缓存表 eb_cache
 * 注意：此表没有自增ID，使用key作为主键
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_cache")
public class CacheEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId  // key作为主键
    @TableField("`key`")  // Escape reserved keyword
    private String key;

    private String result;      // JSON格式存储
    private Integer expireTime; // 过期时间戳，0表示永不过期
    private Integer addTime;    // 添加时间戳
}
