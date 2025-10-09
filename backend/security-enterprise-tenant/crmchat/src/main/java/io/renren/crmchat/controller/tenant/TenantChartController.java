package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.dto.ChartStatisticsDTO;
import io.renren.crmchat.dto.ChartSumDTO;
import io.renren.crmchat.service.AdminChartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static io.renren.crmchat.security.TenantSecurityUtils.requireAppid;

/**
 * Tenant API - 统计图表
 * PHP Reference: /app/controller/tenant/Index.php
 *
 * @author CRMChat Team
 */
@Slf4j
@RestController
@RequestMapping("/api/tenant/chart")
@Tag(name = "Tenant - Statistics Charts")
@AllArgsConstructor
public class TenantChartController {

    private final AdminChartService adminChartService;

    /**
     * 获取统计汇总数据
     * GET /api/tenant/chart/sum
     *
     * PHP Reference: Index.php::sum()
     *
     * Response:
     * {
     *   "all": 100,              // 全部客户数量
     *   "toDayKefu": 10,         // 今日新增客户（非游客）
     *   "month": 50,             // 本月新增客户
     *   "toDayTourist": 5        // 今日新增游客
     * }
     */
    @GetMapping("/sum")
    @Operation(summary = "Get Statistical Summary Data")
    public ApiResult<ChartSumDTO> getSum() {
        String appid = requireAppid();
        log.info("[租户API] chart/sum - appid={}", appid);
        ChartSumDTO dto = adminChartService.getKefuSum(appid);
        log.info("[租户API] chart/sum - appid={}, result: all={}, toDayKefu={}, month={}, toDayTourist={}",
                 appid, dto.getAll(), dto.getToDayKefu(), dto.getMonth(), dto.getToDayTourist());
        return ApiResult.ok(dto);
    }

    /**
     * 获取客服统计图表数据
     * GET /api/tenant/chart
     *
     * PHP Reference: Index.php::chart()
     *
     * Query Parameters:
     * - year: 年份（必填）
     * - month: 月份（可选，如果提供则按月统计）
     *
     * Response:
     * {
     *   "list": [...],    // 客户数据（非游客）
     *   "tourist": [...]  // 游客数据
     * }
     */
    @GetMapping
    @Operation(summary = "获取客服统计图表数据")
    public ApiResult<ChartStatisticsDTO> getChart(
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month) {

        String appid = requireAppid();
        log.info("[租户API] chart - appid={}, year={}, month={}", appid, year, month);

        // PHP参数验证：月份范围1-12
        if (month != null && (month <= 0 || month > 12)) {
            return ApiResult.fail("Invalid month");
        }

        // PHP参数验证：年份格式检查
        if (year == null || year < 1000 || year > 9999) {
            return ApiResult.fail("Invalid year");
        }

        // 如果提供了month参数，则按月统计（type=1），否则按年统计（type=0）
        Integer type = (month != null) ? 1 : 0;

        // 如果是按年统计，月份参数设为1（不影响实际查询）
        if (month == null) {
            month = 1;
        }

        ChartStatisticsDTO dto = adminChartService.getKefuStatistics(type, year, month, appid);
        log.info("[租户API] chart - appid={}, customerCount={}, touristCount={}",
                 appid, dto.getList().size(), dto.getTourist().size());
        return ApiResult.ok(dto);
    }
}
