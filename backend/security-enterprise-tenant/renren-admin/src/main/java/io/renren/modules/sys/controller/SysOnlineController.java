/**
 * Copyright (c) 2021 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */
package io.renren.modules.sys.controller;

import com.github.xiaoymin.knife4j.annotations.Ignore;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.common.validator.AssertUtils;
import io.renren.modules.security.service.SysUserTokenService;
import io.renren.modules.sys.entity.SysOnlineEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 在线用户
 *
 * @author Mark sunlightcs@gmail.com
 */
@AllArgsConstructor
@RestController
@RequestMapping("/sys/online")
@Tag(name = "在线用户")
public class SysOnlineController {
    private final SysUserTokenService sysUserTokenService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @RequiresPermissions("sys:online:all")
    public Result<PageData<SysOnlineEntity>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<SysOnlineEntity> page = sysUserTokenService.onlinePage(params);

        return new Result<PageData<SysOnlineEntity>>().ok(page);
    }

    @PostMapping("logout")
    @Operation(summary = "踢出")
    @RequiresPermissions("sys:online:all")
    public Result logout(Long id) {
        //效验数据
        AssertUtils.isNull(id, "id");

        //退出
        sysUserTokenService.logout(id);

        return new Result();
    }
}
