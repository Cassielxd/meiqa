package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.TenantQrcodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static io.renren.crmchat.security.TenantSecurityUtils.requireAppid;

/**
 * Tenant API - 二维码管理
 * PHP Reference: /app/controller/tenant/chat/Qrcode.php
 */
@RestController
@RequestMapping("/api/tenant/chat/qrcode")
@Tag(name = "Tenant - QR Code Management")
@AllArgsConstructor
public class TenantQrcodeController {

    private final TenantQrcodeService tenantQrcodeService;

    @GetMapping
    @Operation(summary = "Get QR Code List")
    public ApiResult<Map<String, Object>> getQrcodeList(@RequestParam(required = false) String name) {

        requireAppid();
        Map<String, Object> result = tenantQrcodeService.getQrcodeList(name);
        return ApiResult.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get QR Code Form Configuration")
    public ApiResult<Map<String, Object>> getQrcodeForm(@PathVariable Integer id) {

        String appid = requireAppid();
        Map<String, Object> formConfig = tenantQrcodeService.getForm(appid, id);
        return ApiResult.ok(formConfig);
    }

    @PostMapping("/{id}")
    @Operation(summary = "Save/Update QR Code")
    public ApiResult<String> saveQrcode(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> data) {

        requireAppid();
        Integer actualId = (id != null && id > 0) ? id : null;

        tenantQrcodeService.saveQrcode(actualId, data);
        String message = (actualId != null) ? "Modified successfully" : "Saved successfully";
        return ApiResult.ok(message, "success");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete QR Code")
    public ApiResult<String> deleteQrcode(@PathVariable Integer id) {

        requireAppid();
        tenantQrcodeService.deleteQrcode(id);
        return ApiResult.ok("Deleted successfully", "success");
    }
}
