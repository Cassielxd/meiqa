/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.commons.dynamic.datasource.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 多数据源
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicContextHolder.peek();
    }

    public void addDataSources(Map<Object, Object> dataSources) {
        Map<Object, DataSource> resolvedDataSources = super.getResolvedDataSources();

        Map<Object, Object> targetDataSources = new HashMap<>(resolvedDataSources);
        targetDataSources.putAll(dataSources);

        super.setTargetDataSources(targetDataSources);

        super.afterPropertiesSet();
    }

}
