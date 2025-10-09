package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.TenantTouristService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// 游客访问不需要认证，不导入 requireAppid

/**
 * Tenant API - 游客访问（获取客服）
 * PHP Reference: /app/controller/mobile/Service.php::getRecordList()
 *               /app/services/chat/ChatServiceServices.php::getRecord()
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/tenant/tourist")
@Tag(name = "Tenant - Visitor Access")
@AllArgsConstructor
public class TenantTouristController {

    private final TenantTouristService tenantTouristService;

    /**
     * 游客获取在线客服
     * GET /api/tenant/tourist/user
     *
     * PHP Reference: mobile/Service.php::getRecordList()
     *                ChatServiceServices.php::getRecord()
     *
     * Query Parameters:
     * - uid: 用户UID（可选）
     * - nickname: 用户昵称（可选）
     * - avatar: 用户头像（可选）
     * - phone: 用户手机号（可选）
     * - sex: 用户性别（可选）
     * - openid: 用户openid（可选）
     * - type: 用户类型（可选）
     * - idTo: 翻页ID（可选）
     * - limit: 每页条数（可选，默认10）
     * - toUserId: 指定客服ID（可选）
     * - cookieUid: Cookie中的UID（可选）
     * - kefu_id: 客服ID（可选）
     * - kefu_rand: 随机客服ID（可选）
     *
     * Response:
     * {
     *   "to_user_id": 客服用户ID,
     *   "to_user_nickname": "客服昵称",
     *   "to_user_avatar": "客服头像",
     *   "is_tourist": 1,
     *   "uid": 游客UID,
     *   "user_id": 游客内部ID,
     *   "nickname": "游客昵称",
     *   "avatar": "游客头像",
     *   "serviceList": [],
     *   "welcome": false
     * }
     */
    @GetMapping("/user")
    @Operation(summary = "Visitor Get Online Customer Service")
    public ApiResult<Map<String, Object>> getOnlineService(
            @RequestParam String appid,  // 租户appid（必填，游客通过参数传递）
            @RequestParam(required = false, defaultValue = "0") Integer uid,
            @RequestParam(required = false, defaultValue = "") String nickname,
            @RequestParam(required = false, defaultValue = "") String avatar,
            @RequestParam(required = false, defaultValue = "") String phone,
            @RequestParam(required = false, defaultValue = "0") Integer sex,
            @RequestParam(required = false, defaultValue = "") String openid,
            @RequestParam(required = false, defaultValue = "0") Integer type,
            @RequestParam(required = false, defaultValue = "0") Integer idTo,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer toUserId,
            @RequestParam(required = false, defaultValue = "0") Integer cookieUid,
            @RequestParam(required = false, defaultValue = "0") Integer kefu_id,
            @RequestParam(required = false, defaultValue = "0") Integer kefu_rand) {

        // 游客不需要认证，直接使用传递的appid

        Map<String, Object> result = tenantTouristService.getOnlineServiceForTourist(
                appid, uid, nickname, avatar, phone, sex, openid, type,
                idTo, limit, toUserId, cookieUid, kefu_id, kefu_rand
        );

        return ApiResult.ok(result);
    }
}
