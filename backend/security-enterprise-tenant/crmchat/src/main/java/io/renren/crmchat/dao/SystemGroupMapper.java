package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.SystemGroupEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 组合数据表Mapper
 * PHP Reference: app/models/system/config/SystemGroup.php
 *
 * @author CRMChat Team
 */
@Mapper
public interface SystemGroupMapper extends BaseMapper<SystemGroupEntity> {
}
