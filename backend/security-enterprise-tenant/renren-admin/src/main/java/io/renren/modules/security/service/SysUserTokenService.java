/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.security.service;

import io.renren.common.page.PageData;
import io.renren.common.service.BaseService;
import io.renren.common.utils.Result;
import io.renren.modules.security.entity.SysUserTokenEntity;
import io.renren.modules.sys.entity.SysOnlineEntity;

import java.util.Map;

/**
 * 用户Token
 * 
 * @author Mark sunlightcs@gmail.com
 */
public interface SysUserTokenService extends BaseService<SysUserTokenEntity> {

	/**
	 * 生成token
	 * @param userId  用户ID
	 */
	Result createToken(Long userId);

	/**
	 * 退出
	 * @param userId  用户ID
	 */
	void logout(Long userId);

	/**
	 * 在线用户分页
	 */
	PageData<SysOnlineEntity> onlinePage(Map<String, Object> params);

}