package io.renren.crmchat.controller.kefu;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.security.UserContext;
import io.renren.crmchat.service.KefuChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kefu Chat Controller - 客服聊天管理
 * PHP Reference: /app/controller/kefu/Service.php (chat related methods)
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/kefu/chat")
@Tag(name = "Kefu Chat - Customer Service Chat Management")
@AllArgsConstructor
public class KefuChatController {

    private final KefuChatService kefuChatService;

    /**
     * 获取当前客服的聊天记录
     * GET /api/kefu/chat/record
     *
     * PHP Reference: ServiceDialogueRecord.php::index() (filtered by current kefu)
     *
     * Query Parameters:
     * - msn: 消息内容（可选，模糊查询）
     * - user_id: 对方用户ID（可选）
     * - page: 页码（可选，默认1）
     * - limit: 每页数量（可选，默认20）
     *
     * Response:
     * {
     *   "list": [...],
     *   "count": 100
     * }
     */
    @GetMapping("/record")
    @Operation(summary = "Get Current Customer Service Chat History")
    public ApiResult<Map<String, Object>> getMyDialogueRecordList(
            @RequestParam(required = false) String msn,
            @RequestParam(required = false) Integer user_id,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {

        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        Map<String, Object> filters = new HashMap<>();
        if (msn != null && !msn.trim().isEmpty()) {
            filters.put("msn", msn);
        }
        if (user_id != null) {
            filters.put("user_id", user_id);
        }
        filters.put("page", page);
        filters.put("limit", limit);

        Map<String, Object> result = kefuChatService.getMyDialogueRecordList(filters, kefuId, appid);
        return ApiResult.ok(result);
    }

    /**
     * 获取当前客服的聊天用户
     * GET /api/kefu/chat/user
     *
     * PHP Reference: Service.php::chat_user()
     *
     * Response:
     * [
     *   {
     *     "id": 10,
     *     "nickname": "用户A",
     *     "avatar": "http://..."
     *   }
     * ]
     */
    @GetMapping("/user")
    @Operation(summary = "Get Current Customer Service Chat Users")
    public ApiResult<List<Map<String, Object>>> getMyChatUserList() {
        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        List<Map<String, Object>> result = kefuChatService.getMyChatUserList(kefuId, appid);
        return ApiResult.ok(result);
    }

    /**
     * 查看与特定用户的对话
     * GET /api/kefu/chat/message
     *
     * PHP Reference: Service.php::chat_list()
     *
     * Query Parameters:
     * - to_user_id: 对方用户ID（可选）
     * - page: 页码（可选，默认1）
     * - limit: 每页数量（可选，默认20）
     *
     * Response:
     * {
     *   "list": [...],
     *   "count": 50
     * }
     */
    @GetMapping("/message")
    @Operation(summary = "查看与特定用户的对话")
    public ApiResult<Map<String, Object>> getChatMessageList(
            @RequestParam(required = false) Integer to_user_id,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {

        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        Map<String, Object> filters = new HashMap<>();
        if (to_user_id != null) {
            filters.put("to_user_id", to_user_id);
        }
        filters.put("page", page);
        filters.put("limit", limit);

        Map<String, Object> result = kefuChatService.getChatMessageList(filters, kefuId, appid);
        return ApiResult.ok(result);
    }
}
