package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.SystemAdminEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员 Mapper
 *
 * @author CRMChat Team
 */
@Mapper
public interface SystemAdminMapper extends BaseMapper<SystemAdminEntity> {

}
