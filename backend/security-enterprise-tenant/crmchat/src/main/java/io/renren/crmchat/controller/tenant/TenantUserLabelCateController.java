package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatUserLabelCateEntity;
import io.renren.crmchat.service.TenantUserLabelCateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static io.renren.crmchat.security.TenantSecurityUtils.requireAppid;

/**
 * Tenant API - 用户标签分类管理
 * PHP Reference: /app/controller/tenant/user/LabelCate.php
 */
@Slf4j
@RestController
@RequestMapping("/api/tenant/user/label/cate")
@Tag(name = "Tenant - User Tag Category Management")
@AllArgsConstructor
public class TenantUserLabelCateController {

    private final TenantUserLabelCateService tenantUserLabelCateService;

    @GetMapping
    @Operation(summary = "Get Category List")
    public ApiResult<List<ChatUserLabelCateEntity>> getCateList() {

        String appid = requireAppid();
        log.info("[租户API] user/label/cate/list - appid={}", appid);

        List<ChatUserLabelCateEntity> list = tenantUserLabelCateService.getCateList();
        log.info("[租户API] user/label/cate/list - appid={}, count={}", appid, list.size());
        return ApiResult.ok(list);
    }

    @PostMapping
    @Operation(summary = "Create Category")
    public ApiResult<String> createCate(@RequestBody Map<String, Object> data) {

        String appid = requireAppid();
        log.info("[租户API] user/label/cate/create - appid={}, data={}", appid, data);

        tenantUserLabelCateService.createCate(data);
        return ApiResult.ok("Added successfully", "success");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Category")
    public ApiResult<String> updateCate(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> data) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        String appid = requireAppid();
        log.info("[租户API] user/label/cate/update - appid={}, cateId={}, data={}", appid, id, data);

        tenantUserLabelCateService.updateCate(id, data);
        return ApiResult.ok("Updated successfully", "success");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Category")
    public ApiResult<String> deleteCate(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Missing parameters");
        }

        String appid = requireAppid();
        log.info("[租户API] user/label/cate/delete - appid={}, cateId={}", appid, id);

        tenantUserLabelCateService.deleteCate(id);
        return ApiResult.ok("Deleted successfully", "success");
    }

    @PostMapping("/move")
    @Operation(summary = "Sort Move")
    public ApiResult<String> moveCate(@RequestBody Map<String, Object> data) {

        String appid = requireAppid();
        log.info("[租户API] user/label/cate/move - appid={}, data={}", appid, data);

        if (!data.containsKey("ids") || data.get("ids") == null) {
            return ApiResult.fail("Invalid parameters");
        }

        @SuppressWarnings("unchecked")
        List<Integer> ids = (List<Integer>) data.get("ids");

        tenantUserLabelCateService.moveCate(ids);
        return ApiResult.ok("Updated successfully", "success");
    }
}
