package io.renren.crmchat.controller.admin;

import io.renren.common.page.PageData;
import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.AdminTenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin API - 租户管理
 * PHP Reference: /app/controller/admin/tenant/Tenant.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/tenant")
@Tag(name = "Admin - Tenant Management")
@AllArgsConstructor
public class AdminTenantController {

    private final AdminTenantService adminTenantService;

    /**
     * 2.1 获取租户列表（带分页和搜索）
     * GET /api/admin/tenant/list
     *
     * PHP Reference: Tenant.php::list()
     *
     * Query Params:
     * - keyword: 搜索关键词（租户名称、账号、联系人）
     * - status: 状态筛选（0-禁用，1-启用）
     * - date: 日期范围筛选（格式: "2024-01-01 - 2024-12-31"）
     * - page: 页码（默认1）
     * - limit: 每页记录数（默认10）
     *
     * Response:
     * {
     *   "list": [...],
     *   "count": 100,
     *   "page": 1,
     *   "limit": 10
     * }
     */
    @GetMapping("/list")
    @Operation(summary = "Get Tenant List")
    public ApiResult<PageData<Map<String, Object>>> list(@RequestParam Map<String, Object> params) {
        PageData<Map<String, Object>> result = adminTenantService.getTenantList(params);
        return ApiResult.ok(result);
    }

    /**
     * 2.2 获取租户详情
     * GET /api/admin/tenant/info/{id}
     *
     * PHP Reference: Tenant.php::read($id)
     *
     * Response:
     * {
     *   "id": 1,
     *   "tenant_name": "Test Tenant",
     *   "appid": "...",
     *   "is_expired": false,
     *   "remaining_days": 30,
     *   ...
     * }
     */
    @GetMapping("/info/{id}")
    @Operation(summary = "Get Tenant Details")
    public ApiResult<Map<String, Object>> info(@Parameter(description = "Tenant ID") @PathVariable("id") Integer id) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        Map<String, Object> info = adminTenantService.getTenantInfo(id);
        return ApiResult.ok(info);
    }

    /**
     * 2.3 创建租户
     * POST /api/admin/tenant/save
     *
     * PHP Reference: Tenant.php::save()
     *
     * Request Body:
     * {
     *   "tenant_name": "租户名称",
     *   "account": "admin@example.com",
     *   "pwd": "password123",
     *   "contact_email": "contact@example.com",
     *   "contact_phone": "13800138000",
     *   "user_limit": 100,
     *   "service_limit": 10,
     *   "expire_at": "2025-12-31 23:59:59",
     *   "status": 1,
     *   "remark": "备注信息"
     * }
     *
     * Response:
     * {
     *   "id": 123,
     *   "tenant_name": "...",
     *   "appid": "...",
     *   ...
     * }
     */
    @PostMapping("/save")
    @Operation(summary = "Create Tenant")
    public ApiResult<Map<String, Object>> save(@RequestBody Map<String, Object> data) {
        // 业务层会进行详细的参数验证
        Map<String, Object> tenant = adminTenantService.createTenant(data);
        return ApiResult.ok("Created successfully", tenant);
    }

    /**
     * 2.4 更新租户
     * PUT /api/admin/tenant/update/{id}
     *
     * PHP Reference: Tenant.php::update($id)
     *
     * Request Body:
     * {
     *   "tenant_name": "租户名称",
     *   "contact_email": "contact@example.com",
     *   "contact_phone": "13800138000",
     *   "user_limit": 100,
     *   "service_limit": 10,
     *   "expire_at": "2025-12-31 23:59:59",
     *   "auto_renew": 1,
     *   "status": 1,
     *   "remark": "备注信息",
     *   "pwd": "new_password" // 可选，更新密码
     * }
     *
     * Response: { "code": 0, "msg": "更新成功" }
     */
    @PutMapping("/update/{id}")
    @Operation(summary = "Update Tenant")
    public ApiResult<String> update(
            @Parameter(description = "Tenant ID") @PathVariable("id") Integer id,
            @RequestBody Map<String, Object> data) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        adminTenantService.updateTenant(id, data);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 2.5 更新租户状态
     * PUT /api/admin/tenant/status/{id}
     *
     * PHP Reference: Tenant.php::updateStatus($id)
     *
     * Request Body:
     * {
     *   "status": 1  // 0-禁用, 1-启用
     * }
     *
     * Response: { "code": 0, "msg": "状态更新成功" }
     */
    @PutMapping("/status/{id}")
    @Operation(summary = "更新租户状态")
    public ApiResult<String> updateStatus(
            @Parameter(description = "Tenant ID") @PathVariable("id") Integer id,
            @RequestBody Map<String, Object> data) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        if (!data.containsKey("status")) {
            return ApiResult.fail("Please provide status parameter");
        }

        try {
            int status = Integer.parseInt(data.get("status").toString());
            adminTenantService.updateTenantStatus(id, status);
            return ApiResult.ok("Status updated successfully", "success");
        } catch (NumberFormatException e) {
            return ApiResult.fail("Invalid status parameter format");
        }
    }

    /**
     * 2.6 删除租户（软删除）
     * DELETE /api/admin/tenant/delete/{id}
     *
     * PHP Reference: Tenant.php::delete($id)
     *
     * 注意: 这是软删除，不会真正删除数据，只是标记 is_del=1
     *
     * Response: { "code": 0, "msg": "删除成功" }
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除租户")
    public ApiResult<String> delete(@Parameter(description = "Tenant ID") @PathVariable("id") Integer id) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        adminTenantService.deleteTenant(id);
        return ApiResult.ok("Deleted successfully", "success");
    }

    /**
     * 2.7 获取租户统计信息
     * GET /api/admin/tenant/statistics
     *
     * PHP Reference: Tenant.php::statistics()
     *
     * Response:
     * {
     *   "total": 100,
     *   "enabled_count": 80,
     *   "disabled_count": 15,
     *   "expiring_count": 5,
     *   "expired_count": 3,
     *   "status_distribution": {
     *     "enabled": 80,
     *     "disabled": 15,
     *     "pending": 5
     *   }
     * }
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get Tenant Statistics")
    public ApiResult<Map<String, Object>> statistics() {
        Map<String, Object> stats = adminTenantService.getTenantStatistics();
        return ApiResult.ok(stats);
    }

    /**
     * 2.8 获取即将过期的租户列表
     * GET /api/admin/tenant/expiring
     *
     * PHP Reference: Tenant.php::expiring()
     *
     * Query Params:
     * - days: 天数（默认30天），查询N天内即将到期的租户
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "tenant_name": "测试租户",
     *     "expire_at": "2025-11-01 23:59:59",
     *     "remaining_days": 25,
     *     "is_expired": false,
     *     ...
     *   }
     * ]
     */
    @GetMapping("/expiring")
    @Operation(summary = "Get Soon-to-Expire Tenant List")
    public ApiResult<List<Map<String, Object>>> expiring(@RequestParam(value = "days", required = false) Integer days) {
        List<Map<String, Object>> list = adminTenantService.getExpiringTenants(days);
        return ApiResult.ok(list);
    }

    /**
     * 2.9 重置租户密码
     * PUT /api/admin/tenant/reset_password/{id}
     *
     * PHP Reference: Tenant.php::resetPassword($id)
     *
     * Request Body:
     * {
     *   "password": "new_password123"
     * }
     *
     * Response: { "code": 0, "msg": "密码重置成功" }
     */
    @PutMapping("/reset_password/{id}")
    @Operation(summary = "重置租户密码")
    public ApiResult<String> resetPassword(
            @Parameter(description = "Tenant ID") @PathVariable("id") Integer id,
            @RequestBody Map<String, Object> data) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        if (!data.containsKey("password")) {
            return ApiResult.fail("Please enter new password");
        }

        String password = data.get("password").toString();
        adminTenantService.resetTenantPassword(id, password);
        return ApiResult.ok("Password reset successfully", "success");
    }
}
