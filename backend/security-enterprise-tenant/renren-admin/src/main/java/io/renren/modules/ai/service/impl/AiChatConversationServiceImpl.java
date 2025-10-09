package io.renren.modules.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.renren.common.service.impl.BaseServiceImpl;
import io.renren.common.utils.ConvertUtils;
import io.renren.modules.ai.dao.AiChatConversationDao;
import io.renren.modules.ai.dto.AiChatConversationDTO;
import io.renren.modules.ai.dto.AiModelDTO;
import io.renren.modules.ai.entity.AiChatConversationEntity;
import io.renren.modules.ai.service.AiChatConversationService;
import io.renren.modules.ai.service.AiModelService;
import io.renren.modules.security.user.SecurityUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI聊天对话
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service
@AllArgsConstructor
public class AiChatConversationServiceImpl extends BaseServiceImpl<AiChatConversationDao, AiChatConversationEntity> implements AiChatConversationService {
    private final AiModelService aiModelService;

    @Override
    public List<AiChatConversationDTO> getList() {
        LambdaQueryWrapper<AiChatConversationEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AiChatConversationEntity::getId);
        List<AiChatConversationEntity> list = baseDao.selectList(wrapper);

        return ConvertUtils.sourceToTarget(list, AiChatConversationDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiChatConversationDTO save(AiChatConversationDTO dto) {
        AiChatConversationEntity entity = ConvertUtils.sourceToTarget(dto, AiChatConversationEntity.class);

        // 获取第一个模型
        List<AiModelDTO> modelList = aiModelService.getList();
        if (modelList.isEmpty()) {
            throw new RuntimeException("Please add model first");
        }

        // 设置默认模型
        AiModelDTO modelDTO = aiModelService.getList().get(0);
        entity.setModelId(modelDTO.getId());
        entity.setModel(modelDTO.getModel());

        // 设置用户ID
        entity.setUserId(SecurityUser.getUserId());

        baseDao.insert(entity);

        return ConvertUtils.sourceToTarget(entity, AiChatConversationDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AiChatConversationDTO dto) {
        AiChatConversationEntity entity = new AiChatConversationEntity();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());

        // 修改模型
        if (dto.getModelId() != null) {
            AiModelDTO modelDTO = aiModelService.get(dto.getModelId());
            entity.setModelId(dto.getModelId());
            entity.setModel(modelDTO.getModel());
        }

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        baseDao.deleteByIds(idList);
    }

}