package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatServiceDialogueRecordEntity;
import io.renren.crmchat.service.AdminServiceDialogueRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin Service Dialogue Record Controller - 管理员对话记录管理
 * PHP Reference: /app/controller/admin/chat/ServiceDialogueRecord.php
 *
 * 功能说明:
 * 1. GET /api/admin/chat/record - 查看所有聊天记录
 * 2. GET /api/admin/chat/record/list - 查看所有聊天用户列表
 * 3. GET /api/admin/chat/record_kefu - 获取所有客服(用于筛选)
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/chat")
@Tag(name = "Admin Service Dialogue Record")
@AllArgsConstructor
public class AdminServiceDialogueRecordController extends BaseController {

    private final AdminServiceDialogueRecordService adminServiceDialogueRecordService;

    /**
     * 获取所有客服列表(用于筛选)
     * GET /api/admin/chat/record_kefu
     *
     * PHP Reference: ServiceDialogueRecord.php::kefu()
     *
     * Response:
     * [
     *   {"appid": "...", "id": 1, "nickname": "Agent 1"},
     *   {"appid": "...", "id": 2, "nickname": "Agent 2"}
     * ]
     */
    @GetMapping("/record_kefu")
    @Operation(summary = "Get All Customer Service Agents")
    public ApiResult<List<Map<String, Object>>> getAllKefuList() {
        String appid = currentAppid();

        List<Map<String, Object>> result = adminServiceDialogueRecordService.getAllKefuList(appid);
        return ApiResult.ok(result);
    }

    /**
     * 获取对话用户记录列表
     * GET /api/admin/chat/record/list
     *
     * PHP Reference: ServiceDialogueRecord.php::record()
     *
     * Query Parameters:
     * - title: 用户昵称搜索
     * - time: 时间范围
     *
     * Response:
     * {
     *   "list": [...],
     *   "count": 0
     * }
     *
     * TODO: 需要ChatServiceRecordEntity支持完整实现
     */
    @GetMapping("/record/list")
    @Operation(summary = "Get Conversation User List")
    public ApiResult<Map<String, Object>> getAdminUserRecodeList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String time) {

        String appid = currentAppid();

        Map<String, Object> where = new HashMap<>();
        if (title != null) where.put("title", title);
        if (time != null) where.put("time", time);
        where.put("delete", 1);

        Map<String, Object> result = adminServiceDialogueRecordService.getAdminUserRecodeList(where, appid);
        return ApiResult.ok(result);
    }

    /**
     * 获取对话记录列表
     * GET /api/admin/chat/record
     *
     * PHP Reference: ServiceDialogueRecord.php::index()
     *
     * Query Parameters:
     * - kefu_id: 客服ID筛选
     * - msn: 消息内容搜索
     * - time: 时间范围
     * - appid: APPID筛选
     * - user_id: 用户ID筛选
     *
     * Response: [...]
     */
    @GetMapping("/dialogue_record")
    @Operation(summary = "Get Conversation History List")
    public ApiResult<List<ChatServiceDialogueRecordEntity>> getDialogueRecord(
            @RequestParam(required = false) Integer kefu_id,
            @RequestParam(required = false) String msn,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String appid,
            @RequestParam(defaultValue = "0") Integer user_id) {

        String currentAppid = resolveAppid(appid);

        Map<String, Object> where = new HashMap<>();
        if (kefu_id != null && kefu_id > 0) {
            where.put("kefu_id", kefu_id);
        }
        if (msn != null) where.put("msn", msn);
        if (time != null) where.put("time", time);
        if (appid != null) where.put("appid", appid);
        if (user_id > 0) where.put("user_id", user_id);

        List<ChatServiceDialogueRecordEntity> result = adminServiceDialogueRecordService.getDialogueRecord(where, currentAppid);
        return ApiResult.ok(result);
    }
}
