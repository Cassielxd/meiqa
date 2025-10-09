package io.renren.modules.ai.service;

import io.renren.common.service.BaseService;
import io.renren.modules.ai.dto.AiChatConversationDTO;
import io.renren.modules.ai.entity.AiChatConversationEntity;

import java.util.List;

/**
 * AI聊天对话
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface AiChatConversationService extends BaseService<AiChatConversationEntity> {

    List<AiChatConversationDTO> getList();

    AiChatConversationDTO save(AiChatConversationDTO dto);

    void update(AiChatConversationDTO dto);

    void delete(List<Long> idList);

}