package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatUserGroupEntity;
import io.renren.crmchat.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin User Controller - 管理员用户管理
 * PHP Reference: /app/controller/admin/user/User.php
 *
 * 功能说明:
 * 1. GET /api/admin/user - 用户列表(支持多条件查询)
 * 2. GET /api/admin/user/user_label - 用户标签搜索列表
 * 3. GET /api/admin/user/:id/edit - 获取修改用户表单
 * 4. PUT /api/admin/user/:id - 修改用户
 * 5. PUT /api/admin/user/batch/label - 批量修改用户标签
 * 6. PUT /api/admin/user/batch/group - 批量修改用户分组
 * 7. GET /api/admin/user/label/all - 获取全部标签
 * 8. GET /api/admin/user/group/all - 获取全部分组
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/user")
@Tag(name = "Admin User - User Management")
@AllArgsConstructor
public class AdminUserController extends BaseController {

    private final AdminUserService adminUserService;

    /**
     * 获取用户列表
     * GET /api/admin/user
     *
     * PHP Reference: User.php::index()
     *
     * Query Parameters:
     * - nickname: 昵称模糊查询
     * - group_id: 分组ID
     * - label_id: 标签ID
     * - time: 时间范围 "2024-01-01 - 2024-01-31"
     * - sex: 性别
     * - user_type: 用户类型
     * - is_tourist: 是否游客
     * - page: 页码
     * - limit: 每页数量
     *
     * Response:
     * {
     *   "list": [...],
     *   "count": 100
     * }
     */
    @GetMapping({"/", "/index"})
    @Operation(summary = "Get User List")
    public ApiResult<Map<String, Object>> getChatUserList(
            @RequestParam(required = false, defaultValue = "") String nickname,
            @RequestParam(required = false, defaultValue = "") String group_id,
            @RequestParam(required = false, defaultValue = "") String label_id,
            @RequestParam(required = false, defaultValue = "") String time,
            @RequestParam(required = false, defaultValue = "") String sex,
            @RequestParam(required = false, defaultValue = "") String user_type,
            @RequestParam(required = false, defaultValue = "") String field_key,
            @RequestParam(required = false, defaultValue = "") String is_tourist,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {

        String appid = currentAppid();

        Map<String, Object> where = new java.util.HashMap<>();
        if (!nickname.isEmpty()) where.put("nickname", nickname);
        if (!group_id.isEmpty()) where.put("group_id", Integer.parseInt(group_id));
        if (!label_id.isEmpty()) where.put("label_id", label_id);  // 字符串,支持逗号分隔
        if (!time.isEmpty()) where.put("time", time);
        if (!sex.isEmpty()) where.put("sex", Integer.parseInt(sex));
        if (!user_type.isEmpty()) where.put("user_type", user_type);
        if (!field_key.isEmpty()) where.put("field_key", field_key);
        if (!is_tourist.isEmpty()) where.put("is_tourist", is_tourist);
        where.put("page", page);
        where.put("limit", limit);

        Map<String, Object> result = adminUserService.getChatUserList(where, appid);
        return ApiResult.ok(result);
    }

    /**
     * 获取用户标签搜索列表
     * GET /api/admin/user/user_label
     *
     * PHP Reference: User.php::getLavelAll()
     *
     * Response:
     * [
     *   {
     *     "value": 1,
     *     "label": "Customer Category",
     *     "options": [
     *       {"value": 1, "label": "VIP Customer"},
     *       {"value": 2, "label": "普通客户"}
     *     ]
     *   }
     * ]
     */
    @GetMapping("/user_label")
    @Operation(summary = "Get User Tag Search List")
    public ApiResult<List<Map<String, Object>>> getUserLabelSearchList() {
        String appid = currentAppid();

        List<Map<String, Object>> result = adminUserService.getUserLabelSearchList(appid);
        return ApiResult.ok(result);
    }

    /**
     * 获取修改用户表单
     * GET /api/admin/user/edit/:id
     *
     * PHP Reference: User.php::edit()
     *
     * Response:
     * {
     *   "user": {...},
     *   "label_ids": [1, 2, 3]
     * }
     */
    @GetMapping("/edit/{id}")
    @Operation(summary = "Get User Modification Form")
    public ApiResult<Map<String, Object>> getChatUserForm(@PathVariable Integer id) {
        String appid = currentAppid();

        Map<String, Object> result = adminUserService.getChatUserForm(id, appid);
        return ApiResult.ok(result);
    }

    /**
     * 修改用户
     * PUT /api/admin/user/:id
     *
     * PHP Reference: User.php::update()
     *
     * Request Body:
     * {
     *   "avatar": "https://...",
     *   "nickname": "张三",
     *   "group_id": 1,
     *   "remarks": "备注",
     *   "remark_nickname": "备注昵称",
     *   "phone": "13800138000"
     * }
     *
     * Response: { "code": 0, "msg": "修改成功" }
     */
    @PutMapping("/{id}")
    @Operation(summary = "修改用户")
    public ApiResult<String> updateChatUser(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        String appid = currentAppid();

        boolean success = adminUserService.updateChatUser(id, data, appid);
        if (success) {
            return ApiResult.ok("Updated successfully", "success");
        } else {
            return ApiResult.fail("Update failed");
        }
    }

    /**
     * 批量修改用户标签
     * PUT /api/admin/user/batch/label
     *
     * PHP Reference: User.php::batchLabel()
     *
     * Request Body:
     * {
     *   "ids": [1, 2, 3],
     *   "label_id": [1, 2],
     *   "un_label_id": [3, 4]
     * }
     *
     * Response: { "code": 0, "msg": "设置成功" }
     */
    @PutMapping("/batch/label")
    @Operation(summary = "批量修改用户标签")
    public ApiResult<String> batchUpdateLabel(@RequestBody Map<String, Object> data) {
        String appid = currentAppid();

        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) data.get("ids");
        @SuppressWarnings("unchecked")
        List<Integer> labelId = (List<Integer>) data.get("label_id");
        @SuppressWarnings("unchecked")
        List<Integer> unLabelId = (List<Integer>) data.get("un_label_id");

        adminUserService.batchUpdateLabel(ids, labelId, unLabelId, appid);
        return ApiResult.ok("Set successfully", "success");
    }

    /**
     * 批量修改用户分组
     * PUT /api/admin/user/batch/group
     *
     * PHP Reference: User.php::batchGroup()
     *
     * Request Body:
     * {
     *   "ids": [1, 2, 3],
     *   "group_id": 1
     * }
     *
     * Response: { "code": 0, "msg": "批量设置成功" }
     */
    @PutMapping("/batch/group")
    @Operation(summary = "批量修改用户分组")
    public ApiResult<String> batchUpdateGroup(@RequestBody Map<String, Object> data) {
        String appid = currentAppid();

        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) data.get("ids");
        Integer groupId = (Integer) data.get("group_id");

        adminUserService.batchUpdateGroup(ids, groupId, appid);
        return ApiResult.ok("Batch setting successful", "success");
    }

    /**
     * 获取全部标签
     * GET /api/admin/user/label/all
     *
     * PHP Reference: User.php::getLabelAll()
     *
     * Query Parameters:
     * - id: 用户ID(可选,用于标记用户已有标签)
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "name": "客户分类",
     *     "label": [...]
     *   }
     * ]
     */
    @GetMapping("/label/all")
    @Operation(summary = "Get All Tags")
    public ApiResult<List<Map<String, Object>>> getLabelAll(@RequestParam(defaultValue = "0") Integer id) {
        String appid = currentAppid();

        List<Map<String, Object>> result = adminUserService.getLabelAll(id, appid);
        return ApiResult.ok(result);
    }

    /**
     * 获取全部分组
     * GET /api/admin/user/group/all
     *
     * PHP Reference: User.php::getGroupAll()
     *
     * Response: [...]
     */
    @GetMapping("/group/all")
    @Operation(summary = "获取全部分组")
    public ApiResult<List<ChatUserGroupEntity>> getGroupAll() {
        String appid = currentAppid();

        List<ChatUserGroupEntity> result = adminUserService.getGroupAll(appid);
        return ApiResult.ok(result);
    }
}
