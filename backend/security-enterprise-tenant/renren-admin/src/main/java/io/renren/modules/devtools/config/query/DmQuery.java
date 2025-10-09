/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.devtools.config.query;

import io.renren.modules.devtools.utils.DbType;
import org.apache.commons.lang3.StringUtils;

/**
 * 达梦数据库 查询
 *
 * @author Mark sunlightcs@gmail.com
 */
public class DmQuery implements AbstractQuery {

    @Override
    public DbType dbType() {
        return DbType.DM;
    }

    @Override
    public String tablesSql(String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT T.* FROM (SELECT DISTINCT T1.TABLE_NAME AS TABLE_NAME,T2.COMMENTS AS TABLE_COMMENT FROM USER_TAB_COLUMNS T1 ");
        sql.append("INNER JOIN USER_TAB_COMMENTS T2 ON T1.TABLE_NAME = T2.TABLE_NAME) T WHERE 1=1 ");
        //表名查询
        if (StringUtils.isNotBlank(tableName)) {
            sql.append("and T.TABLE_NAME = '").append(tableName).append("' ");
        }
        sql.append("order by T.TABLE_NAME asc");

        return sql.toString();
    }


    public String tableFieldsSql() {
        return
                "SELECT T2.COLUMN_NAME,T1.COMMENTS," +
                        "CASE WHEN T2.DATA_TYPE='NUMBER' THEN (CASE WHEN T2.DATA_PRECISION IS NULL THEN T2.DATA_TYPE WHEN NVL(T2.DATA_SCALE, 0) > 0 THEN T2.DATA_TYPE||'('||T2.DATA_PRECISION||','||T2.DATA_SCALE||')' ELSE T2.DATA_TYPE||'('||T2.DATA_PRECISION||')' END) ELSE T2.DATA_TYPE END DATA_TYPE ," +
                        "CASE WHEN CONSTRAINT_TYPE='P' THEN 'PRI' END AS KEY " +
                        "FROM USER_COL_COMMENTS T1, USER_TAB_COLUMNS T2, " +
                        "(SELECT T4.TABLE_NAME, T4.COLUMN_NAME ,T5.CONSTRAINT_TYPE " +
                        "FROM USER_CONS_COLUMNS T4, USER_CONSTRAINTS T5 " +
                        "WHERE T4.CONSTRAINT_NAME = T5.CONSTRAINT_NAME " +
                        "AND T5.CONSTRAINT_TYPE = 'P')T3 " +
                        "WHERE T1.TABLE_NAME = T2.TABLE_NAME AND " +
                        "T1.COLUMN_NAME=T2.COLUMN_NAME AND " +
                        "T1.TABLE_NAME = T3.TABLE_NAME(+) AND " +
                        "T1.COLUMN_NAME=T3.COLUMN_NAME(+)   AND " +
                        "T1.TABLE_NAME = '%s' " +
                        "ORDER BY T2.TABLE_NAME,T2.COLUMN_ID";
    }

    @Override
    public String tableName() {
        return "TABLE_NAME";
    }

    @Override
    public String tableComment() {
        return "TABLE_COMMENT";
    }

    @Override
    public String fieldName() {
        return "COLUMN_NAME";
    }

    @Override
    public String fieldType() {
        return "DATA_TYPE";
    }

    @Override
    public String fieldComment() {
        return "COMMENTS";
    }

    @Override
    public String fieldKey() {
        return "KEY";
    }
}
