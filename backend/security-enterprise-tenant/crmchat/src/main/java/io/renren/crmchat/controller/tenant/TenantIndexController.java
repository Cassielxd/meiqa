package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.TenantIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static io.renren.crmchat.security.TenantSecurityUtils.requireAppid;

/**
 * Tenant API - 首页/统计
 * PHP Reference: /app/controller/tenant/Index.php
 */
@Slf4j
@RestController
@RequestMapping("/api/tenant")
@Tag(name = "Tenant - Home/Statistics")
@AllArgsConstructor
public class TenantIndexController {

    private final TenantIndexService tenantIndexService;

    @GetMapping("/logo")
    @Operation(summary = "Get Logo Information")
    public ApiResult<Map<String, String>> getLogo() {
        String appid = requireAppid();
        log.info("[租户API] logo - appid={}", appid);

        Map<String, String> result = tenantIndexService.getLogo();
        return ApiResult.ok(result);
    }

    @GetMapping("/jnotice")
    @Operation(summary = "Get Notifications")
    public ApiResult<List<Object>> getJnotice() {
        String appid = requireAppid();
        log.info("[租户API] jnotice - appid={}", appid);

        List<Object> result = tenantIndexService.getJnotice();
        return ApiResult.ok(result);
    }

    @GetMapping("/menus")
    @Operation(summary = "Get Menu List")
    public ApiResult<List<Map<String, Object>>> getMenusList() {
        String appid = requireAppid();
        log.info("[租户API] menus - appid={}", appid);

        List<Map<String, Object>> result = tenantIndexService.getMenusList();
        return ApiResult.ok(result);
    }

    @GetMapping("/sum")
    @Operation(summary = "Customer Statistics")
    public ApiResult<Map<String, Object>> getCustomerSum() {

        String appid = requireAppid();
        log.info("[租户API] sum - appid={}", appid);

        Map<String, Object> result = tenantIndexService.getCustomerSum();
        return ApiResult.ok(result);
    }

    @GetMapping("/index")
    @Operation(summary = "Home Statistics")
    public ApiResult<Map<String, Object>> getIndexStatistics(
            @RequestParam(required = false, defaultValue = "0") Integer type,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        String appid = requireAppid();

        LocalDate now = LocalDate.now();
        if (year == null) {
            year = now.getYear();
        }
        if (month == null) {
            month = now.getMonthValue();
        }

        log.info("[租户API] index - appid={}, type={}, year={}, month={}", appid, type, year, month);

        Map<String, Object> result = tenantIndexService.getIndexStatistics(type, year, month);
        return ApiResult.ok(result);
    }
}
