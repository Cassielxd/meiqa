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
 * 支付宝回调日志
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "支付宝回调日志")
public class AlipayNotifyLogDTO implements Serializable {


    private Long id;
    @Schema(description = "订单号")
    private Long outTradeNo;
    @Schema(description = "订单金额")
    private BigDecimal totalAmount;
    @Schema(description = "付款金额")
    private BigDecimal buyerPayAmount;
    @Schema(description = "实收金额")
    private BigDecimal receiptAmount;
    @Schema(description = "开票金额")
    private BigDecimal invoiceAmount;
    @Schema(description = "通知校验ID")
    private String notifyId;
    @Schema(description = "买家支付宝用户号")
    private String buyerId;
    @Schema(description = "卖家支付宝用户号")
    private String sellerId;
    @Schema(description = "支付宝交易号")
    private String tradeNo;
    @Schema(description = "交易状态")
    private String tradeStatus;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

}