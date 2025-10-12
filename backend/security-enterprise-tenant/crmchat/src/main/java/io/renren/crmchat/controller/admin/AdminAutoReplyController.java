package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatAutoReplyEntity;
import io.renren.crmchat.service.AdminAutoReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Admin Auto Reply Controller - 管理员自动回复管理
 * PHP Reference: /app/controller/admin/chat/AutoReply.php
 *
 * 功能说明:
 * 1. GET /api/admin/chat/reply - 自动回复列表
 * 2. GET /api/admin/chat/reply/:id - 获取自动回复表单
 * 3. POST /api/admin/chat/reply/:id - 保存自动回复
 * 4. DELETE /api/admin/chat/reply/:id - 删除自动回复
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/chat/reply")
@Tag(name = "Admin Auto Reply - Auto Reply Management")
@AllArgsConstructor
public class AdminAutoReplyController extends BaseController {

    private final AdminAutoReplyService adminAutoReplyService;

    /**
     * 获取自动回复列表
     * GET /api/admin/chat/reply
     *
     * PHP Reference: AutoReply.php::index()
     *
     * Query Parameters:
     * - user_id: 客服用户ID(可选)
     * - appid: APPID(可选)
     *
     * Response: [...]
     */
    @GetMapping
    @Operation(summary = "Get Auto Reply List")
    public ApiResult<List<ChatAutoReplyEntity>> getList(
            @RequestParam(defaultValue = "0") Integer user_id,
            @RequestParam(required = false) String appid) {

        String currentAppid = resolveAppid(appid);

        List<ChatAutoReplyEntity> result = adminAutoReplyService.getList(currentAppid, user_id);
        return ApiResult.ok(result);
    }

    /**
     * 获取自动回复表单
     * GET /api/admin/chat/reply/:id
     *
     * PHP Reference: AutoReply.php::create()
     *
     * Query Parameters:
     * - user_id: 客服用户ID
     * - appid: APPID
     *
     * Response:
     * {
     *   "reply": {...}  // 编辑时返回
     * }
     * 或
     * {
     *   "form_rules": [],
     *   "user_id": 0,
     *   "appid": ""
     * }
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Auto Reply Form")
    public ApiResult<Map<String, Object>> getForm(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") Integer user_id,
            @RequestParam(required = false) String appid) {

        String currentAppid = resolveAppid(appid);

        Map<String, Object> result = adminAutoReplyService.getForm(id, user_id, currentAppid);
        return ApiResult.ok(result);
    }

    /**
     * 保存自动回复(创建或更新)
     * POST /api/admin/chat/reply/:id
     *
     * PHP Reference: AutoReply.php::save()
     *
     * Request Body:
     * {
     *   "keyword": "Keyword",
     *   "content": "Reply Content",
     *   "user_id": 0,
     *   "appid": "",
     *   "sort": 0
     * }
     *
     * Response: { "code": 0, "msg": "Modified successfully" } or { "code": 0, "msg": "Saved successfully" }
     */
    @PostMapping("/{id}")
    @Operation(summary = "Save Auto Reply")
    public ApiResult<String> saveAutoReply(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        String appid = resolveAppid(Objects.toString(data.getOrDefault("appid", null), null));

        String message = adminAutoReplyService.saveAutoReply(id, data, appid);
        return ApiResult.ok(message, "success");
    }

    /**
     * 删除自动回复
     * DELETE /api/admin/chat/reply/:id
     *
     * PHP Reference: AutoReply.php::delete()
     *
     * Response: { "code": 0, "msg": "Deleted successfully" }
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Auto Reply")
    public ApiResult<String> deleteAutoReply(@PathVariable Integer id) {
        String appid = currentAppid();

        adminAutoReplyService.deleteAutoReply(id, appid);
        return ApiResult.ok("Deleted successfully", "success");
    }
}
