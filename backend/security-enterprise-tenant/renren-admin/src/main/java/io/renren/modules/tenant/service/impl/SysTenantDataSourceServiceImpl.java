package io.renren.modules.tenant.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.exception.RenException;
import io.renren.common.service.impl.CrudServiceImpl;
import io.renren.commons.dynamic.datasource.config.DynamicDataSource;
import io.renren.commons.dynamic.datasource.config.DynamicDataSourceFactory;
import io.renren.commons.dynamic.datasource.properties.DataSourceProperties;
import io.renren.modules.tenant.dao.SysTenantDataSourceDao;
import io.renren.modules.tenant.dto.SysTenantDataSourceDTO;
import io.renren.modules.tenant.entity.SysTenantDataSourceEntity;
import io.renren.modules.tenant.service.SysTenantDataSourceService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 租户数据源
 *
 * @author Mark sunlightcs@gmail.com
 */
@AllArgsConstructor
@Service
public class SysTenantDataSourceServiceImpl extends CrudServiceImpl<SysTenantDataSourceDao, SysTenantDataSourceEntity, SysTenantDataSourceDTO> implements SysTenantDataSourceService {
    private final DynamicDataSource dynamicDataSource;

    @Override
    public QueryWrapper<SysTenantDataSourceEntity> getWrapper(Map<String, Object> params) {
        String name = (String) params.get("name");

        QueryWrapper<SysTenantDataSourceEntity> wrapper = new QueryWrapper<>();
        wrapper.like(StrUtil.isNotEmpty(name), "name", name);

        return wrapper;
    }

    @Override
    public void save(SysTenantDataSourceDTO dto) {
        super.save(dto);

        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setDriverClassName(dto.getDriverClassName());
        dataSourceProperties.setUrl(dto.getUrl());
        dataSourceProperties.setUsername(dto.getUsername());
        dataSourceProperties.setPassword(dto.getPassword());
        DruidDataSource druidDataSource = DynamicDataSourceFactory.buildDruidDataSource(dataSourceProperties);

        if (!druidDataSource.isEnable()) {
            throw new RenException("Data source unavailable, please check carefully");
        }

        // 新增动态数据源
        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put(dto.getId() + "", druidDataSource);
        dynamicDataSource.addDataSources(dataSources);
    }
}