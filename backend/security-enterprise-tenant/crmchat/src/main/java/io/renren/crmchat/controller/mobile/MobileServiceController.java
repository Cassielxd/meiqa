package io.renren.crmchat.controller.mobile;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.security.UserContext;
import io.renren.crmchat.service.MobileServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Mobile Service Controller - 移动端客服服务管理
 * PHP Reference: /app/controller/mobile/Service.php
 *
 * @author CRMChat Team
 */
@Slf4j
@RestController
@RequestMapping("/api/mobile")
@Tag(name = "Mobile Service - Mobile Customer Service")
@AllArgsConstructor
public class MobileServiceController {

    private final MobileServiceService mobileServiceService;

    /**
     * 获取聊天记录
     * GET /api/mobile/user/record
     *
     * PHP Reference: Service.php::getRecordList()
     *
     * Request Body:
     * {
     *   "idTo": 0,                    // 起始消息ID（分页用，可选）
     *   "limit": 10,                  // 每页数量（可选，默认10）
     *   "toUserId": 0,                // 目标客服ID（可选）
     *   "cookieUid": 0,               // 当前用户ID（可选）
     *   "kefu_id": 0,                 // 指定客服ID（可选）
     *   "kefu_rand": 0,               // 随机客服ID（可选）
     *   "uid": "",                    // 用户标识（可选）
     *   "nickname": "",               // 昵称（可选）
     *   "phone": "",                  // 手机号（可选）
     *   "sex": "",                    // 性别（可选）
     *   "avatar": "",                 // 头像（可选）
     *   "openid": "",                 // OpenID（可选）
     *   "type": ""                    // 类型（可选）
     * }
     *
     * Response:
     * {
     *   "list": [...],
     *   "toUserId": 123,
     *   "kefuId": 456
     * }
     */
    @GetMapping("/user/record")
    @Operation(summary = "Get Chat Records")
    public ApiResult<Map<String, Object>> getRecordList(@RequestParam Map<String, String> params) {
        Map<String, Object> requestParams = new HashMap<>(params);
        String appid = requestParams.containsKey("appid") ? requestParams.get("appid").toString() : UserContext.getAppid();

        // 从JWT token中提取游客的userId（如果已登录）
       /* Long jwtUserId = UserContext.getUserId();
        if (jwtUserId != null && jwtUserId > 0) {
            // 如果JWT中有userId，传递给Service层作为cookieUid
            requestParams.put("cookieUid", String.valueOf(jwtUserId));
            log.info("[游客历史消息] 从JWT提取userId: {}, appid: {}", jwtUserId, appid);
        }*/

        // 调试日志：检查租户隔离
        log.info("[租户隔离检查] Controller层 - 原始params: {}, 提取的appid: {}", params, appid);

        Map<String, Object> result = mobileServiceService.getRecordList(requestParams, appid);
        return ApiResult.ok(result);
    }

    /**
     * 获取客服页面广告内容
     * GET /api/mobile/service/adv
     *
     * PHP Reference: Service.php::getKfAdv()
     *
     * Response:
     * {
     *   "content": "Advertisement Content HTML"
     * }
     */
    @GetMapping("/service/adv")
    @Operation(summary = "Get Customer Service Page Advertisement Content")
    public ApiResult<Map<String, Object>> getKfAdv() {
        Map<String, Object> result = mobileServiceService.getKfAdv();
        return ApiResult.ok(result);
    }

    /**
     * 获取缓存数据
     * GET /api/mobile/service/cache/:key
     *
     * PHP Reference: Service.php::getCache()
     *
     * Path Variable:
     * - key: 缓存键
     *
     * Response:
     * {
     *   "value": {...}
     * }
     */
    @GetMapping("/service/cache/{key}")
    @Operation(summary = "Get Cache Data")
    public ApiResult<Map<String, Object>> getCache(@PathVariable String key) {
        Map<String, Object> result = mobileServiceService.getCache(key);
        return ApiResult.ok(result);
    }

    /**
     * 设置缓存数据
     * POST /api/mobile/service/cache
     *
     * PHP Reference: Service.php::setCache()
     *
     * Request Body:
     * {
     *   "key": "cache_key",         // 缓存键（必填）
     *   "value": {...}              // 缓存值（必填）
     * }
     *
     * Response: { "code": 0, "msg": "ok" }
     */
    @PostMapping("/service/cache")
    @Operation(summary = "设置缓存数据")
    public ApiResult<String> setCache(@RequestBody Map<String, Object> data) {
        if (!data.containsKey("key") || data.get("key") == null) {
            return ApiResult.fail("Key must exist");
        }

        String key = data.get("key").toString();
        Object value = data.get("value");

        mobileServiceService.setCache(key, value);
        return ApiResult.ok("ok", "success");
    }

    /**
     * 图片上传
     * POST /api/mobile/service/upload
     *
     * PHP Reference: Service.php::upload()
     *
     * Request:
     * - Content-Type: multipart/form-data
     * - filename: 上传文件字段名（必填）
     *
     * Response:
     * {
     *   "name": "filename.jpg",
     *   "url": "https://domain.com/uploads/store/comment/filename.jpg"
     * }
     */
    @PostMapping("/service/upload")
    @Operation(summary = "图片上传")
    public ApiResult<Map<String, Object>> upload(
            @RequestParam("filename") MultipartFile file,
            @RequestParam(value = "appid", required = false, defaultValue = "default") String appid) {
        appid = UserContext.getAppid();
        Map<String, Object> result = mobileServiceService.upload(file, appid);
        return ApiResult.ok("Image uploaded successfully", result);
    }

    /**
     * 游客自动登录：创建/更新游客账号并返回 JWT Token
     */
    @PostMapping("/service/auto_login")
    @Operation(summary = "游客自动登录")
    public ApiResult<Map<String, Object>> autoLogin(@RequestBody Map<String, Object> data) {
        String appid = data.containsKey("appid") ? data.get("appid").toString() : UserContext.getAppid();
        Map<String, Object> result = mobileServiceService.autoLogin(data, appid);
        return ApiResult.ok(result);
    }

    /**
     * 获取客服图标配置
     * GET /api/mobile/service/icon
     *
     * PHP Reference: Service.php::getKefuConfig()
     *
     * Response:
     * {
     *   "icon": "https://domain.com/icon.png",
     *   "type": "1"
     * }
     */
    @GetMapping("/service/icon")
    @Operation(summary = "获取客服图标配置")
    public ApiResult<Map<String, Object>> getKefuConfig() {
        Map<String, Object> result = mobileServiceService.getKefuConfig();
        return ApiResult.ok(result);
    }

    /**
     * 获取消息发送ID
     * GET /api/mobile/service/get_send_id
     *
     * PHP Reference: Service.php::getSendId()
     *
     * Response:
     * {
     *   "send_id": "550e8400e29b41d4a716446655440000"
     * }
     */
    @GetMapping("/service/get_send_id")
    @Operation(summary = "获取消息发送ID")
    public ApiResult<Map<String, Object>> getSendId() {
        Map<String, Object> result = mobileServiceService.getSendId();
        return ApiResult.ok(result);
    }

    /**
     * 发送消息
     * POST /api/mobile/service/send_message
     *
     * PHP Reference: Service.php::sendMessage()
     *
     * Request Body:
     * {
     *   "to_user_id": 456,              // 接收人user_id（必填）
     *   "msn": "你好",                   // 消息内容（必填）
     *   "guid": "unique-message-id",    // 消息唯一ID（必填）
     *   "user_id": 123,                 // 发送人user_id（必填）
     *   "msn_type": 1,                  // 消息类型（可选，1-文字，2-表情，3-图片，4-语音）
     *   "other": "",                    // 其他信息（可选，JSON）
     *   "is_tourist": 0                 // 是否游客（可选）
     * }
     *
     * Response:
     * {
     *   "id": 123,
     *   "user_id": 1,
     *   "to_user_id": 456,
     *   "msn": "你好",
     *   "msn_type": 1,
     *   "type": 0,
     *   "other": "",
     *   "guid": "unique-message-id",
     *   "add_time": 1234567890
     * }
     */
    @PostMapping("/service/send_message")
    @Operation(summary = "发送消息")
    public ApiResult<Map<String, Object>> sendMessage(@RequestBody Map<String, Object> data) {
        // 从请求参数中获取appid（游客访问模式下由前端传入）
        String appid = data.containsKey("appid") ? data.get("appid").toString() : UserContext.getAppid();

        Map<String, Object> result = mobileServiceService.sendMessage(data, appid);
        return ApiResult.ok("Sent successfully", result);
    }
}
