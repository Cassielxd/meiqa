package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.ChatComplainEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户投诉Mapper
 * 对应 PHP: ChatComplainDao
 *
 * @author CRMChat Team
 */
@Mapper
public interface ChatComplainMapper extends BaseMapper<ChatComplainEntity> {
}
