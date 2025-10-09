/**
 * Copyright (c) 2021 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */
package io.renren.modules.pay.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.ijpay.alipay.AliPayApi;
import com.ijpay.alipay.AliPayApiConfig;
import com.ijpay.alipay.AliPayApiConfigKit;
import io.renren.common.constant.Constant;
import io.renren.common.exception.RenException;
import io.renren.modules.pay.config.AliPayProperties;
import io.renren.modules.pay.dto.AlipayNotifyLogDTO;
import io.renren.modules.pay.entity.OrderEntity;
import io.renren.modules.pay.service.AlipayNotifyLogService;
import io.renren.modules.pay.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝支付
 *
 * @author Mark sunlightcs@gmail.com
 */
@AllArgsConstructor
@RestController
@RequestMapping("pay/alipay")
public class AliPayController extends AbstractAliPayApiController {
    private final AliPayProperties properties;
    private final OrderService orderService;
    private final AlipayNotifyLogService alipayNotifyLogService;

    @Override
    public AliPayApiConfig getApiConfig() throws AlipayApiException {
        AliPayApiConfig aliPayApiConfig;
        try {
            aliPayApiConfig = AliPayApiConfigKit.getApiConfig(properties.getAppId());
        } catch (Exception e) {
            aliPayApiConfig = AliPayApiConfig.builder()
                    .setAppId(properties.getAppId())
                    .setAliPayPublicKey(properties.getPublicKey())
                    .setAppCertPath(properties.getAppCertPath())
                    .setAliPayCertPath(properties.getAliPayCertPath())
                    .setAliPayRootCertPath(properties.getAliPayRootCertPath())
                    .setCharset("UTF-8")
                    .setPrivateKey(properties.getPrivateKey())
                    .setServiceUrl(properties.getServerUrl())
                    .setSignType("RSA2")
                    // 普通公钥方式
                    //.build();
                    // 证书模式
                    .buildByCert();
        }
        return aliPayApiConfig;
    }

    /**
     * Web支付
     */
    @RequestMapping(value = "/webPay")
    public void webPay(HttpServletResponse response, Long orderId) throws Exception {
        OrderEntity order = orderService.getByOrderId(orderId);
        if (order == null) {
            throw new RenException("Order does not exist");
        }

        if (order.getStatus() != Constant.OrderStatus.WAITING.getValue()) {
            throw new RenException("Order has expired");
        }

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(order.getOrderId() + "");
        //固定值
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        model.setTotalAmount(order.getPayAmount().toString());
        model.setSubject(order.getProductName());
        //公用回传参数，没有则无需设置
        //model.setPassbackParams("passback_params");

        AliPayApi.tradePage(response, model, properties.getNotifyUrl(), properties.getReturnUrl());
    }


    /**
     * 支付宝异步通知接口
     */
    @PostMapping("notify_url")
    public String notifyUrl(HttpServletRequest request) throws Exception {
        //支付宝异步通知内容
        Map<String, String> params = AliPayApi.toMap(request);

        //秘钥模式
        //boolean verifyResult = AlipaySignature.rsaCheckV1(params, properties.getPublicKey(), "UTF-8", "RSA2");
        //证书模式
        boolean verifyResult = AlipaySignature.rsaCertCheckV1(params, properties.getAliPayCertPath(), "UTF-8", "RSA2");

        //验签失败
        if (!verifyResult) {
            return "failure";
        }

        //保存异步通知记录
        AlipayNotifyLogDTO alipayNotifyLog = new AlipayNotifyLogDTO();
        alipayNotifyLog.setNotifyId(params.get("notify_id"));
        alipayNotifyLog.setBuyerId(params.get("buyer_id"));
        alipayNotifyLog.setSellerId(params.get("seller_id"));
        alipayNotifyLog.setTradeNo(params.get("trade_no"));
        alipayNotifyLog.setOutTradeNo(Long.parseLong(params.get("out_trade_no")));
        alipayNotifyLog.setTotalAmount(new BigDecimal(params.get("total_amount")));
        alipayNotifyLog.setBuyerPayAmount(new BigDecimal(params.get("buyer_pay_amount")));
        alipayNotifyLog.setReceiptAmount(new BigDecimal(params.get("receipt_amount")));
        alipayNotifyLog.setInvoiceAmount(new BigDecimal(params.get("invoice_amount")));
        alipayNotifyLog.setTradeStatus(params.get("trade_status"));
        alipayNotifyLogService.save(alipayNotifyLog);

        //查询订单信息
        OrderEntity order = orderService.getByOrderId(alipayNotifyLog.getOutTradeNo());
        //重复通知，不再处理
        if (order.getStatus() == Constant.OrderStatus.FINISH.getValue()) {
            return "success";
        }

        //处理业务逻辑
        orderService.paySuccess(order);

        return "success";
    }

}
