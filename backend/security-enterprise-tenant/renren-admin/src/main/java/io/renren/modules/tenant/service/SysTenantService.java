/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.tenant.service;

import io.renren.common.page.PageData;
import io.renren.common.service.BaseService;
import io.renren.modules.tenant.dto.SysTenantDTO;
import io.renren.modules.tenant.dto.SysTenantListDTO;
import io.renren.modules.tenant.entity.SysTenantEntity;

import java.util.List;
import java.util.Map;


/**
 * 租户管理
 * 
 * @author Mark sunlightcs@gmail.com
 */
public interface SysTenantService extends BaseService<SysTenantEntity> {

	PageData<SysTenantDTO> page(Map<String, Object> params);

	List<SysTenantListDTO> list();

	SysTenantDTO get(Long id);

	void save(SysTenantDTO dto);

	void update(SysTenantDTO dto);

	void delete(Long[] ids);

}
