package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.CacheEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 缓存表Mapper
 *
 * @author CRMChat Team
 */
@Mapper
public interface CacheMapper extends BaseMapper<CacheEntity> {
}
