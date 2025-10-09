package io.renren.modules.tenant.controller;

import com.github.xiaoymin.knife4j.annotations.Ignore;
import io.renren.common.annotation.LogOperation;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.common.validator.AssertUtils;
import io.renren.common.validator.ValidatorUtils;
import io.renren.modules.tenant.dto.SysTenantDataSourceDTO;
import io.renren.modules.tenant.service.SysTenantDataSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 租户数据源
 *
 * @author Mark sunlightcs@gmail.com
 */
@AllArgsConstructor
@RestController
@RequestMapping("/sys/tenant/datasource")
@Tag(name = "租户数据源")
public class SysTenantDataSourceController {
    private final SysTenantDataSourceService sysTenantDataSourceService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @RequiresPermissions("tenant:datasource:all")
    public Result page(@Ignore @RequestParam Map<String, Object> params) {
        PageData<SysTenantDataSourceDTO> page = sysTenantDataSourceService.page(params);

        return new Result().ok(page);
    }

    @GetMapping("list")
    @Operation(summary = "分页")
    @RequiresPermissions("tenant:datasource:all")
    public Result list() {
        List<SysTenantDataSourceDTO> data = sysTenantDataSourceService.list(new HashMap<>(1));

        return new Result().ok(data);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("tenant:datasource:all")
    public Result get(@PathVariable("id") Long id) {
        SysTenantDataSourceDTO data = sysTenantDataSourceService.get(id);

        return new Result().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("tenant:datasource:all")
    public Result save(@RequestBody SysTenantDataSourceDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto);

        sysTenantDataSourceService.save(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("tenant:datasource:all")
    public Result delete(@RequestBody Long[] ids) {
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        sysTenantDataSourceService.delete(ids);

        return new Result();
    }

}