package io.renren.modules.ai.dto;

import lombok.Data;

@Data
public class AiChatRequestDTO {
    private Long conversationId;
    private Long modelId;
    private String content;
}
