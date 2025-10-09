package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatServiceSpeechcraftEntity;
import io.renren.crmchat.service.AdminSpeechcraftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin Speechcraft Controller - 管理员话术管理
 * PHP Reference: /app/controller/admin/chat/ServiceSpeechcraft.php
 *
 * 功能说明:
 * 1. GET /api/admin/chat/speechcraft - 话术列表
 * 2. GET /api/admin/chat/speechcraft/create - 获取创建表单
 * 3. POST /api/admin/chat/speechcraft - 保存话术
 * 4. GET /api/admin/chat/speechcraft/:id - 获取话术详情
 * 5. GET /api/admin/chat/speechcraft/:id/edit - 获取编辑表单
 * 6. PUT /api/admin/chat/speechcraft/:id - 更新话术
 * 7. DELETE /api/admin/chat/speechcraft/:id - 删除话术
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/chat")
@Tag(name = "Admin Chat - Quick Reply Management")
@AllArgsConstructor
public class AdminSpeechcraftController {

    private final AdminSpeechcraftService adminSpeechcraftService;

    @GetMapping("/speechcraft")
    @Operation(summary = "Get Quick Reply List")
    public ApiResult<Map<String, Object>> getSpeechcraftList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) Integer cate_id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {

        Map<String, Object> where = new java.util.HashMap<>();
        if (title != null) where.put("title", title);
        if (message != null) where.put("message", message);
        if (cate_id != null) where.put("cate_id", cate_id);
        where.put("page", page);
        where.put("limit", limit);

        Map<String, Object> result = adminSpeechcraftService.getSpeechcraftList(where);
        return ApiResult.ok(result);
    }

    @GetMapping("/speechcraft/create")
    @Operation(summary = "Get Create Quick Reply Form")
    public ApiResult<Map<String, Object>> getCreateForm() {
        Map<String, Object> result = adminSpeechcraftService.createSpeechcraftForm();
        return ApiResult.ok(result);
    }

    @PostMapping("/speechcraft")
    @Operation(summary = "Save Quick Reply")
    public ApiResult<String> saveSpeechcraft(@RequestBody Map<String, Object> data) {
        boolean success = adminSpeechcraftService.saveSpeechcraft(data);
        if (success) {
            return ApiResult.ok("Quick reply created successfully", "success");
        } else {
            return ApiResult.fail("Failed to create quick reply");
        }
    }

    @GetMapping("/speechcraft/{id}")
    @Operation(summary = "Get Quick Reply Details")
    public ApiResult<ChatServiceSpeechcraftEntity> getSpeechcraft(@PathVariable Integer id) {
        ChatServiceSpeechcraftEntity result = adminSpeechcraftService.getSpeechcraft(id);
        return ApiResult.ok(result);
    }

    @GetMapping("/speechcraft/{id}/edit")
    @Operation(summary = "Get Edit Quick Reply Form")
    public ApiResult<Map<String, Object>> getEditForm(@PathVariable Integer id) {
        Map<String, Object> result = adminSpeechcraftService.updateSpeechcraftForm(id);
        return ApiResult.ok(result);
    }

    @PutMapping("/speechcraft/{id}")
    @Operation(summary = "Update Quick Reply")
    public ApiResult<String> updateSpeechcraft(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        boolean success = adminSpeechcraftService.updateSpeechcraft(id, data);
        if (success) {
            return ApiResult.ok("Updated successfully", "success");
        } else {
            return ApiResult.fail("Update failed");
        }
    }

    @DeleteMapping("/speechcraft/{id}")
    @Operation(summary = "Delete Quick Reply")
    public ApiResult<String> deleteSpeechcraft(@PathVariable Integer id) {
        boolean success = adminSpeechcraftService.deleteSpeechcraft(id);
        if (success) {
            return ApiResult.ok("Deleted successfully", "success");
        } else {
            return ApiResult.fail("Delete failed");
        }
    }
}
