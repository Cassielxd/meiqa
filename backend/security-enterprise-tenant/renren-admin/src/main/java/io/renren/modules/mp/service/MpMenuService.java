package io.renren.modules.mp.service;

import io.renren.common.service.CrudService;
import io.renren.modules.mp.dto.MpMenuDTO;
import io.renren.modules.mp.entity.MpMenuEntity;

/**
 * 公众号自定义菜单
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface MpMenuService extends CrudService<MpMenuEntity, MpMenuDTO> {

    MpMenuDTO getByAppId(String appId);

    void deleteByAppId(String appId);
}