package io.renren.modules.flow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 工作流表单
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "工作流表单")
public class BpmFormDTO implements Serializable {
    @Schema(description = "id")
    private Long id;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "表单内容")
    private String content;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

}