package io.renren.crmchat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 图表数据点 DTO
 * PHP Reference: ChatUserDao::kefuStatistics() return item
 *
 * @author CRMChat Team
 */
@Data
public class ChartDataDTO {

    /**
     * 时间点（日期或月份）
     * 格式：YYYY-MM-DD（type=0）或 YYYY-MM（type=1）
     */
    @JsonProperty("month")
    private String month;

    /**
     * 数量
     */
    @JsonProperty("number")
    private Long number;

    public ChartDataDTO() {
    }

    public ChartDataDTO(String month, Long number) {
        this.month = month;
        this.number = number;
    }
}
