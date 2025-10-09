package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatUserMapper;
import io.renren.crmchat.dto.ChartDataDTO;
import io.renren.crmchat.dto.ChartStatisticsDTO;
import io.renren.crmchat.dto.ChartSumDTO;
import io.renren.crmchat.entity.ChatUserEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

/**
 * Admin Chart Service - 统计图表服务
 * PHP Reference: ChatUserServices.php
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class AdminChartService {

    private final ChatUserMapper chatUserMapper;

    /**
     * 获取统计汇总数据
     * PHP Reference: ChatUserServices::getKefuSum()
     *
     * @param appid 租户APPID（null表示平台管理员查询所有租户数据）
     * @return 统计汇总数据
     */
    public ChartSumDTO getKefuSum(String appid) {
        // Note: appid parameter is ignored for admin endpoints
        // Tenant isolation is handled automatically by MyBatis interceptor (CrmTenantLineHandler)
        // - For super admin (appid="10000"): interceptor skips adding WHERE appid clause → queries all tenants
        // - For regular tenant users: interceptor adds WHERE appid=? → queries single tenant only

        ChartSumDTO dto = new ChartSumDTO();

        // 全部客户数量 - interceptor will add appid filter if needed
        QueryWrapper<ChatUserEntity> allWrapper = new QueryWrapper<>();
        dto.setAll(chatUserMapper.selectCount(allWrapper));

        // 今日新增客户（非游客）
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        QueryWrapper<ChatUserEntity> todayKefuWrapper = new QueryWrapper<>();
        todayKefuWrapper.eq("is_tourist", 0)
                .between("create_time", todayStart, todayEnd);
        dto.setToDayKefu(chatUserMapper.selectCount(todayKefuWrapper));

        // 本月新增客户
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);

        QueryWrapper<ChatUserEntity> monthWrapper = new QueryWrapper<>();
        monthWrapper.between("create_time", monthStart, monthEnd);
        dto.setMonth(chatUserMapper.selectCount(monthWrapper));

        // 今日新增游客
        QueryWrapper<ChatUserEntity> todayTouristWrapper = new QueryWrapper<>();
        todayTouristWrapper.eq("is_tourist", 1)
                .between("create_time", todayStart, todayEnd);
        dto.setToDayTourist(chatUserMapper.selectCount(todayTouristWrapper));

        return dto;
    }

    /**
     * 获取客服统计图表数据
     * PHP Reference: ChatUserServices::getKefuStatistics()
     *
     * @param type  类型：0-按年统计（按天分组），1-按月统计（按月分组）
     * @param year  年份
     * @param month 月份
     * @param appid 租户APPID（null表示平台管理员查询所有租户数据）
     * @return 统计图表数据
     */
    public ChartStatisticsDTO getKefuStatistics(Integer type, Integer year, Integer month, String appid) {
        // Note: appid parameter is ignored - tenant isolation handled by MyBatis interceptor

        LocalDateTime startTime;
        LocalDateTime endTime;

        if (type == 1) {
            // 按月统计：计算指定月份的开始和结束时间
            YearMonth yearMonth = YearMonth.of(year, month);
            startTime = yearMonth.atDay(1).atStartOfDay();
            endTime = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);
        } else {
            // 按年统计：计算指定年份的开始和结束时间
            startTime = LocalDate.of(year, 1, 1).atStartOfDay();
            endTime = LocalDate.of(year, 12, 31).atTime(LocalTime.MAX);
        }

        ChartStatisticsDTO dto = new ChartStatisticsDTO();

        // 查询客户数据（is_tourist=0）
        List<ChartDataDTO> customerList = chatUserMapper.kefuStatistics(0, appid, startTime, endTime, type);
        dto.setList(customerList);

        // 查询游客数据（is_tourist=1）
        List<ChartDataDTO> touristList = chatUserMapper.kefuStatistics(1, appid, startTime, endTime, type);
        dto.setTourist(touristList);

        return dto;
    }
}
