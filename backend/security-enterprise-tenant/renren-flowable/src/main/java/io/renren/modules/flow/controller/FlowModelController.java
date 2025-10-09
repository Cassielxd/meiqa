/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */
package io.renren.modules.flow.controller;

import cn.hutool.core.util.StrUtil;
import com.github.xiaoymin.knife4j.annotations.Ignore;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.modules.flow.dto.ModelDTO;
import io.renren.modules.flow.dto.ModelRequestDTO;
import io.renren.modules.flow.service.FlowModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 模型管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("/flow/model")
@AllArgsConstructor
@Tag(name = "模型管理")
public class FlowModelController {
    private final FlowModelService flowModelService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = "key", description = "key"),
            @Parameter(name = "name", description = "name")
    })
    @RequiresPermissions("sys:model:all")
    public Result<PageData<ModelDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<ModelDTO> page = flowModelService.page(params);

        return new Result<PageData<ModelDTO>>().ok(page);
    }

    @PostMapping
    @Operation(summary = "新建模型")
    @RequiresPermissions("sys:model:all")
    public Result save(ModelRequestDTO modelDTO) {
        if (StrUtil.isNotBlank(modelDTO.getId())) {
            flowModelService.updateModel(modelDTO);
        } else {
            flowModelService.saveModel(modelDTO);
        }

        return new Result();
    }

    @GetMapping("{id}")
    @Operation(summary = "获取模型")
    @RequiresPermissions("sys:model:all")
    public Result<ModelDTO> getModel(@PathVariable("id") String id) {
        ModelDTO model = flowModelService.getModel(id);

        return new Result<ModelDTO>().ok(model);
    }

    @PostMapping("deploy/{id}")
    @Operation(summary = "部署")
    @RequiresPermissions("sys:model:all")
    public Result deploy(@PathVariable("id") String id) {
        flowModelService.deploymentByModelId(id);
        return new Result();
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除模型")
    @RequiresPermissions("sys:model:all")
    public Result delete(@PathVariable("id") String id) {
        flowModelService.deleteModel(id);

        return new Result();
    }
}
