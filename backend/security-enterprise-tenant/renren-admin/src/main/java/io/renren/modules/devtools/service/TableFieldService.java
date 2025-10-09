/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.devtools.service;


import io.renren.common.service.BaseService;
import io.renren.modules.devtools.entity.TableFieldEntity;

import java.util.List;

/**
 * 列
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface TableFieldService extends BaseService<TableFieldEntity> {

    List<TableFieldEntity> getByTableName(String tableName);

    void deleteByTableName(String tableName);

    void deleteBatchTableIds(Long[] tableIds);
}