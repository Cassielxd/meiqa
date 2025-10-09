package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * 二维码表
 * PHP Reference: eb_qrcode
 * 用于管理随机客服二维码，支持将访客分配给指定的客服列表
 *
 * @author CRMChat Team
 */
@Data
@TableName(value = "eb_qrcode", autoResultMap = true)
public class QrcodeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Integer id;

    /**
     * 二维码名称
     */
    private String name;

    /**
     * 二维码地址
     */
    private String url;

    /**
     * 客服用户IDs
     * PHP中存储为逗号分隔的字符串，Java中使用JSON数组存储
     * 示例: [1,2,3]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Integer> userIds;

    /**
     * 所属租户的 appid（用于多租户隔离）
     */
    private String appid;

    /**
     * 排序（数字越大越靠前）
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 客服账号列表（用于前端展示，非数据库字段）
     * PHP Reference: QrcodeServices.php::getList() 中的 user_account
     */
    @TableField(exist = false)
    private List<String> userAccount;
}
