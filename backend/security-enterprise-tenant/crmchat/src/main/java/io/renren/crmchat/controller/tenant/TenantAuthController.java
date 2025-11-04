package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.dto.tenant.TenantRegisterDTO;
import io.renren.crmchat.service.TenantsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 租户认证控制器
 * 参考 PHP: app/controller/tenant/AuthController.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/tenant")
@Tag(name = "Tenant - Tenant Authentication Management")
public class TenantAuthController {

    private final TenantsService tenantsService;

    public TenantAuthController(TenantsService tenantsService) {
        this.tenantsService = tenantsService;
    }

    /**
     * 租户登录
     * POST /api/tenant/login
     *
     * @param request {"account": "xxx", "pwd": "xxx"}
     * @return {"status": 200, "msg": "ok", "data": {"token": "xxx", "user_info": {...}}}
     */
    @PostMapping("/login")
    @Operation(summary = "Tenant Login")
    public ApiResult<Map<String, Object>> login(@RequestBody Map<String, Object> request) {
        String account = (String) request.get("account");
        String password = (String) request.get("pwd");

        if (account == null || account.trim().isEmpty()) {
            return ApiResult.fail("Account cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            return ApiResult.fail("Password cannot be empty");
        }

        Map<String, Object> result = tenantsService.login(account, password);
        return ApiResult.ok(result);
    }

    /**
     * 获取登录页信息
     * GET /api/tenant/login/info
     *
     * @return {"status": 200, "msg": "ok", "data": {...}}
     */
    @GetMapping("/login/info")
    @Operation(summary = "Get Login Page Information")
    public ApiResult<Map<String, Object>> getLoginInfo() {
        Map<String, Object> info = tenantsService.getLoginInfo();
        return ApiResult.ok(info);
    }

    /**
     * 租户注册
     * POST /api/tenant/register
     * PHP参考: app/controller/tenant/Login.php::register()
     *
     * @param registerDTO 注册信息
     * @return {"status": 200, "msg": "注册成功", "data": {"tenant_id": 1, "tenant_code": "xxx", "status": "待审核"}}
     */
    @PostMapping("/register")
    @Operation(summary = "Tenant Registration")
    public ApiResult<Map<String, Object>> register(@Valid @RequestBody TenantRegisterDTO registerDTO) {
        Map<String, Object> result = tenantsService.register(registerDTO);
        return ApiResult.ok((String) result.get("message"), result);
    }

    /**
     * 发送注册验证码
     * POST /api/tenant/send_captcha
     * PHP参考: app/controller/tenant/Login.php::sendCaptcha()
     *
     * @param request {"email": "test@example.com"}
     * @return {"status": 200, "msg": "Verification code sent successfully", "data": {"status": "success", "captcha": "123456"}}
     */
    @PostMapping("/send_captcha")
    @Operation(summary = "发送注册验证码")
    public ApiResult<String> sendCaptcha(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || (email = email.trim()).isEmpty()) {
            return ApiResult.fail("Please enter email address");
        }

        Map<String, Object> result = tenantsService.sendRegisterCaptcha(email);
        String message = (String) result.getOrDefault("message", "验证码发送成功");
        return ApiResult.ok(message);
    }

    /**
     * 更新租户个人信息
     * PUT /api/tenant/profile
     *
     * PHP Reference: Tenant.php::update()
     *
     * Request Body:
     * {
     *   "tenant_name": "公司名称",      // 可选
     *   "contact_name": "联系人",       // 可选
     *   "contact_phone": "13800138000", // 可选
     *   "domain": "example.com",        // 可选
     *   "logo": "http://..."            // 可选
     * }
     *
     * Response: { "code": 0, "msg": "Updated successfully" }
     */
    @PutMapping("/profile")
    @Operation(summary = "更新租户个人信息")
    public ApiResult<String> updateProfile(@RequestBody Map<String, Object> data) {
        Integer tenantId = io.renren.crmchat.security.TenantSecurityUtils.requireTenantId();
        tenantsService.updateTenantProfile(tenantId, data);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 修改租户密码
     * PUT /api/tenant/password
     *
     * PHP Reference: Tenant.php::changePassword()
     *
     * Request Body:
     * {
     *   "old_password": "oldpass",      // 必填
     *   "new_password": "newpass",      // 必填，不少于6位
     *   "confirm_password": "newpass"   // 必填
     * }
     *
     * Response: { "code": 0, "msg": "密码修改成功" }
     */
    @PutMapping("/password")
    @Operation(summary = "Modify Tenant Password")
    public ApiResult<String> changePassword(@RequestBody Map<String, Object> data) {
        Integer tenantId = io.renren.crmchat.security.TenantSecurityUtils.requireTenantId();

        // 参数验证
        if (!data.containsKey("old_password") || data.get("old_password") == null || data.get("old_password").toString().trim().isEmpty()) {
            return ApiResult.fail("Please enter old password");
        }
        if (!data.containsKey("new_password") || data.get("new_password") == null || data.get("new_password").toString().trim().isEmpty()) {
            return ApiResult.fail("Please enter new password");
        }
        if (!data.containsKey("confirm_password") || data.get("confirm_password") == null || data.get("confirm_password").toString().trim().isEmpty()) {
            return ApiResult.fail("Please enter password confirmation");
        }

        String oldPassword = data.get("old_password").toString();
        String newPassword = data.get("new_password").toString();
        String confirmPassword = data.get("confirm_password").toString();

        tenantsService.updatePassword(tenantId, oldPassword, newPassword, confirmPassword);
        return ApiResult.ok("Password changed successfully", "success");
    }
}
