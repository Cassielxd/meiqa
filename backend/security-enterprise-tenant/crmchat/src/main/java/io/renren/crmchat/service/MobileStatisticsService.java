package io.renren.crmchat.service;

import io.renren.crmchat.exception.CrmChatException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Mobile Statistics Service - 移动端统计服务
 * PHP Reference: /app/controller/mobile/Statistics.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. save(): 保存站点访问统计（IP、路径、来源、浏览器）
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class MobileStatisticsService {

    /**
     * 保存站点访问统计
     * POST /api/mobile/statistics
     *
     * PHP Reference: Statistics.php::save()
     *
     * 业务逻辑:
     * 1. 验证必填字段：ip, path
     * 2. 保存访问统计到SiteStatistics表
     * 3. 记录访问来源、浏览器信息
     *
     * @param data 统计数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveSiteStatistics(Map<String, Object> data) {
        // 1. 验证必填字段
        if (!data.containsKey("ip") || data.get("ip") == null || data.get("ip").toString().trim().isEmpty()) {
            throw new CrmChatException("Missing required parameter");
        }
        if (!data.containsKey("path") || data.get("path") == null || data.get("path").toString().trim().isEmpty()) {
            throw new CrmChatException("Missing required parameter");
        }

        String ip = data.get("ip").toString();
        String path = data.get("path").toString();
        String source = data.containsKey("source") && data.get("source") != null
                ? data.get("source").toString() : "";
        String browser = data.containsKey("browser") && data.get("browser") != null
                ? data.get("browser").toString() : "";

        // 2. TODO: 保存到SiteStatistics表
        // PHP调用: $services->saveSite($data);
        // 实际应创建SiteStatisticsEntity和SiteStatisticsMapper
        // 记录访问IP、路径、来源、浏览器、时间等信息

        log.info("保存站点统计: ip={}, path={}, source={}, browser={}", ip, path, source, browser);
    }
}
