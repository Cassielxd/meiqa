package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.ChatUserGroupEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户分组 Mapper
 *
 * @author CRMChat Team
 */
@Mapper
public interface ChatUserGroupMapper extends BaseMapper<ChatUserGroupEntity> {
}
