package io.renren.crmchat.controller.kefu;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.security.UserContext;
import io.renren.crmchat.service.KefuAuxiliaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Kefu Auxiliary Controller - 客服辅助功能管理
 * PHP Reference: /app/controller/kefu/User.php, Statistics.php, Service.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/kefu")
@Tag(name = "Kefu Auxiliary - Customer Service Auxiliary Functions")
@AllArgsConstructor
public class KefuAuxiliaryController {

    private final KefuAuxiliaryService kefuAuxiliaryService;

    /**
     * 客服登出
     * POST /api/kefu/user/logout
     *
     * PHP Reference: User.php::logout()
     *
     * 业务说明:
     * - 清除客服登录状态
     * - 更新online状态为离线
     * - 清空client_id和相关APP状态
     *
     * Response: { "code": 0, "msg": "登出成功" }
     */
    @PostMapping("/user/logout")
    @Operation(summary = "Customer Service Logout")
    public ApiResult<String> logout() {
        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        kefuAuxiliaryService.logout(kefuId, appid);
        return ApiResult.ok("Logged out successfully", "success");
    }

    /**
     * 获取未读消息数
     * GET /api/kefu/user/count
     *
     * PHP Reference: User.php::getMessageCount()
     *
     * Response:
     * {
     *   "count": 10  // 未读消息总数
     * }
     */
    @GetMapping("/user/count")
    @Operation(summary = "Get Unread Message Count")
    public ApiResult<Map<String, Object>> getMessageCount() {
        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        Map<String, Object> result = kefuAuxiliaryService.getMessageCount(kefuId, appid);
        return ApiResult.ok(result);
    }

    /**
     * 保存客服反馈
     * POST /api/kefu/user/feedback
     *
     * PHP Reference: User.php::saveFeedback()
     *
     * Request Body:
     * {
     *   "rela_name": "张三",         // 联系人姓名（必填）
     *   "phone": "13800138000",     // 联系电话（必填）
     *   "content": "反馈内容..."     // 反馈内容（必填）
     * }
     *
     * Response: { "code": 0, "msg": "保存成功" }
     */
    @PostMapping("/user/feedback")
    @Operation(summary = "Save Customer Service Feedback")
    public ApiResult<String> saveFeedback(@RequestBody Map<String, Object> data) {
        // 从JWT token中获取当前客服信息
        String appid = UserContext.getAppid();
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        kefuAuxiliaryService.saveFeedback(data, kefuId, appid);
        return ApiResult.ok("Saved successfully", "success");
    }

    /**
     * 获取用户协议
     * GET /api/kefu/agreement
     *
     * PHP Reference: User.php::getUserAgreement()
     *
     * Response:
     * {
     *   "content": "用户协议内容..."
     * }
     */
    @GetMapping("/agreement")
    @Operation(summary = "获取用户协议")
    public ApiResult<Map<String, Object>> getUserAgreement() {
        Map<String, Object> result = kefuAuxiliaryService.getUserAgreement();
        return ApiResult.ok(result);
    }

    /**
     * 客户统计
     * GET /api/kefu/statistics/all
     *
     * PHP Reference: Statistics.php::sum()
     *
     * Response:
     * {
     *   "all": 100,          // 所有客户总数
     *   "toDayKefu": 10,     // 今日新增客户（非游客）
     *   "month": 50,         // 本月客户总数
     *   "toDayTourist": 5    // 今日游客数
     * }
     */
    @GetMapping("/statistics/all")
    @Operation(summary = "客户统计")
    public ApiResult<Map<String, Object>> getKefuSum() {
        // 从JWT token中获取当前租户appid
        String appid = UserContext.getAppid();

        Map<String, Object> result = kefuAuxiliaryService.getKefuSum(appid);
        return ApiResult.ok(result);
    }

    /**
     * 客户首页统计（移动端）
     * GET /api/kefu/statistics/index
     *
     * PHP Reference: Statistics.php::index()
     *
     * Query Parameters:
     * - time: 时间范围（可选，today/week/month，默认为空表示全部）
     *
     * Response:
     * {
     *   "count": 50,      // 统计数量
     *   "time": "today"   // 时间范围
     * }
     */
    @GetMapping("/statistics/index")
    @Operation(summary = "客户首页统计（移动端）")
    public ApiResult<Map<String, Object>> getKefuMobileStatistics(
            @RequestParam(required = false, defaultValue = "") String time) {

        // 从JWT token中获取当前租户appid
        String appid = UserContext.getAppid();

        Map<String, Object> result = kefuAuxiliaryService.getKefuMobileStatistics(time, appid);
        return ApiResult.ok(result);
    }

    /**
     * APP版本检查
     * POST /api/kefu/service/version
     *
     * PHP Reference: Service.php::version()
     *
     * Request Body:
     * {
     *   "version": "1.0.0",  // 当前APP版本号
     *   "name": "iOS"        // 设备名称（可选）
     * }
     *
     * Response:
     * {
     *   "update": true,               // 是否有更新
     *   "version": "1.0.1",          // 新版本号
     *   "url": "http://...",         // 下载地址
     *   "name": "客服APP",            // 应用名称
     *   "info": "更新说明..."         // 更新说明
     * }
     *
     * 或
     *
     * {
     *   "update": false  // 无需更新
     * }
     */
    @PostMapping("/service/version")
    @Operation(summary = "APP版本检查")
    public ApiResult<Map<String, Object>> checkVersion(@RequestBody Map<String, Object> data) {
        String version = data.containsKey("version") && data.get("version") != null
            ? data.get("version").toString() : "";

        Map<String, Object> result = kefuAuxiliaryService.checkVersion(version);
        return ApiResult.ok(result);
    }

    /**
     * 文件上传
     * POST /api/kefu/upload
     *
     * PHP Reference: User.php::upload()
     *
     * Request: multipart/form-data
     * - filename: 上传的文件
     *
     * Response:
     * {
     *   "name": "file.jpg",           // 文件名
     *   "url": "http://...file.jpg"  // 文件URL
     * }
     *
     */
    @PostMapping("/upload")
    @Operation(summary = "File Upload")
    public ApiResult<Map<String, Object>> upload(@RequestParam("filename") MultipartFile file) {
        // 从JWT token中获取当前客服信息
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;
        String appid = UserContext.getAppid();

        Map<String, Object> result = kefuAuxiliaryService.upload(kefuId, appid, file);
        return ApiResult.ok("Image uploaded successfully", result);
    }
}
