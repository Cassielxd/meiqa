package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.ChatServiceFeedbackEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 服务反馈/留言 Mapper
 *
 * @author CRMChat Team
 */
@Mapper
public interface ChatServiceFeedbackMapper extends BaseMapper<ChatServiceFeedbackEntity> {
}
