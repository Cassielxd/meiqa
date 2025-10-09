package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.ChatUserLabelAssistEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户标签关联 Mapper
 *
 * @author CRMChat Team
 */
@Mapper
public interface ChatUserLabelAssistMapper extends BaseMapper<ChatUserLabelAssistEntity> {
}
