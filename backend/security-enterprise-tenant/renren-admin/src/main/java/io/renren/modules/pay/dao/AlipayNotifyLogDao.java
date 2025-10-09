/**
 * Copyright (c) 2021 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.pay.dao;

import io.renren.common.dao.BaseDao;
import io.renren.modules.pay.entity.AlipayNotifyLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
* 支付宝回调日志
*
* @author Mark sunlightcs@gmail.com
*/
@Mapper
public interface AlipayNotifyLogDao extends BaseDao<AlipayNotifyLogEntity> {
	
}