package io.renren.crmchat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.renren.crmchat.entity.QrcodeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 二维码表 Mapper
 * PHP Reference: app/dao/other/QrcodeDao.php
 *
 * @author CRMChat Team
 */
@Mapper
public interface QrcodeMapper extends BaseMapper<QrcodeEntity> {
}
