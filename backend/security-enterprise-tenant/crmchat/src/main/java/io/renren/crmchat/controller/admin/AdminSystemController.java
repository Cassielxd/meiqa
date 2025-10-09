package io.renren.crmchat.controller.admin;

import io.renren.common.page.PageData;
import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.AdminSystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin API - 系统管理
 * PHP Reference: /app/controller/admin/system/Log.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/system")
@Tag(name = "Admin - System Management")
@AllArgsConstructor
public class AdminSystemController {

    private final AdminSystemService adminSystemService;

    /**
     * 8.1 系统日志列表
     * GET /api/admin/system/log
     *
     * PHP Reference: Log.php::index()
     *
     * Query Params:
     * - page: 页码
     * - limit: 每页数量
     * - admin_id: 管理员ID（可选）
     * - path: 请求路径（搜索）
     * - ip: IP地址（搜索）
     * - pages: 页面名称（搜索）
     *
     * Response:
     * {
     *   "list": [
     *     {
     *       "id": 1,
     *       "admin_id": 1,
     *       "admin_name": "admin",
     *       "path": "/api/admin/login",
     *       "method": "POST",
     *       "ip": "127.0.0.1",
     *       "type": "登录",
     *       "page": "管理员登录",
     *       "add_time": "2025-01-01 10:00:00"
     *     }
     *   ],
     *   "count": 100
     * }
     */
    @GetMapping("/log")
    @Operation(summary = "System Log List")
    public ApiResult<PageData<Map<String, Object>>> logList(
            @RequestParam Map<String, Object> params,
            @Parameter(description = "Current Administrator Level", hidden = true) @RequestAttribute(value = "adminLevel", required = false) Integer adminLevel) {

        // TODO: 从JWT Token或Session中获取当前管理员的level
        // 临时使用0（超级管理员）
        if (adminLevel == null) {
            adminLevel = 0;
        }

        PageData<Map<String, Object>> result = adminSystemService.getLogList(params, adminLevel);
        return ApiResult.ok(result);
    }

    /**
     * 8.2 系统日志管理员搜索条件
     * GET /api/admin/system/log/search_admin
     *
     * PHP Reference: Log.php::search_admin()
     *
     * 用途: 获取有权限查看的管理员列表，用于日志搜索的下拉框
     *
     * Response:
     * {
     *   "info": [
     *     {"id": 1, "real_name": "admin"},
     *     {"id": 2, "real_name": "manager"}
     *   ]
     * }
     */
    @GetMapping("/log/search_admin")
    @Operation(summary = "System Log Admin Search Criteria")
    public ApiResult<Map<String, Object>> searchAdmin(
            @Parameter(description = "Current Administrator Level", hidden = true) @RequestAttribute(value = "adminLevel", required = false) Integer adminLevel) {

        // TODO: 从JWT Token或Session中获取当前管理员的level
        // 临时使用0（超级管理员）
        if (adminLevel == null) {
            adminLevel = 0;
        }

        Map<String, Object> result = adminSystemService.getSearchAdminList(adminLevel);
        return ApiResult.ok(result);
    }
}
