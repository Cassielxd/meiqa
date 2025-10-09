package io.renren.modules.ai.dao;

import io.renren.common.dao.BaseDao;
import io.renren.modules.ai.entity.AiChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI聊天消息
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface AiChatMessageDao extends BaseDao<AiChatMessageEntity> {

}