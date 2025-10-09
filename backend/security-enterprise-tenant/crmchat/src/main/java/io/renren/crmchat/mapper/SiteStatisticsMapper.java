package io.renren.crmchat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.SiteStatisticsEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 站点统计表 Mapper
 * PHP Reference: app/dao/other/SiteStatisticsDao.php
 *
 * @author CRMChat Team
 */
@Mapper
public interface SiteStatisticsMapper extends BaseMapper<SiteStatisticsEntity> {
}
