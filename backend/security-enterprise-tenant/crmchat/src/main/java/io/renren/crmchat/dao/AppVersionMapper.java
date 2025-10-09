package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.AppVersionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppVersionMapper extends BaseMapper<AppVersionEntity> {
}
