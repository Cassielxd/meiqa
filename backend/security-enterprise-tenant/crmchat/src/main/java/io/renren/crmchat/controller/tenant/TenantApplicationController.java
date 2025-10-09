package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ApplicationEntity;
import io.renren.crmchat.service.TenantApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static io.renren.crmchat.security.TenantSecurityUtils.requireAppid;

/**
 * Tenant API - 应用管理
 * PHP Reference: /app/controller/tenant/Application.php
 */
@RestController
@RequestMapping("/api/tenant/app")
@Tag(name = "Tenant - Application Management")
@AllArgsConstructor
public class TenantApplicationController {

    private final TenantApplicationService tenantApplicationService;

    @GetMapping
    @Operation(summary = "Get Application Information")
    public ApiResult<ApplicationEntity> getApplication() {
        String resolvedAppid = requireAppid();
        ApplicationEntity application = tenantApplicationService.getApplication(resolvedAppid);
        return ApiResult.ok("Query successful", application);
    }

    @PostMapping
    @Operation(summary = "Create Application")
    public ApiResult<ApplicationEntity> createApplication(@RequestBody Map<String, Object> data) {
        requireAppid();
        ApplicationEntity application = tenantApplicationService.createApplication(data);
        return ApiResult.ok("Added successfully", application);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Application")
    public ApiResult<String> updateApplication(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> data) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        tenantApplicationService.updateApplication(id, data);
        return ApiResult.ok("Edited successfully", "success");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Application")
    public ApiResult<String> deleteApplication(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        tenantApplicationService.deleteApplication(id);
        return ApiResult.ok("Deleted successfully", "success");
    }

    @PutMapping("/reset/{id}")
    @Operation(summary = "重置token")
    public ApiResult<Map<String, Object>> resetToken(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        Map<String, Object> tokenInfo = tenantApplicationService.resetToken(id);
        return ApiResult.ok("Reset successfully", tokenInfo);
    }
}
