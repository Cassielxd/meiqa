package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.AdminApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin Application Controller - 管理员应用管理
 * PHP Reference: /app/controller/admin/Application.php
 *
 * 功能说明:
 * 1. GET /api/admin/app - 获取应用列表
 * 2. GET /api/admin/app/create - 获取创建表单
 * 3. POST /api/admin/app - 保存应用
 * 4. GET /api/admin/app/:id/edit - 获取编辑表单
 * 5. PUT /api/admin/app/:id - 更新应用
 * 6. DELETE /api/admin/app/:id - 删除应用
 * 7. PUT /api/admin/app/reset/:id - 重置Token
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/app")
@Tag(name = "Admin Application - 应用管理")
@AllArgsConstructor
public class AdminApplicationController {

    private final AdminApplicationService adminApplicationService;

    /**
     * 获取应用列表
     * GET /api/admin/app
     *
     * PHP Reference: Application.php::index()
     *
     * Query Parameters:
     * - name: 应用名称模糊搜索
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
    @Operation(summary = "Get Application List")
    public ApiResult<Map<String, Object>> getApplicationList(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {

        Map<String, Object> where = new HashMap<>();
        if (name != null) where.put("name", name);
        where.put("page", page);
        where.put("limit", limit);

        Map<String, Object> result = adminApplicationService.getList(where);
        return ApiResult.ok(result);
    }

    /**
     * 获取创建表单
     * GET /api/admin/app/create
     *
     * PHP Reference: Application.php::create()
     *
     * Response:
     * {
     *   "form_rules": []
     * }
     */
    @GetMapping("/create")
    @Operation(summary = "Get Create Form")
    public ApiResult<Map<String, Object>> getCreateForm() {
        Map<String, Object> result = adminApplicationService.getCreateForm();
        return ApiResult.ok(result);
    }

    /**
     * 保存应用
     * POST /api/admin/app
     *
     * PHP Reference: Application.php::save()
     *
     * Request Body:
     * {
     *   "icon": "Icon URL",
     *   "name": "应用名称",
     *   "introduce": "应用介绍"
     * }
     *
     * Response: { "code": 0, "msg": "Saved successfully" }
     */
    @PostMapping
    @Operation(summary = "Save Application")
    public ApiResult<String> saveApplication(@RequestBody Map<String, Object> data) {
        String message = adminApplicationService.saveApplication(data);
        return ApiResult.ok(message, "success");
    }

    /**
     * 获取编辑表单
     * GET /api/admin/app/:id/edit
     *
     * PHP Reference: Application.php::edit()
     *
     * Response:
     * {
     *   "app": {...},
     *   "form_rules": []
     * }
     */
    @GetMapping("/{id}/edit")
    @Operation(summary = "Get Edit Form")
    public ApiResult<Map<String, Object>> getEditForm(@PathVariable Integer id) {
        Map<String, Object> result = adminApplicationService.getEditForm(id);
        return ApiResult.ok(result);
    }

    /**
     * 更新应用
     * PUT /api/admin/app/:id
     *
     * PHP Reference: Application.php::update()
     *
     * Request Body:
     * {
     *   "icon": "Icon URL",
     *   "name": "应用名称",
     *   "introduce": "应用介绍"
     * }
     *
     * Response: { "code": 0, "msg": "保存成功" }
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新应用")
    public ApiResult<String> updateApplication(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        String message = adminApplicationService.updateApplication(id, data);
        return ApiResult.ok(message, "success");
    }

    /**
     * 删除应用
     * DELETE /api/admin/app/:id
     *
     * PHP Reference: Application.php::delete()
     *
     * Response: { "code": 0, "msg": "删除成功" }
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除应用")
    public ApiResult<String> deleteApplication(@PathVariable Integer id) {
        String message = adminApplicationService.deleteApplication(id);
        return ApiResult.ok(message, "success");
    }

    /**
     * 重置Token
     * PUT /api/admin/app/reset/:id
     *
     * PHP Reference: Application.php::reset()
     *
     * Response:
     * {
     *   "rand": 1234,
     *   "timestamp": 1234567890,
     *   "app_secret": "...",
     *   "token": "...",
     *   "token_md5": "..."
     * }
     */
    @PutMapping("/reset/{id}")
    @Operation(summary = "重置Token")
    public ApiResult<Map<String, Object>> resetToken(@PathVariable Integer id) {
        Map<String, Object> result = adminApplicationService.resetToken(id);
        return ApiResult.ok(result);
    }
}
