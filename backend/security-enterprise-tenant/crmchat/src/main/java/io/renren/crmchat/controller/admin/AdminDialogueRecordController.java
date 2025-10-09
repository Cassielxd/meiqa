package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.AdminDialogueRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin Dialogue Record Controller - 管理员对话记录管理
 * PHP Reference: /app/controller/admin/chat/ServiceDialogueRecord.php
 *
 * 功能说明:
 * 1. GET /api/admin/dialogue/record_kefu - 获取所有客服
 * 2. GET /api/admin/dialogue/record/list - 查看所有聊天用户列表
 * 3. GET /api/admin/dialogue/record - 查看所有聊天记录
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/dialogue")
@Tag(name = "Admin Dialogue - Conversation History Management")
@AllArgsConstructor
public class AdminDialogueRecordController extends BaseController {

    private final AdminDialogueRecordService adminDialogueRecordService;

    @GetMapping("/record_kefu")
    @Operation(summary = "Get All Customer Service Agents")
    public ApiResult<List<Map<String, Object>>> getKefuList() {
        String appid = currentAppid();

        List<Map<String, Object>> result = adminDialogueRecordService.getKefuList(appid);
        return ApiResult.ok(result);
    }

    @GetMapping("/record/list")
    @Operation(summary = "View All Chat Users")
    public ApiResult<Map<String, Object>> getUserRecordList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String time) {

        String appid = currentAppid();

        Map<String, Object> where = new java.util.HashMap<>();
        if (title != null) where.put("title", title);
        if (time != null) where.put("time", time);
        where.put("delete", 1);

        Map<String, Object> result = adminDialogueRecordService.getUserRecordList(where, appid);
        return ApiResult.ok(result);
    }

    @GetMapping("/record")
    @Operation(summary = "View All Chat History")
    public ApiResult<Map<String, Object>> getDialogueRecord(
            @RequestParam(required = false) Integer kefu_id,
            @RequestParam(required = false) String msn,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String appid_param,
            @RequestParam(defaultValue = "0") Integer user_id) {

        String appid = resolveAppid(appid_param);

        Map<String, Object> where = new java.util.HashMap<>();
        if (kefu_id != null && kefu_id > 0) where.put("kefu_id", kefu_id);
        if (msn != null) where.put("msn", msn);
        if (time != null) where.put("time", time);
        if (appid_param != null) where.put("appid", appid_param);
        if (user_id != null && user_id > 0) where.put("user_id", user_id);

        Map<String, Object> result = adminDialogueRecordService.getDialogueRecord(where, appid);
        return ApiResult.ok(result);
    }
}
