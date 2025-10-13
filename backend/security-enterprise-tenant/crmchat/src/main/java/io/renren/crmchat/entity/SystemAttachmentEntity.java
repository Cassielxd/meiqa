package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 附件管理表
 * 参考 PHP: eb_system_attachment
 *
 * 注意：JSON 返回字段使用下划线格式（snake_case）以匹配 PHP 前端期望
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_system_attachment")
public class SystemAttachmentEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 附件ID
     * JSON: att_id
     */
    @TableId
    @JsonProperty("att_id")
    private Integer attId;

    /**
     * 附件名称（文件名）
     * JSON: name
     */
    @JsonProperty("name")
    private String name;

    /**
     * 附件路径
     * JSON: att_dir
     */
    @JsonProperty("att_dir")
    private String attDir;

    /**
     * 压缩图片路径（缩略图）
     * JSON: satt_dir
     */
    @JsonProperty("satt_dir")
    private String sattDir;

    /**
     * 附件大小
     * JSON: att_size
     */
    @JsonProperty("att_size")
    private String attSize;

    /**
     * 附件类型（MIME类型）
     * JSON: att_type
     */
    @JsonProperty("att_type")
    private String attType;

    /**
     * 图片类型：0-本地，1-OSS，2-七牛云，3-COS
     * JSON: image_type
     */
    @JsonProperty("image_type")
    private Integer imageType;

    /**
     * 上传时间（Unix时间戳）
     * JSON: time
     */
    @JsonProperty("time")
    private Integer time;

    /**
     * 模块类型：0-系统，1-附件，2-其他
     * JSON: module_type
     */
    @JsonProperty("module_type")
    private Integer moduleType;

    /**
     * 分类ID（关联 eb_system_attachment_category）
     * JSON: pid
     */
    @JsonProperty("pid")
    private Integer pid;

    /**
     * 真实文件名（用户上传的原始文件名）
     * JSON: real_name
     */
    @JsonProperty("real_name")
    private String realName;
}
