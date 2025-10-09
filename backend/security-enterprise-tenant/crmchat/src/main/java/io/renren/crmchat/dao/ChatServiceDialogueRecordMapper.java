package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.ChatServiceDialogueRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对话记录 Mapper
 *
 * @author CRMChat Team
 */
@Mapper
public interface ChatServiceDialogueRecordMapper extends BaseMapper<ChatServiceDialogueRecordEntity> {
}
