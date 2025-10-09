package io.renren.modules.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.service.impl.CrudServiceImpl;
import io.renren.modules.flow.dao.BpmFormDao;
import io.renren.modules.flow.dto.BpmFormDTO;
import io.renren.modules.flow.entity.BpmFormEntity;
import io.renren.modules.flow.service.BpmFormService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 工作流表单
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service
public class BpmFormServiceImpl extends CrudServiceImpl<BpmFormDao, BpmFormEntity, BpmFormDTO> implements BpmFormService {

    @Override
    public QueryWrapper<BpmFormEntity> getWrapper(Map<String, Object> params) {
        QueryWrapper<BpmFormEntity> wrapper = new QueryWrapper<>();

        String name = (String) params.get("name");
        wrapper.like(StringUtils.isNotBlank(name), "name", name);
        wrapper.orderByDesc("create_date");
        return wrapper;
    }

}