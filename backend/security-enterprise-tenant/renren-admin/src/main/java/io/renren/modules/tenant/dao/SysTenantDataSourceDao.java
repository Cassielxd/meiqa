package io.renren.modules.tenant.dao;

import io.renren.common.dao.BaseDao;
import io.renren.modules.tenant.entity.SysTenantDataSourceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 租户数据源
*
* @author Mark sunlightcs@gmail.com
*/
@Mapper
public interface SysTenantDataSourceDao extends BaseDao<SysTenantDataSourceEntity> {
	
}