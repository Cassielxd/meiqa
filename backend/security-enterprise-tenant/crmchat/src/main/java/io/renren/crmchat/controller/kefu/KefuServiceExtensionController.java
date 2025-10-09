package io.renren.crmchat.controller.kefu;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatServiceDialogueRecordEntity;
import io.renren.crmchat.security.UserContext;
import io.renren.crmchat.service.KefuServiceExtensionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Kefu Service Extension Controller - 客服扩展功能管理
 * PHP Reference: /app/controller/kefu/Service.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/kefu/service")
@Tag(name = "Kefu Service Extension - 客服扩展功能")
@AllArgsConstructor
public class KefuServiceExtensionController {

    private final KefuServiceExtensionService kefuServiceExtensionService;

    /**
     * 获取在线客服列表（用于转接）
     * GET /api/kefu/service/transfer_list
     *
     * PHP Reference: Service.php::getServiceList()
     *
     * Query Parameters:
     * - nickname: 客服昵称（可选，模糊查询）
     * - user_id: 当前聊天的用户ID（可选，用于排除）
     * - page: 页码（可选，默认1）
     * - limit: 每页数量（可选，默认20）
     *
     * Response:
     * {
     *   "list": [...],   // 客服列表
     *   "count": 10      // 总数
     * }
     */
    @GetMapping({"/transfer_list", "/list"})
    @Operation(summary = "Get Online Customer Service List (For Transfer)")
    public ApiResult<Map<String, Object>> getServiceList(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) Integer user_id,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {

        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        Map<String, Object> filters = new HashMap<>();
        if (nickname != null && !nickname.trim().isEmpty()) {
            filters.put("nickname", nickname);
        }
        filters.put("page", page);
        filters.put("limit", limit);

        // 排除当前客服和当前聊天的用户（如果有）
        List<Integer> excludeIds = new ArrayList<>();
        excludeIds.add(kefuId);
        if (user_id != null && user_id > 0) {
            excludeIds.add(user_id);
        }

        Map<String, Object> result = kefuServiceExtensionService.getServiceList(filters, excludeIds, appid);
        return ApiResult.ok(result);
    }

    /**
     * 获取当前客服详细信息
     * GET /api/kefu/service/info
     *
     * PHP Reference: Service.php::getServiceInfo()
     *
     * Response:
     * {
     *   "id": 1,
     *   "user_id": 123,
     *   "account": "kefu01",
     *   "nickname": "客服小李",
     *   "avatar": "http://...",
     *   "online": 1,
     *   "auto_reply": 1,
     *   "is_backstage": 0,
     *   "site_name": "CRM客服系统",
     *   "config_export_open": 1,
     *   "user_ids": [123, 456, 789],  // 同租户所有客服的user_id列表
     *   ...
     * }
     */
    @GetMapping("/info")
    @Operation(summary = "Get Current Customer Service Details")
    public ApiResult<Map<String, Object>> getServiceInfo() {
        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        Map<String, Object> result = kefuServiceExtensionService.getServiceInfo(kefuId, appid);
        return ApiResult.ok(result);
    }

    /**
     * 客服转接
     * POST /api/kefu/service/transfer
     *
     * PHP Reference: Service.php::transfer()
     *
     * Request Body:
     * {
     *   "kefuToUserId": 456,  // 目标客服的user_id（必填）
     *   "user_id": 789        // 要转接的用户ID（必填）
     * }
     *
     * Response: { "code": 0, "msg": "转接成功" }
     *
     * 业务说明:
     * - 将当前客服与指定用户的对话转接给目标客服
     * - 会删除原客服的聊天记录，创建新客服的聊天记录
     * - TODO: 发送WebSocket通知给相关方
     */
    @PostMapping("/transfer")
    @Operation(summary = "Customer Service Transfer")
    public ApiResult<String> transfer(@RequestBody Map<String, Object> data) {
        // 参数验证
        if (!data.containsKey("kefuToUserId") || data.get("kefuToUserId") == null) {
            return ApiResult.fail("Missing target customer service ID");
        }
        if (!data.containsKey("user_id") || data.get("user_id") == null) {
            return ApiResult.fail("Missing user ID");
        }

        Integer kefuToUserId = Integer.parseInt(data.get("kefuToUserId").toString());
        Integer userId = Integer.parseInt(data.get("user_id").toString());

        if (kefuToUserId <= 0 || userId <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long currentKefuUserId = UserContext.getUserId();
        Integer kefuId = currentKefuUserId != null ? currentKefuUserId.intValue() : null;

        kefuServiceExtensionService.transfer(kefuId, kefuToUserId, userId, appid);
        return ApiResult.ok("Transferred successfully", "success");
    }

    /**
     * 设置自动回复开关
     * PUT /api/kefu/service/auth_reply/:value
     *
     * PHP Reference: Service.php::setAutoReply()
     *
     * Path Variable:
     * - value: 0-关闭，1-开启
     *
     * Response: { "code": 0, "msg": "设置成功" }
     */
    @PutMapping("/auth_reply/{value}")
    @Operation(summary = "设置自动回复开关")
    public ApiResult<String> setAutoReply(@PathVariable Integer value) {
        if (value != 0 && value != 1) {
            return ApiResult.fail("Invalid parameter: value must be 0 or 1");
        }

        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        kefuServiceExtensionService.setAutoReply(kefuId, value, appid);
        return ApiResult.ok("Set successfully", "success");
    }

    /**
     * 设置是否后台运行
     * PUT /api/kefu/service/backstage/:backstage
     *
     * PHP Reference: Service.php::backstage()
     *
     * Path Variable:
     * - value: 0-关闭，1-开启
     *
     * Response: { "code": 0, "msg": "设置成功" }
     */
    @PutMapping("/backstage/{value}")
    @Operation(summary = "设置是否后台运行")
    public ApiResult<String> setBackstage(@PathVariable("value") Integer value) {
        if (value != 0 && value != 1) {
            return ApiResult.fail("Invalid parameter: value must be 0 or 1");
        }

        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        kefuServiceExtensionService.setBackstage(kefuId, value, appid);
        return ApiResult.ok("Set successfully", "success");
    }

    /**
     * 获取聊天历史记录
     * GET /api/kefu/chat/history
     *
     * PHP Reference: KefuServices.php::getChatList()
     *
     * Query Parameters:
     * - user_id: 对方用户ID（必填）
     * - upperId: 上拉加载的起始ID（可选，用于分页加载更早的消息）
     * - limit: 每次加载数量（可选，默认20）
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "user_id": 123,
     *     "to_user_id": 456,
     *     "msn": "你好",
     *     "msn_type": 1,
     *     "add_time": 1234567890,
     *     ...
     *   }
     * ]
     */
    @GetMapping("/chat/history")
    @Operation(summary = "获取聊天历史记录")
    public ApiResult<List<ChatServiceDialogueRecordEntity>> getChatHistory(
            @RequestParam(required = false) Integer user_id,
            @RequestParam(required = false, defaultValue = "0") Integer upperId,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {

        if (user_id == null || user_id <= 0) {
            return ApiResult.fail("Missing user ID parameter");
        }

        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        Map<String, Object> filters = new HashMap<>();
        filters.put("user_id", user_id);
        if (upperId > 0) {
            filters.put("upperId", upperId);
        }
        filters.put("limit", limit);

        List<ChatServiceDialogueRecordEntity> result = kefuServiceExtensionService.getChatHistory(filters, kefuId, appid);
        return ApiResult.ok(result);
    }

    /**
     * 心跳检测
     * GET /api/kefu/service/ping
     *
     * PHP Reference: Service.php::ping()
     *
     * Response:
     * {
     *   "time": 1234567890  // 当前Unix时间戳（秒）
     * }
     */
    @GetMapping("/ping")
    @Operation(summary = "心跳检测")
    public ApiResult<Map<String, Object>> ping() {
        Map<String, Object> result = kefuServiceExtensionService.ping();
        return ApiResult.ok(result);
    }
}
