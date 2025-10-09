/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.devtools.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.modules.devtools.entity.DataSourceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据源管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface DataSourceDao extends BaseMapper<DataSourceEntity> {
	
}