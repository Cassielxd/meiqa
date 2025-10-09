package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.SystemAdminEntity;
import io.renren.crmchat.service.TenantSystemAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Tenant System Admin Controller - 租户级系统管理员管理
 * PHP Reference: /app/controller/tenant/system/Admin.php
 *
 * 功能说明:
 * 1. GET /api/tenant/system/admin - 管理员列表
 * 2. POST /api/tenant/system/admin/create - 获取创建表单（REST不需要）
 * 3. POST /api/tenant/system/admin - 创建管理员
 * 4. GET /api/tenant/system/admin/:id/edit - 获取编辑表单
 * 5. PUT /api/tenant/system/admin/:id - 更新管理员
 * 6. DELETE /api/tenant/system/admin/:id - 删除管理员
 * 7. PUT /api/tenant/system/admin/set_status/:id/:status - 修改状态
 * 8. GET /api/tenant/system/admin/info - 获取当前管理员信息
 * 9. PUT /api/tenant/system/admin/update_admin - 修改当前管理员信息
 * 10. POST /api/tenant/system/admin/logout - 退出登录
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/tenant/system/admin")
@Tag(name = "Tenant System - 管理员管理")
@AllArgsConstructor
public class TenantSystemAdminController {

    private final TenantSystemAdminService tenantSystemAdminService;

    /**
     * 获取管理员列表
     * GET /api/tenant/system/admin
     *
     * PHP Reference: Admin.php::index()
     *
     * Query Parameters:
     * - name: 账号（模糊查询）
     * - roles: 角色ID
     * - is_del: 是否删除（默认0）
     * - status: 状态
     * - page: 页码
     * - limit: 每页数量
     *
     * Response:
     * {
     *   "list": [...],
     *   "count": 100
     * }
     */
    @GetMapping
    @Operation(summary = "Get Administrator List")
    public ApiResult<Map<String, Object>> getAdminList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String roles,
            @RequestParam(required = false, defaultValue = "0") Integer is_del,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {

        // TODO: 从JWT token中获取当前管理员level
        // Integer currentAdminLevel = SecurityUtils.getCurrentAdminLevel();
        Integer currentAdminLevel = 0; // 临时：假设租户管理员level=0

        Map<String, Object> filters = new java.util.HashMap<>();
        if (name != null && !name.trim().isEmpty()) {
            filters.put("name", name);
        }
        if (roles != null && !roles.trim().isEmpty()) {
            filters.put("roles", roles);
        }
        filters.put("is_del", is_del);
        if (status != null) {
            filters.put("status", status);
        }
        filters.put("page", page);
        filters.put("limit", limit);

        Map<String, Object> result = tenantSystemAdminService.getAdminList(filters, currentAdminLevel);
        return ApiResult.ok(result);
    }

    /**
     * 创建管理员
     * POST /api/tenant/system/admin
     *
     * PHP Reference: Admin.php::save()
     *
     * Request Body:
     * {
     *   "account": "admin001",
     *   "pwd": "password123",
     *   "conf_pwd": "password123",
     *   "real_name": "张三",
     *   "roles": "1,2,3",
     *   "status": 1
     * }
     *
     * Response: { "code": 0, "msg": "添加成功" }
     */
    @PostMapping
    @Operation(summary = "Create Administrator")
    public ApiResult<String> createAdmin(@RequestBody Map<String, Object> data) {
        // TODO: 从JWT token中获取当前管理员level
        Integer currentAdminLevel = 0; // 临时

        tenantSystemAdminService.createAdmin(data, currentAdminLevel);
        return ApiResult.ok("Added successfully", "success");
    }

    /**
     * 获取管理员编辑表单
     * GET /api/tenant/system/admin/:id/edit
     *
     * PHP Reference: Admin.php::edit()
     *
     * Response:
     * {
     *   "id": 1,
     *   "account": "admin001",
     *   "real_name": "张三",
     *   "roles": "1,2,3",
     *   "status": 1,
     *   ...
     * }
     */
    @GetMapping("/{id}/edit")
    @Operation(summary = "Get Administrator Edit Form")
    public ApiResult<SystemAdminEntity> getAdminEditForm(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Failed to read administrator information");
        }

        // TODO: 从JWT token中获取当前管理员level
        Integer currentAdminLevel = 0; // 临时

        SystemAdminEntity admin = tenantSystemAdminService.getAdminInfo(id, currentAdminLevel);
        return ApiResult.ok(admin);
    }

    /**
     * 更新管理员信息
     * PUT /api/tenant/system/admin/:id
     *
     * PHP Reference: Admin.php::update()
     *
     * Request Body:
     * {
     *   "account": "admin001",
     *   "pwd": "newpassword", // 可选
     *   "conf_pwd": "newpassword", // 可选
     *   "real_name": "李四",
     *   "roles": "1,2",
     *   "status": 1
     * }
     *
     * Response: { "code": 0, "msg": "Modified successfully" }
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新管理员信息")
    public ApiResult<String> updateAdmin(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        // TODO: 从JWT token中获取当前管理员level
        Integer currentAdminLevel = 0; // 临时

        boolean success = tenantSystemAdminService.updateAdmin(id, data, currentAdminLevel);

        if (success) {
            return ApiResult.ok("Updated successfully", "success");
        } else {
            return ApiResult.fail("Update failed");
        }
    }

    /**
     * 删除管理员（软删除）
     * DELETE /api/tenant/system/admin/:id
     *
     * PHP Reference: Admin.php::delete()
     *
     * Response: { "code": 0, "msg": "删除成功！" }
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除管理员")
    public ApiResult<String> deleteAdmin(@PathVariable Integer id) {
        // TODO: 从JWT token中获取当前管理员level
        Integer currentAdminLevel = 0; // 临时

        boolean success = tenantSystemAdminService.deleteAdmin(id, currentAdminLevel);

        if (success) {
            return ApiResult.ok("Deleted successfully", "success");
        } else {
            return ApiResult.fail("Delete failed");
        }
    }

    /**
     * 修改管理员状态
     * PUT /api/tenant/system/admin/set_status/:id/:status
     *
     * PHP Reference: Admin.php::set_status()
     *
     * Response: { "code": 0, "msg": "Enabled successfully" } 或 { "code": 0, "msg": "Closed successfully" }
     */
    @PutMapping("/set_status/{id}/{status}")
    @Operation(summary = "修改管理员状态")
    public ApiResult<String> setStatus(@PathVariable Integer id, @PathVariable Integer status) {
        // TODO: 从JWT token中获取当前管理员level
        Integer currentAdminLevel = 0; // 临时

        tenantSystemAdminService.updateStatus(id, status, currentAdminLevel);

        String message = status == 0 ? "Closed successfully" : "开启成功";
        return ApiResult.ok(message, "success");
    }

    /**
     * 获取当前登录管理员信息
     * GET /api/tenant/system/admin/info
     *
     * PHP Reference: Admin.php::info()
     *
     * Response:
     * {
     *   "id": 1,
     *   "account": "tenant001",
     *   "real_name": "租户管理员",
     *   ...
     * }
     */
    @GetMapping("/info")
    @Operation(summary = "Get Current Administrator Information")
    public ApiResult<SystemAdminEntity> getAdminInfo() {
        // TODO: 从JWT token中获取当前管理员ID
        // Integer currentAdminId = SecurityUtils.getCurrentAdminId();
        Integer currentAdminId = 1; // 临时

        SystemAdminEntity admin = tenantSystemAdminService.getCurrentAdminInfo(currentAdminId);
        return ApiResult.ok(admin);
    }

    /**
     * 修改当前管理员信息
     * PUT /api/tenant/system/admin/update_admin
     *
     * PHP Reference: Admin.php::update_admin()
     *
     * Request Body:
     * {
     *   "real_name": "新名字",
     *   "head_pic": "http://...",
     *   "pwd": "旧密码",
     *   "new_pwd": "新密码",
     *   "conf_pwd": "Confirm New Password"
     * }
     *
     * 业务规则:
     * 1. 新密码必须 >=6 位且包含数字和字母
     * 2. 旧密码必须正确
     * 3. 新密码和确认密码必须一致
     *
     * Response: { "code": 0, "msg": "修改成功" }
     */
    @PutMapping("/update_admin")
    @Operation(summary = "修改当前管理员信息")
    public ApiResult<String> updateCurrentAdmin(@RequestBody Map<String, Object> data) {
        // TODO: 从JWT token中获取租户ID（注意：这里需要的是tenant_id，不是admin_id）
        // Integer tenantId = SecurityUtils.getCurrentTenantId();

        // PHP: if (!preg_match('/^(?![^a-zA-Z]+$)(?!\D+$).{6,}$/', $data['new_pwd']))
        if (data.containsKey("new_pwd") && data.get("new_pwd") != null) {
            String newPwd = data.get("new_pwd").toString();
            if (!newPwd.matches("^(?![^a-zA-Z]+$)(?!\\D+$).{6,}$")) {
                return ApiResult.fail("Password is too simple (must be at least 6 characters containing letters and numbers)");
            }
        }

        // TODO: 调用 TenantsService.updateAdmin(tenantId, data)
        // 注意：这个方法更新的是 eb_tenants 表，不是 eb_system_admin 表

        return ApiResult.fail("Please get tenant ID from token and implement update logic");
    }

    /**
     * 退出登录
     * POST /api/tenant/system/admin/logout
     *
     * PHP Reference: Admin.php::logout()
     *
     * Response: { "code": 0, "msg": "success" }
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout")
    public ApiResult<String> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        // PHP: CacheService::redisHandler()->delete($cacheKey);
        // TODO: 实现Redis缓存清除逻辑
        // if (token != null && !token.isEmpty()) {
        //     String cleanToken = token.replace("Bearer ", "").trim();
        //     String cacheKey = DigestUtils.md5DigestAsHex(cleanToken.getBytes());
        //     redisTemplate.delete(cacheKey);
        // }

        return ApiResult.ok("Logged out successfully", "success");
    }
}
