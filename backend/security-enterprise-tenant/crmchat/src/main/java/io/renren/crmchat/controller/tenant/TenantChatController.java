package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.TenantChatService;
import io.renren.crmchat.service.TenantSiteStatisticsService;
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
 * Tenant API - 聊天对话管理
 * PHP Reference: /app/controller/tenant/chat/ServiceDialogueRecord.php
 * PHP Reference: /app/controller/tenant/chat/Service.php
 */
@Slf4j
@RestController
@RequestMapping("/api/tenant/chat")
@Tag(name = "Tenant - Chat Conversation Management")
@AllArgsConstructor
public class TenantChatController {

    private final TenantChatService tenantChatService;
    private final TenantSiteStatisticsService tenantSiteStatisticsService;

    @GetMapping("/record")
    @Operation(summary = "Get All Chat History")
    public ApiResult<Map<String, Object>> getDialogueRecordList(
            @RequestParam(required = false) Integer kefu_id,
            @RequestParam(required = false) String msn,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) Integer user_id,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {

        requireAppid();

        Map<String, Object> filters = new HashMap<>();
        if (kefu_id != null) {
            filters.put("kefu_id", kefu_id);
        }
        if (msn != null && !msn.trim().isEmpty()) {
            filters.put("msn", msn);
        }
        if (time != null && !time.trim().isEmpty()) {
            filters.put("time", time);
        }
        if (user_id != null) {
            filters.put("user_id", user_id);
        }
        filters.put("page", page);
        filters.put("limit", limit);

        Map<String, Object> result = tenantChatService.getDialogueRecordList(filters);
        return ApiResult.ok(result);
    }

    @GetMapping("/record/list")
    @Operation(summary = "Get All Chat Users")
    public ApiResult<Map<String, Object>> getServiceRecordList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String time) {

        requireAppid();

        Map<String, Object> filters = new HashMap<>();
        if (title != null && !title.trim().isEmpty()) {
            filters.put("title", title);
        }
        if (time != null && !time.trim().isEmpty()) {
            filters.put("time", time);
        }

        Map<String, Object> result = tenantChatService.getServiceRecordList(filters);
        return ApiResult.ok(result);
    }

    @GetMapping("/record_kefu")
    @Operation(summary = "Get All Customer Service Agents")
    public ApiResult<List<Map<String, Object>>> getAllKefu() {

        requireAppid();
        List<Map<String, Object>> result = tenantChatService.getAllKefu();
        return ApiResult.ok(result);
    }

    @GetMapping("/kefu/record/{id}")
    @Operation(summary = "Get Customer Service Chat Users")
    public ApiResult<List<Map<String, Object>>> getChatUserList(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Customer service ID cannot be empty");
        }

        requireAppid();
        List<Map<String, Object>> result = tenantChatService.getChatUserList(id);
        return ApiResult.ok(result);
    }

    @GetMapping("/kefu/chat_list")
    @Operation(summary = "View Conversation")
    public ApiResult<Map<String, Object>> getChatMessageList(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) Integer to_user_id,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {

        requireAppid();

        Map<String, Object> filters = new HashMap<>();
        if (id != null) {
            filters.put("id", id);
        }
        if (to_user_id != null) {
            filters.put("to_user_id", to_user_id);
        }
        filters.put("page", page);
        filters.put("limit", limit);

        Map<String, Object> result = tenantChatService.getChatMessageList(filters);
        return ApiResult.ok(result);
    }

    /**
     * 网站访问统计
     * GET /api/tenant/chat/statistics
     *
     * PHP Reference: SiteStatistics.php::index()
     *
     * Query Parameters:
     * - province: 地区（可选）
     * - create_time: 时间（可选）
     * - page: 页码（可选，默认1）
     * - limit: 每页数量（可选，默认15）
     *
     * Response:
     * {
     *   "list": [...],
     *   "count": 0
     * }
     */
    @GetMapping({"/statistics", "/site_statistics"})
    @Operation(summary = "网站访问统计")
    public ApiResult<Map<String, Object>> getStatistics(
            @RequestParam(required = false, defaultValue = "") String province,
            @RequestParam(required = false, defaultValue = "") String create_time,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "15") Integer limit) {

        String appid = requireAppid();
        log.info("[租户API] chat/statistics - appid={}, province={}, create_time={}, page={}, limit={}",
                 appid, province, create_time, page, limit);

        Map<String, Object> result = tenantSiteStatisticsService.getSiteStatisticsList(province, create_time, page, limit);
        log.info("[租户API] chat/statistics - appid={}, count={}", appid, result.getOrDefault("count", 0));
        return ApiResult.ok(result);
    }
}
