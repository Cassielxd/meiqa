package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.ChatServiceRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天记录Mapper
 * 参考 PHP: ChatServiceRecordDao
 *
 * @author CRMChat Team
 */
@Mapper
public interface ChatServiceRecordMapper extends BaseMapper<ChatServiceRecordEntity> {
}
