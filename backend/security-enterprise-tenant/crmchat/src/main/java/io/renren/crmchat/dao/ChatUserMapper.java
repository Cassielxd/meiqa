package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.dto.ChartDataDTO;
import io.renren.crmchat.entity.ChatUserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户/用户 Mapper
 *
 * @author CRMChat Team
 */
@Mapper
public interface ChatUserMapper extends BaseMapper<ChatUserEntity> {

    /**
     * 统计时间段内的客户/游客数据，按时间分组
     * PHP Reference: ChatUserDao::kefuStatistics()
     *
     * @param isTourist 是否游客：0-客户，1-游客
     * @param appid     租户APPID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param type      类型：0-按年统计（按天分组），1-按月统计（按月分组）
     * @return 统计数据列表
     */
    List<ChartDataDTO> kefuStatistics(@Param("isTourist") Integer isTourist,
                                      @Param("appid") String appid,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime,
                                      @Param("type") Integer type);
}
