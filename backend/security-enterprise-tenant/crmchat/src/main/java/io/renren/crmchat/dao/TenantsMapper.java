package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.TenantsEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户 Mapper
 *
 * @author CRMChat Team
 */
@Mapper
public interface TenantsMapper extends BaseMapper<TenantsEntity> {
}
