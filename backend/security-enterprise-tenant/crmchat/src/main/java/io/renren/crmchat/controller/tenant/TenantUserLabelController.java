package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatUserLabelEntity;
import io.renren.crmchat.service.TenantUserLabelService;
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
 * Tenant API - 用户标签管理
 * PHP Reference: /app/controller/tenant/user/Label.php
 *
 * @author CRMChat Team
 */
@Slf4j
@RestController
@RequestMapping("/api/tenant/user/label")
@Tag(name = "Tenant - User Tag Management")
@AllArgsConstructor
public class TenantUserLabelController {

    private final TenantUserLabelService tenantUserLabelService;

    @GetMapping
    @Operation(summary = "Get Tag List")
    public ApiResult<Map<String, Object>> getLabelList(
            @RequestParam(required = false) String cate_id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "15") Integer limit) {

        String resolvedAppid = requireAppid();
        log.info("[租户API] user/label/list - appid={}, cate_id={}, page={}, limit={}",
            resolvedAppid, cate_id, page, limit);

        Map<String, Object> filters = new HashMap<>();
        if (cate_id != null && !cate_id.trim().isEmpty()) {
            filters.put("cate_id", cate_id);
        }

        Map<String, Object> result = tenantUserLabelService.getLabelListWithPagination(
            filters, resolvedAppid, page, limit);
        log.info("[租户API] user/label/list - appid={}, count={}", resolvedAppid,
            result.get("count"));
        return ApiResult.ok(result);
    }

    @GetMapping("/create")
    @Operation(summary = "Get Create Form")
    public ApiResult<Map<String, Object>> getCreateForm() {

        String resolvedAppid = requireAppid();
        log.info("[租户API] user/label/create - appid={}", resolvedAppid);

        Map<String, Object> result = tenantUserLabelService.getCreateForm(resolvedAppid);
        return ApiResult.ok(result);
    }

    @GetMapping("/{id}/edit")
    @Operation(summary = "Get Edit Form")
    public ApiResult<Map<String, Object>> getEditForm(@PathVariable Integer id) {

        String resolvedAppid = requireAppid();
        log.info("[租户API] user/label/edit - appid={}, labelId={}", resolvedAppid, id);

        Map<String, Object> result = tenantUserLabelService.getEditForm(id, resolvedAppid);
        return ApiResult.ok(result);
    }

    @PostMapping
    @Operation(summary = "Create Tag")
    public ApiResult<String> createLabel(@RequestBody Map<String, Object> data) {

        String resolvedAppid = requireAppid();
        log.info("[租户API] user/label/create - appid={}, data={}", resolvedAppid, data);

        tenantUserLabelService.createLabel(data, resolvedAppid);
        return ApiResult.ok("Saved successfully", "success");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Tag")
    public ApiResult<String> updateLabel(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> data) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        String resolvedAppid = requireAppid();
        log.info("[租户API] user/label/update - appid={}, labelId={}, data={}", resolvedAppid, id, data);

        tenantUserLabelService.updateLabel(id, data, resolvedAppid);
        return ApiResult.ok("Updated successfully", "success");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Tag")
    public ApiResult<String> deleteLabel(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        String resolvedAppid = requireAppid();
        log.info("[租户API] user/label/delete - appid={}, labelId={}", resolvedAppid, id);

        tenantUserLabelService.deleteLabel(id, resolvedAppid);
        return ApiResult.ok("Deleted successfully", "success");
    }

    @PostMapping("/move")
    @Operation(summary = "Move Sort")
    public ApiResult<String> moveLabel(@RequestBody Map<String, Object> data) {

        String resolvedAppid = requireAppid();
        log.info("[租户API] user/label/move - appid={}, data={}", resolvedAppid, data);

        if (!data.containsKey("ids") || data.get("ids") == null) {
            return ApiResult.fail("Invalid parameters");
        }

        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) data.get("ids");

        Integer page = 0;
        if (data.containsKey("page") && data.get("page") != null) {
            page = Integer.parseInt(data.get("page").toString());
        }

        tenantUserLabelService.moveLabel(ids, page, resolvedAppid);
        return ApiResult.ok("Updated successfully", "success");
    }
}
