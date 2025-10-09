/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.message.sms;

import io.renren.common.validator.group.AliyunGroup;
import io.renren.common.validator.group.QcloudGroup;
import io.renren.common.validator.group.QiniuGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 短信配置信息
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "短信配置信息")
public class SmsConfig implements Serializable {


    @Schema(description = "阿里云AccessKeyId")
    @NotBlank(message = "{aliyun.accesskeyid.require}", groups = AliyunGroup.class)
    private String aliyunAccessKeyId;

    @Schema(description = "阿里云AccessKeySecret")
    @NotBlank(message = "{aliyun.accesskeysecret.require}", groups = AliyunGroup.class)
    private String aliyunAccessKeySecret;

    @Schema(description = "阿里云短信签名")
    @NotBlank(message = "{aliyun.signname.require}", groups = AliyunGroup.class)
    private String aliyunSignName;

    @Schema(description = "阿里云短信模板")
    @NotBlank(message = "{aliyun.templatecode.require}", groups = AliyunGroup.class)
    private String aliyunTemplateCode;

    @Schema(description = "腾讯云AppId")
    @NotNull(message = "{qcloud.appid.require}", groups = QcloudGroup.class)
    private Integer qcloudAppId;

    @Schema(description = "腾讯云AppKey")
    @NotBlank(message = "{qcloud.appkey.require}", groups = QcloudGroup.class)
    private String qcloudAppKey;

    @Schema(description = "腾讯云短信签名")
    @NotBlank(message = "{qcloud.signname.require}", groups = QcloudGroup.class)
    private String qcloudSignName;

    @Schema(description = "腾讯云短信模板ID")
    @NotBlank(message = "{qcloud.templateid.require}", groups = QcloudGroup.class)
    private String qcloudTemplateId;

    @Schema(description = "七牛accesskey")
    @NotNull(message = "{qiniu.accesskey.require}", groups = QiniuGroup.class)
    private String qiniuAccessKey;

    @Schema(description = "七牛SecretKey")
    @NotBlank(message = "{qiniu.secretkey.require}", groups = QiniuGroup.class)
    private String qiniuSecretKey;

    @Schema(description = "七牛短信模板ID")
    @NotBlank(message = "{qiniu.templateId.require}", groups = QiniuGroup.class)
    private String qiniuTemplateId;
}