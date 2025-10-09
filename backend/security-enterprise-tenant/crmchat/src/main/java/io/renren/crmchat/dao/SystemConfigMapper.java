package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.SystemConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置Mapper
 *
 * @author CRMChat Team
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfigEntity> {
}
