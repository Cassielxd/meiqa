package io.renren.crmchat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 统计汇总数据 DTO
 * PHP Reference: ChatUserServices::getKefuSum()
 *
 * @author CRMChat Team
 */
@Data
public class ChartSumDTO {

    /**
     * 全部客户数量
     */
    @JsonProperty("all")
    private Long all;

    /**
     * 今日新增客户数量（非游客）
     */
    @JsonProperty("toDayKefu")
    private Long toDayKefu;

    /**
     * 本月新增客户数量
     */
    @JsonProperty("month")
    private Long month;

    /**
     * 今日新增游客数量
     */
    @JsonProperty("toDayTourist")
    private Long toDayTourist;
}
