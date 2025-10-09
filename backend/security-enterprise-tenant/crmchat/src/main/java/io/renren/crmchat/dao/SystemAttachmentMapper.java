package io.renren.crmchat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.SystemAttachmentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 附件管理 Mapper
 *
 * @author CRMChat Team
 */
@Mapper
public interface SystemAttachmentMapper extends BaseMapper<SystemAttachmentEntity> {
}
