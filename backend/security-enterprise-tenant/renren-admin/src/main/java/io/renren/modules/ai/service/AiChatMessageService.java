package io.renren.modules.ai.service;

import io.renren.common.service.BaseService;
import io.renren.modules.ai.dto.AiChatMessageDTO;
import io.renren.modules.ai.entity.AiChatMessageEntity;

import java.util.List;

/**
 * AI聊天消息
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface AiChatMessageService extends BaseService<AiChatMessageEntity> {

    List<AiChatMessageDTO> getList(Long conversationId);

    void clearList(Long conversationId);

}