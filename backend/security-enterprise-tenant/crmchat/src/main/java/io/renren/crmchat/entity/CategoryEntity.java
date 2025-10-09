package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 分类表（通用分类，包括用户标签分类、快捷话术分类、投诉分类）
 * 对应PHP: eb_category表
 *
 * type字段说明:
 * 0 = 用户标签分类
 * 1 = 快捷话术分类
 * 2 = 投诉分类
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_category")
public class CategoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Integer id;

    /**
     * 上级ID（0表示顶级分类）
     */
    private Integer pid;

    /**
     * 所属人ID（0表示全部，系统级）
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
     * 分类类型
     * 0 = 用户标签分类
     * 1 = 快捷话术分类
     * 2 = 投诉分类
     */
    private Integer type;

    /**
     * 其他参数（JSON或文本）
     */
    private String other;

    /**
     * 添加时间（时间戳）
     */
    private Integer addTime;
}
