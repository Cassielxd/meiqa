package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatAutoReplyEntity;
import io.renren.crmchat.service.TenantAutoReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static io.renren.crmchat.security.TenantSecurityUtils.requireAppid;

/**
 * Tenant API - 自动回复管理
 * PHP Reference: /app/controller/tenant/chat/AutoReply.php
 */
@RestController
@RequestMapping({"/api/tenant/chat/auto_reply", "/api/tenant/chat/reply"})
@Tag(name = "Tenant - Auto Reply Management")
@AllArgsConstructor
public class TenantAutoReplyController {

    private final TenantAutoReplyService tenantAutoReplyService;

    @GetMapping
    @Operation(summary = "Get Auto Reply List")
    public ApiResult<List<ChatAutoReplyEntity>> getAutoReplyList(
            @RequestParam(required = false, defaultValue = "0") Integer user_id) {

        requireAppid();
        List<ChatAutoReplyEntity> list = tenantAutoReplyService.getAutoReplyList(user_id);
        return ApiResult.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Auto Reply Details")
    public ApiResult<ChatAutoReplyEntity> getAutoReplyDetail(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        requireAppid();
        ChatAutoReplyEntity autoReply = tenantAutoReplyService.getAutoReplyDetail(id);
        return ApiResult.ok(autoReply);
    }

    @PostMapping
    @Operation(summary = "Create Auto Reply")
    public ApiResult<String> createAutoReply(@RequestBody Map<String, Object> data) {

        requireAppid();
        tenantAutoReplyService.createAutoReply(data);
        return ApiResult.ok("Saved successfully", "success");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Auto Reply")
    public ApiResult<String> updateAutoReply(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> data) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        requireAppid();
        tenantAutoReplyService.updateAutoReply(id, data);
        return ApiResult.ok("Updated successfully", "success");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Auto Reply")
    public ApiResult<String> deleteAutoReply(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        requireAppid();
        tenantAutoReplyService.deleteAutoReply(id);
        return ApiResult.ok("Deleted successfully", "success");
    }
}
