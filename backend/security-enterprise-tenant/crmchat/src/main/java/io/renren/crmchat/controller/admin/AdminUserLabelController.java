package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatUserLabelEntity;
import io.renren.crmchat.service.AdminUserLabelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin User Label Controller - 管理员用户标签管理
 * PHP Reference: /app/controller/admin/user/Label.php
 *
 * 功能说明:
 * 1. GET /api/admin/user/label - 标签列表
 * 2. GET /api/admin/user/label/create - 获取创建表单
 * 3. POST /api/admin/user/label - 保存标签
 * 4. POST /api/admin/user/label/move - 批量排序
 * 5. GET /api/admin/user/label/:id/edit - 获取编辑表单
 * 6. PUT /api/admin/user/label/:id - 更新标签
 * 7. DELETE /api/admin/user/label/:id - 删除标签
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/user")
@Tag(name = "Admin User - Tag Management")
@AllArgsConstructor
public class AdminUserLabelController extends BaseController {

    private final AdminUserLabelService adminUserLabelService;

    /**
     * 获取标签列表
     * GET /api/admin/user/label
     *
     * PHP Reference: Label.php::index()
     *
     * Query Parameters:
     * - cate_id: 分类ID筛选
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "label": "VIP Customer",
     *     "cate_id": 1,
     *     ...
     *   }
     * ]
     */
    @GetMapping("/label")
    @Operation(summary = "Get Tag List")
    public ApiResult<List<ChatUserLabelEntity>> getLabelList(
            @RequestParam(required = false) Integer cate_id) {

        String appid = currentAppid();

        Map<String, Object> filters = new java.util.HashMap<>();
        if (cate_id != null) {
            filters.put("cate_id", cate_id);
        }

        List<ChatUserLabelEntity> result = adminUserLabelService.getLabelList(filters, appid);
        return ApiResult.ok(result);
    }

    /**
     * 获取创建标签表单
     * GET /api/admin/user/label/create
     *
     * PHP Reference: Label.php::create()
     *
     * Response:
     * {
 *   "title": "Create Label",
     *   "action": "user/label",
     *   "method": "POST",
     *   "rules": [
     *     {
     *       "type": "select",
     *       "field": "cate_id",
 *       "title": "Label Category",
     *       "value": 0,
 *       "options": [{value: 1, label: "Category 1"}, ...],
 *       "validate": [{"required": true, "message": "Label category is required"}]
     *     },
     *     {
     *       "type": "input",
     *       "field": "label",
 *       "title": "Label Name",
     *       "value": "",
 *       "validate": [{"required": true, "message": "Label name is required"}]
     *     }
     *   ]
     * }
     */
    @GetMapping("/label/create")
    @Operation(summary = "Get Create Tag Form")
    public ApiResult<Map<String, Object>> getCreateForm() {
        String appid = currentAppid();
        Map<String, Object> result = adminUserLabelService.getCreateForm(appid);
        return ApiResult.ok(result);
    }

    /**
     * 保存标签
     * POST /api/admin/user/label
     *
     * PHP Reference: Label.php::save()
     *
     * Request Body:
     * {
     *   "cate_id": 1,
 *   "label": "VIP Customer",
     *   "sort": 0
     * }
     *
 * Response: { "code": 0, "msg": "Saved successfully" }
     */
    @PostMapping("/label")
    @Operation(summary = "Save Tag")
    public ApiResult<String> saveLabel(@RequestBody Map<String, Object> data) {
        String appid = currentAppid();

        adminUserLabelService.saveLabel(data, appid);
        return ApiResult.ok("Saved successfully", "success");
    }

    /**
     * 批量排序
     * POST /api/admin/user/label/move
     *
     * PHP Reference: Label.php::move()
     *
     * Request Body:
     * {
     *   "ids": [1, 2, 3],
     *   "page": 0
     * }
     *
     * Response: { "code": 0, "msg": "Modified successfully" }
     */
    @PostMapping("/label/move")
    @Operation(summary = "Batch Sort")
    public ApiResult<String> moveSort(@RequestBody Map<String, Object> data) {
        String appid = currentAppid();

        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) data.get("ids");
        Integer page = (Integer) data.getOrDefault("page", 0);

        adminUserLabelService.moveSort(ids, page, appid);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 获取编辑标签表单
     * GET /api/admin/user/label/:id/edit
     *
     * PHP Reference: Label.php::edit()
     *
     * Response:
     * {
     *   "label": {...},
     *   "form_rules": [...]
     * }
     */
    @GetMapping("/label/{id}/edit")
    @Operation(summary = "Get Edit Tag Form")
    public ApiResult<Map<String, Object>> getEditForm(@PathVariable Integer id) {
        String appid = currentAppid();

        Map<String, Object> result = adminUserLabelService.getEditForm(id, appid);
        return ApiResult.ok(result);
    }

    /**
     * 更新标签
     * PUT /api/admin/user/label/:id
     *
     * PHP Reference: Label.php::update()
     *
     * Request Body:
     * {
     *   "cate_id": 1,
 *   "label": "VIP Customer",
     *   "sort": 0
     * }
     *
 * Response: { "code": 0, "msg": "Updated successfully" }
     */
    @PutMapping("/label/{id}")
    @Operation(summary = "Update Label")
    public ApiResult<String> updateLabel(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        String appid = currentAppid();

        adminUserLabelService.updateLabel(id, data, appid);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 删除标签
     * DELETE /api/admin/user/label/:id
     *
     * PHP Reference: Label.php::delete()
     *
     * Response: { "code": 0, "msg": "Deleted successfully" }
     */
    @DeleteMapping("/label/{id}")
    @Operation(summary = "Delete Label")
    public ApiResult<String> deleteLabel(@PathVariable Integer id) {
        String appid = currentAppid();

        adminUserLabelService.deleteLabel(id, appid);
        return ApiResult.ok("Deleted successfully", "success");
    }
}
