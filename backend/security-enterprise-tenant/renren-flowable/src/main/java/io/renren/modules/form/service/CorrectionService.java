/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */
package io.renren.modules.form.service;

import io.renren.common.service.BaseService;
import io.renren.modules.form.dto.CorrectionDTO;
import io.renren.modules.form.entity.CorrectionEntity;

/**
 * 转正申请
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface CorrectionService extends BaseService<CorrectionEntity> {

    CorrectionDTO get(String instanceId);

    void save(CorrectionDTO dto);
}