package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.SystemLogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.Map;

/**
 * 系统日志管理控制器
 * PHP Reference: app/controller/admin/system/Log.php
 *
 * 功能说明:
 * - 查看系统操作日志
 * - 支持按管理员、时间、IP、路径等条件筛选
 *
 * PHP路由:
 * - GET /api/admin/system/log/lst
 *
 * @author CRMChat Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/system/log")
@AllArgsConstructor
public class AdminSystemLogController {

    private final SystemLogService systemLogService;

    /**
     * 获取系统日志列表
     * PHP Reference: Log::index()
     *
     * @param pages 页面关键字 (可选)
     * @param path 路径关键字 (可选)
     * @param ip IP地址 (可选)
     * @param adminId 管理员ID (可选)
     * @param data 时间范围,格式: "startTime,endTime" (可选)
     * @param page 页码 (默认1)
     * @param limit 每页数量 (默认20)
     * @return {list: [], count: N}
     */
    @GetMapping("/lst")
    public ApiResult<Map<String, Object>> getLogList(
            @RequestParam(required = false) String pages,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) Integer adminId,
            @RequestParam(required = false) String data,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {

        // PHP: $where['data'] - 时间范围参数
        // 将前端传来的 Unix 时间戳（秒）转换为 Timestamp 对象
        Timestamp startTime = null;
        Timestamp endTime = null;
        if (data != null && !data.isEmpty()) {
            String[] times = data.split(",");
            if (times.length == 2) {
                try {
                    long startUnix = Long.parseLong(times[0].trim());
                    long endUnix = Long.parseLong(times[1].trim());
                    // Unix 时间戳是秒，需要转换为毫秒
                    startTime = new Timestamp(startUnix * 1000);
                    endTime = new Timestamp(endUnix * 1000);
                } catch (NumberFormatException e) {
                    log.warn("Invalid time format: {}", data);
                }
            }
        }

        // PHP: $this->services->getLogList($where, (int)$this->adminInfo['level'])
        // 注: 这里暂不处理level权限,因为PHP中的level逻辑复杂
        Map<String, Object> result = systemLogService.getLogList(
                pages, path, ip, adminId, startTime, endTime, page, limit
        );

        return ApiResult.ok(result);
    }
}
