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
import java.util.Map;

/**
 * @author Jone
 */
@Data
@Tag(name = "任务")
public class TaskDTO {
    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "任务参数")
    private Map<String, Object> params;

    @Schema(description = "流程定义ID")
    private String processDefinitionId;

    @Schema(description = "实例ID")
    private String processInstanceId;

    @Schema(description = "角色")
    private String roleIds;

    @Schema(description = "受理人")
    private String assignee;

    @Schema(description = "受理人姓名")
    private String assigneeName;

    @Schema(description = "任务所有人")
    private String owner;

    @Schema(description = "审核意见")
    private String comment;

    @Schema(description = "活动节点ID")
    private String activityId;

    @Schema(description = "角色组")
    private List<String> lstGroupId;

    @Schema(description = "候选人")
    private List<String> lstUserIds;

    @Schema(description = "处理时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date dueDate;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createTime;

    @Schema(description = "业务ID")
    private String businessKey;

    @Schema(description = "流程定义名称")
    private String processDefinitionName;

    @Schema(description = "流程定义KEY")
    private String processDefinitionKey;

    @Schema(description = "流程发起时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date startTime;

    @Schema(description = "任务发起人")
    private String startUserName;

}
