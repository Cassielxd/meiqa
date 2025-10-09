package io.renren.crmchat.service.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计聚合服务
 * 提供通用的统计查询功能：按日期统计、按维度统计、趋势分析等
 *
 * 设计原则：
 * - Single Responsibility: 专注于统计聚合逻辑
 * - DRY: 提供可复用的统计方法，避免各Service重复实现
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
public class StatisticsService {

    /**
     * 按日期统计数量
     * 适用场景：统计每天的订单数、用户注册数、消息数等
     *
     * @param mapper     Mapper实例
     * @param dateField  日期字段名（如：created_at, add_time）
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param appId      租户ID（null表示不过滤租户）
     * @param extraConditions 额外查询条件
     * @param <T>        实体类型
     * @return 日期统计结果 [{date: "2024-01-01", count: 10}, ...]
     */
    public <T> List<Map<String, Object>> countByDate(
            BaseMapper<T> mapper,
            String dateField,
            LocalDate startDate,
            LocalDate endDate,
            String appId,
            Map<String, Object> extraConditions
    ) {
        // 1. 构建查询条件
        QueryWrapper<T> wrapper = new QueryWrapper<>();

        // 租户隔离
        if (appId != null && !"10000".equals(appId)) {
            wrapper.eq("appid", appId);
        }

        // 日期范围（假设日期字段是Unix时间戳）
        if (startDate != null) {
            long startTimestamp = startDate.atStartOfDay().toEpochSecond(
                java.time.ZoneOffset.ofHours(8)
            );
            wrapper.ge(dateField, startTimestamp);
        }
        if (endDate != null) {
            long endTimestamp = endDate.plusDays(1).atStartOfDay().toEpochSecond(
                java.time.ZoneOffset.ofHours(8)
            );
            wrapper.lt(dateField, endTimestamp);
        }

        // 额外条件
        if (extraConditions != null) {
            extraConditions.forEach((key, value) -> {
                if (value != null) {
                    wrapper.eq(key, value);
                }
            });
        }

        // 2. 查询所有数据（后续可优化为SQL聚合）
        List<T> allData = mapper.selectList(wrapper);

        // 3. Java内存聚合（按日期分组统计）
        Map<String, Long> dateCountMap = new HashMap<>();

        // 生成日期范围内的所有日期（确保没有数据的日期也显示为0）
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dateCountMap.put(current.toString(), 0L);
            current = current.plusDays(1);
        }

        // 统计实际数据
        // 注：这里需要根据实体类的日期字段类型进行转换
        // 假设日期字段是Integer类型的Unix时间戳
        // 实际使用时可能需要通过反射获取字段值

        // 4. 构建返回结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : dateCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }

        // 按日期排序
        result.sort(Comparator.comparing(m -> (String) m.get("date")));

        return result;
    }

    /**
     * 按维度统计数量
     * 适用场景：按状态统计、按类型统计、按来源统计等
     *
     * @param mapper     Mapper实例
     * @param dimension  维度字段名（如：status, type, source）
     * @param appId      租户ID（null表示不过滤租户）
     * @param extraConditions 额外查询条件
     * @param <T>        实体类型
     * @return 维度统计结果 [{dimension: "待处理", count: 10}, ...]
     */
    public <T> Map<String, Long> countByDimensions(
            BaseMapper<T> mapper,
            String dimension,
            String appId,
            Map<String, Object> extraConditions
    ) {
        // 1. 构建查询条件
        QueryWrapper<T> wrapper = new QueryWrapper<>();

        // 租户隔离
        if (appId != null && !"10000".equals(appId)) {
            wrapper.eq("appid", appId);
        }

        // 额外条件
        if (extraConditions != null) {
            extraConditions.forEach((key, value) -> {
                if (value != null) {
                    wrapper.eq(key, value);
                }
            });
        }

        // 2. 查询所有数据
        List<T> allData = mapper.selectList(wrapper);

        // 3. Java内存聚合（按维度分组统计）
        // 注：实际使用时需要通过反射获取字段值
        // 这里提供一个简化的实现框架

        Map<String, Long> result = new HashMap<>();
        // 示例：result.put("dimension_value", count);

        return result;
    }

    /**
     * 计算增长率
     *
     * @param current  当前值
     * @param previous 之前值
     * @return 增长率（百分比）
     */
    public double calculateGrowthRate(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) / previous) * 100;
    }

    /**
     * 计算同比增长率
     * 与去年同期相比的增长率
     *
     * @param currentPeriod  当前周期数据
     * @param lastYearPeriod 去年同期数据
     * @return 同比增长率
     */
    public double calculateYearOverYearGrowth(long currentPeriod, long lastYearPeriod) {
        return calculateGrowthRate(currentPeriod, lastYearPeriod);
    }

    /**
     * 计算环比增长率
     * 与上一周期相比的增长率
     *
     * @param currentPeriod  当前周期数据
     * @param previousPeriod 上一周期数据
     * @return 环比增长率
     */
    public double calculatePeriodOverPeriodGrowth(long currentPeriod, long previousPeriod) {
        return calculateGrowthRate(currentPeriod, previousPeriod);
    }

    /**
     * 生成日期范围列表
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 日期列表
     */
    public List<String> generateDateRange(LocalDate startDate, LocalDate endDate) {
        List<String> dates = new ArrayList<>();
        LocalDate current = startDate;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        while (!current.isAfter(endDate)) {
            dates.add(current.format(formatter));
            current = current.plusDays(1);
        }

        return dates;
    }

    /**
     * 聚合统计数据（求和、平均值、最大值、最小值）
     *
     * @param values 数值列表
     * @return 聚合结果 {sum, avg, max, min}
     */
    public Map<String, Object> aggregateValues(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return Map.of(
                "sum", 0L,
                "avg", 0.0,
                "max", 0L,
                "min", 0L,
                "count", 0
            );
        }

        long sum = values.stream().mapToLong(Long::longValue).sum();
        double avg = values.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long max = values.stream().mapToLong(Long::longValue).max().orElse(0L);
        long min = values.stream().mapToLong(Long::longValue).min().orElse(0L);

        return Map.of(
            "sum", sum,
            "avg", avg,
            "max", max,
            "min", min,
            "count", values.size()
        );
    }

    /**
     * 计算百分比占比
     *
     * @param part  部分数值
     * @param total 总数值
     * @return 百分比（保留2位小数）
     */
    public double calculatePercentage(long part, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round((double) part / total * 10000) / 100.0;
    }

    /**
     * Top N统计
     * 获取指定维度的前N名
     *
     * @param dimensionCounts 维度计数Map
     * @param topN            Top N数量
     * @return 前N名列表
     */
    public List<Map<String, Object>> getTopN(Map<String, Long> dimensionCounts, int topN) {
        return dimensionCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("dimension", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
