package io.renren.modules.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * AI聊天消息
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "AI聊天消息")
public class AiChatMessageDTO {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "消息类型")
    private String type;

    @Schema(description = "回答内容")
    private String content;

    @Schema(description = "模型ID")
    private Long modelId;

    @Schema(description = "模型标识")
    private String model;

    @Schema(description = "对话ID")
    private Long conversationId;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

}