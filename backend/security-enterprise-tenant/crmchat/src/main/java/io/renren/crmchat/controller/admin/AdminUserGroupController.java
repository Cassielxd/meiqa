package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatUserGroupEntity;
import io.renren.crmchat.service.AdminUserGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin User Group Controller - 管理员用户分组管理
 * PHP Reference: /app/controller/admin/user/Group.php
 *
 * 功能说明:
 * 1. GET /api/admin/user/group - 分组列表
 * 2. GET /api/admin/user/group/create - 获取创建表单
 * 3. POST /api/admin/user/group - 保存分组
 * 4. GET /api/admin/user/group/:id/edit - 获取编辑表单
 * 5. PUT /api/admin/user/group/:id - 更新分组
 * 6. DELETE /api/admin/user/group - 删除分组
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/user")
@Tag(name = "Admin User - Group Management")
@AllArgsConstructor
public class AdminUserGroupController extends BaseController {

    private final AdminUserGroupService adminUserGroupService;

    @GetMapping("/group")
    @Operation(summary = "Get Group List")
    public ApiResult<Map<String, Object>> getGroupList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "15") Integer limit) {
        String appid = currentAppid();
        Map<String, Object> result = adminUserGroupService.getGroupList(appid, page, limit);
        return ApiResult.ok(result);
    }

    @GetMapping("/group/create")
    @Operation(summary = "Get Create Form")
    public ApiResult<Map<String, Object>> getCreateForm() {
        Map<String, Object> result = adminUserGroupService.getCreateForm();
        return ApiResult.ok(result);
    }

    @PostMapping("/group")
    @Operation(summary = "Save Group")
    public ApiResult<String> saveGroup(@RequestBody Map<String, Object> data) {
        String appid = currentAppid();
        adminUserGroupService.saveGroup(data, appid);
        return ApiResult.ok("Submitted successfully", "success");
    }

    @GetMapping("/group/{id}/edit")
    @Operation(summary = "Get Edit Form")
    public ApiResult<Map<String, Object>> getEditForm(@PathVariable Integer id) {
        String appid = currentAppid();
        Map<String, Object> result = adminUserGroupService.getEditForm(id, appid);
        return ApiResult.ok(result);
    }

    @PutMapping("/group/{id}")
    @Operation(summary = "Update Group")
    public ApiResult<String> updateGroup(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        String appid = currentAppid();
        boolean success = adminUserGroupService.updateGroup(id, data, appid);
        return success ? ApiResult.ok("Submitted successfully", "success") : ApiResult.fail("Update failed");
    }

    @DeleteMapping("/group/{id}")
    @Operation(summary = "Delete Group")
    public ApiResult<String> deleteGroup(@PathVariable Integer id) {
        String appid = currentAppid();
        boolean success = adminUserGroupService.deleteGroup(id, appid);
        return success ? ApiResult.ok("Deleted successfully", "success") : ApiResult.fail("Delete failed, please try again later");
    }
}
