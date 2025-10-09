/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.devtools.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.renren.common.constant.Constant;
import io.renren.common.exception.RenException;
import io.renren.common.utils.DateUtils;
import io.renren.modules.devtools.config.DataSourceInfo;
import io.renren.modules.devtools.dao.GeneratorMenuDao;
import io.renren.modules.devtools.entity.*;
import io.renren.modules.devtools.service.*;
import io.renren.modules.devtools.utils.DbUtils;
import io.renren.modules.devtools.utils.GenUtils;
import io.renren.modules.devtools.utils.TemplateContent;
import io.renren.modules.sys.service.SysParamsService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 代码生成
 *
 * @author Mark sunlightcs@gmail.com
 */
@AllArgsConstructor
@Service
public class GeneratorServiceImpl implements GeneratorService {
    private final TableInfoService tableInfoService;
    private final TableFieldService tableFieldService;
    private final DataSourceService datasourceService;
    private final FieldTypeService fieldTypeService;
    private final BaseClassService baseClassService;
    private final SysParamsService sysParamsService;
    private final GeneratorMenuDao generatorMenuDao;
    private final DataSource dataSource;

    @Override
    public DataSourceInfo getDataSourceInfo(Long datasourceId) {
        //初始化配置信息
        DataSourceInfo info = null;
        if (datasourceId.intValue() == 0) {
            try {
                info = new DataSourceInfo(dataSource.getConnection());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            info = new DataSourceInfo(datasourceService.selectById(datasourceId));
        }

        return info;
    }


    @Override
    public void datasourceTable(TableInfoEntity tableInfo) {
        //初始化配置信息
        DataSourceInfo info = getDataSourceInfo(tableInfo.getDatasourceId());

        TableInfoEntity table = tableInfoService.getByTableName(tableInfo.getTableName());
        //表存在
        if (table != null) {
            throw new RenException(tableInfo.getTableName() + " table already exists");
        }

        table = DbUtils.getTablesInfo(info, tableInfo.getTableName());

        //代码生成器参数
        GenParam param = sysParamsService.getValueObject(Constant.DEV_TOOLS_PARAM_KEY, GenParam.class);

        //保存表信息
        table.setPackageName(param.getPackageName());
        table.setVersion(param.getVersion());
        table.setAuthor(param.getAuthor());
        table.setEmail(param.getEmail());
        table.setBackendPath(param.getBackendPath());
        table.setFrontendPath(param.getFrontendPath());
        tableInfoService.insert(table);

        //获取原生列数据
        List<TableFieldEntity> tableFieldList = DbUtils.getTableColumns(info, table.getId(), tableInfo.getTableName());
        //初始化列数据
        initFieldList(tableFieldList);
        //批量保存列数据
        tableFieldService.insertBatch(tableFieldList);

        try {
            //释放数据源
            info.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTableField(Long tableId, List<TableFieldEntity> tableFieldList) {
        //删除旧列信息
        tableFieldService.deleteBatchTableIds(new Long[]{tableId});

        //保存新列数据
        int sort = 0;
        for (TableFieldEntity tableField : tableFieldList) {
            tableField.setSort(sort++);
            tableFieldService.insert(tableField);
        }

    }

    /**
     * 初始化列数据
     */
    private void initFieldList(List<TableFieldEntity> tableFieldList) {
        //字段类型、属性类型映射
        Map<String, FieldTypeEntity> fieldTypeMap = fieldTypeService.getMap();
        int index = 0;
        for (TableFieldEntity tableField : tableFieldList) {
            tableField.setAttrName(StringUtils.uncapitalize(GenUtils.columnToJava(tableField.getColumnName())));
            //获取字段对应的类型
            FieldTypeEntity fieldTypeMapping = fieldTypeMap.get(tableField.getColumnType().toLowerCase());
            if (fieldTypeMapping == null) {
                //没找到对应的类型，则为Object类型
                tableField.setAttrType("Object");
            } else {
                tableField.setAttrType(fieldTypeMapping.getAttrType());
                tableField.setPackageName(fieldTypeMapping.getPackageName());
            }

            tableField.setList(true);
            tableField.setForm(true);

            tableField.setQueryType("=");
            tableField.setFormType("text");

            tableField.setSort(index++);
        }
    }

    @Override
    public void generatorCode(TableInfoEntity tableInfo) {
        //删除旧表信息
        tableInfoService.deleteByTableName(tableInfo.getTableName());
        //删除旧列信息
        tableFieldService.deleteByTableName(tableInfo.getTableName());

        //保存新表信息
        tableInfoService.insert(tableInfo);
        //保存新列信息
        tableFieldService.insertBatch(tableInfo.getFields());

        //数据模型
        Map<String, Object> dataModel = new HashMap<>();
        //项目信息
        dataModel.put("package", tableInfo.getPackageName());
        dataModel.put("packagePath", tableInfo.getPackageName().replace(".", File.separator));
        dataModel.put("version", tableInfo.getVersion());
        dataModel.put("moduleName", tableInfo.getModuleName());

        String subModuleName = tableInfo.getSubModuleName();
        if (StringUtils.isBlank(subModuleName)) {
            subModuleName = null;
        }
        dataModel.put("subModuleName", subModuleName);
        dataModel.put("backendPath", tableInfo.getBackendPath());
        dataModel.put("frontendPath", tableInfo.getFrontendPath());
        //开发者信息
        dataModel.put("author", tableInfo.getAuthor());
        dataModel.put("email", tableInfo.getEmail());
        dataModel.put("datetime", DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN));
        dataModel.put("date", DateUtils.format(new Date(), DateUtils.DATE_PATTERN));
        //表信息
        dataModel.put("tableName", tableInfo.getTableName());
        dataModel.put("tableComment", tableInfo.getTableComment());
        dataModel.put("ClassName", tableInfo.getClassName());
        dataModel.put("className", StringUtils.uncapitalize(tableInfo.getClassName()));
        dataModel.put("classname", tableInfo.getClassName().toLowerCase());
        dataModel.put("columnList", tableInfo.getFields());

        //主键
        for (TableFieldEntity tableField : tableInfo.getFields()) {
            if (tableField.isPk()) {
                dataModel.put("pk", tableField);
                break;
            }
        }

        //导入的包列表
        Set<String> imports = fieldTypeService.getPackageListByTableId(tableInfo.getId());
        //过滤为空的数据
        imports = imports.stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        dataModel.put("imports", imports);

        //基类
        if (tableInfo.getBaseclassId() != null) {
            BaseClassEntity baseClassEntity = baseClassService.selectById(tableInfo.getBaseclassId());
            baseClassEntity.setPackageName(GenUtils.getTemplateContent(baseClassEntity.getPackageName(), dataModel));
            dataModel.put("baseClassEntity", baseClassEntity);
        }

        //获取模板
        List<TemplateContent> templateList = GenUtils.getTemplateList();

        //渲染模板并输出
        for (TemplateContent template : templateList) {
            dataModel.put("templateName", template.getName());
            String content = GenUtils.getTemplateContent(template.getContent(), dataModel);
            String path = GenUtils.getTemplateContent(template.getPath(), dataModel);
            FileUtil.writeUtf8String(content, path);
            System.out.println(path);
        }
    }

    @Override
    public void generatorMenu(MenuEntity menu) {
        Map<String, Object> params = new HashMap<>();
        params.put("pid", menu.getPid());
        params.put("name", menu.getName());
        params.put("icon", menu.getIcon());
        params.put("menu_type", 0);
        int sort = 0;
        params.put("sort", sort);
        params.put("url", menu.getModuleName() + "/" + menu.getClassName().toLowerCase());
        params.put("now", new Date());
        //权限标识
        String permission = menu.getModuleName() + ":" + menu.getClassName().toLowerCase();
        //菜单id
        Long menuId = IdWorker.getId();

        //菜单
        params.put("id", menuId);
        generatorMenuDao.generatorMenu(params);
        params.put("language", "en-US");
        generatorMenuDao.generatorMenuLanguage(params);
        params.put("language", "zh-CN");
        generatorMenuDao.generatorMenuLanguage(params);
        params.put("language", "zh-TW");
        generatorMenuDao.generatorMenuLanguage(params);

        //查看
        params.put("id", IdWorker.getId());
        params.put("pid", menuId);
        params.put("permissions", permission + ":page," + permission + ":info");
        params.put("icon", null);
        params.put("menu_type", 1);
        params.put("sort", sort);
        generatorMenuDao.generatorMenu(params);
        params.put("name", "View");
        params.put("language", "en-US");
        generatorMenuDao.generatorMenuLanguage(params);
        params.put("name", "查看");
        params.put("language", "zh-CN");
        generatorMenuDao.generatorMenuLanguage(params);
        params.put("name", "查看");
        params.put("language", "zh-TW");
        generatorMenuDao.generatorMenuLanguage(params);

        //新增
        params.put("id", IdWorker.getId());
        params.put("pid", menuId);
        params.put("permissions", permission + ":save");
        params.put("sort", ++sort);
        generatorMenuDao.generatorMenu(params);
        params.put("name", "Add");
        params.put("language", "en-US");
        generatorMenuDao.generatorMenuLanguage(params);
        params.put("name", "新增");
        params.put("language", "zh-CN");
        generatorMenuDao.generatorMenuLanguage(params);
        params.put("name", "新增");
        params.put("language", "zh-TW");
        generatorMenuDao.generatorMenuLanguage(params);

        //修改
        params.put("id", IdWorker.getId());
        params.put("pid", menuId);
        params.put("permissions", permission + ":update");
        params.put("sort", ++sort);
        generatorMenuDao.generatorMenu(params);
        params.put("name", "Edit");
        params.put("language", "en-US");
        generatorMenuDao.generatorMenuLanguage(params);
        params.put("name", "修改");
        params.put("language", "zh-CN");
        generatorMenuDao.generatorMenuLanguage(params);
        params.put("name", "修改");
        params.put("language", "zh-TW");
        generatorMenuDao.generatorMenuLanguage(params);

        //删除
        params.put("id", IdWorker.getId());
        params.put("pid", menuId);
        params.put("permissions", permission + ":delete");
        params.put("sort", ++sort);
        generatorMenuDao.generatorMenu(params);
        params.put("name", "Delete");
        params.put("language", "en-US");
        generatorMenuDao.generatorMenuLanguage(params);
        params.put("name", "删除");
        params.put("language", "zh-CN");
        generatorMenuDao.generatorMenuLanguage(params);
        params.put("name", "删除");
        params.put("language", "zh-TW");
        generatorMenuDao.generatorMenuLanguage(params);

        //导出
        params.put("id", IdWorker.getId());
        params.put("pid", menuId);
        params.put("permissions", permission + ":export");
        params.put("sort", ++sort);
        generatorMenuDao.generatorMenu(params);
        params.put("name", "Export");
        params.put("language", "en-US");
        generatorMenuDao.generatorMenuLanguage(params);
        params.put("name", "导出");
        params.put("language", "zh-CN");
        generatorMenuDao.generatorMenuLanguage(params);
        params.put("name", "導出");
        params.put("language", "zh-TW");
        generatorMenuDao.generatorMenuLanguage(params);
    }

}