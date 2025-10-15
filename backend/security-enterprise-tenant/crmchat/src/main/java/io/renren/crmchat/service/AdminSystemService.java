package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.page.PageData;
import io.renren.crmchat.dao.SystemAdminMapper;
import io.renren.crmchat.dao.SystemLogMapper;
import io.renren.crmchat.entity.SystemAdminEntity;
import io.renren.crmchat.entity.SystemLogEntity;
import io.renren.crmchat.service.common.PaginationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin 系统管理服务
 * PHP Reference: /app/services/system/log/SystemLogServices.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getLogList(): 获取系统日志列表，支持多条件搜索和权限过滤
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class AdminSystemService {

    private final SystemLogMapper systemLogMapper;
    private final SystemAdminMapper systemAdminMapper;
    private final PaginationService paginationService;

    /**
     * 获取系统日志列表
     * PHP Reference: SystemLogServices::getLogList()
     *
     * 业务逻辑:
     * 1. 分页查询
     * 2. 如果未指定admin_id，根据当前管理员level获取有权限查看的admin_id列表
     * 3. 支持path、ip、page等多条件搜索
     * 4. 按add_time倒序排列
     *
     * @param params 查询参数 (page, limit, admin_id, path, ip, pages)
     * @param currentLevel 当前管理员级别 (用于权限控制)
     * @return PageData包含list和count
     */
    public PageData<Map<String, Object>> getLogList(Map<String, Object> params, Integer currentLevel) {
        // 1. 构建查询条件
        QueryWrapper<SystemLogEntity> wrapper = new QueryWrapper<>();

        // 2. PHP逻辑: 如果未指定admin_id，根据level获取admin_id列表
        Object adminIdObj = params.get("admin_id");
        List<Integer> adminIds = new ArrayList<>();

        if (adminIdObj == null || adminIdObj.toString().trim().isEmpty()) {
            // 获取当前管理员有权限查看的admin_id列表
            // PHP: $service->getAdminIds($level)
            adminIds = getAdminIdsByLevel(currentLevel);
            if (!adminIds.isEmpty()) {
                wrapper.in("admin_id", adminIds);
            }
        } else {
            // 如果指定了admin_id，直接使用
            Integer adminId = Integer.parseInt(adminIdObj.toString());
            wrapper.eq("admin_id", adminId);
        }

        // 3. path 搜索 (PHP: searchPathAttr - LIKE)
        if (params.containsKey("path") && params.get("path") != null) {
            String path = params.get("path").toString().trim();
            if (!path.isEmpty()) {
                wrapper.like("path", path);
            }
        }

        // 4. ip 搜索 (PHP: searchIpAttr - LIKE)
        if (params.containsKey("ip") && params.get("ip") != null) {
            String ip = params.get("ip").toString().trim();
            if (!ip.isEmpty()) {
                wrapper.like("ip", ip);
            }
        }

        // 5. pages 搜索 (PHP: searchPagesAttr - LIKE on 'page' field)
        // Note: the PHP parameter is "pages", but the query checks the "page" column
        if (params.containsKey("pages") && params.get("pages") != null) {
            String pages = params.get("pages").toString().trim();
            if (!pages.isEmpty()) {
                wrapper.like("page", pages);
            }
        }

        // 6. 排序: add_time DESC (PHP: order('add_time DESC'))
        wrapper.orderByDesc("add_time");

        // 7. 分页查询
        PaginationService.PaginationParams paginationParams = paginationService.extractParams(params);

        // 查询总数
        Long count = systemLogMapper.selectCount(wrapper);

        // 查询列表数据
        wrapper.last("LIMIT " + paginationParams.getOffset() + "," + paginationParams.getLimit());
        List<SystemLogEntity> logList = systemLogMapper.selectList(wrapper);

        // 8. 转换为Map格式 (与PHP返回格式一致)
        List<Map<String, Object>> list = logList.stream()
                .map(this::logEntityToMap)
                .collect(Collectors.toList());

        // 9. 返回格式: { list: [...], count: 100 }
        return new PageData<>(list, count);
    }

    /**
     * 根据管理员级别获取有权限查看的admin_id列表
     * PHP Reference: SystemAdminServices::getAdminIds($level)
     *
     * 业务逻辑:
     * - 如果level=0 (超级管理员): 返回所有admin_id
     * - 如果level>0 (普通管理员): 返回level >= 当前level的admin_id
     *
     * @param currentLevel 当前管理员级别
     * @return admin_id列表
     */
    private List<Integer> getAdminIdsByLevel(Integer currentLevel) {
        QueryWrapper<SystemAdminEntity> wrapper = new QueryWrapper<>();

        if (currentLevel != null && currentLevel > 0) {
            // 普通管理员只能查看同级或下级管理员的日志
            wrapper.ge("level", currentLevel);
        }
        // 超级管理员(level=0)可以查看所有日志，不加条件

        List<SystemAdminEntity> admins = systemAdminMapper.selectList(wrapper);
        return admins.stream()
                .map(SystemAdminEntity::getId)
                .collect(Collectors.toList());
    }

    /**
     * 将SystemLogEntity转换为Map
     * 匹配PHP返回格式
     */
    private Map<String, Object> logEntityToMap(SystemLogEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("admin_id", entity.getAdminId());
        map.put("admin_name", entity.getAdminName());
        map.put("method", entity.getMethod());
        map.put("path", entity.getPath());
        map.put("page", entity.getPage());
        map.put("ip", entity.getIp());
        map.put("type", entity.getType());

        // PHP返回的add_time格式化为日期字符串 "2025-01-01 10:00:00"
        // Timestamp 可以直接传给 SimpleDateFormat.format()
        if (entity.getAddTime() != null) {
            map.put("add_time", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(entity.getAddTime()));
        }

        return map;
    }

    /**
     * 获取管理员搜索条件列表
     * PHP Reference: SystemAdminDao::getOrdAdmin()
     *
     * 业务逻辑:
     * 1. 根据当前管理员的level，获取有权限查看的管理员列表
     * 2. 只返回id和real_name字段
     * 3. 用于日志搜索的管理员下拉列表
     *
     * PHP逻辑:
     * - where('level', '>=', $level)
     * - field('id,real_name')
     * - select()->toArray()
     *
     * @param currentLevel 当前管理员级别
     * @return 管理员列表 (只包含id和real_name)
     */
    public Map<String, Object> getSearchAdminList(Integer currentLevel) {
        QueryWrapper<SystemAdminEntity> wrapper = new QueryWrapper<>();

        // PHP: where('level', '>=', $level)
        if (currentLevel != null && currentLevel > 0) {
            wrapper.ge("level", currentLevel);
        }
        // 超级管理员(level=0)可以查看所有管理员

        // PHP: field('id,real_name')
        wrapper.select("id", "real_name");

        // PHP: select()->toArray()
        List<SystemAdminEntity> admins = systemAdminMapper.selectList(wrapper);

        // 转换为Map格式
        List<Map<String, Object>> info = admins.stream()
                .map(admin -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", admin.getId());
                    map.put("real_name", admin.getRealName());
                    return map;
                })
                .collect(Collectors.toList());

        // PHP: compact('info') 返回 { info: [...] }
        Map<String, Object> result = new HashMap<>();
        result.put("info", info);
        return result;
    }
}
