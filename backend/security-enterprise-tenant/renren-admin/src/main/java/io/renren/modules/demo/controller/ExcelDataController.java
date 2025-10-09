package io.renren.modules.demo.controller;

import com.alibaba.excel.EasyExcel;
import com.github.xiaoymin.knife4j.annotations.Ignore;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.utils.ExcelDataListener;
import io.renren.common.utils.ExcelUtils;
import io.renren.common.utils.Result;
import io.renren.common.validator.AssertUtils;
import io.renren.common.validator.ValidatorUtils;
import io.renren.common.validator.group.AddGroup;
import io.renren.common.validator.group.DefaultGroup;
import io.renren.common.validator.group.UpdateGroup;
import io.renren.modules.demo.dto.ExcelDataDTO;
import io.renren.modules.demo.excel.ExcelDataExcel;
import io.renren.modules.demo.service.ExcelDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


/**
 * Excel导入演示
 *
 * @author Mark sunlightcs@gmail.com
 */
@AllArgsConstructor
@RestController
@RequestMapping("demo/excel")
@Tag(name = "Excel导入演示")
public class ExcelDataController {
    private final ExcelDataService excelDataService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @RequiresPermissions("demo:excel:all")
    public Result<PageData<ExcelDataDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<ExcelDataDTO> page = excelDataService.page(params);

        return new Result<PageData<ExcelDataDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("demo:excel:all")
    public Result<ExcelDataDTO> get(@PathVariable("id") Long id) {
        ExcelDataDTO data = excelDataService.get(id);

        return new Result<ExcelDataDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @RequiresPermissions("demo:excel:all")
    public Result save(@RequestBody ExcelDataDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        excelDataService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @RequiresPermissions("demo:excel:all")
    public Result update(@RequestBody ExcelDataDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        excelDataService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @RequiresPermissions("demo:excel:all")
    public Result delete(@RequestBody Long[] ids) {
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        excelDataService.delete(ids);

        return new Result();
    }

    @PostMapping("import")
    @Operation(summary = "导入")
    @RequiresPermissions("demo:excel:all")
    public Result importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        //解析并保存到数据库
        EasyExcel.read(file.getInputStream(), ExcelDataExcel.class, new ExcelDataListener<>(excelDataService)).sheet().doRead();

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @RequiresPermissions("demo:excel:all")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ExcelDataDTO> list = excelDataService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "Excel导入演示", list, ExcelDataExcel.class);
    }

}