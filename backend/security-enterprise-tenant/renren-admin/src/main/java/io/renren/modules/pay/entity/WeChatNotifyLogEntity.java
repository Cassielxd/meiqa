/**
 * Copyright (c) 2021 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.pay.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 微信支付回调日志
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@TableName("tb_wechat_notify_log")
public class WeChatNotifyLogEntity implements Serializable {
    @TableId
    private Long id;
    /**
     * 订单号
     */
    private String outTradeNo;
    /**
     * 订单总金额，单位为分
     */
    private Integer total;
    /**
     * 用户支付金额，单位为分
     */
    private Integer payerTotal;
    /**
     * CNY：人民币，境内商户号仅支持人民币
     */
    private String currency;
    /**
     * 用户支付币种
     */
    private String payerCurrency;
    /**
     * 银行类型
     */
    private String bankType;
    /**
     * 交易状态
     * SUCCESS：支付成功
     * REFUND：转入退款
     * NOTPAY：未支付
     * CLOSED：已关闭
     * REVOKED：已撤销（付款码支付）
     * USERPAYING：用户支付中（付款码支付）
     * PAYERROR：支付失败(其他原因，如银行返回失败)
     */
    private String tradeState;
    /**
     * 交易状态描述
     */
    private String tradeStateDesc;
    /**
     * 交易类型
     * JSAPI：公众号支付
     * NATIVE：扫码支付
     * App：App支付
     * MICROPAY：付款码支付
     * MWEB：H5支付
     * FACEPAY：刷脸支付
     */
    private String tradeType;
    /**
     * 微信支付系统生成的订单号
     */
    private String transactionId;
    /**
     * 支付完成时间
     */
    private String successTime;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;
}