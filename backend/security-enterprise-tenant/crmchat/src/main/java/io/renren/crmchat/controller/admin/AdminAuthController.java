package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.SystemAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin API - 认证相关
 *
 * @author CRMChat
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin - Authentication Management")
public class AdminAuthController {

    /**
     * 1.1 管理员登录
     * POST /api/admin/login
     *
     * Request: {"account": "admin", "pwd": "123456", "imgcode": "", "captchaVerification": "", "captchaType": ""}
     * Response: {"status": 200, "msg": "ok", "data": {"token": "...", "expires_time": 1760497298, ...}}
     */
    private final SystemAdminService systemAdminService;

    public AdminAuthController(SystemAdminService systemAdminService) {
        this.systemAdminService = systemAdminService;
    }

    @PostMapping("/login")
    @Operation(summary = "Administrator Login")
    public ApiResult<Map<String, Object>> login(@RequestBody Map<String, Object> request) {
        String account = (String) request.get("account");
        String password = (String) request.get("pwd");

        // 参数验证
        if (account == null || account.trim().isEmpty()) {
            return ApiResult.fail("Account cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            return ApiResult.fail("Password cannot be empty");
        }

        // 登录
        Map<String, Object> result = systemAdminService.login(account, password);
        return ApiResult.ok(result);
    }

    /**
     * 1.2 获取登录信息
     * GET /api/admin/login/info
     *
     * Response: {"status": 200, "msg": "ok", "data": {"slide": [], "logo_square": "", ...}}
     */
    @GetMapping("/login/info")
    @Operation(summary = "Get Login Page Information")
    public ApiResult<Map<String, Object>> getLoginInfo() {
        Map<String, Object> info = systemAdminService.getLoginInfo();
        return ApiResult.ok(info);
    }

    /**
     * 临时API：重置admin密码为admin123
     * POST /api/admin/reset_admin_password
     */
    @PostMapping("/reset_admin_password")
    @Operation(summary = "Reset Admin Password")
    public ApiResult<String> resetAdminPassword() {
        systemAdminService.resetAdminPassword("admin", "admin123");
        return ApiResult.ok("Password has been reset to: admin123", "success");
    }

    /**
     * Temporary API: fix tenant max_services
     * POST /api/admin/fix_tenant_max_services
     */
    @PostMapping("/fix_tenant_max_services")
    @Operation(summary = "Fix tenant max_services")
    public ApiResult<String> fixTenantMaxServices() {
        // 直接执行SQL更新
        org.springframework.jdbc.core.JdbcTemplate jdbcTemplate = new org.springframework.jdbc.core.JdbcTemplate(
            new com.alibaba.druid.pool.DruidDataSource(){{
                setUrl("jdbc:mysql://localhost:3306/crmeb");
                setUsername("root");
                setPassword("root123");
            }}
        );
        jdbcTemplate.update("UPDATE eb_tenants SET max_services=5 WHERE id=8");
        return ApiResult.ok("max_services has been updated to 5", "success");
    }

    // 注意：验证码相关API已在独立的Controller中实现，避免重复定义
}
