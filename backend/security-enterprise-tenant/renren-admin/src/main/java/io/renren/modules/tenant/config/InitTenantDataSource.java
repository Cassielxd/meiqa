/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.tenant.config;

import com.alibaba.druid.pool.DruidDataSource;
import io.renren.commons.dynamic.datasource.config.DynamicDataSource;
import io.renren.commons.dynamic.datasource.config.DynamicDataSourceFactory;
import io.renren.commons.dynamic.datasource.properties.DataSourceProperties;
import io.renren.modules.tenant.dao.SysTenantDataSourceDao;
import io.renren.modules.tenant.entity.SysTenantDataSourceEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 初始化租户数据源
 *
 * @author Mark sunlightcs@gmail.com
 */
public class InitTenantDataSource implements CommandLineRunner {
    @Autowired
    private SysTenantDataSourceDao sysTenantDataSourceDao;
    @Autowired
    private DynamicDataSource dynamicDataSource;

    @Override
    public void run(String... args) {
        List<SysTenantDataSourceEntity> dataSourceList = sysTenantDataSourceDao.selectList(null);

        Map<Object, Object> dataSources = new HashMap<>();
        for(SysTenantDataSourceEntity entity : dataSourceList){
            DataSourceProperties dataSourceProperties = new DataSourceProperties();
            dataSourceProperties.setDriverClassName(entity.getDriverClassName());
            dataSourceProperties.setUrl(entity.getUrl());
            dataSourceProperties.setUsername(entity.getUsername());
            dataSourceProperties.setPassword(entity.getPassword());
            DruidDataSource druidDataSource = DynamicDataSourceFactory.buildDruidDataSource(dataSourceProperties);

            dataSources.put(entity.getId()+ "", druidDataSource);
        }

        dynamicDataSource.addDataSources(dataSources);
    }
}