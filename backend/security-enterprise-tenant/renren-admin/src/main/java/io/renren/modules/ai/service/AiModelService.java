package io.renren.modules.ai.service;

import io.renren.common.page.PageData;
import io.renren.common.service.BaseService;
import io.renren.modules.ai.dto.AiModelDTO;
import io.renren.modules.ai.entity.AiModelEntity;

import java.util.List;
import java.util.Map;

/**
 * AI模型
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface AiModelService extends BaseService<AiModelEntity> {

    PageData<AiModelDTO> page(Map<String, Object> params);

    List<AiModelDTO> getList();

    AiModelDTO get(Long id);

    void save(AiModelDTO dto);

    void update(AiModelDTO dto);

    void delete(List<Long> idList);

}