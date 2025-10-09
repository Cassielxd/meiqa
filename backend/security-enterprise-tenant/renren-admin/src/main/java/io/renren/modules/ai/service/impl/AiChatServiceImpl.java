package io.renren.modules.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.modules.ai.dao.AiChatMessageDao;
import io.renren.modules.ai.dao.AiModelDao;
import io.renren.modules.ai.dto.AiChatMessageDTO;
import io.renren.modules.ai.dto.AiChatRequestDTO;
import io.renren.modules.ai.entity.AiChatMessageEntity;
import io.renren.modules.ai.entity.AiModelEntity;
import io.renren.modules.ai.model.AiModelFactory;
import io.renren.modules.ai.service.AiChatService;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AiChatServiceImpl implements AiChatService {
    private final AiChatMessageDao aiChatMessageDao;
    private final AiModelDao aiModelDao;

    @Override
    public Flux<AiChatMessageDTO> sendChatMessage(AiChatRequestDTO requestDTO) {
        AiModelEntity modelEntity = aiModelDao.selectById(requestDTO.getModelId());
        List<Message> messages = new ArrayList<>();

        // 保存用户消息
        AiChatMessageEntity messageEntity = new AiChatMessageEntity();
        messageEntity.setType(MessageType.USER.getValue());
        messageEntity.setConversationId(requestDTO.getConversationId());
        messageEntity.setContent(requestDTO.getContent());
        messageEntity.setModelId(requestDTO.getModelId());
        messageEntity.setModel(modelEntity.getModel());
        aiChatMessageDao.insert(messageEntity);

        // 历史消息
        List<AiChatMessageEntity> historyMessageList = aiChatMessageDao.selectList(Wrappers.lambdaQuery(AiChatMessageEntity.class)
                .eq(AiChatMessageEntity::getConversationId, requestDTO.getConversationId()));
        for (AiChatMessageEntity historyMessage : historyMessageList) {
            messages.add(getMessage(historyMessage.getType(), historyMessage.getContent()));
        }

        // 新发送消息
        messages.add(new UserMessage(requestDTO.getContent()));
        Prompt prompt = new Prompt(messages);
        ChatModel chatModel = AiModelFactory.buildChatModel(modelEntity.getPlatform(), modelEntity.getModel(), modelEntity.getApiUrl(), modelEntity.getApiKey());
        Flux<ChatResponse> streamResponse = chatModel.stream(prompt);


        // AI回答的内容
        StringBuffer assistantContent = new StringBuffer();
        return streamResponse
                .map(response -> {
                    String text = response.getResult().getOutput().getText();
                    text = StrUtil.nullToDefault(text, "");
                    assistantContent.append(text);

                    AiChatMessageDTO aiChatMessageDTO = new AiChatMessageDTO();
                    aiChatMessageDTO.setType(MessageType.ASSISTANT.getValue());
                    aiChatMessageDTO.setContent(text);
                    return aiChatMessageDTO;
                }).doOnComplete(() -> {
                    // 保存AI消息
                    AiChatMessageEntity messageEntity2 = new AiChatMessageEntity();
                    messageEntity2.setType(MessageType.ASSISTANT.getValue());
                    messageEntity2.setConversationId(requestDTO.getConversationId());
                    messageEntity2.setContent(assistantContent.toString());
                    messageEntity2.setModelId(requestDTO.getModelId());
                    messageEntity2.setModel(modelEntity.getModel());
                    aiChatMessageDao.insert(messageEntity2);
                }).onErrorResume(throwable -> {
                    AiChatMessageDTO result = new AiChatMessageDTO();
                    result.setType(MessageType.ASSISTANT.getValue());
                    result.setContent("调用大模型失败，请查看报错信息！");
                    return Flux.just(result);
                });
    }


    private Message getMessage(String type, String content) {
        if (MessageType.USER.getValue().equals(type)) {
            return new UserMessage(content);
        }
        if (MessageType.ASSISTANT.getValue().equals(type)) {
            return new AssistantMessage(content);
        }
        throw new IllegalArgumentException(StrUtil.format("Unknown AI message type {}", type));
    }

}
