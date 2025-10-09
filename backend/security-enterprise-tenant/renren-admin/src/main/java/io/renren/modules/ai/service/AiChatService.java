package io.renren.modules.ai.service;

import io.renren.modules.ai.dto.AiChatMessageDTO;
import io.renren.modules.ai.dto.AiChatRequestDTO;
import reactor.core.publisher.Flux;

public interface AiChatService {

    Flux<AiChatMessageDTO> sendChatMessage(AiChatRequestDTO requestDTO);

}
