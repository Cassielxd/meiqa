/**
 * Copyright (c) 2021 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "订单")
public class OrderDTO implements Serializable {


    private Long id;
    @Schema(description = "订单ID")
    private Long orderId;
    @Schema(description = "产品ID")
    private Long productId;
    @Schema(description = "产品名称")
    private String productName;
    @Schema(description = "支付金额")
    private BigDecimal payAmount;
    @Schema(description = "订单状态")
    private Integer status;
    @Schema(description = "购买者ID")
    private Long userId;
    @Schema(description = "支付时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date payAt;
    @Schema(description = "下单时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

}