package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.AdminConfigTabService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin Config Tab Controller - 管理员配置分类管理
 * PHP Reference: /app/controller/admin/system/ConfigTab.php
 *
 * 功能说明:
 * 1. GET /api/admin/setting/config_tab - 配置分类列表
 * 2. GET /api/admin/setting/config_tab/create - 获取创建表单
 * 3. POST /api/admin/setting/config_tab - 保存配置分类
 * 4. GET /api/admin/setting/config_tab/:id/edit - 获取编辑表单
 * 5. PUT /api/admin/setting/config_tab/:id - 更新配置分类
 * 6. DELETE /api/admin/setting/config_tab/:id - 删除配置分类
 * 7. PUT /api/admin/setting/config_tab/set_status/:id/:status - 修改状态
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/setting")
@Tag(name = "Admin Setting - Configuration Category Management")
@AllArgsConstructor
public class AdminConfigTabController {

    private final AdminConfigTabService adminConfigTabService;

    /**
     * 获取配置分类列表
     * GET /api/admin/setting/config_tab
     *
     * PHP Reference: ConfigTab.php::index()
     *
     * Query Parameters:
     * - status: 状态筛选（0隐藏，1显示）
     * - title: 分类名称筛选
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "title": "Basic Configuration",
     *     "eng_title": "basic",
     *     ...
     *   }
     * ]
     */
    @GetMapping({"/config_tab", "/config_class"})
    @Operation(summary = "Get Configuration Category List")
    public ApiResult<Map<String, Object>> getConfigTabList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String title) {

        Map<String, Object> filters = new java.util.HashMap<>();
        if (status != null && !status.trim().isEmpty()) {
            filters.put("status", status);
        }
        if (title != null && !title.trim().isEmpty()) {
            filters.put("title", title);
        }

        // PHP: return $this->success($this->services->getConfgTabList($where));
        // Service返回树形结构的list
        List<Map<String, Object>> treeList = adminConfigTabService.getConfigTabList(filters);

        // PHP: return compact('list', 'count');
        // 计算总数(包括所有子节点)
        int totalCount = countAllNodes(treeList);

        Map<String, Object> result = new HashMap<>();
        result.put("list", treeList);
        result.put("count", totalCount);

        return ApiResult.ok(result);
    }

    /**
     * 递归计算树中所有节点数量
     */
    private int countAllNodes(List<Map<String, Object>> tree) {
        int count = tree.size();
        for (Map<String, Object> node : tree) {
            if (node.containsKey("children")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
                count += countAllNodes(children);
            }
        }
        return count;
    }

    /**
     * 获取创建配置分类表单
     * GET /api/admin/setting/config_tab/create
     *
     * PHP Reference: ConfigTab.php::create()
     *
     * Response:
     * {
     *   "tabs": [...]  // 所有分类列表，用于选择父级
     * }
     */
    @GetMapping({"/config_tab/create", "/config_class/create"})
    @Operation(summary = "Get Create Configuration Category Form")
    public ApiResult<Map<String, Object>> getCreateForm() {
        Map<String, Object> result = adminConfigTabService.getCreateForm();
        return ApiResult.ok(result);
    }

    /**
     * 保存配置分类
     * POST /api/admin/setting/config_tab
     *
     * PHP Reference: ConfigTab.php::save()
     *
     * Request Body:
     * {
     *   "title": "Basic Configuration",
     *   "eng_title": "basic",
     *   "status": 1,
     *   "icon": "setting",
     *   "type": 0,
     *   "sort": 0,
     *   "pid": 0
     * }
     *
     * Response: { "code": 0, "msg": "Configuration category added successfully" }
     */
    @PostMapping({"/config_tab", "/config_class"})
    @Operation(summary = "Save Configuration Category")
    public ApiResult<String> createConfigTab(@RequestBody Map<String, Object> data) {
        adminConfigTabService.createConfigTab(data);
        return ApiResult.ok("Configuration category added successfully", "success");
    }

    /**
     * 获取编辑配置分类表单
     * GET /api/admin/setting/config_tab/:id/edit
     *
     * PHP Reference: ConfigTab.php::edit()
     *
     * Response:
     * {
     *   "tab": {...},     // 当前分类信息
     *   "tabs": [...]     // 所有分类列表，用于选择父级
     * }
     */
    @GetMapping({"/config_tab/{id}/edit", "/config_class/{id}/edit"})
    @Operation(summary = "Get Edit Configuration Category Form")
    public ApiResult<Map<String, Object>> getEditForm(@PathVariable Integer id) {
        Map<String, Object> result = adminConfigTabService.getEditForm(id);
        return ApiResult.ok(result);
    }

    /**
     * 更新配置分类
     * PUT /api/admin/setting/config_tab/:id
     *
     * PHP Reference: ConfigTab.php::update()
     *
     * Request Body: 同保存配置分类
     *
     * Response: { "code": 0, "msg": "Updated successfully" }
     */
    @PutMapping({"/config_tab/{id}", "/config_class/{id}"})
    @Operation(summary = "Update Configuration Category")
    public ApiResult<String> updateConfigTab(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        boolean success = adminConfigTabService.updateConfigTab(id, data);
        if (success) {
            return ApiResult.ok("Updated successfully", "success");
        } else {
            return ApiResult.fail("Update failed");
        }
    }

    /**
     * 删除配置分类
     * DELETE /api/admin/setting/config_tab/:id
     *
     * PHP Reference: ConfigTab.php::delete()
     *
     * Response: { "code": 0, "msg": "Deleted successfully" }
     */
    @DeleteMapping({"/config_tab/{id}", "/config_class/{id}"})
    @Operation(summary = "Delete Configuration Category")
    public ApiResult<String> deleteConfigTab(@PathVariable Integer id) {
        boolean success = adminConfigTabService.deleteConfigTab(id);
        if (success) {
            return ApiResult.ok("Deleted successfully", "success");
        } else {
            return ApiResult.fail("Delete failed, please try again later");
        }
    }

    /**
     * 修改配置分类状态
     * PUT /api/admin/setting/config_tab/set_status/:id/:status
     *
     * PHP Reference: ConfigTab.php::set_status()
     *
     * Response: { "code": 0, "msg": "Hidden successfully" } or { "code": 0, "msg": "Shown successfully" }
     */
    @PutMapping({"/config_tab/set_status/{id}/{status}", "/config_class/set_status/{id}/{status}"})
    @Operation(summary = "Modify Configuration Category Status")
    public ApiResult<String> updateStatus(@PathVariable Integer id, @PathVariable Integer status) {
        adminConfigTabService.updateStatus(id, status);
        return ApiResult.ok(status == 0 ? "Hidden successfully" : "Shown successfully", "success");
    }
}
