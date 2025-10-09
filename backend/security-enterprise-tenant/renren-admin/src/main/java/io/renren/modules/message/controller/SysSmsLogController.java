package io.renren.modules.message.controller;

import io.renren.common.annotation.LogOperation;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.modules.message.dto.SysSmsLogDTO;
import io.renren.modules.message.service.SysSmsLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * 短信日志
 *
 * @author Mark sunlightcs@gmail.com
 */
@AllArgsConstructor
@RestController
@RequestMapping("sys/smslog")
@Tag(name = "短信日志")
public class SysSmsLogController {
    private final SysSmsLogService sysSmsLogService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @RequiresPermissions("sys:smslog:all")
    public Result<PageData<SysSmsLogDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<SysSmsLogDTO> page = sysSmsLogService.page(params);

        return new Result<PageData<SysSmsLogDTO>>().ok(page);
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("sys:smslog:all")
    public Result delete(@RequestBody Long[] ids) {
        sysSmsLogService.deleteBatchIds(Arrays.asList(ids));

        return new Result();
    }
}