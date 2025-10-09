/**
 * Copyright (c) 2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.flow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

import java.util.Date;

/**
 * 历史细节
 *
 * @author Jone
 */
@Data
@Tag(name = "历史细节")
public class HistoryDetailDTO {
    @Schema(description = "ID")
    private String id;

    @Schema(description = "环节名称")
    private String activityName;

    @Schema(description = "环节类型")
    private String activityType;

    @Schema(description = "流程定义ID")
    private String processDefinitionId;

    @Schema(description = "实例ID")
    private String processInstanceId;

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "执行ID")
    private String executionId;

    @Schema(description = "受理人")
    private String assignee;

    @Schema(description = "受理人姓名")
    private String assigneeName;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date endTime;

    @Schema(description = "时长（秒）")
    private String durationInSeconds;

    @Schema(description = "审批意见")
    private String comment;
}
