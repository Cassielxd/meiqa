/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */
package io.renren.modules.flow.controller;

import io.renren.common.constant.Constant;
import io.renren.common.exception.ErrorCode;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.modules.flow.dto.HistoryDetailDTO;
import io.renren.modules.flow.dto.ProcessInstanceDTO;
import io.renren.modules.flow.dto.TaskDTO;
import io.renren.modules.flow.service.FlowProcessService;
import io.renren.modules.flow.service.FlowService;
import io.renren.modules.flow.user.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通用流程接口
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("/flow/common")
@AllArgsConstructor
@Tag(name = "通用流程接口")
public class FlowController {
    private final FlowProcessService flowProcessService;
    private final FlowService flowService;

    @GetMapping("start/page")
    @Operation(summary = "发起流程列表")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @RequiresPermissions("sys:flow:all")
    public Result<PageData<Map<String, Object>>> startPage(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        params.put("isLatestVersion", true);
        PageData<Map<String, Object>> page = flowProcessService.page(params);
        return new Result<PageData<Map<String, Object>>>().ok(page);
    }

    @GetMapping("form/{processDefinitionId}")
    @Operation(summary = "获取流程表单")
    @RequiresPermissions("sys:flow:all")
    public Result<Map<String, String>> form(@PathVariable("processDefinitionId") String processDefinitionId) {
        Map<String, String> data = flowService.form(processDefinitionId);

        return new Result<Map<String, String>>().ok(data);
    }

    @GetMapping("form/instance/{processInstanceId}")
    @Operation(summary = "获取流程实例表单值")
    @RequiresPermissions("sys:flow:all")
    public Result<Map<String, Object>> formInstanceVariables(@PathVariable("processInstanceId") String processInstanceId) {
        Map<String, Object> formVariables = flowService.formInstanceVariables(processInstanceId);

        return new Result<Map<String, Object>>().ok(formVariables);
    }

    @PostMapping("start/instance/{processDefinitionId}")
    @Operation(summary = "启动流程实例")
    @RequiresPermissions("sys:flow:all")
    public Result<String> startInstance(@PathVariable("processDefinitionId") String processDefinitionId,
                                        @RequestBody Map<String, Object> variables) {
        flowService.startInstance(processDefinitionId, variables, true);

        return new Result<>();
    }

    @GetMapping("my/page")
    @Operation(summary = "我的申请")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true)
    })
    @RequiresPermissions("sys:flow:all")
    public Result<ProcessInstanceDTO> myPage(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<ProcessInstanceDTO> page = flowService.myPage(params);
        return new Result().ok(page);
    }

    @GetMapping("done/page")
    @Operation(summary = "已办任务")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = "processInstanceId", description = "实例ID"),
            @Parameter(name = "businessKey", description = "业务KEY")
    })
    @RequiresPermissions("sys:flow:all")
    public Result<ProcessInstanceDTO> donePage(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<ProcessInstanceDTO> page = flowService.donePage(params);

        return new Result().ok(page);
    }

    @GetMapping("todo/page")
    @Operation(summary = "待办任务")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = "taskName", description = "任务名称")
    })
    @RequiresPermissions("sys:flow:all")
    public Result<PageData<TaskDTO>> todoPage(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<TaskDTO> page = flowService.todoPage(params);
        return new Result<PageData<TaskDTO>>().ok(page);
    }

    @GetMapping("diagram/image")
    @Operation(summary = "获取流程活动图")
    @Parameter(name = "processInstanceId", description = "流程实例ID")
    @RequiresPermissions("sys:flow:all")
    public void diagramImage(String processInstanceId, @Parameter(hidden = true) HttpServletResponse response) throws Exception {
        flowService.diagramImage(processInstanceId, response);
    }

    @GetMapping("historic/list")
    @Operation(summary = "获取流转详情列表")
    @Parameter(name = "processInstanceId", description = "流程实例ID")
    @RequiresPermissions("sys:flow:all")
    public Result<HistoryDetailDTO> historicTaskList(String processInstanceId) {
        List<HistoryDetailDTO> list = flowService.historicTaskList(processInstanceId);
        return new Result().ok(list);
    }


    @PostMapping("delegate")
    @Operation(summary = "委托任务")
    @Parameters({
            @Parameter(name = "taskId", description = "任务ID"),
            @Parameter(name = "userId", description = "委托人")
    })
    @RequiresPermissions("sys:flow:all")
    public Result delegate(String taskId, String userId) {
        if (StringUtils.isBlank(taskId) || StringUtils.isBlank(userId)) {
            return new Result().error(ErrorCode.PARAMS_GET_ERROR);
        }

        flowService.delegate(taskId, LoginUser.getUserId().toString(), userId);

        return new Result();
    }

    @PostMapping("complete")
    @Operation(summary = "完成任务(同意)")
    @Parameters({
            @Parameter(name = "taskId", description = "任务ID"),
            @Parameter(name = "comment", description = "审批意见")
    })
    @RequiresPermissions("sys:flow:all")
    public Result complete(String taskId, String comment) {
        if (StringUtils.isEmpty(taskId)) {
            return new Result().error(ErrorCode.PARAMS_GET_ERROR);
        }

        flowService.complete(taskId, comment, true);

        return new Result();
    }

    @PostMapping("reject")
    @Operation(summary = "驳回")
    @Parameters({
            @Parameter(name = "taskId", description = "任务ID"),
            @Parameter(name = "comment", description = "审批意见")
    })
    @RequiresPermissions("sys:flow:all")
    public Result reject(String taskId, String comment) {
        if (StringUtils.isEmpty(taskId)) {
            return new Result().error(ErrorCode.PARAMS_GET_ERROR);
        }

        flowService.reject(taskId, comment);

        return new Result();
    }
}