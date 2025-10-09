package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.ChatServiceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客服 Mapper
 *
 * @author CRMChat Team
 */
@Mapper
public interface ChatServiceMapper extends BaseMapper<ChatServiceEntity> {
}
