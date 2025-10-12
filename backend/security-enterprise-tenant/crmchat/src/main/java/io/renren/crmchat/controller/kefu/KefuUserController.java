package io.renren.crmchat.controller.kefu;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.security.UserContext;
import io.renren.crmchat.service.KefuUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Kefu User Controller - 客服用户管理
 * PHP Reference: /app/controller/kefu/User.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/kefu/user")
@Tag(name = "Kefu User - Customer Service User Management")
@AllArgsConstructor
public class KefuUserController {

    private final KefuUserService kefuUserService;

    /**
     * 获取客户列表
     * GET /api/kefu/user/list
     *
     * PHP Reference: User.php::getUserList()
     *
     * Query Parameters:
     * - nickname: 昵称搜索
     * - label_id: 标签ID（逗号分隔）
     * - group_id: 分组ID（逗号分隔）
     *
     * Response: 按首字母分组的用户列表
     */
    @GetMapping("/list")
    @Operation(summary = "Get Customer List")
    public ApiResult<Map<String, Object>> getUserList(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false, name = "label_id") String labelId,
            @RequestParam(required = false, name = "group_id") String groupId) {

        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long userId = UserContext.getUserId();
        Integer kefuUserId = userId != null ? userId.intValue() : null;

        Map<String, Object> result = kefuUserService.getUserList(appid, kefuUserId, nickname, labelId, groupId);
        return ApiResult.ok(result);
    }

    /**
     * 获取聊天记录列表
     * GET /api/kefu/user/record
     *
     * PHP Reference: User.php::recordList()
     *
     * Query Parameters:
     * - nickname: 昵称搜索
     * - is_tourist: 是否游客
     * - label_id: 标签ID
     * - group_id: 分组ID
     *
     * Response: 聊天记录列表
     */
    @GetMapping("/record")
    @Operation(summary = "Get Chat Record List")
    public ApiResult<List<Map<String, Object>>> getRecordList(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false, name = "is_tourist") String isTourist,
            @RequestParam(required = false, name = "label_id") String labelId,
            @RequestParam(required = false, name = "group_id") String groupId) {

        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long kefuId = UserContext.getUserId();  // 这是eb_chat_service.id

        System.out.println("=== DEBUG RECORD LIST ===");
        System.out.println("JWT kefuId (service.id): " + kefuId);
        System.out.println("JWT appid: " + appid);

        // 需要获取eb_chat_service.user_id字段,因为eb_chat_service_record.to_user_id对应的是user_id
        Integer kefuUserId = kefuUserService.getKefuUserIdByServiceId(kefuId != null ? kefuId.intValue() : null, appid);
        System.out.println("Resolved kefuUserId (service.user_id): " + kefuUserId);

        List<Map<String, Object>> result = kefuUserService.getRecordList(appid, kefuUserId, nickname, isTourist, labelId, groupId);
        System.out.println("Records returned: " + (result != null ? result.size() : 0));
        System.out.println("=== DEBUG END ===");

        return ApiResult.ok(result);
    }

    /**
     * 获取用户信息
     * GET /api/kefu/user/info/:userId
     *
     * PHP Reference: User.php::userInfo()
     *
     * Response: 用户详细信息（包含标签）
     */
    @GetMapping("/info/{id}")
    @Operation(summary = "Get User Information")
    public ApiResult<Map<String, Object>> getUserInfo(@PathVariable("id") Integer id) {
        Map<String, Object> result = kefuUserService.getUserInfo(id);
        return ApiResult.ok(result);
    }

    /**
     * 获取用户标签
     * GET /api/kefu/user/label
     *
     * PHP Reference: User.php::getUserLabel()
     *
     * Query Parameters:
     * - id: 用户ID
     *
     * Response: 标签列表
     */
    @GetMapping("/label")
    @Operation(summary = "Get user tags")
    public ApiResult<List<Map<String, Object>>> getUserLabel(@RequestParam(defaultValue = "0") Integer id) {
        List<Map<String, Object>> result = kefuUserService.getUserLabel(id);
        return ApiResult.ok(result);
    }

    /**
     * 获取用户分组
     * GET /api/kefu/user/group
     *
     * PHP Reference: User.php::getUserGroup()
     *
     * Response: 分组列表
     */
    @GetMapping("/group")
    @Operation(summary = "Get user groups")
    public ApiResult<List<Map<String, Object>>> getUserGroup() {
        List<Map<String, Object>> result = kefuUserService.getUserGroup();
        return ApiResult.ok(result);
    }

    /**
     * 设置用户分组
     * PUT /api/kefu/user/group/:userId/:groupId
     *
     * PHP Reference: User.php::setUserGroup()
     *
     * Request Body:
     * {
     *   "group_id": 1
     * }
     *
     * Response: 设置成功
     */
    @PutMapping("/group/{userId}/{id}")
    @Operation(summary = "Set User Group")
    public ApiResult<String> setUserGroup(@PathVariable Integer userId, @PathVariable("id") Integer groupId) {
        kefuUserService.setUserGroup(userId, groupId);
        return ApiResult.ok("Set successfully", "success");
    }

    /**
     * 设置用户标签
     * PUT /api/kefu/user/label/:userId
     *
     * PHP Reference: User.php::setUserLabel()
     *
     * Request Body:
     * {
     *   "label_ids": [1, 2, 3],
     *   "un_label_ids": [4, 5]
     * }
     *
     * Response: 设置成功
     */
    @PutMapping("/label/{id}")
    @Operation(summary = "Set user tags")
    public ApiResult<String> setUserLabel(@PathVariable("id") Integer userId, @RequestBody Map<String, Object> data) {
        @SuppressWarnings("unchecked")
        List<Integer> labelIds = (List<Integer>) data.get("label_ids");
        @SuppressWarnings("unchecked")
        List<Integer> unLabelIds = (List<Integer>) data.get("un_label_ids");

        if ((labelIds == null || labelIds.isEmpty()) && (unLabelIds == null || unLabelIds.isEmpty())) {
            return ApiResult.fail("Missing label ID");
        }

        kefuUserService.setUserLabel(userId, labelIds, unLabelIds);
        return ApiResult.ok("Set successfully", "success");
    }

    /**
     * 修改用户信息
     * PUT /api/kefu/user/updateUser/:userId
     *
     * PHP Reference: User.php::updateUser()
     *
     * Request Body:
     * {
     *   "nickname": "Nickname",
     *   "remark_nickname": "Remark nickname",
     *   "sex": "Gender",
     *   "phone": "Phone number",
     *   "remarks": "Remarks"
     * }
     *
     * Response: 修改成功
     */
    @PutMapping("/updateUser/{id}")
    @Operation(summary = "Update user information")
    public ApiResult<String> updateUser(@PathVariable("id") Integer userId, @RequestBody Map<String, Object> data) {
        kefuUserService.updateUser(userId, data);
        return ApiResult.ok("Updated successfully", "success");
    }
}
