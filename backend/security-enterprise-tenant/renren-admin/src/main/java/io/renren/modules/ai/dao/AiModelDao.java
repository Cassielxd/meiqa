package io.renren.modules.ai.dao;

import io.renren.common.dao.BaseDao;
import io.renren.modules.ai.entity.AiModelEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI模型
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface AiModelDao extends BaseDao<AiModelEntity> {

}