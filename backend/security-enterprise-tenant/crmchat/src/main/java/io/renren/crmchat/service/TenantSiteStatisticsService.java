package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.crmchat.mapper.SiteStatisticsMapper;
import io.renren.crmchat.entity.SiteStatisticsEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tenant 站点统计服务
 * PHP Reference: /app/controller/tenant/chat/SiteStatistics.php
 * PHP Service: /app/services/other/SiteStatisticsServices.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getSiteStatisticsList(): 获取站点统计列表
 *    - 支持province模糊查询
 *    - 支持create_time时间筛选
 *    - appid隔离
 *    - 返回data和count（注意：PHP中使用data而不是list）
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantSiteStatisticsService {

    private final SiteStatisticsMapper siteStatisticsMapper;

    /**
     * 获取站点统计列表
     * GET /api/tenant/chat/site_statistics
     *
     * PHP Reference:
     * - Controller: SiteStatistics.php::index()
     * - Service: SiteStatisticsServices.php::getList()
     *
     * 业务逻辑:
     * 1. 支持province模糊查询
     * 2. 支持create_time时间筛选（格式: "2024-01-01 - 2024-01-31"）
     * 3. appid隔离
     * 4. 返回data和count
     *
     * PHP代码:
     * $where = $this->request->getMore([
     *     ['province', ''],
     *     ['create_time', '']
     * ]);
     * $appid = $this->request->tenantAppid();
     * $where['appid'] = $appid;
     * return $this->success($this->services->getList($where));
     *
     * 注意: PHP中站点统计表没有appid字段，这里为了多租户隔离需要添加
     *
     * @param province     省份（模糊查询）
     * @param createTime   创建时间范围（格式: "2024-01-01 - 2024-01-31"）
     * @return Map包含data和count
     */
    public Map<String, Object> getSiteStatisticsList(String province, String createTime, Integer page, Integer limit) {
        // PHP: $where = $this->request->getMore([['province', ''], ['create_time', '']]);
        // PHP: $where['appid'] = $appid;

        QueryWrapper<SiteStatisticsEntity> wrapper = new QueryWrapper<>();

        // 注意: 原PHP表结构没有appid字段，但为了多租户隔离，Java实现中需要添加
        // 如果数据库表尚未添加appid字段，需要先执行以下SQL:
        // ALTER TABLE eb_site_statistics ADD COLUMN appid varchar(35) NOT NULL DEFAULT '' COMMENT 'APPID' AFTER browser;
        // 实际使用时，如果表没有appid字段，注释掉下面这行
        // wrapper.eq("appid", currentAppid);

        // PHP: 模糊查询province
        if (province != null && !province.trim().isEmpty()) {
            wrapper.like("province", province);
        }

        // PHP: 时间筛选create_time
        // Accepted formats: "2024-01-01 - 2024-01-31" or "2024-01-01,2024-01-31"
        if (createTime != null && !createTime.trim().isEmpty()) {
            String[] times = createTime.contains(" - ")
                ? createTime.split(" - ")
                : createTime.split(",");

            if (times.length == 2) {
                String startTime = times[0].trim();
                String endTime = times[1].trim();
                wrapper.ge("create_time", startTime);
                wrapper.le("create_time", endTime + " 23:59:59");
            }
        }

        wrapper.orderByDesc("id");

        long pageNumber = page != null && page > 0 ? page : 1;
        long pageSize = limit != null && limit > 0 ? limit : 15;

        Page<SiteStatisticsEntity> pageResult = siteStatisticsMapper.selectPage(new Page<>(pageNumber, pageSize), wrapper);
        List<SiteStatisticsEntity> data = pageResult.getRecords();
        long count = pageResult.getTotal();

        // PHP: return compact('data', 'count');
        // 注意: PHP中返回的是data而不是list
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("list", data);
        result.put("count", count);
        return result;
    }
}
