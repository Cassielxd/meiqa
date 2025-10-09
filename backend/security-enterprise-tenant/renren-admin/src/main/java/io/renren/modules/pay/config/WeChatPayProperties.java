package io.renren.modules.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Data
@Component
@PropertySource("classpath:/wechat.properties")
@ConfigurationProperties(prefix = "wechat")
public class WeChatPayProperties {
    // 应用ID
    private String appId;
    // 商户号
    private String mchId;
    // 商户密钥
    private String mchKey;
    // apiclient_key.pem 证书路径
    private String keyPath;
    // apiclient_cert.pem 证书序列号  https://myssl.com/cert_decode.html
    private String serialNumber;
    // 通知地址
    private String notifyUrl;
}