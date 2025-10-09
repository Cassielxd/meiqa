package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.ChatAutoReplyEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 自动回复 Mapper
 *
 * @author CRMChat Team
 */
@Mapper
public interface ChatAutoReplyMapper extends BaseMapper<ChatAutoReplyEntity> {
}
