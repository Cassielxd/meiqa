/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.devtools.controller;

import com.google.gson.Gson;
import io.renren.common.constant.Constant;
import io.renren.common.utils.Result;
import io.renren.modules.devtools.entity.GenParam;
import io.renren.modules.sys.service.SysParamsService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 代码生成参数配置
 *
 * @author Mark sunlightcs@gmail.com
 */
@AllArgsConstructor
@RestController
@RequestMapping("devtools/param")
public class GenParamController {
    private final SysParamsService sysParamsService;

    @GetMapping("info")
    public Result<GenParam> info() {
        GenParam param = sysParamsService.getValueObject(Constant.DEV_TOOLS_PARAM_KEY, GenParam.class);

        return new Result<GenParam>().ok(param);
    }

    @PostMapping
    public Result saveConfig(@RequestBody GenParam param) {
        sysParamsService.updateValueByCode(Constant.DEV_TOOLS_PARAM_KEY, new Gson().toJson(param));

        return new Result();
    }
}