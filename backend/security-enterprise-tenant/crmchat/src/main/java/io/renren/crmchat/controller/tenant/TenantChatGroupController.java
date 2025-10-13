package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.controller.admin.BaseController;
import io.renren.crmchat.entity.ChatServiceGroupEntity;
import io.renren.crmchat.service.AdminServiceGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Tenant Chat Group Controller - 租户客服组管理
 * PHP Reference: /app/controller/tenant/chat/ServiceGroup.php
 *
 * 功能说明:
 * 1. GET /api/tenant/chat/group - 客服组列表
 * 2. GET /api/tenant/chat/group/create/:id - 获取客服组表单
 * 3. POST /api/tenant/chat/group/:id - 保存客服组
 * 4. DELETE /api/tenant/chat/group/:id - 删除客服组
 *
 * 注意: 复用 AdminServiceGroupService，操作同一张表 chat_service_group
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/tenant/chat/group")
@Tag(name = "Tenant - Service Group Management")
@AllArgsConstructor
public class TenantChatGroupController extends BaseController {

    private final AdminServiceGroupService adminServiceGroupService;

    /**
     * 获取客服组列表
     * GET /api/tenant/chat/group
     *
     * PHP Reference: ServiceGroup.php::index()
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "appid": "tenant",
     *     "name": "Support Team 1",
     *     "sort": 0
     *   }
     * ]
     */
    @GetMapping
    @Operation(summary = "Get Customer Service Group List")
    public ApiResult<List<ChatServiceGroupEntity>> getGroupList() {
        String appid = currentAppid();

        List<ChatServiceGroupEntity> result = adminServiceGroupService.getGroupList(appid);
        return ApiResult.ok(result);
    }

    /**
     * 获取客服组表单
     * GET /api/tenant/chat/group/create/:id
     *
     * PHP Reference: ServiceGroup.php::create()
     *
     * Path Parameters:
     * - id: 客服组ID，0表示创建新的
     *
     * Response (编辑时):
     * {
     *   "group": {
     *     "id": 1,
     *     "appid": "tenant",
     *     "name": "Support Team 1",
     *     "sort": 0
     *   }
     * }
     *
     * Response (创建时):
     * {
     *   "form_rules": []
     * }
     */
    @GetMapping("/create/{id}")
    @Operation(summary = "Get Customer Service Group Form")
    public ApiResult<Map<String, Object>> getGroupForm(@PathVariable Integer id) {
        String appid = currentAppid();

        Map<String, Object> result = adminServiceGroupService.getGroupForm(id, appid);
        return ApiResult.ok(result);
    }

    /**
     * 保存客服组(创建或更新)
     * POST /api/tenant/chat/group/:id
     *
     * PHP Reference: ServiceGroup.php::save()
     *
     * Path Parameters:
     * - id: 客服组ID，0表示创建新的
     *
     * Request Body:
     * {
     *   "name": "Support Team 1",  // 必填
     *   "sort": 0                  // 可选，默认0
     * }
     *
     * Response:
     * { "code": 0, "msg": "Modified successfully" }  // 更新时
     * 或
     * { "code": 0, "msg": "Added successfully" }     // 创建时
     */
    @PostMapping("/{id}")
    @Operation(summary = "Save Service Group")
    public ApiResult<String> saveGroup(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        String appid = currentAppid();

        String message = adminServiceGroupService.saveGroup(id, data, appid);
        return ApiResult.ok(message, "success");
    }

    /**
     * 删除客服组
     * DELETE /api/tenant/chat/group/:id
     *
     * PHP Reference: ServiceGroup.php::delete()
     *
     * Path Parameters:
     * - id: 客服组ID
     *
     * Response: { "code": 0, "msg": "Deleted successfully" }
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Service Group")
    public ApiResult<String> deleteGroup(@PathVariable Integer id) {
        String appid = currentAppid();

        adminServiceGroupService.deleteGroup(id, appid);
        return ApiResult.ok("Deleted successfully", "success");
    }
}
