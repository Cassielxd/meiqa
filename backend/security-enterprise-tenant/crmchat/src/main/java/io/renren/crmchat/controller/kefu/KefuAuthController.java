package io.renren.crmchat.controller.kefu;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.security.UserContext;
import io.renren.crmchat.service.ChatServiceService;
import io.renren.crmchat.service.KefuLoginCodeManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 客服认证控制器
 * 参考 PHP: app/controller/kefu/AuthController.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/kefu")
@Tag(name = "Kefu - Customer Service Authentication Management")
public class KefuAuthController {

    private final ChatServiceService chatServiceService;
    private final KefuLoginCodeManager kefuLoginCodeManager;

    public KefuAuthController(ChatServiceService chatServiceService,
                              KefuLoginCodeManager kefuLoginCodeManager) {
        this.chatServiceService = chatServiceService;
        this.kefuLoginCodeManager = kefuLoginCodeManager;
    }

    /**
     * 客服登录
     * POST /api/kefu/login
     *
     * @param request {"account": "xxx", "password": "xxx", "is_app": 0, "client_id": "xxx"}
     * @return {"status": 200, "msg": "ok", "data": {"token": "xxx", "kefu_info": {...}}}
     */
    @PostMapping("/login")
    @Operation(summary = "Customer Service Login")
    public ApiResult<Map<String, Object>> login(@RequestBody Map<String, Object> request) {
        String account = (String) request.get("account");
        String password = (String) request.get("password");

        if (account == null || account.trim().isEmpty()) {
            return ApiResult.fail("Account cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            return ApiResult.fail("Password cannot be empty");
        }

        Map<String, Object> result = chatServiceService.login(account, password);
        return ApiResult.ok(result);
    }

    /**
     * 获取登录页信息
     * GET /api/kefu/login/info
     *
     * @return {"status": 200, "msg": "ok", "data": {...}}
     */
    @GetMapping("/login/info")
    @Operation(summary = "Get Login Page Information")
    public ApiResult<Map<String, Object>> getLoginInfo() {
        Map<String, Object> info = chatServiceService.getLoginInfo();
        return ApiResult.ok(info);
    }

    /**
     * 获取扫码登录key
     * GET /api/kefu/key
     */
    @GetMapping("/key")
    @Operation(summary = "Get QR login key")
    public ApiResult<Map<String, Object>> getLoginKey() {
        Map<String, Object> payload = kefuLoginCodeManager.generateLoginKey();
        return ApiResult.ok(payload);
    }

    /**
     * 扫码登录状态查询
     * GET /api/kefu/scan/{key}
     */
    @GetMapping("/scan/{key}")
    @Operation(summary = "Check QR login status")
    public ApiResult<Map<String, Object>> scanLogin(@PathVariable String key) {
        KefuLoginCodeManager.LoginCode code = kefuLoginCodeManager.get(key);
        if (code == null) {
            return ApiResult.ok(Map.of("status", 0));
        }

        if (code.getStatus() == 1 && code.getKefuId() != null) {
            Map<String, Object> loginResult = chatServiceService.loginById(code.getKefuId());
            loginResult.put("status", 3);
            kefuLoginCodeManager.consume(key);
            return ApiResult.ok(loginResult);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("status", code.getStatus());
        resp.put("time", code.getExpireAt());
        return ApiResult.ok(resp);
    }

    /**
     * 获取客服登录配置
     * GET /api/kefu/config
     *
     * PHP Reference: Login.php::getAppid()
     */
    @GetMapping("/config")
    @Operation(summary = "Get agent login configuration")
    public ApiResult<Map<String, Object>> getKefuConfig() {
        Map<String, Object> info = chatServiceService.getLoginInfo();
        return ApiResult.ok(info);
    }

    /**
     * 获取客服信息
     * GET /api/kefu/user/userInfo
     *
     * PHP Reference: User.php::getKefuInfo()
     *
     * @return 客服信息
     */
    @GetMapping("/user/userInfo")
    @Operation(summary = "Get agent information")
    public ApiResult<Map<String, Object>> getKefuInfo() {
        // 从JWT token中获取当前客服ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ApiResult.fail("Not logged in or session expired");
        }

        Map<String, Object> result = chatServiceService.getKefuInfo(userId.intValue());
        return ApiResult.ok(result);
    }

    /**
     * 更新个人信息
     * PUT /api/kefu/user/userInfo
     *
     * PHP Reference: User.php::updateKefu()
     *
     * Request Body:
     * {
     *   "nickname": "Agent nickname",
     *   "avatar": "Avatar URL",
     *   "phone": "Phone number"
     * }
     *
     * @return 修改成功
     */
    @PutMapping("/user/userInfo")
    @Operation(summary = "Update personal information")
    public ApiResult<String> updateProfile(@RequestBody Map<String, Object> data) {
        // 从JWT token中获取当前客服ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ApiResult.fail("Not logged in or session expired");
        }

        chatServiceService.updateKefuProfile(userId.intValue(), data);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 修改密码
     * PUT /api/kefu/password
     *
     * PHP Reference: User.php::updateKefu() (password字段)
     *
     * Request Body:
     * {
     *   "old_password": "Old password",
     *   "new_password": "New password"
     * }
     *
     * @return 修改成功
     */
    @PutMapping("/password")
    @Operation(summary = "Change Password")
    public ApiResult<String> updatePassword(@RequestBody Map<String, Object> data) {
        // 从JWT token中获取当前客服ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ApiResult.fail("Not logged in or session expired");
        }

        String oldPassword = (String) data.get("old_password");
        String newPassword = (String) data.get("new_password");

        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return ApiResult.fail("Old password cannot be empty");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ApiResult.fail("New password cannot be empty");
        }

        chatServiceService.updateKefuPassword(userId.intValue(), oldPassword, newPassword);
        return ApiResult.ok("Updated successfully", "success");
    }
}
