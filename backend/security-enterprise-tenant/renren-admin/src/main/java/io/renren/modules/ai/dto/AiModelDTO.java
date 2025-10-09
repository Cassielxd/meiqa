package io.renren.modules.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * AI模型
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "AI模型")
public class AiModelDTO {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "所属平台")
    private String platform;

    @Schema(description = "模型名称")
    private String name;

    @Schema(description = "模型标识")
    private String model;

    @Schema(description = "API地址")
    private String apiUrl;

    @Schema(description = "API秘钥")
    private String apiKey;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

}