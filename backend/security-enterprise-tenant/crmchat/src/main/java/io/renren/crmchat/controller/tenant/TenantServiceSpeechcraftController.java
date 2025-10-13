package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatServiceSpeechcraftEntity;
import io.renren.crmchat.service.TenantServiceSpeechcraftService;
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
 * Tenant API - 快捷回复管理
 * PHP Reference: /app/controller/tenant/chat/ServiceSpeechcraft.php
 *
 * @author CRMChat Team
 */
@Slf4j
@RestController
@RequestMapping("/api/tenant/chat/speechcraft")
@Tag(name = "Tenant - Quick Reply Management")
@AllArgsConstructor
public class TenantServiceSpeechcraftController {

    private final TenantServiceSpeechcraftService tenantServiceSpeechcraftService;

    /**
     * 获取快捷回复列表
     * GET /api/tenant/chat/speechcraft
     *
     * PHP Reference: ServiceSpeechcraft.php::index()
     *
     * Query Parameters:
     * - title: 标题（可选，模糊查询）
     * - message: 内容（可选，模糊查询）
     * - cate_id: 分类ID（可选）
     * - page: 页码（可选，默认1）
     * - limit: 每页数量（可选，默认20）
     * - appid: 租户appid（必填）
     *
     * Response:
     * {
     *   "list": [
     *     {
     *       "id": 1,
     *       "title": "Welcome Message",
     *       "message": "您好，有什么可以帮您？",
     *       "cate_id": 1,
     *       "sort": 0,
     *       "add_time": 1234567890,
     *       "kefu_id": 0,
     *       "appid": "202517350001234"
     *     }
     *   ],
     *   "count": 1
     * }
     */
    @GetMapping
    @Operation(summary = "Get Quick Reply List")
    public ApiResult<Map<String, Object>> getSpeechcraftList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String cate_id,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {

        String appid = requireAppid();
        log.info("[租户API] chat/speechcraft/list - appid={}, title={}, message={}, cate_id={}, page={}, limit={}",
                 appid, title, message, cate_id, page, limit);

        Map<String, Object> filters = new HashMap<>();
        if (title != null && !title.trim().isEmpty()) {
            filters.put("title", title);
        }
        if (message != null && !message.trim().isEmpty()) {
            filters.put("message", message);
        }
        if (cate_id != null && !cate_id.trim().isEmpty()) {
            filters.put("cate_id", cate_id);
        }

        // PHP returns {list: [...], count: X}
        Map<String, Object> result = tenantServiceSpeechcraftService.getSpeechcraftList(filters, page, limit);
        log.info("[租户API] chat/speechcraft/list - appid={}, count={}", appid, result.get("count"));
        return ApiResult.ok(result);
    }

    /**
     * 获取快捷回复详情
     * GET /api/tenant/chat/speechcraft/:id
     *
     * PHP Reference: ServiceSpeechcraft.php::read()
     * IMPORTANT: Regex [0-9]+ to avoid route conflicts with /create and /{id}/edit
     */
    @GetMapping("/{id:[0-9]+}")
    @Operation(summary = "Get Quick Reply Details")
    public ApiResult<ChatServiceSpeechcraftEntity> getSpeechcraftDetail(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        String appid = requireAppid();
        log.info("[租户API] chat/speechcraft/read - appid={}, id={}", appid, id);

        ChatServiceSpeechcraftEntity speechcraft = tenantServiceSpeechcraftService.getSpeechcraftDetail(id);
        return ApiResult.ok(speechcraft);
    }

    /**
     * 获取创建快捷回复表单配置
     * GET /api/tenant/chat/speechcraft/create
     *
     * PHP Reference: ServiceSpeechcraft.php::create()
     */
    @GetMapping("/create")
    @Operation(summary = "Get Create Speechcraft Form Configuration")
    public ApiResult<Map<String, Object>> getCreateForm() {
        String appid = requireAppid();
        log.info("[租户API] chat/speechcraft/create - appid={}", appid);

        Map<String, Object> formConfig = tenantServiceSpeechcraftService.getCreateForm();
        return ApiResult.ok(formConfig);
    }

    /**
     * 获取编辑快捷回复表单配置
     * GET /api/tenant/chat/speechcraft/:id/edit
     *
     * PHP Reference: ServiceSpeechcraft.php::edit()
     */
    @GetMapping("/{id:[0-9]+}/edit")
    @Operation(summary = "Get Edit Speechcraft Form Configuration")
    public ApiResult<Map<String, Object>> getEditForm(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        String appid = requireAppid();
        log.info("[租户API] chat/speechcraft/edit - appid={}, id={}", appid, id);

        Map<String, Object> formConfig = tenantServiceSpeechcraftService.getEditForm(id);
        return ApiResult.ok(formConfig);
    }

    /**
     * 创建快捷回复
     * POST /api/tenant/chat/speechcraft
     *
     * PHP Reference: ServiceSpeechcraft.php::save()
     */
    @PostMapping
    @Operation(summary = "创建快捷回复")
    public ApiResult<String> createSpeechcraft(@RequestBody Map<String, Object> data) {
        String appid = requireAppid();
        log.info("[租户API] chat/speechcraft/create - appid={}, data={}", appid, data);

        tenantServiceSpeechcraftService.createSpeechcraft(data);
        return ApiResult.ok("Quick reply created successfully", "success");
    }

    /**
     * 更新快捷回复
     * PUT /api/tenant/chat/speechcraft/:id
     *
     * PHP Reference: ServiceSpeechcraft.php::update()
     */
    @PutMapping("/{id:[0-9]+}")
    @Operation(summary = "更新快捷回复")
    public ApiResult<String> updateSpeechcraft(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> data) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        String appid = requireAppid();
        log.info("[租户API] chat/speechcraft/update - appid={}, id={}, data={}", appid, id, data);

        tenantServiceSpeechcraftService.updateSpeechcraft(id, data);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 删除快捷回复
     * DELETE /api/tenant/chat/speechcraft/:id
     *
     * PHP Reference: ServiceSpeechcraft.php::delete()
     */
    @DeleteMapping("/{id:[0-9]+}")
    @Operation(summary = "删除快捷回复")
    public ApiResult<String> deleteSpeechcraft(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        String appid = requireAppid();
        log.info("[租户API] chat/speechcraft/delete - appid={}, id={}", appid, id);

        tenantServiceSpeechcraftService.deleteSpeechcraft(id);
        return ApiResult.ok("Deleted successfully", "success");
    }
}
