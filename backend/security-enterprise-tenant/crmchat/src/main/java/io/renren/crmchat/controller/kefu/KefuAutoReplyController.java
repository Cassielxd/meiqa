package io.renren.crmchat.controller.kefu;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatAutoReplyEntity;
import io.renren.crmchat.security.UserContext;
import io.renren.crmchat.service.KefuAutoReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Kefu AutoReply Controller - 客服个人自动回复管理
 * PHP Reference: /app/controller/kefu/AutoReply.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/kefu/service/auth_reply")
@Tag(name = "Kefu AutoReply - Personal Auto Reply Management")
@AllArgsConstructor
public class KefuAutoReplyController {

    private final KefuAutoReplyService kefuAutoReplyService;

    /**
     * 获取个人自动回复列表
     * GET /api/kefu/service/auth_reply
     *
     * PHP Reference: AutoReply.php::index() (filtered by current kefu user_id)
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "keyword": "Hello",
     *     "content": "Hello, how can I help you?",
     *     "user_id": 123,
     *     "sort": 0,
     *     "add_time": 1234567890,
     *     "appid": "202517350001234"
     *   }
     * ]
     */
    @GetMapping
    @Operation(summary = "Get Personal Auto Reply List")
    public ApiResult<List<ChatAutoReplyEntity>> getMyAutoReplyList() {
        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        List<ChatAutoReplyEntity> list = kefuAutoReplyService.getMyAutoReplyList(kefuId, appid);
        return ApiResult.ok(list);
    }

    /**
     * 获取自动回复详情
     * GET /api/kefu/service/auth_reply/:id
     *
     * PHP Reference: AutoReply.php::create() (用于获取表单数据)
     *
     * Response:
     * {
     *   "id": 1,
     *   "keyword": "Hello",
     *   "content": "Hello, how can I help you?",
     *   ...
     * }
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get Auto Reply Details")
    public ApiResult<ChatAutoReplyEntity> getAutoReplyDetail(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        ChatAutoReplyEntity autoReply = kefuAutoReplyService.getAutoReplyDetail(id, kefuId, appid);
        return ApiResult.ok(autoReply);
    }

    /**
     * 创建或更新个人自动回复
     * POST /api/kefu/service/auth_reply/:id
     *
     * PHP Reference: Service.php::saveAuthReply()
     *
     * Request Body:
     * {
     *   "keyword": "Hello",                  // required
     *   "content": "Hi there, how can we help you?",   // required
     *   "sort": 0                          // 可选
     * }
     *
     * Response: { "code": 0, "msg": "Saved successfully" }
     */
    @PostMapping("/{id}")
    @Operation(summary = "Create or update personal auto reply")
    public ApiResult<String> saveAutoReply(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, Object> data) {

        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        if (id == null || id <= 0) {
            kefuAutoReplyService.createAutoReply(data, kefuId, appid);
        } else {
            kefuAutoReplyService.updateAutoReply(id, data, kefuId, appid);
        }

        return ApiResult.ok("Saved successfully", "success");
    }

    /**
     * 删除个人自动回复
     * DELETE /api/kefu/service/auth_reply/:id
     *
     * PHP Reference: AutoReply.php::delete()
     *
     * Response: { "code": 0, "msg": "Deleted successfully" }
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete personal auto reply")
    public ApiResult<String> deleteAutoReply(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        kefuAutoReplyService.deleteAutoReply(id, kefuId, appid);
        return ApiResult.ok("Deleted successfully", "success");
    }
}
