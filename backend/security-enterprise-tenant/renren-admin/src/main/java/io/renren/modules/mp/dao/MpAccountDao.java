package io.renren.modules.mp.dao;

import io.renren.common.dao.BaseDao;
import io.renren.modules.mp.entity.MpAccountEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 公众号账号管理
*
* @author Mark sunlightcs@gmail.com
*/
@Mapper
public interface MpAccountDao extends BaseDao<MpAccountEntity> {
	
}