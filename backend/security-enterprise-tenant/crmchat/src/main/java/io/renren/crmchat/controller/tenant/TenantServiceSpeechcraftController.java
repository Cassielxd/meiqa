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
     * - appid: 租户appid（必填）
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "title": "Welcome Message",
     *     "message": "您好，有什么可以帮您？",
     *     "cate_id": 1,
     *     "sort": 0,
     *     "add_time": 1234567890,
     *     "kefu_id": 0,
     *     "appid": "202517350001234"
     *   }
     * ]
     */
    @GetMapping
    @Operation(summary = "Get Quick Reply List")
    public ApiResult<List<ChatServiceSpeechcraftEntity>> getSpeechcraftList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String cate_id) {

        String appid = requireAppid();
        log.info("[租户API] chat/speechcraft/list - appid={}, title={}, message={}, cate_id={}",
                 appid, title, message, cate_id);

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

        List<ChatServiceSpeechcraftEntity> list = tenantServiceSpeechcraftService.getSpeechcraftList(filters);
        log.info("[租户API] chat/speechcraft/list - appid={}, count={}", appid, list.size());
        return ApiResult.ok(list);
    }

    /**
     * 获取快捷回复详情
     * GET /api/tenant/chat/speechcraft/:id
     *
     * PHP Reference: ServiceSpeechcraft.php::read()
     *
     * Response:
     * {
     *   "id": 1,
     *   "title": "Welcome Message",
     *   "message": "您好，有什么可以帮您？",
     *   ...
     * }
     */
    @GetMapping("/{id}")
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
     * 创建快捷回复
     * POST /api/tenant/chat/speechcraft
     *
     * PHP Reference: ServiceSpeechcraft.php::save()
     *
     * Request Body:
     * {
     *   "title": "Welcome Message",                // 必填
     *   "message": "您好，有什么可以帮您？",  // 必填
     *   "cate_id": 1,                    // 可选
     *   "sort": 0                        // 可选
     * }
     *
     * Response: { "code": 0, "msg": "Quick reply created successfully" }
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
     *
     * Request Body:
     * {
     *   "title": "Welcome Message",                // 必填
     *   "message": "您好，有什么可以帮您？",  // 必填
     *   "cate_id": 1,                    // 可选
     *   "sort": 0                        // 可选
     * }
     *
     * Response: { "code": 0, "msg": "修改成功" }
     */
    @PutMapping("/{id}")
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
     *
     * Response: { "code": 0, "msg": "删除成功" }
     */
    @DeleteMapping("/{id}")
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
