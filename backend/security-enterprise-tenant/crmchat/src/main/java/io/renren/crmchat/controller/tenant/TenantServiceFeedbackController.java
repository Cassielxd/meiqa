package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatServiceFeedbackEntity;
import io.renren.crmchat.service.TenantServiceFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static io.renren.crmchat.security.TenantSecurityUtils.requireAppid;

/**
 * Tenant API - 客服评价管理
 */
@RestController
@RequestMapping("/api/tenant/chat/feedback")
@Tag(name = "Tenant - Customer Service Rating Management")
@AllArgsConstructor
public class TenantServiceFeedbackController {

    private final TenantServiceFeedbackService tenantServiceFeedbackService;

    @GetMapping
    @Operation(summary = "Rating List")
    public ApiResult<Map<String, Object>> getFeedbackList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String time) {

        requireAppid();

        Map<String, Object> filters = new HashMap<>();
        filters.put("page", page);
        filters.put("limit", limit);
        if (title != null && !title.trim().isEmpty()) {
            filters.put("title", title);
        }
        if (time != null && !time.trim().isEmpty()) {
            filters.put("time", time);
        }

        Map<String, Object> list = tenantServiceFeedbackService.getFeedbackList(filters);
        return ApiResult.ok(list);
    }

    @GetMapping("/{id}/edit")
    @Operation(summary = "Rating Edit Form")
    public ApiResult<Map<String, Object>> getFeedbackEditForm(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        requireAppid();
        Map<String, Object> form = tenantServiceFeedbackService.getEditForm(id);
        return ApiResult.ok(form);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Rating Details")
    public ApiResult<ChatServiceFeedbackEntity> getFeedbackDetail(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        requireAppid();
        ChatServiceFeedbackEntity entity = tenantServiceFeedbackService.getFeedbackDetail(id);
        return ApiResult.ok(entity);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Rating")
    public ApiResult<String> updateFeedback(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> data) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        requireAppid();
        tenantServiceFeedbackService.updateFeedback(id, data);
        return ApiResult.ok("Updated successfully", "success");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Rating")
    public ApiResult<String> deleteFeedback(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        requireAppid();
        tenantServiceFeedbackService.deleteFeedback(id);
        return ApiResult.ok("Deleted successfully", "success");
    }
}
