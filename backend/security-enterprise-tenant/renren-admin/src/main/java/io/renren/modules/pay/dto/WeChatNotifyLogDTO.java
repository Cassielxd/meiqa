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
import java.util.Date;

/**
 * 微信支付回调日志
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "微信支付回调日志")
public class WeChatNotifyLogDTO implements Serializable {


    private Long id;
    @Schema(description = "订单号")
    private String outTradeNo;
    @Schema(description = "订单总金额，单位为分")
    private Integer total;
    @Schema(description = "用户支付金额，单位为分")
    private Integer payerTotal;
    @Schema(description = "CNY：人民币，境内商户号仅支持人民币")
    private String currency;

    @Schema(description = "用户支付币种")
    private String payerCurrency;

    @Schema(description = "银行类型")
    private String bankType;

    @Schema(description = "交易状态")
    private String tradeState;

    @Schema(description = "交易状态描述")
    private String tradeStateDesc;

    @Schema(description = "交易类型")
    private String tradeType;

    @Schema(description = "微信支付系统生成的订单号")
    private String transactionId;

    @Schema(description = "支付完成时间")
    private String successTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;


}