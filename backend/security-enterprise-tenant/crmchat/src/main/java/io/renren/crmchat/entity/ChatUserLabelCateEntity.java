package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户标签分类表
 * 参考 PHP: ChatUserLabelCateServices extends CategoryServices
 * PHP使用 eb_category 表，type=0 表示用户标签分类
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_category")
public class ChatUserLabelCateEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    /**
     * 父级ID
     */
    private Integer pid;

    /**
     * 拥有者ID（0表示公共）
     */
    private Integer ownerId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 类型：0-用户标签分类, 1-商品分类, 2-投诉分类等
     */
    private Integer type;

    /**
     * 其他扩展信息（JSON）
     */
    private String other;

    /**
     * 添加时间
     */
    private Integer addTime;
}
