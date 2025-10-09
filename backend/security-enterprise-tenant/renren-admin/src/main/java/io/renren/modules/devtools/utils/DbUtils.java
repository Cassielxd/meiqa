/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.devtools.utils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.renren.modules.devtools.config.DataSourceInfo;
import io.renren.modules.devtools.config.query.AbstractQuery;
import io.renren.modules.devtools.entity.TableFieldEntity;
import io.renren.modules.devtools.entity.TableInfoEntity;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.driver.OracleConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DB工具类
 *
 * @author Mark sunlightcs@gmail.com
 */
@Slf4j
public class DbUtils {
    private static final int CONNECTION_TIMEOUTS_SECONDS = 6;

    /**
     * 获得数据库连接
     *
     * @param info
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static Connection getConnection(DataSourceInfo info) throws ClassNotFoundException, SQLException {
        DriverManager.setLoginTimeout(CONNECTION_TIMEOUTS_SECONDS);

        Class.forName(info.getDbType().getDriverClass());

        Connection connection = DriverManager.getConnection(info.getConnUrl(), info.getUsername(), info.getPassword());
        if (info.getDbType() == DbType.Oracle) {
            ((OracleConnection) connection).setRemarksReporting(true);
        }

        return connection;
    }


    /**
     * 获取数据库表信息
     */
    public static TableInfoEntity getTablesInfo(DataSourceInfo info, String tableName) {
        try {
            AbstractQuery dbQuery = info.getDbQuery();

            //查询数据
            PreparedStatement preparedStatement = info.getConnection().prepareStatement(dbQuery.tablesSql(tableName));
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                TableInfoEntity tableInfo = new TableInfoEntity();
                tableInfo.setTableName(rs.getString(dbQuery.tableName()));
                tableInfo.setClassName(GenUtils.columnToJava(tableInfo.getTableName()));
                tableInfo.setTableComment(rs.getString(dbQuery.tableComment()));
                tableInfo.setDatasourceId(info.getId());
                return tableInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取所有的数据库表
     */
    public static List<TableInfoEntity> getTablesInfoList(DataSourceInfo info) {
        List<TableInfoEntity> tableInfoList = new ArrayList<>();
        try {
            AbstractQuery dbQuery = info.getDbQuery();

            //查询数据
            PreparedStatement preparedStatement = info.getConnection().prepareStatement(dbQuery.tablesSql(null));
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                TableInfoEntity tableInfo = new TableInfoEntity();
                tableInfo.setTableName(rs.getString(dbQuery.tableName()));
                tableInfo.setClassName(GenUtils.columnToJava(tableInfo.getTableName()));
                tableInfo.setTableComment(rs.getString(dbQuery.tableComment()));
                tableInfo.setDatasourceId(info.getId());
                tableInfoList.add(tableInfo);
            }

            info.getConnection().close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tableInfoList;
    }


    /**
     * 获取表的列属性
     *
     * @param info      数据库配置文件
     * @param tableName 表名
     */
    public static List<TableFieldEntity> getTableColumns(DataSourceInfo info, Long tableId, String tableName) {
        List<TableFieldEntity> tableFieldList = new ArrayList<>();

        try {
            AbstractQuery dbQuery = info.getDbQuery();
            String tableFieldsSql = dbQuery.tableFieldsSql();
            if (info.getDbType() == DbType.Oracle) {
                DatabaseMetaData md = info.getConnection().getMetaData();
                tableFieldsSql = String.format(tableFieldsSql.replace("#schema", md.getUserName()), tableName);
            } else {
                tableFieldsSql = String.format(tableFieldsSql, tableName);
            }
            PreparedStatement preparedStatement = info.getConnection().prepareStatement(tableFieldsSql);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                TableFieldEntity field = new TableFieldEntity();
                field.setTableId(tableId);
                field.setTableName(tableName);
                field.setColumnName(rs.getString(dbQuery.fieldName()));
                String columnType = rs.getString(dbQuery.fieldType());
                if (columnType.indexOf(" ") != -1) {
                    columnType = columnType.substring(0, columnType.indexOf(" "));
                }
                field.setColumnType(columnType);
                field.setColumnComment(rs.getString(dbQuery.fieldComment()));
                String key = rs.getString(dbQuery.fieldKey());
                field.setPk(StringUtils.isNotBlank(key) && "PRI".equals(key.toUpperCase()));

                tableFieldList.add(field);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tableFieldList;
    }

}