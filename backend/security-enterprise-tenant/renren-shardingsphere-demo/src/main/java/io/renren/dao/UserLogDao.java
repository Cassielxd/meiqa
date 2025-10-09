package io.renren.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.entity.UserLogEntity;
import org.apache.ibatis.annotations.Mapper;


/**
 * 用户日志
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface UserLogDao extends BaseMapper<UserLogEntity> {


}