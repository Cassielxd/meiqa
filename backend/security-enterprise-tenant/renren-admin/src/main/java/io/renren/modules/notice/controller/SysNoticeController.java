/**
 * Copyright (c) 2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */
package io.renren.modules.notice.controller;

import com.github.xiaoymin.knife4j.annotations.Ignore;
import io.renren.common.annotation.LogOperation;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.common.validator.AssertUtils;
import io.renren.common.validator.ValidatorUtils;
import io.renren.common.validator.group.AddGroup;
import io.renren.common.validator.group.DefaultGroup;
import io.renren.common.validator.group.UpdateGroup;
import io.renren.modules.notice.dto.SysNoticeDTO;
import io.renren.modules.notice.service.SysNoticeService;
import io.renren.modules.notice.service.SysNoticeUserService;
import io.renren.modules.security.user.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * 通知管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@AllArgsConstructor
@RestController
@RequestMapping("sys/notice")
@Tag(name = "通知管理")
public class SysNoticeController {
    private final SysNoticeService sysNoticeService;
    private final SysNoticeUserService sysNoticeUserService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @RequiresPermissions("sys:notice:all")
    public Result<PageData<SysNoticeDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<SysNoticeDTO> page = sysNoticeService.page(params);

        return new Result<PageData<SysNoticeDTO>>().ok(page);
    }

    @GetMapping("user/page")
    @Operation(summary = "获取被通知的用户")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @RequiresPermissions("sys:notice:all")
    public Result<PageData<SysNoticeDTO>> userPage(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<SysNoticeDTO> page = sysNoticeService.getNoticeUserPage(params);

        return new Result<PageData<SysNoticeDTO>>().ok(page);
    }

    @GetMapping("mynotice/page")
    @Operation(summary = "获取我的通知")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    public Result<PageData<SysNoticeDTO>> myNoticePage(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<SysNoticeDTO> page = sysNoticeService.getMyNoticePage(params);

        return new Result<PageData<SysNoticeDTO>>().ok(page);
    }

    @PutMapping("mynotice/read/{noticeId}")
    @Operation(summary = "标记我的通知为已读")
    public Result read(@PathVariable("noticeId") Long noticeId) {
        sysNoticeUserService.updateReadStatus(SecurityUser.getUserId(), noticeId);

        return new Result();
    }

    @GetMapping("mynotice/unread")
    @Operation(summary = "我的通知未读读")
    public Result<Integer> unRead() {
        int count = sysNoticeUserService.getUnReadNoticeCount(SecurityUser.getUserId());

        return new Result<Integer>().ok(count);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @RequiresPermissions("sys:notice:all")
    public Result<SysNoticeDTO> get(@PathVariable("id") Long id) {
        SysNoticeDTO data = sysNoticeService.get(id);

        return new Result<SysNoticeDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @RequiresPermissions("sys:notice:all")
    public Result save(@RequestBody SysNoticeDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        sysNoticeService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @RequiresPermissions("sys:notice:all")
    public Result update(@RequestBody SysNoticeDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        sysNoticeService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @RequiresPermissions("sys:notice:all")
    public Result delete(@RequestBody Long[] ids) {
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        sysNoticeService.delete(ids);

        return new Result();
    }

}