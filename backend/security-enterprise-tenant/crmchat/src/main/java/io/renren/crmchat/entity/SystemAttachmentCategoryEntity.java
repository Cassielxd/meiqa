package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 附件分类表
 * 参考 PHP: eb_system_attachment_category
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_system_attachment_category")
public class SystemAttachmentCategoryEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    /**
     * 父级ID（0表示顶级分类）
     */
    private Integer pid;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类目录（英文名）
     */
    private String enname;

    /**
     * 前端 Tree 组件需要的 title 字段（等于 name）
     * PHP: $menu['title'] = $menu['name']
     */
    @TableField(exist = false)
    private String title;

    /**
     * 子分类列表（树形结构）
     * PHP: $menu['children'] = $this->tidyMenuTier(...)
     */
    @TableField(exist = false)
    private List<SystemAttachmentCategoryEntity> children;

    /**
     * 是否展开（有子节点时为 true）
     * PHP: if ($menu['children']) $menu['expand'] = true;
     */
    @TableField(exist = false)
    @JsonProperty("expand")
    private Boolean expand;
}
