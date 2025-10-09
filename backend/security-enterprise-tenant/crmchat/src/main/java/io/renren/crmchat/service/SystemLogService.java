package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.crmchat.dao.SystemLogMapper;
import io.renren.crmchat.entity.SystemLogEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统日志服务
 * PHP Reference: app/services/system/log/SystemLogServices.php
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class SystemLogService {

    private final SystemLogMapper systemLogMapper;

    /**
     * 获取系统日志列表
     * PHP Reference: SystemLogServices::getLogList()
     *
     * @param pages 页面关键字搜索
     * @param path 路径搜索
     * @param ip IP地址搜索
     * @param adminId 管理员ID筛选
     * @param startTime 开始时间 (Unix timestamp)
     * @param endTime 结束时间 (Unix timestamp)
     * @param page 页码
     * @param limit 每页数量
     * @return {list: [], count: N}
     */
    public Map<String, Object> getLogList(String pages, String path, String ip,
                                          Integer adminId, Long startTime, Long endTime,
                                          Integer page, Integer limit) {
        QueryWrapper<SystemLogEntity> query = new QueryWrapper<>();

        // PHP: $where['admin_id']
        if (adminId != null && adminId > 0) {
            query.eq("admin_id", adminId);
        }

        // PHP: $where['pages'] - 搜索page字段
        if (pages != null && !pages.isEmpty()) {
            query.like("page", pages);
        }

        // PHP: $where['path'] - 搜索path字段
        if (path != null && !path.isEmpty()) {
            query.like("path", path);
        }

        // PHP: $where['ip'] - 搜索ip字段
        if (ip != null && !ip.isEmpty()) {
            query.like("ip", ip);
        }

        // PHP: $where['time'] - 时间范围筛选
        if (startTime != null && endTime != null) {
            query.between("add_time", startTime, endTime);
        }

        // 按时间倒序排列,最新的在前
        query.orderByDesc("add_time");

        // 分页查询
        Page<SystemLogEntity> pageObj = new Page<>(page, limit);
        IPage<SystemLogEntity> pageResult = systemLogMapper.selectPage(pageObj, query);

        List<SystemLogEntity> list = pageResult.getRecords();
        long totalCount = pageResult.getTotal();

        // PHP返回格式: compact('list', 'count')
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("count", totalCount);

        return result;
    }
}
