package io.renren.modules.ai.dao;

import io.renren.common.dao.BaseDao;
import io.renren.modules.ai.entity.AiChatConversationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI聊天对话
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface AiChatConversationDao extends BaseDao<AiChatConversationEntity> {

}