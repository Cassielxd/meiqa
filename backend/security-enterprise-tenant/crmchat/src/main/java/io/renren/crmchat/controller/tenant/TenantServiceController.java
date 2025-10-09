package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Tenant API - 客服服务相关
 * PHP Reference: /app/controller/mobile/Service.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/tenant/service")
@Tag(name = "Tenant - Customer Service")
@AllArgsConstructor
public class TenantServiceController {

    /**
     * 获取客服页面广告内容
     * GET /api/tenant/service/adv
     *
     * PHP Reference: Service.php::getKfAdv()
     *
     * Response:
     * {
     *   "content": "广告内容HTML"
     * }
     */
    @GetMapping("/adv")
    @Operation(summary = "Get Customer Service Page Advertisement Content")
    public ApiResult<Map<String, Object>> adv() {

        // 临时返回空广告
        Map<String, Object> result = new HashMap<>();
        result.put("content", "");

        return ApiResult.ok(result);
    }
}
