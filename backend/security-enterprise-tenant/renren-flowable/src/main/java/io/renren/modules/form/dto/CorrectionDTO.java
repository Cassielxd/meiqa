/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */
package io.renren.modules.form.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 转正申请
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "转正申请")
public class CorrectionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;

    @Schema(description = "申请岗位")
    private String applyPost;

    @Schema(description = "入职日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date entryDate;

    @Schema(description = "转正日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date correctionDate;

    @Schema(description = "工作内容")
    private String workContent;

    @Schema(description = "工作成绩")
    private String achievement;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

    @Schema(description = "实例ID")
    private String instanceId;

    @Schema(description = "流程定义ID")
    private String processDefinitionId;

}