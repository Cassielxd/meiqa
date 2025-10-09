package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatUserLabelCateEntity;
import io.renren.crmchat.service.AdminSpeechcraftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin Speechcraft Cate Controller - 管理员话术分类管理
 * PHP Reference: /app/controller/admin/chat/ServiceSpeechcraftCate.php
 *
 * 功能说明:
 * 1. GET /api/admin/chat/speechcraftcate - 分类列表
 * 2. GET /api/admin/chat/speechcraftcate/create - 获取创建表单
 * 3. POST /api/admin/chat/speechcraftcate - 保存分类
 * 4. GET /api/admin/chat/speechcraftcate/:id - 获取分类详情
 * 5. GET /api/admin/chat/speechcraftcate/:id/edit - 获取编辑表单
 * 6. PUT /api/admin/chat/speechcraftcate/:id - 更新分类
 * 7. DELETE /api/admin/chat/speechcraftcate/:id - 删除分类
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/chat")
@Tag(name = "Admin Chat - Quick Reply Category Management")
@AllArgsConstructor
public class AdminSpeechcraftCateController extends BaseController {

    private final AdminSpeechcraftService adminSpeechcraftService;

    @GetMapping("/speechcraftcate")
    @Operation(summary = "Get Quick Reply Category List")
    public ApiResult<List<ChatUserLabelCateEntity>> getSpeechcraftCateList(
            @RequestParam(required = false) String name) {

        String appid = currentAppid();

        Map<String, Object> where = new java.util.HashMap<>();
        if (name != null) where.put("name", name);

        List<ChatUserLabelCateEntity> result = adminSpeechcraftService.getSpeechcraftCateList(where, appid);
        return ApiResult.ok(result);
    }

    @GetMapping("/speechcraftcate/create")
    @Operation(summary = "Get Create Quick Reply Category Form")
    public ApiResult<Map<String, Object>> getCreateForm() {
        Map<String, Object> result = adminSpeechcraftService.createSpeechcraftCateForm();
        return ApiResult.ok(result);
    }

    @PostMapping("/speechcraftcate")
    @Operation(summary = "Save Quick Reply Category")
    public ApiResult<String> saveSpeechcraftCate(@RequestBody Map<String, Object> data) {
        String appid = currentAppid();

        boolean success = adminSpeechcraftService.saveSpeechcraftCate(data, appid);
        if (success) {
            return ApiResult.ok("Added successfully", "success");
        } else {
            return ApiResult.fail("Failed to add");
        }
    }

    @GetMapping("/speechcraftcate/{id}")
    @Operation(summary = "Get Quick Reply Category Details")
    public ApiResult<ChatUserLabelCateEntity> getSpeechcraftCate(@PathVariable Integer id) {
        String appid = currentAppid();

        ChatUserLabelCateEntity result = adminSpeechcraftService.getSpeechcraftCate(id, appid);
        return ApiResult.ok(result);
    }

    @GetMapping("/speechcraftcate/{id}/edit")
    @Operation(summary = "Get Edit Quick Reply Category Form")
    public ApiResult<Map<String, Object>> getEditForm(@PathVariable Integer id) {
        String appid = currentAppid();

        Map<String, Object> result = adminSpeechcraftService.editSpeechcraftCateForm(id, appid);
        return ApiResult.ok(result);
    }

    @PutMapping("/speechcraftcate/{id}")
    @Operation(summary = "Update Quick Reply Category")
    public ApiResult<String> updateSpeechcraftCate(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        String appid = currentAppid();

        boolean success = adminSpeechcraftService.updateSpeechcraftCate(id, data, appid);
        if (success) {
            return ApiResult.ok("Updated successfully", "success");
        } else {
            return ApiResult.fail("Update failed");
        }
    }

    @DeleteMapping("/speechcraftcate/{id}")
    @Operation(summary = "Delete Quick Reply Category")
    public ApiResult<String> deleteSpeechcraftCate(@PathVariable Integer id) {
        String appid = currentAppid();

        boolean success = adminSpeechcraftService.deleteSpeechcraftCate(id, appid);
        if (success) {
            return ApiResult.ok("Deleted successfully", "success");
        } else {
            return ApiResult.fail("Delete failed");
        }
    }
}
