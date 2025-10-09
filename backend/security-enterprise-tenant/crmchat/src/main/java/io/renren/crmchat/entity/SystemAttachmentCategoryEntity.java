package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

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
}
