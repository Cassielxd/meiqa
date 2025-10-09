package io.renren.crmchat.controller.kefu;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.CategoryEntity;
import io.renren.crmchat.entity.ChatServiceRecordEntity;
import io.renren.crmchat.security.UserContext;
import io.renren.crmchat.service.KefuUserExtensionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Kefu User Extension Controller - 客服用户交互扩展功能管理
 * PHP Reference: /app/controller/kefu/User.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/kefu")
@Tag(name = "Kefu User Extension - Customer Service User Interaction Extensions")
@AllArgsConstructor
public class KefuUserExtensionController {

    private final KefuUserExtensionService kefuUserExtensionService;

    /**
     * 获取所有聊天用户列表
     * GET /api/kefu/user/record/all
     *
     * PHP Reference: User.php::recordAllList()
     *
     * Query Parameters:
     * - nickname: 用户昵称（可选，模糊查询）
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "user_id": 123,
     *     "to_user_id": 456,
     *     "nickname": "张三",
     *     "avatar": "http://...",
     *     ...
     *   }
     * ]
     */
    @GetMapping("/user/record/all")
    @Operation(summary = "Get All Chat Users")
    public ApiResult<List<ChatServiceRecordEntity>> getRecordAllList(
            @RequestParam(required = false, defaultValue = "") String nickname) {

        Long currentUserId = UserContext.getUserId();
        String appid = UserContext.getAppid();
        if (currentUserId == null || appid == null || appid.trim().isEmpty()) {
            return ApiResult.fail("Please log in first");
        }

        List<ChatServiceRecordEntity> result = kefuUserExtensionService.getRecordAllList(currentUserId.intValue(), nickname, appid);
        return ApiResult.ok(result);
    }

    /**
     * 删除聊天记录用户
     * DELETE /api/kefu/user/record/:id
     *
     * PHP Reference: User.php::deleteRecordUser()
     *
     * Path Variable:
     * - id: ChatServiceRecord的ID
     *
     * 业务说明:
     * - 软删除：设置delete_time字段
     * - 不做物理删除
     *
     * Response: { "code": 0, "msg": "Deleted successfully" }
     */
    @DeleteMapping("/user/record/{id}")
    @Operation(summary = "Delete Chat History User")
    public ApiResult<String> deleteRecordUser(@PathVariable Integer id) {
        kefuUserExtensionService.deleteRecordUser(id);
        return ApiResult.ok("Deleted successfully", "success");
    }

    /**
     * 删除用户标签
     * DELETE /api/kefu/user/:userId/label/:labelId
     *
     * PHP Reference: User.php::delUserLabel()
     *
     * Path Variables:
     * - userId: 用户ID
     * - labelId: 标签ID
     *
     * 业务说明:
     * - 删除用户和标签的关联关系（ChatUserLabelAssist表）
     * - 不是删除标签本身
     *
     * Response: { "code": 0, "msg": "删除成功" }
     */
    @DeleteMapping("/user/{userId}/label/{labelId}")
    @Operation(summary = "Delete User Tag")
    public ApiResult<String> deleteUserLabel(
            @PathVariable Integer userId,
            @PathVariable Integer labelId) {

        kefuUserExtensionService.deleteUserLabel(userId, labelId);
        return ApiResult.ok("Deleted successfully", "success");
    }

    /**
     * 更新客服client_id
     * PUT /api/kefu/service
     *
     * PHP Reference: User.php::updateService()
     *
     * Request Body:
     * {
     *   "client_id": "websocket_client_id_123"
     * }
     *
     * 业务说明:
     * - 保存WebSocket客户端ID
     * - 先清空所有客服的相同client_id（保证唯一性）
     * - 更新当前客服的client_id
     *
     * Response: { "code": 0, "msg": "更新成功" }
     */
    @PutMapping("/service")
    @Operation(summary = "更新客服client_id")
    public ApiResult<String> updateServiceClientId(@RequestBody Map<String, Object> data) {
        Long currentUserId = UserContext.getUserId();
        String appid = UserContext.getAppid();
        if (currentUserId == null || appid == null || appid.trim().isEmpty()) {
            return ApiResult.fail("Please log in first");
        }

        if (!data.containsKey("client_id") || data.get("client_id") == null) {
            return ApiResult.fail("Missing client_id parameter");
        }

        String clientId = data.get("client_id").toString();
        kefuUserExtensionService.updateServiceClientId(currentUserId.intValue(), appid, clientId);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 拉黑用户
     * POST /api/kefu/user/:userId/block
     *
     * PHP Reference: User.php::status()
     *
     * Path Variable:
     * - userId: 要拉黑的用户ID
     *
     * 业务说明:
     * - 删除以下所有相关记录（硬删除，不是软删除）:
     *   - ChatServiceRecord（to_user_id = userId）
     *   - ChatServiceRecord（user_id = userId）
     *   - ChatServiceDialogueRecord（to_user_id = userId）
     *   - ChatUser（userId）
     *
     * Response: { "code": 0, "msg": "拉黑成功" }
     */
    @PostMapping("/user/{userId}/block")
    @Operation(summary = "拉黑用户")
    public ApiResult<String> blockUser(@PathVariable Integer userId) {
        kefuUserExtensionService.blockUser(userId);
        return ApiResult.ok("Blocked successfully", "success");
    }

    /**
     * 保存聊天日志
     * POST /api/kefu/chat/log
     *
     * PHP Reference: User.php::savelog()
     *
     * Request Body:
     * {
     *   "to_user_id": 456,              // 接收人user_id（必填）
     *   "msn": "你好",                   // 消息内容（必填）
     *   "other": {},                     // 其他信息（可选，JSON）
     *   "type": 0,                       // 是否已读（可选）
     *   "msn_type": 1                    // 消息类型（可选，1-文字，2-表情，3-图片，4-语音）
     * }
     *
     * Response:
     * {
     *   "id": 123,
     *   "user_id": 1,
     *   "to_user_id": 456,
     *   "msn": "你好",
     *   "msn_type": 1,
     *   "type": 0,
     *   "add_time": 1234567890,
     *   "nickname": "客服小李",
     *   "avatar": "http://...",
     *   "_add_time": "2023-01-01 12:00:00"
     * }
     */
    @PostMapping("/chat/log")
    @Operation(summary = "保存聊天日志")
    public ApiResult<Map<String, Object>> saveChatLog(@RequestBody Map<String, Object> data) {
        Long currentUserId = UserContext.getUserId();
        String appid = UserContext.getAppid();
        if (currentUserId == null || appid == null || appid.trim().isEmpty()) {
            return ApiResult.fail("Please log in first");
        }

        Map<String, Object> result = kefuUserExtensionService.saveChatLog(data, currentUserId.intValue(), appid);
        return ApiResult.ok(result);
    }

    /**
     * 获取投诉分类
     * GET /api/kefu/complain/categories
     *
     * PHP Reference: User.php::getComplainList()
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "pid": 0,
     *     "name": "服务态度",
     *     "type": 2,
     *     "sort": 0,
     *     ...
     *   },
     *   {
     *     "id": 2,
     *     "pid": 1,
     *     "name": "服务不热情",
     *     "type": 2,
     *     "sort": 0,
     *     ...
     *   }
     * ]
     */
    @GetMapping("/complain/categories")
    @Operation(summary = "获取投诉分类")
    public ApiResult<List<CategoryEntity>> getComplainCategories() {
        List<CategoryEntity> result = kefuUserExtensionService.getComplainCategories();
        return ApiResult.ok(result);
    }

    /**
     * 提交投诉
     * POST /api/kefu/complain
     *
     * PHP Reference: User.php::complain()
     *
     * Request Body:
     * {
     *   "content": "投诉内容...",         // 投诉内容（必填）
     *   "user_id": 789,                 // 被投诉的用户ID（必填）
     *   "cate_id": [1, 3]               // 分类ID数组（必填）
     * }
     *
     * 业务说明:
     * - cate_id是数组，会被转成"1/3"格式存储
     * - 例如: [1, 3] → "1/3" 表示一级分类1下的二级分类3
     *
     * Response: { "code": 0, "msg": "Complaint submitted successfully" }
     */
    @PostMapping("/complain")
    @Operation(summary = "提交投诉")
    public ApiResult<String> submitComplain(@RequestBody Map<String, Object> data) {
        kefuUserExtensionService.submitComplain(data);
        return ApiResult.ok("Complaint submitted successfully", "success");
    }
}
