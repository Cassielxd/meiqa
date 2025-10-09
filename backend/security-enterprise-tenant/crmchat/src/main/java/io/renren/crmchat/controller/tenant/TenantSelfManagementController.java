package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.TenantsEntity;
import io.renren.crmchat.security.TenantSecurityUtils;
import io.renren.crmchat.service.TenantsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Tenant API - 租户自助管理
 * PHP Reference: /app/controller/tenant/Tenant.php
 */
@RestController
@RequestMapping("/api/tenant/tenant")
@Tag(name = "Tenant - Tenant Self-Service Management")
@AllArgsConstructor
public class TenantSelfManagementController {

    private final TenantsService tenantsService;

    @GetMapping("/info")
    @Operation(summary = "Get Current Tenant Information")
    public ApiResult<TenantsEntity> getTenantInfo() {
        Integer tenantId = TenantSecurityUtils.requireTenantId();
        TenantsEntity tenant = tenantsService.getTenantInfo(tenantId);
        return ApiResult.ok(tenant);
    }

    @GetMapping("/status")
    @Operation(summary = "Get Current Tenant Status")
    public ApiResult<Map<String, Object>> getTenantStatus() {
        Integer tenantId = TenantSecurityUtils.requireTenantId();
        Map<String, Object> status = tenantsService.getTenantStatus(tenantId);
        return ApiResult.ok(status);
    }

    @PutMapping("/update")
    @Operation(summary = "Update Tenant Profile")
    public ApiResult<String> updateTenantInfo(@RequestBody Map<String, Object> data) {
        Integer tenantId = TenantSecurityUtils.requireTenantId();
        tenantsService.updateTenantProfile(tenantId, data);
        return ApiResult.ok("Updated successfully", "success");
    }

    @PutMapping("/password")
    @Operation(summary = "Modify Tenant Password")
    public ApiResult<String> changePassword(@RequestBody Map<String, Object> data) {
        Integer tenantId = TenantSecurityUtils.requireTenantId();

        if (!data.containsKey("old_password") || data.get("old_password") == null
                || !data.containsKey("new_password") || data.get("new_password") == null
                || !data.containsKey("confirm_password") || data.get("confirm_password") == null) {
            return ApiResult.fail("Please fill in complete information");
        }

        String oldPassword = data.get("old_password").toString();
        String newPassword = data.get("new_password").toString();
        String confirmPassword = data.get("confirm_password").toString();

        if (oldPassword.trim().isEmpty() || newPassword.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
            return ApiResult.fail("Please fill in complete information");
        }

        if (!newPassword.equals(confirmPassword)) {
            return ApiResult.fail("New passwords do not match");
        }

        if (newPassword.length() < 6) {
            return ApiResult.fail("New password must be at least 6 characters");
        }

        tenantsService.updatePassword(tenantId, oldPassword, newPassword, confirmPassword);
        return ApiResult.ok("Password changed successfully", "success");
    }
}
