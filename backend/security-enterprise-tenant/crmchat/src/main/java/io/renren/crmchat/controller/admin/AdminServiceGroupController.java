package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatServiceGroupEntity;
import io.renren.crmchat.service.AdminServiceGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin Service Group Controller - 管理员客服组管理
 * PHP Reference: /app/controller/admin/chat/ServiceGroup.php
 *
 * 功能说明:
 * 1. GET /api/admin/chat/group - 客服组列表
 * 2. GET /api/admin/chat/group/create/:id - 获取客服组表单
 * 3. POST /api/admin/chat/group/:id - 保存客服组
 * 4. DELETE /api/admin/chat/group/:id - 删除客服组
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/chat/group")
@Tag(name = "Admin Service Group - 客服组管理")
@AllArgsConstructor
public class AdminServiceGroupController extends BaseController {

    private final AdminServiceGroupService adminServiceGroupService;

    /**
     * 获取客服组列表
     * GET /api/admin/chat/group
     *
     * PHP Reference: ServiceGroup.php::index()
     *
     * Response: [...]
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
     * GET /api/admin/chat/group/create/:id
     *
     * PHP Reference: ServiceGroup.php::create()
     *
     * Response:
     * {
     *   "group": {...}  // 编辑时返回
     * }
     * 或
     * {
     *   "form_rules": []  // 创建时返回
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
     * POST /api/admin/chat/group/:id
     *
     * PHP Reference: ServiceGroup.php::save()
     *
     * Request Body:
     * {
     *   "name": "客服组1",
     *   "sort": 0
     * }
     *
     * Response: { "code": 0, "msg": "Modified successfully" } 或 { "code": 0, "msg": "添加成功" }
     */
    @PostMapping("/{id}")
    @Operation(summary = "保存客服组")
    public ApiResult<String> saveGroup(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        String appid = currentAppid();

        String message = adminServiceGroupService.saveGroup(id, data, appid);
        return ApiResult.ok(message, "success");
    }

    /**
     * 删除客服组
     * DELETE /api/admin/chat/group/:id
     *
     * PHP Reference: ServiceGroup.php::delete()
     *
     * Response: { "code": 0, "msg": "删除成功" }
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除客服组")
    public ApiResult<String> deleteGroup(@PathVariable Integer id) {
        String appid = currentAppid();

        adminServiceGroupService.deleteGroup(id, appid);
        return ApiResult.ok("Deleted successfully", "success");
    }
}
