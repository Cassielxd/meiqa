package io.renren.modules.flow.controller;

import com.github.xiaoymin.knife4j.annotations.Ignore;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.common.validator.AssertUtils;
import io.renren.common.validator.ValidatorUtils;
import io.renren.common.validator.group.AddGroup;
import io.renren.common.validator.group.DefaultGroup;
import io.renren.common.validator.group.UpdateGroup;
import io.renren.modules.flow.dto.BpmFormDTO;
import io.renren.modules.flow.service.BpmFormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * 工作流表单
 *
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("flow/bpmform")
@Tag(name = "工作流表单")
@AllArgsConstructor
public class BpmFormController {
    private final BpmFormService bpmFormService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @RequiresPermissions("flow:bpmform:all")
    public Result<PageData<BpmFormDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<BpmFormDTO> page = bpmFormService.page(params);

        return new Result<PageData<BpmFormDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("flow:bpmform:all")
    public Result<BpmFormDTO> get(@PathVariable("id") Long id) {
        BpmFormDTO data = bpmFormService.get(id);

        return new Result<BpmFormDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @RequiresPermissions("flow:bpmform:all")
    public Result save(@RequestBody BpmFormDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        bpmFormService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @RequiresPermissions("flow:bpmform:all")
    public Result update(@RequestBody BpmFormDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        bpmFormService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @RequiresPermissions("flow:bpmform:all")
    public Result delete(@RequestBody Long[] ids) {
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        bpmFormService.delete(ids);

        return new Result();
    }

}