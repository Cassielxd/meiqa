/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.devtools.service;

import io.renren.modules.devtools.config.DataSourceInfo;
import io.renren.modules.devtools.entity.MenuEntity;
import io.renren.modules.devtools.entity.TableFieldEntity;
import io.renren.modules.devtools.entity.TableInfoEntity;

import java.util.List;

/**
 * 代码生成
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface GeneratorService {

    DataSourceInfo getDataSourceInfo(Long datasourceId);

    void datasourceTable(TableInfoEntity tableInfo);

    void updateTableField(Long tableId, List<TableFieldEntity> tableFieldList);

    void generatorCode(TableInfoEntity tableInfo);

    void generatorMenu(MenuEntity menu);
}
