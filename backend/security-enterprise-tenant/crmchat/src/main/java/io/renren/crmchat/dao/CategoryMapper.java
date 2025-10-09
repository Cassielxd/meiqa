package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.CategoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类Mapper（通用分类）
 * 对应 PHP: CategoryDao
 *
 * @author CRMChat Team
 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryEntity> {
}
