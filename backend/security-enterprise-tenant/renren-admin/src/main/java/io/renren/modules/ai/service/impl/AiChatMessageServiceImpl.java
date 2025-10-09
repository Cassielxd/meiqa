package io.renren.modules.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.common.service.impl.BaseServiceImpl;
import io.renren.common.utils.ConvertUtils;
import io.renren.modules.ai.dao.AiChatMessageDao;
import io.renren.modules.ai.dto.AiChatMessageDTO;
import io.renren.modules.ai.entity.AiChatMessageEntity;
import io.renren.modules.ai.service.AiChatMessageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI聊天消息
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service
@AllArgsConstructor
public class AiChatMessageServiceImpl extends BaseServiceImpl<AiChatMessageDao, AiChatMessageEntity> implements AiChatMessageService {

    @Override
    public List<AiChatMessageDTO> getList(Long conversationId) {
        LambdaQueryWrapper<AiChatMessageEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiChatMessageEntity::getConversationId, conversationId);
        List<AiChatMessageEntity> list = baseDao.selectList(wrapper);

        return ConvertUtils.sourceToTarget(list, AiChatMessageDTO.class);
    }

    @Override
    public void clearList(Long conversationId) {
        LambdaQueryWrapper<AiChatMessageEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiChatMessageEntity::getConversationId, conversationId);
        baseDao.delete(wrapper);
    }
}