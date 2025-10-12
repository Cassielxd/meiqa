package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.dto.ChartStatisticsDTO;
import io.renren.crmchat.dto.ChartSumDTO;
import io.renren.crmchat.security.UserContext;
import io.renren.crmchat.service.AdminIndexService;
import io.renren.crmchat.service.SystemMenusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin Index Controller - 管理员首页/统计
 * PHP Reference: /app/controller/admin/Index.php
 *
 * 功能说明:
 * 1. GET /api/admin/logo - 获取logo
 * 2. GET /api/admin/jnotice - 消息通知
 * 3. GET /api/admin/menusList - 获取菜单列表
 * 4. GET /api/admin/chart/sum - 客户统计
 * 5. GET /api/admin/chart - 客户首页统计
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Index")
@AllArgsConstructor
public class AdminIndexController extends BaseController {

    private final AdminIndexService adminIndexService;
    private final SystemMenusService systemMenusService;

    /**
     * 获取logo
     * GET /api/admin/logo
     *
     * PHP Reference: Index.php::logo()
     *
     * Response:
     * {
     *   "logo": "...",
     *   "logo_square": "...",
     *   "site_name": "CRMChat"
     * }
     */
    @GetMapping("/logo")
    @Operation(summary = "Get Logo")
    public ApiResult<Map<String, Object>> getLogo() {
        Map<String, Object> result = adminIndexService.getLogo();
        return ApiResult.ok(result);
    }

    /**
     * 消息通知
     * GET /api/admin/jnotice
     *
     * PHP Reference: Index.php::jnotice()
     *
     * Response: []
     */
    @GetMapping("/jnotice")
    @Operation(summary = "Message Notification")
    public ApiResult<List<Object>> getJNotice() {
        List<Object> result = adminIndexService.getJNotice();
        return ApiResult.ok(result);
    }

    /**
     * 获取菜单列表（前端导航菜单）
     * GET /api/admin/menus
     *
     * PHP Reference: Menus.php::menus()
     * PHP: [$menus, $unique] = $this->services->getMenusList($this->adminInfo['roles'], (int)$this->adminInfo['level']);
     *
     * 前端使用的菜单API，返回格式化的树形菜单结构和权限标识
     * Response: {menus: [树形菜单结构], unique: [权限标识数组]}
     */
    @GetMapping("/menus")
    @Operation(summary = "Get Frontend Navigation Menu")
    public ApiResult<Map<String, Object>> getMenus() {
        // 参考PHP: $this->services->getMenusList($this->adminInfo['roles'], (int)$this->adminInfo['level']);
        String roles = UserContext.getRoles();
        Integer level = UserContext.getLevel();

        SystemMenusService.MenusResult result = systemMenusService.getMenusList(roles, level);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("menus", result.getMenus());
        response.put("unique", result.getUniqueAuth());

        return ApiResult.ok(response);
    }

    /**
     * 获取菜单列表
     * GET /api/admin/menusList
     *
     * PHP Reference: Index.php::getMenusList()
     *
     * Response: [树形菜单结构]
     */
    @GetMapping("/menusList")
    @Operation(summary = "Get Menu List")
    public ApiResult<List<Map<String, Object>>> getMenusList() {
        List<Map<String, Object>> result = adminIndexService.getMenusList();
        return ApiResult.ok(result);
    }


    /**
     * 客户统计
     * GET /api/admin/chart/sum
     *
     * PHP Reference: Index.php::sum()
     *
     * Response:
     * {
     *   "all": 30,
     *   "toDayKefu": 0,
     *   "month": 0,
     *   "toDayTourist": 1
     * }
     */
    @GetMapping("/chart/sum")
    @Operation(summary = "Customer Statistics")
    public ApiResult<ChartSumDTO> getKefuSum() {
        ChartSumDTO result = adminIndexService.getKefuSum(currentAppid());
        return ApiResult.ok(result);
    }

    /**
     * 客户首页统计
     * GET /api/admin/chart
     *
     * PHP Reference: Index.php::index()
     *
     * Query Parameters:
     * - type: 统计类型(默认0)
     * - year: 年份(默认当前年)
     * - month: 月份(默认当前月)
     *
     * Response:
     * {
     *   "list": [...],    // 客户统计
     *   "tourist": [...]  // 游客统计
     * }
     */
    @GetMapping("/chart")
    @Operation(summary = "Customer Home Statistics")
    public ApiResult<ChartStatisticsDTO> getKefuStatistics(
            @RequestParam(defaultValue = "0") Integer type,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        // 默认当前年月
        if (year == null) {
            year = java.time.Year.now().getValue();
        }
        if (month == null) {
            month = java.time.MonthDay.now().getMonthValue();
        }

        ChartStatisticsDTO result = adminIndexService.getKefuStatistics(currentAppid(), type, year, month);
        return ApiResult.ok(result);
    }
}
