package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.SystemMenusEntity;
import io.renren.crmchat.service.AdminMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin Menu Controller - 管理员菜单管理
 * PHP Reference: /app/controller/admin/system/Menus.php
 *
 * 功能说明:
 * 1. GET /api/admin/setting/menus - 菜单列表
 * 2. GET /api/admin/setting/menus/create - 获取创建表单
 * 3. POST /api/admin/setting/menus - 保存菜单
 * 4. GET /api/admin/setting/menus/:id - 获取菜单详情
 * 5. GET /api/admin/setting/menus/:id/edit - 获取编辑表单
 * 6. PUT /api/admin/setting/menus/:id - 更新菜单
 * 7. DELETE /api/admin/setting/menus/:id - 删除菜单
 * 8. PUT /api/admin/setting/menus/:id/show - 显示/隐藏切换
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/setting")
@Tag(name = "Admin Setting - 菜单管理")
@AllArgsConstructor
public class AdminMenuController {

    private final AdminMenuService adminMenuService;

    /**
     * 获取菜单列表
     * GET /api/admin/setting/menus
     *
     * PHP Reference: Menus.php::index()
     *
     * Query Parameters:
     * - is_show: 显示状态筛选（0隐藏，1显示）
     * - keyword: 关键词筛选（菜单名称）
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "menu_name": "System Settings",
     *     "path": "admin/system",
     *     ...
     *   }
     * ]
     */
    @GetMapping("/menus")
    @Operation(summary = "Get Menu List")
    public ApiResult<List<SystemMenusEntity>> getMenuList(
            @RequestParam(required = false) String is_show,
            @RequestParam(required = false) String keyword) {

        Map<String, Object> filters = new java.util.HashMap<>();
        if (is_show != null && !is_show.trim().isEmpty()) {
            filters.put("is_show", is_show);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            filters.put("keyword", keyword);
        }

        List<SystemMenusEntity> result = adminMenuService.getMenuList(filters);
        return ApiResult.ok(result);
    }

    /**
     * 获取创建菜单表单
     * GET /api/admin/setting/menus/create
     *
     * PHP Reference: Menus.php::create()
     *
     * Response:
     * {
     *   "menus": [...]  // 所有菜单列表，用于选择父级
     * }
     */
    @GetMapping("/menus/create")
    @Operation(summary = "Get Create Menu Form")
    public ApiResult<Map<String, Object>> getCreateForm() {
        Map<String, Object> result = adminMenuService.getCreateForm();
        return ApiResult.ok(result);
    }

    /**
     * 保存菜单
     * POST /api/admin/setting/menus
     *
     * PHP Reference: Menus.php::save()
     *
     * Request Body:
     * {
     *   "menu_name": "System Settings",
     *   "controller": "System",
     *   "module": "admin",
     *   "action": "index",
     *   "icon": "setting",
     *   "params": "",
     *   "path": ["admin", "system"],  // 数组，后端会用"/"连接
     *   "menu_path": "/admin/system",
     *   "api_url": "api/admin/system",
     *   "methods": "GET",
     *   "unique_auth": "admin_system_index",
     *   "header": "",
     *   "is_header": 0,
     *   "pid": 0,
     *   "sort": 0,
     *   "auth_type": 0,
     *   "access": 1,
     *   "is_show": 1,
     *   "is_show_path": 0
     * }
     *
     * Response: { "code": 0, "msg": "添加成功" }
     */
    @PostMapping("/menus")
    @Operation(summary = "Save Menu")
    public ApiResult<String> createMenu(@RequestBody Map<String, Object> data) {
        adminMenuService.createMenu(data);
        return ApiResult.ok("Added successfully", "success");
    }

    /**
     * 获取菜单详情
     * GET /api/admin/setting/menus/:id
     *
     * PHP Reference: Menus.php::read()
     *
     * Response:
     * {
     *   "id": 1,
     *   "menu_name": "系统设置",
     *   ...
     * }
     */
    @GetMapping("/menus/{id}")
    @Operation(summary = "Get Menu Details")
    public ApiResult<SystemMenusEntity> getMenuInfo(@PathVariable Integer id) {
        SystemMenusEntity menu = adminMenuService.getMenuInfo(id);
        return ApiResult.ok(menu);
    }

    /**
     * 获取编辑菜单表单
     * GET /api/admin/setting/menus/:id/edit
     *
     * PHP Reference: Menus.php::edit()
     *
     * Response:
     * {
     *   "menu": {...},    // 当前菜单信息
     *   "menus": [...]    // 所有菜单列表，用于选择父级
     * }
     */
    @GetMapping("/menus/{id}/edit")
    @Operation(summary = "Get Edit Menu Form")
    public ApiResult<Map<String, Object>> getEditForm(@PathVariable Integer id) {
        Map<String, Object> result = adminMenuService.getEditForm(id);
        return ApiResult.ok(result);
    }

    /**
     * 更新菜单
     * PUT /api/admin/setting/menus/:id
     *
     * PHP Reference: Menus.php::update()
     *
     * Request Body: 同保存菜单
     *
     * Response: { "code": 0, "msg": "Modified successfully" }
     */
    @PutMapping("/menus/{id}")
    @Operation(summary = "更新菜单")
    public ApiResult<String> updateMenu(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        boolean success = adminMenuService.updateMenu(id, data);
        if (success) {
            return ApiResult.ok("Updated successfully", "success");
        } else {
            return ApiResult.fail("Update failed");
        }
    }

    /**
     * 删除菜单
     * DELETE /api/admin/setting/menus/:id
     *
     * PHP Reference: Menus.php::delete()
     *
     * Response: { "code": 0, "msg": "Deleted successfully" }
     */
    @DeleteMapping("/menus/{id}")
    @Operation(summary = "删除菜单")
    public ApiResult<String> deleteMenu(@PathVariable Integer id) {
        boolean success = adminMenuService.deleteMenu(id);
        if (success) {
            return ApiResult.ok("Deleted successfully", "success");
        } else {
            return ApiResult.fail("Delete failed, please try again later");
        }
    }

    /**
     * 显示/隐藏菜单
     * PUT /api/admin/setting/menus/:id/show
     *
     * PHP Reference: Menus.php::show()
     *
     * Request Body:
     * {
     *   "is_show": 1  // 0=隐藏，1=显示
     * }
     *
     * Response: { "code": 0, "msg": "Modified successfully" }
     */
    @PutMapping("/menus/{id}/show")
    @Operation(summary = "显示/隐藏菜单")
    public ApiResult<String> updateMenuShow(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        Integer isShow = (Integer) data.getOrDefault("is_show", 0);
        boolean success = adminMenuService.updateMenuShow(id, isShow);
        if (success) {
            return ApiResult.ok("Updated successfully", "success");
        } else {
            return ApiResult.fail("Update failed");
        }
    }

    /**
     * 获取用户菜单（用于前端显示）
     * GET /api/admin/setting/menus/user
     *
     * PHP Reference: Menus.php::menus()
     *
     * Response:
     * {
     *   "menus": [...],      // 菜单树
     *   "unique": [...]      // 权限标识列表
     * }
     *
     * TODO: 需要从JWT token获取roles和level
     */
    @GetMapping("/menus/user")
    @Operation(summary = "Get User Menu")
    public ApiResult<Map<String, Object>> getUserMenus() {
        // TODO: 从JWT token中获取当前管理员roles和level
        String roles = "";  // 临时
        Integer level = 0;  // 临时

        Map<String, Object> result = adminMenuService.getUserMenus(roles, level);
        return ApiResult.ok(result);
    }
}
