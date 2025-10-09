package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 附件管理表
 * 参考 PHP: eb_system_attachment
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_system_attachment")
public class SystemAttachmentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 附件ID
     */
    @TableId
    private Integer attId;

    /**
     * 附件名称（文件名）
     */
    private String name;

    /**
     * 附件路径
     */
    private String attDir;

    /**
     * 压缩图片路径（缩略图）
     */
    private String sattDir;

    /**
     * 附件大小
     */
    private String attSize;

    /**
     * 附件类型（MIME类型）
     */
    private String attType;

    /**
     * 图片类型：0-本地，1-OSS，2-七牛云，3-COS
     */
    private Integer imageType;

    /**
     * 上传时间（Unix时间戳）
     */
    private Integer time;

    /**
     * 模块类型：0-系统，1-附件，2-其他
     */
    private Integer moduleType;

    /**
     * 分类ID（关联 eb_system_attachment_category）
     */
    private Integer pid;

    /**
     * 真实文件名（用户上传的原始文件名）
     */
    private String realName;
}
