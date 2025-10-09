package io.renren.crmchat.service.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 统一查询构建服务
 * 提供动态查询条件构建、租户隔离、搜索条件处理的公共逻辑
 *
 * 设计原则:
 * - Single Responsibility: 专注于查询构建
 * - DRY: 消除各 Service 中重复的查询构建代码
 * - Tenant Isolation: 内置多租户隔离支持
 *
 * PHP 参考:
 * - crmeb/basic/BaseDao.php::search()
 * - app/dao/tenant/TenantDao.php::search()
 *
 * @author CRMChat Team
 */
@Service
public class QueryBuilderService {

    /**
     * 构建带租户隔离的基础查询
     *
     * 核心功能：
     * - 自动添加 appid 条件（多租户隔离）
     * - 管理员（appid="10000"）可以查询所有租户数据
     *
     * @param wrapper 查询包装器
     * @param appId   租户的 appid
     * @param <T>     实体类型
     * @return 添加租户隔离后的查询包装器
     */
    public <T> QueryWrapper<T> buildTenantQuery(QueryWrapper<T> wrapper, String appId) {
        if (wrapper == null) {
            wrapper = new QueryWrapper<>();
        }

        // 管理员（appid="10000"）可以查询所有数据，不需要添加 appid 条件
        if (!"10000".equals(appId)) {
            wrapper.eq("appid", appId);
        }

        return wrapper;
    }

    /**
     * 添加关键词搜索条件（模糊查询）
     *
     * PHP 对比:
     * - PHP: $query->where('tenant_name', 'like', "%{$keyword}%")
     * - Java: wrapper.like("tenant_name", keyword)
     *
     * @param wrapper 查询包装器
     * @param keyword 搜索关键词
     * @param fields  需要搜索的字段列表
     * @param <T>     实体类型
     * @return 添加搜索条件后的查询包装器
     */
    public <T> QueryWrapper<T> addSearchCondition(QueryWrapper<T> wrapper, String keyword, String... fields) {
        if (wrapper == null) {
            wrapper = new QueryWrapper<>();
        }

        if (keyword == null || keyword.trim().isEmpty() || fields == null || fields.length == 0) {
            return wrapper;
        }

        // 构建 OR 条件：field1 LIKE '%keyword%' OR field2 LIKE '%keyword%' OR ...
        wrapper.and(w -> {
            for (int i = 0; i < fields.length; i++) {
                if (i == 0) {
                    w.like(fields[i], keyword.trim());
                } else {
                    w.or().like(fields[i], keyword.trim());
                }
            }
        });

        return wrapper;
    }

    /**
     * 添加日期范围查询条件
     *
     * PHP 对比:
     * - PHP: $query->where('created_at', '>=', $startTime)->where('created_at', '<=', $endTime)
     * - Java: wrapper.ge("created_at", startTime).le("created_at", endTime)
     *
     * 支持格式：
     * - "2024-01-01 - 2024-01-31" （PHP 常用格式）
     * - "2024-01-01" （单日查询）
     *
     * @param wrapper   查询包装器
     * @param dateRange 日期范围字符串
     * @param fieldName 日期字段名
     * @param <T>       实体类型
     * @return 添加日期条件后的查询包装器
     */
    public <T> QueryWrapper<T> addDateRange(QueryWrapper<T> wrapper, String dateRange, String fieldName) {
        if (wrapper == null) {
            wrapper = new QueryWrapper<>();
        }

        if (dateRange == null || dateRange.trim().isEmpty()) {
            return wrapper;
        }

        try {
            // PHP 格式："2024-01-01 - 2024-01-31"
            if (dateRange.contains(" - ")) {
                String[] dates = dateRange.split(" - ");
                if (dates.length == 2) {
                    LocalDateTime startTime = parseDate(dates[0].trim(), true);
                    LocalDateTime endTime = parseDate(dates[1].trim(), false);

                    wrapper.ge(fieldName, startTime)
                            .le(fieldName, endTime);
                }
            } else {
                // 单日查询：查询当天 00:00:00 到 23:59:59
                LocalDateTime startTime = parseDate(dateRange.trim(), true);
                LocalDateTime endTime = parseDate(dateRange.trim(), false);

                wrapper.ge(fieldName, startTime)
                        .le(fieldName, endTime);
            }
        } catch (Exception e) {
            // 日期解析失败，忽略该条件
        }

        return wrapper;
    }

    /**
     * 添加状态条件
     *
     * @param wrapper 查询包装器
     * @param status  状态值（可以是 String, Integer, 或 null）
     * @param <T>     实体类型
     * @return 添加状态条件后的查询包装器
     */
    public <T> QueryWrapper<T> addStatusCondition(QueryWrapper<T> wrapper, Object status) {
        if (wrapper == null) {
            wrapper = new QueryWrapper<>();
        }

        if (status == null) {
            return wrapper;
        }

        // 处理空字符串（PHP 常见情况）
        if (status instanceof String && ((String) status).trim().isEmpty()) {
            return wrapper;
        }

        try {
            Integer statusValue = parseIntValue(status);
            if (statusValue != null) {
                wrapper.eq("status", statusValue);
            }
        } catch (Exception e) {
            // 状态值解析失败，忽略该条件
        }

        return wrapper;
    }

    /**
     * 从 Map 参数中添加查询条件（通用方法）
     *
     * 支持的参数：
     * - keyword: 关键词搜索
     * - status: 状态筛选
     * - date: 日期范围
     * - appid: 租户隔离（自动处理）
     *
     * @param wrapper       查询包装器
     * @param params        请求参数 Map
     * @param appId         租户 appid
     * @param searchFields  关键词搜索的字段列表
     * @param dateFieldName 日期字段名（默认 "created_at"）
     * @param <T>           实体类型
     * @return 添加所有条件后的查询包装器
     */
    public <T> QueryWrapper<T> buildFromParams(
            QueryWrapper<T> wrapper,
            Map<String, Object> params,
            String appId,
            String[] searchFields,
            String dateFieldName) {

        if (wrapper == null) {
            wrapper = new QueryWrapper<>();
        }

        if (params == null) {
            return wrapper;
        }

        // 1. 租户隔离
        wrapper = buildTenantQuery(wrapper, appId);

        // 2. 关键词搜索
        Object keyword = params.get("keyword");
        if (keyword != null && searchFields != null && searchFields.length > 0) {
            wrapper = addSearchCondition(wrapper, keyword.toString(), searchFields);
        }

        // 3. 状态筛选
        Object status = params.get("status");
        if (status != null) {
            wrapper = addStatusCondition(wrapper, status);
        }

        // 4. 日期范围
        Object date = params.get("date");
        if (date != null) {
            String field = dateFieldName != null ? dateFieldName : "created_at";
            wrapper = addDateRange(wrapper, date.toString(), field);
        }

        return wrapper;
    }

    /**
     * 添加排序条件
     *
     * @param wrapper   查询包装器
     * @param orderBy   排序字段
     * @param isAsc     是否升序
     * @param <T>       实体类型
     * @return 添加排序后的查询包装器
     */
    public <T> QueryWrapper<T> addOrderBy(QueryWrapper<T> wrapper, String orderBy, boolean isAsc) {
        if (wrapper == null) {
            wrapper = new QueryWrapper<>();
        }

        if (orderBy == null || orderBy.trim().isEmpty()) {
            return wrapper;
        }

        wrapper.orderBy(true, isAsc, orderBy);
        return wrapper;
    }

    /**
     * 添加默认排序（ID 倒序）
     *
     * PHP 对比:
     * - PHP: $query->order('id desc')
     * - Java: wrapper.orderByDesc("id")
     *
     * @param wrapper 查询包装器
     * @param <T>     实体类型
     * @return 添加排序后的查询包装器
     */
    public <T> QueryWrapper<T> addDefaultOrder(QueryWrapper<T> wrapper) {
        if (wrapper == null) {
            wrapper = new QueryWrapper<>();
        }

        wrapper.orderByDesc("id");
        return wrapper;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 解析日期字符串
     *
     * @param dateStr 日期字符串（格式：yyyy-MM-dd）
     * @param isStart true=开始时间（00:00:00），false=结束时间（23:59:59）
     * @return LocalDateTime 对象
     */
    private LocalDateTime parseDate(String dateStr, boolean isStart) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateStr, formatter);

        if (isStart) {
            // 开始时间：00:00:00
            return date.atStartOfDay();
        } else {
            // 结束时间：23:59:59
            return date.atTime(23, 59, 59);
        }
    }

    /**
     * 解析整数值（支持多种类型）
     *
     * @param value 参数值
     * @return 整数值，解析失败返回 null
     */
    private Integer parseIntValue(Object value) {
        if (value == null) {
            return null;
        }

        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            if (value instanceof String) {
                String str = ((String) value).trim();
                if (!str.isEmpty()) {
                    return Integer.parseInt(str);
                }
            }
        } catch (NumberFormatException e) {
            // 解析失败
        }

        return null;
    }
}
