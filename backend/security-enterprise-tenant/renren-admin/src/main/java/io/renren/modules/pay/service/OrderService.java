/**
 * Copyright (c) 2021 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.pay.service;

import io.renren.common.service.CrudService;
import io.renren.modules.pay.dto.OrderDTO;
import io.renren.modules.pay.entity.OrderEntity;

/**
 * 订单
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface OrderService extends CrudService<OrderEntity, OrderDTO> {


    OrderEntity getByOrderId(Long orderId);

    /**
     * 支付成功
     * @param order 订单
     */
    void paySuccess(OrderEntity order);
}