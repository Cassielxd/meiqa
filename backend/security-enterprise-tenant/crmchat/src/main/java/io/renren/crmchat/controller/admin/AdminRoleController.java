package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.SystemRoleEntity;
import io.renren.crmchat.service.AdminRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin Role Controller - 管理员角色管理
 * PHP Reference: /app/controller/admin/system/Role.php
 *
 * 功能说明:
 * 1. GET /api/admin/setting/role - 角色列表
 * 2. GET /api/admin/setting/role/create - 获取创建表单（菜单权限树）
 * 3. GET /api/admin/setting/role/:id/edit - 获取编辑表单
 * 4. POST /api/admin/setting/role/:id - 保存角色（创建或更新）
 * 5. PUT /api/admin/setting/role/set_status/:id/:status - 修改状态
 * 6. DELETE /api/admin/setting/role/:id - 删除角色
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin - Role Management")
@AllArgsConstructor
public class AdminRoleController {

    private final AdminRoleService adminRoleService;

    /**
     * 获取角色列表
     * GET /api/admin/setting/role
     *
     * PHP Reference: Role.php::index()
     *
     * Query Parameters:
     * - status: 状态筛选
     * - role_name: 角色名称筛选
     * - page: 页码
     * - limit: 每页数量
     *
     * Response:
     * {
     *   "list": [...],
     *   "count": 100
     * }
     */
    @GetMapping("/role")
    @Operation(summary = "Get Role List")
    public ApiResult<Map<String, Object>> getRoleList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role_name,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {

        // TODO: 从JWT token中获取当前管理员level
        Integer currentAdminLevel = 0; // 临时：假设超级管理员level=0

        Map<String, Object> filters = new java.util.HashMap<>();
        if (status != null && !status.trim().isEmpty()) {
            filters.put("status", status);
        }
        if (role_name != null && !role_name.trim().isEmpty()) {
            filters.put("role_name", role_name);
        }
        filters.put("page", page);
        filters.put("limit", limit);

        Map<String, Object> result = adminRoleService.getRoleList(filters, currentAdminLevel);
        return ApiResult.ok(result);
    }

    /**
     * 获取创建角色表单（菜单权限树）
     * GET /api/admin/setting/role/create
     *
     * PHP Reference: Role.php::create()
     *
     * Response:
     * {
     *   "menus": [...]  // 菜单权限树
     * }
     *
     * TODO: 需要实现SystemMenusService获取菜单树
     */
    @GetMapping("/role/create")
    @Operation(summary = "Get Create Role Form")
    public ApiResult<Map<String, Object>> getCreateForm() {
        // TODO: 从JWT token中获取当前管理员level和roles
        // Integer currentAdminLevel = SecurityUtils.getCurrentAdminLevel();
        // String currentAdminRoles = SecurityUtils.getCurrentAdminRoles();

        // TODO: 调用SystemMenusService获取菜单权限树
        // PHP: $menus = $services->getmenus($this->adminInfo['level'] == 0 ? [] : $this->adminInfo['roles']);
        // List<Map<String, Object>> menus = systemMenusService.getMenus(currentAdminLevel, currentAdminRoles);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("menus", new java.util.ArrayList<>()); // 临时返回空数组

        return ApiResult.ok(result);
    }

    /**
     * 获取编辑角色表单
     * GET /api/admin/setting/role/:id/edit
     *
     * PHP Reference: Role.php::edit()
     *
     * Response:
     * {
     *   "role": {...},  // 角色信息
     *   "menus": [...]  // 菜单权限树
     * }
     */
    @GetMapping("/role/{id}/edit")
    @Operation(summary = "Get Edit Role Form")
    public ApiResult<Map<String, Object>> getEditForm(@PathVariable Integer id) {
        // TODO: 从JWT token中获取当前管理员level
        Integer currentAdminLevel = 0; // 临时

        SystemRoleEntity role = adminRoleService.getRoleInfo(id, currentAdminLevel);

        // TODO: 调用SystemMenusService获取菜单权限树
        // PHP: $menus = $services->getMenus($this->adminInfo['level'] == 0 ? [] : $this->adminInfo['roles']);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("role", role);
        result.put("menus", new java.util.ArrayList<>()); // 临时返回空数组

        return ApiResult.ok(result);
    }

    /**
     * 保存角色（创建或更新）
     * POST /api/admin/setting/role/:id
     *
     * PHP Reference: Role.php::save()
     *
     * Request Body:
     * {
     *   "role_name": "Editor",
     *   "status": 1,
     *   "rules": "1,2,3,4,5"  // 菜单ID，逗号分隔
     * }
     *
     * Response: { "code": 0, "msg": "Added successfully" } or { "code": 0, "msg": "Updated successfully" }
     */
    @PostMapping("/role/{id}")
    @Operation(summary = "Save Role")
    public ApiResult<String> saveRole(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        // TODO: 从JWT token中获取当前管理员level
        Integer currentAdminLevel = 0; // 临时

        if (id == null || id == 0) {
            // 创建新角色
            adminRoleService.createRole(data, currentAdminLevel);
            return ApiResult.ok("Role added successfully", "success");
        } else {
            // 更新角色
            boolean success = adminRoleService.updateRole(id, data, currentAdminLevel);
            if (success) {
                return ApiResult.ok("Updated successfully", "success");
            } else {
                return ApiResult.fail("Update failed");
            }
        }
    }

    /**
     * 修改角色状态
     * PUT /api/admin/setting/role/set_status/:id/:status
     *
     * PHP Reference: Role.php::set_status()
     *
     * Response: { "code": 0, "msg": "Updated successfully" }
     */
    @PutMapping("/role/set_status/{id}/{status}")
    @Operation(summary = "Modify Role Status")
    public ApiResult<String> setStatus(@PathVariable Integer id, @PathVariable Integer status) {
        // TODO: 从JWT token中获取当前管理员level
        Integer currentAdminLevel = 0; // 临时

        adminRoleService.updateStatus(id, status, currentAdminLevel);

        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 删除角色
     * DELETE /api/admin/setting/role/:id
     *
     * PHP Reference: Role.php::delete()
     *
     * Response: { "code": 0, "msg": "Deleted successfully" }
     */
    @DeleteMapping("/role/{id}")
    @Operation(summary = "Delete Role")
    public ApiResult<String> deleteRole(@PathVariable Integer id) {
        // TODO: 从JWT token中获取当前管理员level
        Integer currentAdminLevel = 0; // 临时

        boolean success = adminRoleService.deleteRole(id, currentAdminLevel);

        if (success) {
            return ApiResult.ok("Deleted successfully", "success");
        } else {
            return ApiResult.fail("Delete failed, please try again later");
        }
    }
}
