package io.renren.modules.mp.dao;

import io.renren.common.dao.BaseDao;
import io.renren.modules.mp.entity.MpMenuEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 公众号自定义菜单
*
* @author Mark sunlightcs@gmail.com
*/
@Mapper
public interface MpMenuDao extends BaseDao<MpMenuEntity> {
	
}