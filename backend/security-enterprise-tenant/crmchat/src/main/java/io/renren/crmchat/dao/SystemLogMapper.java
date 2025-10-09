package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.SystemLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统日志 Mapper
 * PHP Reference: /app/dao/system/log/SystemLogDao.php
 *
 * @author CRMChat Team
 */
@Mapper
public interface SystemLogMapper extends BaseMapper<SystemLogEntity> {

}
