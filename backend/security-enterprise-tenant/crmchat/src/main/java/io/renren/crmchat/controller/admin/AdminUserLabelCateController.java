package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatUserLabelCateEntity;
import io.renren.crmchat.service.AdminUserLabelCateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin User Label Cate Controller - 管理员用户标签分类管理
 * PHP Reference: /app/controller/admin/user/LabelCate.php
 *
 * 功能说明:
 * 1. GET /api/admin/user/label_cate - 分类列表
 * 2. GET /api/admin/user/label_cate/create - 获取创建表单
 * 3. POST /api/admin/user/label_cate - 保存分类
 * 4. GET /api/admin/user/label_cate/:id/edit - 获取编辑表单
 * 5. PUT /api/admin/user/label_cate/:id - 更新分类
 * 6. POST /api/admin/user/label_cate/move - 批量排序
 * 7. DELETE /api/admin/user/label_cate/:id - 删除分类
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/user")
@Tag(name = "Admin User - Tag Category Management")
@AllArgsConstructor
public class AdminUserLabelCateController extends BaseController {

    private final AdminUserLabelCateService adminUserLabelCateService;

    /**
     * 获取标签分类列表
     * GET /api/admin/user/label_cate
     *
     * PHP Reference: LabelCate.php::index()
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "name": "Customer Category",
     *     "sort": 1,
     *     ...
     *   }
     * ]
     */
    @GetMapping({"/label_cate", "/label/cate"})
    @Operation(summary = "Get Tag Category List")
    public ApiResult<List<ChatUserLabelCateEntity>> getCateList() {
        // TODO: 从JWT token中获取当前租户appid
        String appid = currentAppid();

        List<ChatUserLabelCateEntity> result = adminUserLabelCateService.getCateList(appid);
        return ApiResult.ok(result);
    }

    /**
     * 获取创建分类表单
     * GET /api/admin/user/label_cate/create
     *
     * PHP Reference: LabelCate.php::create()
     *
     * Response:
     * {
     *   "title": "添加标签分类",
     *   "action": "user/label/cate",
     *   "method": "POST",
     *   "rules": [
     *     {
     *       "type": "input",
     *       "field": "name",
     *       "title": "分类名称",
     *       "value": "",
     *       "props": {
     *         "placeholder": "请输入分类名称"
     *       },
     *       "validate": [
     *         {
     *           "required": true,
     *           "message": "分类名称不能为空",
     *           "trigger": "blur"
     *         }
     *       ]
     *     }
     *   ],
     *   "config": {
     *     "form": {...},
     *     "submitBtn": {...},
     *     "resetBtn": {...}
     *   },
     *   "info": "",
     *   "status": true
     * }
     */
    @GetMapping({"/label_cate/create", "/label/cate/create"})
    @Operation(summary = "Get Create Category Form")
    public ApiResult<Map<String, Object>> getCreateForm() {
        Map<String, Object> result = adminUserLabelCateService.getCreateForm();
        return ApiResult.ok(result);
    }

    /**
     * 保存标签分类
     * POST /api/admin/user/label_cate
     *
     * PHP Reference: LabelCate.php::save()
     *
     * Request Body:
     * {
     *   "name": "客户分类",
     *   "sort": 0
     * }
     *
     * Response: { "code": 0, "msg": "添加成功" }
     */
    @PostMapping({"/label_cate", "/label/cate"})
    @Operation(summary = "保存标签分类")
    public ApiResult<String> saveCate(@RequestBody Map<String, Object> data) {
        // TODO: 从JWT token中获取当前租户appid
        String appid = currentAppid();

        adminUserLabelCateService.saveCate(data, appid);
        return ApiResult.ok("Added successfully", "success");
    }

    /**
     * 获取编辑分类表单
     * GET /api/admin/user/label_cate/:id/edit
     *
     * PHP Reference: LabelCate.php::edit()
     *
     * Response:
     * {
     *   "cate": {...},
     *   "form_rules": [...]
     * }
     */
    @GetMapping({"/label_cate/{id}/edit", "/label/cate/{id}/edit"})
    @Operation(summary = "Get Edit Category Form")
    public ApiResult<Map<String, Object>> getEditForm(@PathVariable Integer id) {
        // TODO: 从JWT token中获取当前租户appid
        String appid = currentAppid();

        Map<String, Object> result = adminUserLabelCateService.getEditForm(id, appid);
        return ApiResult.ok(result);
    }

    /**
     * 更新标签分类
     * PUT /api/admin/user/label_cate/:id
     *
     * PHP Reference: LabelCate.php::update()
     *
     * Request Body:
     * {
     *   "name": "客户分类",
     *   "sort": 0
     * }
     *
     * Response: { "code": 0, "msg": "修改成功" }
     */
    @PutMapping({"/label_cate/{id}", "/label/cate/{id}"})
    @Operation(summary = "更新标签分类")
    public ApiResult<String> updateCate(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        // TODO: 从JWT token中获取当前租户appid
        String appid = currentAppid();

        boolean success = adminUserLabelCateService.updateCate(id, data, appid);
        if (success) {
            return ApiResult.ok("Updated successfully", "success");
        } else {
            return ApiResult.fail("Update failed");
        }
    }

    /**
     * 批量排序
     * POST /api/admin/user/label_cate/move
     *
     * PHP Reference: LabelCate.php::move()
     *
     * Request Body:
     * {
     *   "ids": [1, 2, 3]
     * }
     *
     * Response: { "code": 0, "msg": "修改成功" }
     */
    @PostMapping({"/label_cate/move", "/label/cate/move"})
    @Operation(summary = "批量排序")
    public ApiResult<String> labelMove(@RequestBody Map<String, Object> data) {
        // TODO: 从JWT token中获取当前租户appid
        String appid = currentAppid();

        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) data.get("ids");

        adminUserLabelCateService.labelMove(ids, appid);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 删除标签分类
     * DELETE /api/admin/user/label_cate/:id
     *
     * PHP Reference: LabelCate.php::delete()
     *
     * Response: { "code": 0, "msg": "删除成功" }
     */
    @DeleteMapping({"/label_cate/{id}", "/label/cate/{id}"})
    @Operation(summary = "删除标签分类")
    public ApiResult<String> deleteCate(@PathVariable Integer id) {
        // TODO: 从JWT token中获取当前租户appid
        String appid = currentAppid();

        boolean success = adminUserLabelCateService.deleteCate(id, appid);
        if (success) {
            return ApiResult.ok("Deleted successfully", "success");
        } else {
            return ApiResult.fail("Delete failed, please try again later");
        }
    }
}
