/**
 * Copyright (c) 2018 人人开源 All rights reserved.
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
import java.util.List;

/**
 * @author Jone
 */
@Data
@Tag(name = "实例")
public class ProcessInstanceDTO {
    @Schema(description = "实例ID")
    private String processInstanceId;

    @Schema(description = "流程定义ID")
    private String processDefinitionId;

    @Schema(description = "流程定义名称")
    private String processDefinitionName;

    @Schema(description = "流程定义KEY")
    private String processDefinitionKey;

    @Schema(description = "流程定义版本")
    private Integer processDefinitionVersion;

    @Schema(description = "部署ID")
    private String deploymentId;

    @Schema(description = "业务唯一KEY")
    private String businessKey;

    @Schema(description = "实例名称")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "是否结束")
    private boolean isEnded;

    @Schema(description = "是否挂起")
    private boolean isSuspended;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date endTime;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date startTime;

    @Schema(description = "发起人ID")
    private String createUserId;

    @Schema(description = "发起人姓名")
    private String startUserName;

    @Schema(description = "当前任务")
    private List<TaskDTO> currentTaskList;

    @Schema(description = "任务名称")
    private String taskName;
}
