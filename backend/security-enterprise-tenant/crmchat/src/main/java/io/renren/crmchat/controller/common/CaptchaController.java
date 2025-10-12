package io.renren.crmchat.controller.common;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 公共验证码API - 所有角色共用
 * Admin、Tenant、Kefu 都使用此验证码接口
 *
 * 参考PHP实现: app/controller/admin/Login.php
 *
 * @author CRMChat
 */
@RestController
@AllArgsConstructor
@Tag(name = "Public - CAPTCHA Management")
public class CaptchaController {

    private final CaptchaService captchaService;

    /**
     * 获取AJ滑块验证码 - 公共接口
     * GET /api/ajcaptcha?captchaType=blockPuzzle
     *
     * PHP参考: Login::ajcaptcha()
     * Response: {"repCode":"0000","repData":{...},"repMsg":"Captcha retrieved successfully","success":true}
     */
    @GetMapping("/api/ajcaptcha")
    @Operation(summary = "Get Slider CAPTCHA (Public API)")
    public ApiResult<Map<String, Object>> getAjCaptcha(
            @Parameter(description = "Verification Code Type") @RequestParam(defaultValue = "blockPuzzle") String captchaType) {
        Map<String, Object> result = captchaService.createAjCaptcha(captchaType);
        return ApiResult.ok(result);
    }

    /**
     * 验证滑块验证码（一次验证）- 公共接口
     * POST /api/ajcheck
     *
     * PHP参考: Login::ajcheck()
     * Request: {"token":"xxx","pointJson":"xxx","captchaType":"blockPuzzle"}
     * Response: {"repCode":"0000","repMsg":"Verification successful","success":true}
     */
    @PostMapping("/api/ajcheck")
    @Operation(summary = "Verify Slider CAPTCHA (Public API)")
    public ApiResult<Map<String, Object>> ajCheck(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String pointJson = request.get("pointJson");
        String captchaType = request.get("captchaType");

        Map<String, Object> result = captchaService.checkAjCaptcha(captchaType, token, pointJson);
        return ApiResult.ok(result);
    }

    /**
     * Admin路径别名 - 兼容前端现有调用
     * GET /api/admin/ajcaptcha
     */
    @GetMapping("/api/admin/ajcaptcha")
    @Operation(summary = "Get Slider CAPTCHA (Admin Path)")
    public ApiResult<Map<String, Object>> getAdminAjCaptcha(
            @Parameter(description = "Verification Code Type") @RequestParam(defaultValue = "blockPuzzle") String captchaType) {
        return getAjCaptcha(captchaType);
    }

    /**
     * Admin路径别名 - 兼容前端现有调用
     * POST /api/admin/ajcheck
     */
    @PostMapping("/api/admin/ajcheck")
    @Operation(summary = "Verify slider captcha (admin path)")
    public ApiResult<Map<String, Object>> adminAjCheck(@RequestBody Map<String, String> request) {
        return ajCheck(request);
    }

    /**
     * Tenant路径别名 - 租户登录使用
     * GET /api/tenant/ajcaptcha
     */
    @GetMapping("/api/tenant/ajcaptcha")
    @Operation(summary = "Get Slider CAPTCHA (Tenant Path)")
    public ApiResult<Map<String, Object>> getTenantAjCaptcha(
            @Parameter(description = "Verification Code Type") @RequestParam(defaultValue = "blockPuzzle") String captchaType) {
        return getAjCaptcha(captchaType);
    }

    /**
     * Tenant路径别名 - 租户登录使用
     * POST /api/tenant/ajcheck
     */
    @PostMapping("/api/tenant/ajcheck")
    @Operation(summary = "Verify Slider CAPTCHA (Tenant Path)")
    public ApiResult<Map<String, Object>> tenantAjCheck(@RequestBody Map<String, String> request) {
        return ajCheck(request);
    }

    /**
     * Kefu路径别名 - 客服登录使用
     * GET /api/kefu/ajcaptcha
     */
    @GetMapping("/api/kefu/ajcaptcha")
    @Operation(summary = "Get Slider CAPTCHA (Kefu Path)")
    public ApiResult<Map<String, Object>> getKefuAjCaptcha(
            @Parameter(description = "Verification Code Type") @RequestParam(defaultValue = "blockPuzzle") String captchaType) {
        return getAjCaptcha(captchaType);
    }

    /**
     * Kefu路径别名 - 客服登录使用
     * POST /api/kefu/ajcheck
     */
    @PostMapping("/api/kefu/ajcheck")
    @Operation(summary = "Verify Slider CAPTCHA (Kefu Path)")
    public ApiResult<Map<String, Object>> kefuAjCheck(@RequestBody Map<String, String> request) {
        return ajCheck(request);
    }
}
