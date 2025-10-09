/**
 * Copyright (c) 2021 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.pay.service;

import io.renren.common.service.CrudService;
import io.renren.modules.pay.dto.WeChatNotifyLogDTO;
import io.renren.modules.pay.entity.WeChatNotifyLogEntity;

/**
 * 微信支付回调日志
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface WeChatNotifyLogService extends CrudService<WeChatNotifyLogEntity, WeChatNotifyLogDTO> {

}