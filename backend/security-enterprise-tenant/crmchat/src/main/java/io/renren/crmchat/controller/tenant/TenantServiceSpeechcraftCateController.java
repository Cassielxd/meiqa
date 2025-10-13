package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatServiceSpeechcraftCateEntity;
import io.renren.crmchat.service.TenantServiceSpeechcraftCateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.renren.crmchat.security.TenantSecurityUtils.requireAppid;

/**
 * Tenant API - 快捷回复分类管理
 * PHP Reference: /app/controller/tenant/chat/ServiceSpeechcraftCate.php
 *
 * @author CRMChat Team
 */
@Slf4j
@RestController
@RequestMapping("/api/tenant/chat/speechcraftcate")
@Tag(name = "Tenant - Quick Reply Category Management")
@AllArgsConstructor
public class TenantServiceSpeechcraftCateController {

    private final TenantServiceSpeechcraftCateService tenantServiceSpeechcraftCateService;

    /**
     * 获取快捷回复分类列表
     * GET /api/tenant/chat/speechcraftcate
     */
    @GetMapping
    @Operation(summary = "Get Quick Reply Category List")
    public ApiResult<List<ChatServiceSpeechcraftCateEntity>> getCateList(
            @RequestParam(required = false) String name) {

        String appid = requireAppid();
        log.info("[租户API] chat/speechcraftcate/list - appid={}, name={}", appid, name);

        Map<String, Object> filters = new HashMap<>();
        if (name != null && !name.trim().isEmpty()) {
            filters.put("name", name);
        }

        List<ChatServiceSpeechcraftCateEntity> list = tenantServiceSpeechcraftCateService.getCateList(filters);
        log.info("[租户API] chat/speechcraftcate/list - appid={}, count={}", appid, list.size());
        return ApiResult.ok(list);
    }

    /**
     * 获取创建分类表单配置
     * GET /api/tenant/chat/speechcraftcate/create
     *
     * PHP Reference: ServiceSpeechcraftCate.php::create()
     *
     * 返回FormBuilder格式的表单配置，用于前端渲染表单
     *
     * Response:
     * {
     *   "rules": [
     *     {"type": "input", "field": "name", "title": "分类名称", "value": "", "validate": [...]},
     *     {"type": "inputNumber", "field": "sort", "title": "排序", "value": 0}
     *   ],
     *   "title": "添加分类",
     *   "action": "/chat/speechcraftcate",
     *   "method": "POST",
     *   "info": "",
     *   "status": true
     * }
     */
    @GetMapping("/create")
    @Operation(summary = "Get Create Category Form Configuration")
    public ApiResult<Map<String, Object>> getCreateForm() {
        String appid = requireAppid();
        log.info("[租户API] chat/speechcraftcate/create - appid={}", appid);

        Map<String, Object> formConfig = tenantServiceSpeechcraftCateService.getCreateForm();
        return ApiResult.ok(formConfig);
    }

    /**
     * 获取快捷回复分类详情
     * GET /api/tenant/chat/speechcraftcate/:id
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Quick Reply Category Details")
    public ApiResult<ChatServiceSpeechcraftCateEntity> getCateDetail(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        String appid = requireAppid();
        log.info("[租户API] chat/speechcraftcate/read - appid={}, id={}", appid, id);

        ChatServiceSpeechcraftCateEntity cate = tenantServiceSpeechcraftCateService.getCateDetail(id);
        return ApiResult.ok(cate);
    }

    /**
     * 获取编辑分类表单配置
     * GET /api/tenant/chat/speechcraftcate/:id/edit
     *
     * PHP Reference: ServiceSpeechcraftCate.php::edit()
     *
     * 返回FormBuilder格式的表单配置，包含现有分类数据
     *
     * Response:
     * {
     *   "rules": [
     *     {"type": "input", "field": "name", "title": "分类名称", "value": "现有名称", "validate": [...]},
     *     {"type": "inputNumber", "field": "sort", "title": "排序", "value": 10}
     *   ],
     *   "title": "修改分类",
     *   "action": "/chat/speechcraftcate/1",
     *   "method": "PUT",
     *   "info": "",
     *   "status": true
     * }
     */
    @GetMapping("/{id}/edit")
    @Operation(summary = "Get Edit Category Form Configuration")
    public ApiResult<Map<String, Object>> getEditForm(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        String appid = requireAppid();
        log.info("[租户API] chat/speechcraftcate/edit - appid={}, id={}", appid, id);

        Map<String, Object> formConfig = tenantServiceSpeechcraftCateService.getEditForm(id);
        return ApiResult.ok(formConfig);
    }

    /**
     * 创建快捷回复分类
     * POST /api/tenant/chat/speechcraftcate
     */
    @PostMapping
    @Operation(summary = "创建快捷回复分类")
    public ApiResult<String> createCate(@RequestBody Map<String, Object> data) {
        String appid = requireAppid();
        log.info("[租户API] chat/speechcraftcate/create - appid={}, data={}", appid, data);

        tenantServiceSpeechcraftCateService.createCate(data);
        return ApiResult.ok("Added successfully", "success");
    }

    /**
     * 更新快捷回复分类
     * PUT /api/tenant/chat/speechcraftcate/:id
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新快捷回复分类")
    public ApiResult<String> updateCate(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        String appid = requireAppid();
        log.info("[租户API] chat/speechcraftcate/update - appid={}, id={}, data={}", appid, id, data);

        tenantServiceSpeechcraftCateService.updateCate(id, data);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 删除快捷回复分类
     * DELETE /api/tenant/chat/speechcraftcate/:id
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除快捷回复分类")
    public ApiResult<String> deleteCate(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        String appid = requireAppid();
        log.info("[租户API] chat/speechcraftcate/delete - appid={}, id={}", appid, id);

        tenantServiceSpeechcraftCateService.deleteCate(id);
        return ApiResult.ok("Deleted successfully", "success");
    }
}
