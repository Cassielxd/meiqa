package io.renren.crmchat.controller.admin;

import io.renren.common.page.PageData;
import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.AdminSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin API - 管理员设置
 * PHP Reference: /app/controller/admin/system/Admin.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/setting")
@Tag(name = "Admin - Administrator Settings")
@AllArgsConstructor
public class AdminSettingController {

    private final AdminSettingService adminSettingService;

    /**
     * 8.3 管理员列表
     * GET /api/admin/setting/admin
     *
     * PHP Reference: Admin.php::index()
     *
     * Query Params:
     * - page: 页码
     * - limit: 每页数量
     * - name: 管理员账号或姓名（搜索）
     * - roles: 角色ID（搜索）
     * - status: 状态（0-禁用，1-启用）
     *
     * Response:
     * {
     *   "list": [
     *     {
     *       "id": 1,
     *       "account": "admin",
     *       "real_name": "管理员",
     *       "roles": "Super Administrator, Manager",  // 角色名称，逗号分隔
     *       "status": 1,
     *       "last_ip": "127.0.0.1",
     *       "last_time": "2025-01-01 10:00:00",
     *       "_add_time": "2025-01-01 08:00:00",
     *       "_last_time": "2025-01-01 10:00:00"
     *     }
     *   ],
     *   "count": 100
     * }
     */
    @GetMapping("/admin")
    @Operation(summary = "Administrator List")
    public ApiResult<PageData<Map<String, Object>>> adminList(
            @RequestParam Map<String, Object> params,
            @Parameter(description = "Current Administrator Level", hidden = true) @RequestAttribute(value = "adminLevel", required = false) Integer adminLevel) {

        // TODO: 从JWT Token或Session中获取当前管理员的level
        // 临时使用0（超级管理员）
        if (adminLevel == null) {
            adminLevel = 0;
        }

        PageData<Map<String, Object>> result = adminSettingService.getAdminList(params, adminLevel);
        return ApiResult.ok(result);
    }

    /**
     * 8.4 创建管理员
     * POST /api/admin/setting/admin
     *
     * PHP Reference: Admin.php::save()
     *
     * Request Body:
     * {
     *   "account": "manager",
     *   "pwd": "123456",
     *   "conf_pwd": "123456",
     *   "real_name": "经理",
     *   "roles": [1, 2],  // 或 "1,2" 字符串
     *   "status": 1
     * }
     *
     * Response: { "code": 0, "msg": "Added successfully" }
     */
    @PostMapping("/admin")
    @Operation(summary = "创建管理员")
    public ApiResult<String> createAdmin(
            @RequestBody Map<String, Object> data,
            @Parameter(description = "Current Administrator Level", hidden = true) @RequestAttribute(value = "adminLevel", required = false) Integer adminLevel) {

        // TODO: 从JWT Token或Session中获取当前管理员的level
        // 临时使用0（超级管理员）
        if (adminLevel == null) {
            adminLevel = 0;
        }

        // 参数验证
        if (!data.containsKey("account") || data.get("account") == null || data.get("account").toString().trim().isEmpty()) {
            return ApiResult.fail("Please enter administrator account");
        }
        if (!data.containsKey("pwd") || data.get("pwd") == null || data.get("pwd").toString().trim().isEmpty()) {
            return ApiResult.fail("Please enter administrator password");
        }
        if (!data.containsKey("conf_pwd") || data.get("conf_pwd") == null || data.get("conf_pwd").toString().trim().isEmpty()) {
            return ApiResult.fail("Please enter password confirmation");
        }
        if (!data.containsKey("real_name") || data.get("real_name") == null || data.get("real_name").toString().trim().isEmpty()) {
            return ApiResult.fail("Please enter administrator name");
        }
        if (!data.containsKey("roles") || data.get("roles") == null) {
            return ApiResult.fail("Please select administrator role");
        }

        adminSettingService.createAdmin(data, adminLevel);
        return ApiResult.ok("Added successfully", "success");
    }

    /**
     * 8.5 更新管理员
     * PUT /api/admin/setting/admin/:id
     *
     * PHP Reference: Admin.php::update()
     *
     * Request Body:
     * {
     *   "account": "manager",
     *   "pwd": "newpassword",       // 可选，修改密码时提供
     *   "conf_pwd": "newpassword",  // pwd不为空时必须
     *   "real_name": "经理",
     *   "roles": [1, 2],
     *   "status": 1
     * }
     *
     * Response: { "code": 0, "msg": "修改成功" }
     */
    @PutMapping("/admin/{id}")
    @Operation(summary = "更新管理员")
    public ApiResult<String> updateAdmin(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> data,
            @Parameter(description = "Current Administrator Level", hidden = true) @RequestAttribute(value = "adminLevel", required = false) Integer adminLevel) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid administrator ID");
        }

        // 参数验证 - 必填字段
        if (!data.containsKey("real_name") || data.get("real_name") == null || data.get("real_name").toString().trim().isEmpty()) {
            return ApiResult.fail("Please enter administrator name");
        }

        adminSettingService.updateAdmin(id, data);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 8.6 删除管理员
     * DELETE /api/admin/setting/admin/:id
     *
     * PHP Reference: Admin.php::delete()
     *
     * 说明: 软删除，设置is_del=1, status=0
     *
     * Response: { "code": 0, "msg": "Deleted successfully" }
     */
    @DeleteMapping("/admin/{id}")
    @Operation(summary = "删除管理员")
    public ApiResult<String> deleteAdmin(@PathVariable Integer id) {
        adminSettingService.deleteAdmin(id);
        return ApiResult.ok("Deleted successfully", "success");
    }

    /**
     * 8.7 修改管理员状态
     * PUT /api/admin/setting/set_status/:id/:status
     *
     * PHP Reference: Admin.php::set_status()
     *
     * Path Parameters:
     * - id: 管理员ID
     * - status: 状态值（0-禁用，1-启用）
     *
     * Response: { "code": 0, "msg": "关闭成功" } 或 { "code": 0, "msg": "开启成功" }
     */
    @PutMapping("/set_status/{id}/{status}")
    @Operation(summary = "修改管理员状态")
    public ApiResult<String> setStatus(
            @PathVariable Integer id,
            @PathVariable Integer status) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid administrator ID");
        }

        if (status == null || (status != 0 && status != 1)) {
            return ApiResult.fail("Invalid status value");
        }

        String message = adminSettingService.setStatus(id, status);
        return ApiResult.ok(message, "success");
    }

    /**
     * 8.8 获取当前管理员信息
     * GET /api/admin/setting/info
     *
     * PHP Reference: Admin.php::info()
     *
     * Response:
     * {
     *   "id": 1,
     *   "account": "admin",
     *   "real_name": "管理员",
     *   "head_pic": "",
     *   "roles": "1,2",
     *   "level": 0,
     *   "status": 1,
     *   "last_ip": "127.0.0.1",
     *   "last_time": 1696234567,
     *   "add_time": 1696234567,
     *   "login_count": 10
     * }
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前管理员信息")
    public ApiResult<Map<String, Object>> info(
            @Parameter(description = "Current Administrator ID", hidden = true) @RequestAttribute(value = "adminId", required = false) Integer adminId) {

        // TODO: 从JWT Token或Session中获取当前管理员的ID
        // 临时使用默认值1（超级管理员）
        if (adminId == null) {
            adminId = 1;
        }

        Map<String, Object> info = adminSettingService.getAdminInfo(adminId);
        return ApiResult.ok(info);
    }

    /**
     * 8.9 修改当前管理员信息
     * PUT /api/admin/setting/update_admin
     *
     * PHP Reference: Admin.php::update_admin()
     *
     * Request Body:
     * {
     *   "real_name": "管理员",
     *   "head_pic": "http://...",
     *   "pwd": "oldpassword",      // 可选，修改密码时提供
     *   "new_pwd": "newpassword",  // pwd不为空时必填
     *   "conf_pwd": "newpassword"  // pwd不为空时必填
     * }
     *
     * 密码要求: 不小于六位且包含数字和字母
     *
     * Response: { "code": 0, "msg": "修改成功" }
     */
    @PutMapping("/update_admin")
    @Operation(summary = "修改当前管理员信息")
    public ApiResult<String> updateAdmin(
            @RequestBody Map<String, Object> data,
            @Parameter(description = "Current Administrator ID", hidden = true) @RequestAttribute(value = "adminId", required = false) Integer adminId) {

        // TODO: 从JWT Token或Session中获取当前管理员的ID
        // 临时使用默认值1（超级管理员）
        if (adminId == null) {
            adminId = 1;
        }

        // 参数验证 - real_name必填
        if (!data.containsKey("real_name") || data.get("real_name") == null || data.get("real_name").toString().trim().isEmpty()) {
            return ApiResult.fail("Administrator name cannot be empty");
        }

        // PHP: 密码强度验证
        // if (!preg_match('/^(?![^a-zA-Z]+$)(?!\D+$).{6,}$/', $data['new_pwd']))
        if (data.containsKey("new_pwd") && data.get("new_pwd") != null && !data.get("new_pwd").toString().trim().isEmpty()) {
            String newPwd = data.get("new_pwd").toString();
            // 正则: 不小于六位且包含数字和字母
            if (!newPwd.matches("^(?![^a-zA-Z]+$)(?!\\D+$).{6,}$")) {
                return ApiResult.fail("Password is too simple (must be at least 6 characters containing letters and numbers)");
            }
        }

        adminSettingService.updateCurrentAdmin(adminId, data);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 8.10 管理员身份列表
     * GET /api/admin/setting/role
     *
     * PHP Reference: SystemRoleServices::getRoleFormSelect()
     *
     * Query Params:
     * - 无（使用当前管理员的level）
     *
     * Response:
     * [
     *   { "label": "Super Administrator", "value": 1 },
     *   { "label": "经理", "value": 2 },
     *   { "label": "客服", "value": 3 }
     * ]
     */
    @GetMapping("/role")
    @Operation(summary = "管理员身份列表")
    public ApiResult<java.util.List<Map<String, Object>>> roleList(
            @Parameter(description = "当前管理员级别", hidden = true) @RequestAttribute(value = "adminLevel", required = false) Integer adminLevel) {

        // TODO: 从JWT Token或Session中获取当前管理员的level
        // 临时使用0（超级管理员）
        if (adminLevel == null) {
            adminLevel = 0;
        }

        java.util.List<Map<String, Object>> roleList = adminSettingService.getRoleList(adminLevel);
        return ApiResult.ok(roleList);
    }
}
