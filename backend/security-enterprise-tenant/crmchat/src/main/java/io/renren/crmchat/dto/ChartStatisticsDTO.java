package io.renren.crmchat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 图表统计数据 DTO
 * PHP Reference: ChatUserServices::getKefuStatistics()
 *
 * @author CRMChat Team
 */
@Data
public class ChartStatisticsDTO {

    /**
     * 客户统计数据（is_tourist=0）
     */
    @JsonProperty("list")
    private List<ChartDataDTO> list;

    /**
     * 游客统计数据（is_tourist=1）
     */
    @JsonProperty("tourist")
    private List<ChartDataDTO> tourist;
}
