package io.renren.crmchat.controller.kefu;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.security.UserContext;
import io.renren.crmchat.service.KefuMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Kefu Message Controller - 客服消息发送管理
 * PHP Reference: /app/controller/kefu/Service.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/kefu")
@Tag(name = "Kefu Message - Customer Service Message Sending")
@AllArgsConstructor
public class KefuMessageController {

    private final KefuMessageService kefuMessageService;

    /**
     * 获取消息发送ID
     * GET /api/kefu/service/get_send_id
     *
     * PHP Reference: Service.php::getSendId()
     *
     * 业务说明:
     * - 生成唯一消息ID（用于消息去重和追踪）
     * - PHP使用Snowflake分布式ID算法
     * - Java简化版使用UUID
     *
     * Response:
     * {
     *   "send_id": "550e8400e29b41d4a716446655440000"
     * }
     */
    @GetMapping("/service/get_send_id")
    @Operation(summary = "Get Message Sending ID")
    public ApiResult<Map<String, Object>> getSendId() {
        Map<String, Object> result = kefuMessageService.getSendId();
        return ApiResult.ok(result);
    }

    /**
     * 发送消息
     * POST /api/kefu/service/send_message
     *
     * PHP Reference: Service.php::sendMessage()
     *
     * Request Body:
     * {
     *   "to_user_id": 456,              // 接收人user_id（必填）
     *   "msn": "Hello",                   // message content (required)
     *   "guid": "unique-message-id",    // 消息唯一ID（必填，由getSendId获取）
     *   "msn_type": 1,                  // 消息类型（可选，1-文字，2-表情，3-图片，4-语音）
     *   "other": "",                    // 其他信息（可选，JSON）
     *   "is_tourist": 0                 // 是否游客（可选）
     * }
     *
     * 业务说明:
     * - 保存消息到ChatServiceDialogueRecord表
     * - 更新ChatServiceRecord表（最新消息、未读数）
     * - TODO: 通过WebSocket推送消息给目标用户
     *
     * Response:
     * {
     *   "id": 123,
     *   "user_id": 1,
     *   "to_user_id": 456,
     *   "msn": "Hello",
     *   "msn_type": 1,
     *   "type": 0,
     *   "other": "",
     *   "guid": "unique-message-id",
     *   "add_time": 1234567890
     * }
     */
    @PostMapping("/service/send_message")
    @Operation(summary = "Send Message")
    public ApiResult<Map<String, Object>> sendMessage(@RequestBody Map<String, Object> data) {
        Long currentUserId = UserContext.getUserId();
        String appid = UserContext.getAppid();
        if (currentUserId == null || appid == null || appid.trim().isEmpty()) {
            return ApiResult.fail("Please log in first");
        }

        Map<String, Object> result = kefuMessageService.sendMessage(data, currentUserId.intValue(), appid);
        return ApiResult.ok("Sent successfully", result);
    }

    /**
     * 设置扫码登录code
     * POST /api/kefu/service/code
     *
     * PHP Reference: Service.php::setLoginCode()
     *
     * Request Body:
     * {
     *   "code": "scan-code-from-qr"  // 扫码获取的登录code（必填）
     * }
     *
     * 业务说明:
     * - 扫码登录流程：
     *   1. 前端生成二维码（包含code）
     *   2. 客服扫码后，调用此接口设置code
     *   3. 将客服的uniqid字段设置为code
     *   4. 前端轮询检测code状态，完成登录
     * - TODO: 需要Redis支持，验证code是否过期
     *
     * Response: { "code": 0, "msg": "Login successful" }
     */
    @PostMapping("/service/code")
    @Operation(summary = "Set QR Code Login Code")
    public ApiResult<Map<String, Object>> setLoginCode(@RequestBody Map<String, Object> data) {
        Long currentUserId = UserContext.getUserId();
        String appid = UserContext.getAppid();
        if (currentUserId == null || appid == null || appid.trim().isEmpty()) {
            return ApiResult.fail("Please log in first");
        }

        if (!data.containsKey("code") || data.get("code") == null) {
            return ApiResult.fail("Login code does not exist");
        }

        String code = data.get("code").toString();
        Map<String, Object> result = kefuMessageService.setLoginCode(code, currentUserId.intValue(), appid);
        return ApiResult.ok(result);
    }
}
