package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatUserGroupEntity;
import io.renren.crmchat.service.TenantUserGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static io.renren.crmchat.security.TenantSecurityUtils.requireAppid;

/**
 * Tenant API - 用户分组管理
 * PHP Reference: /app/controller/tenant/user/Group.php
 *
 * @author CRMChat Team
 */
@Slf4j
@RestController
@RequestMapping("/api/tenant/user/group")
@Tag(name = "Tenant - User Group Management")
@AllArgsConstructor
public class TenantUserGroupController {

    private final TenantUserGroupService tenantUserGroupService;

    /**
     * 获取分组列表
     * GET /api/tenant/user/group
     *
     * PHP Reference: Group.php::index()
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "group_name": "VIP Customer",
     *     "appid": "202517350001234"
     *   }
     * ]
     */
    @GetMapping
    @Operation(summary = "Get Group List")
    public ApiResult<List<ChatUserGroupEntity>> getGroupList() {

        String appid = requireAppid();
        log.info("[租户API] user/group/list - appid={}", appid);

        List<ChatUserGroupEntity> list = tenantUserGroupService.getGroupList();
        return ApiResult.ok(list);
    }

    /**
     * 创建分组
     * POST /api/tenant/user/group
     *
     * PHP Reference: Group.php::save()
     *
     * Request Body:
     * {
     *   "group_name": "VIP客户"  // 必填
     * }
     *
     * Response: { "code": 0, "msg": "提交成功！" }
     */
    @PostMapping
    @Operation(summary = "Create Group")
    public ApiResult<String> createGroup(@RequestBody Map<String, Object> data) {

        String appid = requireAppid();
        log.info("[租户API] user/group/create - appid={}", appid);

        tenantUserGroupService.createGroup(data);
        return ApiResult.ok("Submitted successfully", "success");
    }

    /**
     * 更新分组
     * PUT /api/tenant/user/group/:id
     *
     * PHP Reference: Group.php::update()
     *
     * Request Body:
     * {
     *   "group_name": "VIP客户"  // 必填
     * }
     *
     * Response: { "code": 0, "msg": "提交成功！" }
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update Group")
    public ApiResult<String> updateGroup(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> data) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        String appid = requireAppid();
        log.info("[租户API] user/group/update - appid={}, groupId={}", appid, id);

        tenantUserGroupService.updateGroup(id, data);
        return ApiResult.ok("Submitted successfully", "success");
    }

    /**
     * 删除分组
     * DELETE /api/tenant/user/group
     *
     * PHP Reference: Group.php::delete()
     *
     * Query Parameters:
     * - id: 分组ID（必填）
     *
     * Response: { "code": 0, "msg": "success" }
     */
    @DeleteMapping
    @Operation(summary = "删除分组")
    public ApiResult<String> deleteGroup(@RequestParam Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Data does not exist");
        }

        String appid = requireAppid();
        log.info("[租户API] user/group/delete - appid={}, groupId={}", appid, id);

        tenantUserGroupService.deleteGroup(id);
        return ApiResult.ok("success");
    }
}
