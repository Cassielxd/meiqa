package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.ChatUserLabelEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户标签 Mapper
 *
 * @author CRMChat Team
 */
@Mapper
public interface ChatUserLabelMapper extends BaseMapper<ChatUserLabelEntity> {
}
