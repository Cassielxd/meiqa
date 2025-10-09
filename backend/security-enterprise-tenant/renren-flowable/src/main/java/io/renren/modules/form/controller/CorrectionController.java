/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */
package io.renren.modules.form.controller;

import io.renren.common.utils.Result;
import io.renren.common.validator.ValidatorUtils;
import io.renren.common.validator.group.AddGroup;
import io.renren.common.validator.group.DefaultGroup;
import io.renren.modules.form.dto.CorrectionDTO;
import io.renren.modules.form.service.CorrectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 转正申请
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("flow/form/correction")
@AllArgsConstructor
@Tag(name = "转正申请")
public class CorrectionController {
    private final CorrectionService correctionService;


    @PostMapping("start")
    @Operation(summary = "启动流程")
    public Result startProcess(@RequestBody CorrectionDTO dto) {
        // 效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);


        correctionService.save(dto);

        return new Result();
    }

    @GetMapping("{instanceId}")
    @Operation(summary = "表单信息")
    public Result<CorrectionDTO> info(@PathVariable("instanceId") String instanceId) {
        CorrectionDTO correction = correctionService.get(instanceId);

        return new Result<CorrectionDTO>().ok(correction);
    }

}
