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
import io.renren.modules.devtools.dao.BaseClassDao;
import io.renren.modules.devtools.entity.BaseClassEntity;
import io.renren.modules.devtools.service.BaseClassService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 基类管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service
public class BaseClassServiceImpl extends BaseServiceImpl<BaseClassDao, BaseClassEntity> implements BaseClassService {

    @Override
    public PageData<BaseClassEntity> page(Map<String, Object> params) {
        IPage<BaseClassEntity> page = baseDao.selectPage(
            getPage(params, Constant.CREATE_DATE, false),
            getWrapper(params)
        );
        return new PageData<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<BaseClassEntity> list() {
        return baseDao.selectList(null);
    }

    private QueryWrapper<BaseClassEntity> getWrapper(Map<String, Object> params){
        String code = (String)params.get("code");

        QueryWrapper<BaseClassEntity> wrapper = new QueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(code), "code", code);

        return wrapper;
    }
}