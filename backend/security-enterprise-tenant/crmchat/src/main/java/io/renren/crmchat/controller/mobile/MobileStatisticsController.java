package io.renren.crmchat.controller.mobile;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.MobileStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mobile Statistics Controller - 移动端统计管理
 * PHP Reference: /app/controller/mobile/Statistics.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/mobile/user")
@Tag(name = "Mobile Statistics - 移动端统计")
@AllArgsConstructor
public class MobileStatisticsController {

    private final MobileStatisticsService mobileStatisticsService;

    /**
     * 保存站点访问统计
     * POST /api/mobile/user/statistics
     *
     * PHP Reference: Statistics.php::save()
     *
     * Request Body:
     * {
     *   "ip": "192.168.1.1",          // 访问IP（必填）
     *   "path": "/chat",              // 访问路径（必填）
     *   "source": "wechat",           // 来源（可选）
     *   "browser": "Chrome/120.0"     // 浏览器（可选）
     * }
     *
     * Response: { "code": 0, "msg": "添加成功" }
     */
    @PostMapping("/statistics")
    @Operation(summary = "Save Site Visit Statistics")
    public ApiResult<String> saveSiteStatistics(@RequestBody Map<String, Object> data) {
        mobileStatisticsService.saveSiteStatistics(data);
        return ApiResult.ok("Added successfully", "success");
    }
}
