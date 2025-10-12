package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.SystemGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 组合数据管理
 * PHP Reference: app/controller/admin/system/Group.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/setting")
@AllArgsConstructor
@Tag(name = "Combined Data Management", description = "Group Controller")
public class AdminGroupController {

    private final SystemGroupService systemGroupService;

    /**
     * 获取组合数据列表
     * PHP Reference: Group::index()
     * 路由: GET /api/admin/setting/group
     *
     * @param title 搜索关键字(可选)
     * @param page 页码(默认1)
     * @param limit 每页数量(默认20)
     * @return 组合数据列表
     */
    @GetMapping("/group")
    @Operation(summary = "Get Combined Data List")
    public ApiResult<Map<String, Object>> index(
        @RequestParam(value = "title", required = false, defaultValue = "") String title,
        @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
        @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit
    ) {
        Map<String, Object> result = systemGroupService.getGroupList(title, page, limit);
        return ApiResult.ok(result);
    }

    /**
     * 获取所有组合数据(简化版)
     * PHP Reference: Group::getGroup()
     * 路由: GET /api/admin/setting/group_all
     * Response: [{"id": 1, "name": "Group Name"}]
     *
     * @return 简化的组合数据列表
     */
    @GetMapping("/group_all")
    @Operation(summary = "Get All Combined Data")
    public ApiResult<List<Map<String, Object>>> getGroupAll() {
        List<Map<String, Object>> result = systemGroupService.getGroupAll();
        return ApiResult.ok(result);
    }
}
