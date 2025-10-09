package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatUserEntity;
import io.renren.crmchat.entity.ChatUserGroupEntity;
import io.renren.crmchat.service.TenantUserService;
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
 * Tenant API - 用户管理
 * PHP Reference: /app/controller/tenant/user/User.php
 *
 * @author CRMChat Team
 */
@Slf4j
@RestController
@RequestMapping("/api/tenant/user")
@Tag(name = "Tenant - User Management")
@AllArgsConstructor
public class TenantUserController {

    private final TenantUserService tenantUserService;

    /**
     * 用户列表
     * GET /api/tenant/user
     * GET /api/tenant/user/index (别名，兼容前端)
     *
     * PHP Reference: User.php::index()
     *
     * Query Parameters:
     * - nickname: 用户昵称（可选，模糊查询）
     * - group_id: 分组ID（可选）
     * - label_id: 标签ID（可选）
     * - time: 时间范围（可选）
     * - sex: 性别（可选）
     * - user_type: 用户类型（可选）
     * - field_key: 自定义字段（可选）
     * - is_tourist: 是否游客（可选）
     * - appid: 租户appid（必填）
     * - page: 页码（可选，默认1）
     * - limit: 每页数量（可选，默认20）
     *
     * Response:
     * {
     *   "list": [...],
     *   "total": 100,
     *   "page": 1,
     *   "limit": 20
     * }
     */
    @GetMapping(value = {"", "/index"})
    @Operation(summary = "User List")
    public ApiResult<Map<String, Object>> getUserList(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String group_id,
            @RequestParam(required = false) String label_id,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String sex,
            @RequestParam(required = false) String user_type,
            @RequestParam(required = false) String field_key,
            @RequestParam(required = false) String is_tourist,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {

        String appid = requireAppid();
        log.info("[租户API] user/index - appid={}, page={}, limit={}", appid, page, limit);

        Map<String, Object> filters = new HashMap<>();
        if (nickname != null && !nickname.trim().isEmpty()) {
            filters.put("nickname", nickname);
        }
        if (group_id != null && !group_id.trim().isEmpty()) {
            filters.put("group_id", group_id);
        }
        if (label_id != null && !label_id.trim().isEmpty()) {
            filters.put("label_id", label_id);
        }
        if (time != null && !time.trim().isEmpty()) {
            filters.put("time", time);
        }
        if (sex != null && !sex.trim().isEmpty()) {
            filters.put("sex", sex);
        }
        if (user_type != null && !user_type.trim().isEmpty()) {
            filters.put("user_type", user_type);
        }
        if (field_key != null && !field_key.trim().isEmpty()) {
            filters.put("field_key", field_key);
        }
        if (is_tourist != null && !is_tourist.trim().isEmpty()) {
            filters.put("is_tourist", is_tourist);
        }
        filters.put("page", page);
        filters.put("limit", limit);

        Map<String, Object> result = tenantUserService.getChatUserList(filters);
        return ApiResult.ok(result);
    }

    /**
     * 获取用户编辑表单
     * GET /api/tenant/user/:id/edit
     *
     * PHP Reference: User.php::edit()
     *
     * Response:
     * {
     *   "id": 1,
     *   "nickname": "张三",
     *   "avatar": "http://...",
     *   ...
     * }
     */
    @GetMapping("/{id}/edit")
    @Operation(summary = "Get User Edit Form")
    public ApiResult<ChatUserEntity> edit(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        String appid = requireAppid();
        log.info("[租户API] user/edit - appid={}, userId={}", appid, id);

        ChatUserEntity user = tenantUserService.getChatUserForm(id);
        return ApiResult.ok(user);
    }

    /**
     * 更新用户
     * PUT /api/tenant/user/:id
     *
     * PHP Reference: User.php::update()
     *
     * Request Body:
     * {
     *   "avatar": "http://...",      // 必填
     *   "nickname": "张三",           // 必填
     *   "group_id": 1,               // 可选
     *   "remarks": "备注",            // 可选
     *   "remark_nickname": "Remark Nickname", // 可选
     *   "phone": "13800138000"       // 可选
     * }
     *
     * Response: { "code": 0, "msg": "Modified successfully" }
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update User")
    public ApiResult<String> update(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> data) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        String appid = requireAppid();
        log.info("[租户API] user/update - appid={}, userId={}", appid, id);

        tenantUserService.updateChatUser(id, data);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 批量设置分组
     * PUT /api/tenant/user/batch/group
     *
     * PHP Reference: User.php::batchGroup()
     *
     * Request Body:
     * {
     *   "ids": [1, 2, 3],  // 用户ID数组
     *   "group_id": 1      // 分组ID
     * }
     *
     * Response: { "code": 0, "msg": "批量设置成功" }
     */
    @PutMapping("/batch/group")
    @Operation(summary = "Batch Set Groups")
    public ApiResult<String> batchGroup(@RequestBody Map<String, Object> data) {

        String appid = requireAppid();

        if (!data.containsKey("ids") || data.get("ids") == null) {
            return ApiResult.fail("Please select at least one user");
        }

        if (!data.containsKey("group_id") || data.get("group_id") == null) {
            return ApiResult.fail("Please select a group");
        }

        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) data.get("ids");
        Integer groupId = Integer.parseInt(data.get("group_id").toString());

        log.info("[租户API] user/batch/group - appid={}, userIds={}, groupId={}", appid, ids, groupId);

        tenantUserService.batchUpdateGroup(ids, groupId);
        return ApiResult.ok("Batch setting successful", "success");
    }

    /**
     * 批量设置标签
     * PUT /api/tenant/user/batch/label
     *
     * PHP Reference: User.php::batchLabel()
     *
     * Request Body:
     * {
     *   "ids": [1, 2, 3],         // 用户ID数组
     *   "label_id": [1, 2],       // 要添加的标签ID数组
     *   "un_label_id": [3, 4]     // 要移除的标签ID数组
     * }
     *
     * Response: { "code": 0, "msg": "设置成功" }
     */
    @PutMapping("/batch/label")
    @Operation(summary = "批量设置标签")
    public ApiResult<String> batchLabel(@RequestBody Map<String, Object> data) {

        String appid = requireAppid();

        if (!data.containsKey("ids") || data.get("ids") == null) {
            return ApiResult.fail("Please select at least one user");
        }

        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) data.get("ids");

        @SuppressWarnings("unchecked")
        List<Integer> labelIds = data.containsKey("label_id") && data.get("label_id") != null
                ? (List<Integer>) data.get("label_id")
                : null;

        @SuppressWarnings("unchecked")
        List<Integer> unLabelIds = data.containsKey("un_label_id") && data.get("un_label_id") != null
                ? (List<Integer>) data.get("un_label_id")
                : null;

        log.info("[租户API] user/batch/label - appid={}, userIds={}, labelIds={}, unLabelIds={}",
                 appid, ids, labelIds, unLabelIds);

        tenantUserService.batchUpdateLabel(ids, labelIds, unLabelIds);
        return ApiResult.ok("Set successfully", "success");
    }

    /**
     * 获取所有标签（层级结构）
     * GET /api/tenant/user/label/all
     * GET /api/tenant/user/user_label (别名，兼容前端)
     *
     * PHP Reference: User.php::getLavelAll()
     *
     * Response:
     * [
     *   {
     *     "label": "分类1",
     *     "value": 1,
     *     "options": [
     *       {"label": "标签1", "value": 1},
     *       {"label": "标签2", "value": 2}
     *     ]
     *   }
     * ]
     */
    @GetMapping(value = {"/label/all", "/user_label"})
    @Operation(summary = "Get All Tags")
    public ApiResult<List<Map<String, Object>>> getLabelAll() {

        String appid = requireAppid();
        log.info("[租户API] user/label/all - appid={}", appid);

        List<Map<String, Object>> result = tenantUserService.getLabelAllHierarchy();
        return ApiResult.ok(result);
    }

    /**
     * 获取所有分组
     * GET /api/tenant/user/group/all
     *
     * PHP Reference: User.php::getGroupAll()
     *
     * Response:
     * [
     *   {"id": 1, "group_name": "VIP客户"},
     *   {"id": 2, "group_name": "普通客户"}
     * ]
     */
    @GetMapping("/group/all")
    @Operation(summary = "获取所有分组")
    public ApiResult<List<ChatUserGroupEntity>> getGroupAll() {

        String appid = requireAppid();
        log.info("[租户API] user/group/all - appid={}", appid);

        List<ChatUserGroupEntity> result = tenantUserService.getGroupAll();
        return ApiResult.ok(result);
    }

    /**
     * 获取用户聊天记录列表(访客端)
     * GET /api/tenant/user/record
     *
     * PHP Reference: Service.php::getRecordList()
     *
     * Query Parameters:
     * - uid: 用户ID (可选)
     * - limit: 每页条数 (可选，默认20)
     * - kefu_id: 客服ID (可选)
     * - toUserId: 接收方用户ID (可选)
     * - type: 类型 (可选)
     *
     * Response:
     * {
     *   "list": [],
     *   "count": 0
     * }
     */
    @GetMapping("/record")
    @Operation(summary = "获取用户聊天记录列表")
    public ApiResult<Map<String, Object>> record(
            @RequestParam(required = false, defaultValue = "0") Integer uid,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer kefu_id,
            @RequestParam(required = false, defaultValue = "0") Integer toUserId,
            @RequestParam(required = false, defaultValue = "0") Integer type) {

        // 临时返回空数据，避免前端报错
        Map<String, Object> result = new HashMap<>();
        result.put("list", new java.util.ArrayList<>());
        result.put("count", 0);

        return ApiResult.ok(result);
    }

    /**
     * 网站访问统计(访客端)
     * POST /api/tenant/user/statistics
     *
     * PHP Reference: Statistics.php::save()
     *
     * Request Body:
     * {
     *   "ip": "访客IP",
     *   "path": "Access Path",
     *   "source": "来源",
     *   "browser": "浏览器信息"
     * }
     *
     * Response: { "code": 0, "msg": "添加成功" }
     */
    @PostMapping("/statistics")
    @Operation(summary = "网站访问统计")
    public ApiResult<String> statistics(@RequestBody Map<String, Object> data) {

        // 临时返回成功，不做实际统计
        return ApiResult.ok("Added successfully", "success");
    }
}
