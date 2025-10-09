/**
 * Copyright (c) 2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.tenant.controller;

import com.github.xiaoymin.knife4j.annotations.Ignore;
import io.renren.common.annotation.LogOperation;
import io.renren.common.constant.Constant;
import io.renren.common.context.TenantContext;
import io.renren.common.enums.TenantModeEnum;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.common.validator.AssertUtils;
import io.renren.common.validator.ValidatorUtils;
import io.renren.common.validator.group.AddGroup;
import io.renren.common.validator.group.DefaultGroup;
import io.renren.common.validator.group.UpdateGroup;
import io.renren.modules.security.user.SecurityUser;
import io.renren.modules.sys.service.SysRoleUserService;
import io.renren.modules.tenant.dto.SysTenantDTO;
import io.renren.modules.tenant.dto.SysTenantListDTO;
import io.renren.modules.tenant.service.SysTenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 租户管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@AllArgsConstructor
@RestController
@RequestMapping("/sys/tenant")
@Tag(name = "租户管理")
public class SysTenantController {
    private final SysTenantService sysTenantService;
    private final SysRoleUserService sysRoleUserService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)"),
            @Parameter(name = "tenantName", description = "租户名")
    })
    @RequiresPermissions("sys:tenant:all")
    public Result page(@Ignore @RequestParam Map<String, Object> params) {
        PageData<SysTenantDTO> page = sysTenantService.page(params);

        return new Result().ok(page);
    }

    @GetMapping("info")
    @Operation(summary = "当前租户信息")
    public Result<SysTenantDTO> info() {
        // 获取当前租户ID
        Long tenantId = TenantContext.getTenantCode(SecurityUser.getUser());
        SysTenantDTO data = sysTenantService.get(tenantId);

        return new Result<SysTenantDTO>().ok(data);
    }

    @GetMapping("list")
    @Operation(summary = "列表")
    public Result list() {
        List<SysTenantListDTO> list = sysTenantService.list();

        return new Result().ok(list);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("sys:tenant:all")
    public Result get(@PathVariable("id") Long id) {
        SysTenantDTO data = sysTenantService.get(id);

        // 字段隔离，则需要查询对应的角色
        if (data.getTenantMode() == TenantModeEnum.COLUMN.value()) {
            // 用户角色列表
            List<Long> roleIdList = sysRoleUserService.getRoleIdList(data.getUserId());
            data.setRoleIdList(roleIdList);
        }

        return new Result().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("sys:tenant:all")
    public Result save(@RequestBody SysTenantDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        sysTenantService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("sys:tenant:all")
    public Result update(@RequestBody SysTenantDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        sysTenantService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("sys:tenant:all")
    public Result delete(@RequestBody Long[] ids) {
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        sysTenantService.deleteBatchIds(Arrays.asList(ids));

        return new Result();
    }
}