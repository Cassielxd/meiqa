/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.devtools.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.service.impl.BaseServiceImpl;
import io.renren.modules.devtools.dao.FieldTypeDao;
import io.renren.modules.devtools.entity.FieldTypeEntity;
import io.renren.modules.devtools.service.FieldTypeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 字段类型管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service
public class FieldTypeServiceImpl extends BaseServiceImpl<FieldTypeDao, FieldTypeEntity> implements FieldTypeService {

    @Override
    public PageData<FieldTypeEntity> page(Map<String, Object> params) {
        IPage<FieldTypeEntity> page = baseDao.selectPage(
            getPage(params, Constant.CREATE_DATE, false),
            getWrapper(params)
        );
        return new PageData<>(page.getRecords(), page.getTotal());
    }

    @Override
    public Map<String, FieldTypeEntity> getMap() {
        List<FieldTypeEntity> list = baseDao.selectList(null);
        Map<String, FieldTypeEntity> map = new LinkedHashMap<>(list.size());
        for(FieldTypeEntity entity : list){
            map.put(entity.getColumnType().toLowerCase(), entity);
        }
        return map;
    }

    private QueryWrapper<FieldTypeEntity> getWrapper(Map<String, Object> params){
        String attrType = (String)params.get("attrType");
        String columnType = (String)params.get("columnType");

        QueryWrapper<FieldTypeEntity> wrapper = new QueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(attrType), "attr_type", attrType);
        wrapper.like(StringUtils.isNotEmpty(columnType), "column_type", columnType);

        return wrapper;
    }

    @Override
    public Set<String> getPackageListByTableId(Long tableId) {
        return baseDao.getPackageListByTableId(tableId);
    }

    @Override
    public Set<String> list() {
        return baseDao.list();
    }

}