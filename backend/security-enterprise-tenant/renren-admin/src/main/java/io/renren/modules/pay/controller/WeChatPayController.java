/**
 * Copyright (c) 2021 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */
package io.renren.modules.pay.controller;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.exception.ValidationException;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.partnerpayments.nativepay.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.core.notification.RequestParam;

import io.renren.common.constant.Constant;
import io.renren.common.exception.RenException;
import io.renren.common.utils.JsonUtils;
import io.renren.common.utils.Result;
import io.renren.modules.pay.config.WeChatPayProperties;
import io.renren.modules.pay.dto.WeChatNotifyLogDTO;
import io.renren.modules.pay.entity.OrderEntity;
import io.renren.modules.pay.service.OrderService;
import io.renren.modules.pay.service.WeChatNotifyLogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 微信支付
 *
 * @author Mark sunlightcs@gmail.com
 */
@AllArgsConstructor
@RestController
@Slf4j
@RequestMapping("pay/wechat")
public class WeChatPayController {
    private final OrderService orderService;
    private final WeChatPayProperties properties;
    private final WeChatNotifyLogService weChatNotifyLogService;

    /**
     * 生成支付二维码
     *
     * @param orderId 订单id
     * @return 二维码url
     */
    @RequestMapping("/nativePay")
    public Result<String> nativePay(Long orderId) {
        OrderEntity order = orderService.getByOrderId(orderId);
        if (order == null) {
            throw new RenException("Order does not exist");
        }

        if (order.getStatus() != Constant.OrderStatus.WAITING.getValue()) {
            throw new RenException("Order has expired");
        }

        // 使用自动更新平台证书的RSA配置
        Config config =
                new RSAAutoCertificateConfig.Builder()
                        .merchantId(properties.getMchId())
                        .privateKeyFromPath(properties.getKeyPath())
                        .merchantSerialNumber(properties.getSerialNumber())
                        .apiV3Key(properties.getMchKey())
                        .build();

        // 订单金额，单位为分
        Amount amount = new Amount();
        amount.setTotal(order.getPayAmount().multiply(new BigDecimal(100)).intValue());

        // 构建service
        NativePayService service = new NativePayService.Builder().config(config).build();
        PrepayRequest request = new PrepayRequest();
        request.setAmount(amount);
        request.setAppid(properties.getAppId());
        request.setMchid(properties.getMchId());
        request.setDescription(order.getProductName());
        request.setNotifyUrl(properties.getNotifyUrl());
        request.setOutTradeNo(order.getOrderId() + "");


        try {
            // 调用下单方法，得到应答
            PrepayResponse response = service.prepay(request);
            // 使用微信扫描 code_url 对应的二维码，即可体验Native支付
            String codeUrl = response.getCodeUrl();
            return new Result<String>().ok(codeUrl);
        } catch (ServiceException e) {
            log.error("微信支付下单失败", e);

            return new Result<String>().error(e.getErrorMessage());
        }

    }

    @PostMapping(value = "notify_url")
    public ResponseEntity.BodyBuilder wxAppPayNotify(@RequestHeader("Wechatpay-Serial") String serialNumber,
                                                     @RequestHeader("Wechatpay-Signature") String signature,
                                                     @RequestHeader("Wechatpay-Timestamp") String timestamp,
                                                     @RequestHeader("Wechatpay-Nonce") String nonce,
                                                     @RequestBody String body) {

        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(serialNumber)
                .nonce(nonce)
                .signature(signature)
                .timestamp(timestamp)
                .body(body)
                .build();

        // 如果已经初始化了 RSAAutoCertificateConfig，可直接使用
        // 没有的话，则构造一个
        NotificationConfig config = new RSAAutoCertificateConfig.Builder()
                .merchantId(properties.getMchId())
                .privateKeyFromPath(properties.getKeyPath())
                .merchantSerialNumber(properties.getSerialNumber())
                .apiV3Key(properties.getMchKey())
                .build();

        NotificationParser parser = new NotificationParser(config);

        try {
            // 以支付通知回调为例，验签、解密并转换成 Transaction
            Transaction transaction = parser.parse(requestParam, Transaction.class);
            log.info("支付通知：{}", JsonUtils.toJsonString(transaction));

            // 保存通知记录
            WeChatNotifyLogDTO weChatNotifyLog = new WeChatNotifyLogDTO();
            weChatNotifyLog.setTotal(transaction.getAmount().getTotal());
            weChatNotifyLog.setPayerTotal(transaction.getAmount().getPayerTotal());
            weChatNotifyLog.setCurrency(transaction.getAmount().getCurrency());
            weChatNotifyLog.setPayerCurrency(transaction.getAmount().getPayerCurrency());
            weChatNotifyLog.setOutTradeNo(transaction.getOutTradeNo());
            weChatNotifyLog.setTransactionId(transaction.getTransactionId());
            weChatNotifyLog.setBankType(transaction.getBankType());
            weChatNotifyLog.setTradeState(transaction.getTradeState().name());
            weChatNotifyLog.setTradeStateDesc(transaction.getTradeStateDesc());
            weChatNotifyLog.setSuccessTime(transaction.getSuccessTime());
            weChatNotifyLog.setTradeType(transaction.getTradeType().name());

            weChatNotifyLogService.save(weChatNotifyLog);

            //查询订单信息
            OrderEntity order = orderService.getByOrderId(Long.parseLong(weChatNotifyLog.getOutTradeNo()));
            //重复通知，不再处理
            if (order.getStatus() == Constant.OrderStatus.FINISH.getValue()) {
                return ResponseEntity.status(HttpStatus.OK);
            }

            //处理业务逻辑
            orderService.paySuccess(order);
        } catch (ValidationException e) {
            // 签名验证失败，返回 401 UNAUTHORIZED 状态码
            log.error("sign verification failed: {0}", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED);
        }


        return ResponseEntity.status(HttpStatus.OK);
    }


}
