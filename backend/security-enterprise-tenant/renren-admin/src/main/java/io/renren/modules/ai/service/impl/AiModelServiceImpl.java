package io.renren.modules.ai.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.service.impl.BaseServiceImpl;
import io.renren.common.utils.ConvertUtils;
import io.renren.modules.ai.dao.AiModelDao;
import io.renren.modules.ai.dto.AiModelDTO;
import io.renren.modules.ai.entity.AiModelEntity;
import io.renren.modules.ai.service.AiModelService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * AI模型
 *
 * @author Mark sunlightcs@gmail.com
 */
@Service
@AllArgsConstructor
public class AiModelServiceImpl extends BaseServiceImpl<AiModelDao, AiModelEntity> implements AiModelService {

    @Override
    public PageData<AiModelDTO> page(Map<String, Object> params) {
        IPage<AiModelEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                getWrapper(params)
        );

        return getPageData(page, AiModelDTO.class);
    }


    private LambdaQueryWrapper<AiModelEntity> getWrapper(Map<String, Object> params) {
        String platform = (String) params.get("platform");
        String name = (String) params.get("name");
        String model = (String) params.get("model");

        LambdaQueryWrapper<AiModelEntity> wrapper = Wrappers.lambdaQuery();
        if (ObjectUtil.isNotEmpty(platform)) {
            wrapper.eq(AiModelEntity::getPlatform, Long.parseLong(platform));
        }
        wrapper.like(ObjectUtil.isNotEmpty(name), AiModelEntity::getName, name);
        wrapper.like(ObjectUtil.isNotEmpty(model), AiModelEntity::getModel, model);

        return wrapper;
    }

    @Override
    public List<AiModelDTO> getList() {
        LambdaQueryWrapper<AiModelEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(AiModelEntity::getStatus, 1);
        List<AiModelEntity> list = baseDao.selectList(wrapper);

        return ConvertUtils.sourceToTarget(list, AiModelDTO.class);
    }


    @Override
    public AiModelDTO get(Long id) {
        AiModelEntity entity = baseDao.selectById(id);

        return ConvertUtils.sourceToTarget(entity, AiModelDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(AiModelDTO dto) {
        AiModelEntity entity = ConvertUtils.sourceToTarget(dto, AiModelEntity.class);

        baseDao.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AiModelDTO dto) {
        AiModelEntity entity = ConvertUtils.sourceToTarget(dto, AiModelEntity.class);

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        baseDao.deleteByIds(idList);
    }
}