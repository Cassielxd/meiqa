/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.devtools.service;


import io.renren.common.page.PageData;
import io.renren.common.service.BaseService;
import io.renren.modules.devtools.entity.TableInfoEntity;

import java.util.Map;

/**
 * 表
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface TableInfoService extends BaseService<TableInfoEntity> {

    PageData<TableInfoEntity> page(Map<String, Object> params);

    TableInfoEntity getByTableName(String tableName);

    void deleteByTableName(String tableName);

    void deleteBatchIds(Long[] ids);
}