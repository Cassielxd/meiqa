package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.ApplicationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应用管理 Mapper
 *
 * @author CRMChat Team
 */
@Mapper
public interface ApplicationMapper extends BaseMapper<ApplicationEntity> {
}
