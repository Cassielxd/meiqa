/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.oss.cloud;

import io.renren.common.validator.group.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;

/**
 * 云存储配置信息
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "云存储配置信息")
public class CloudStorageConfig implements Serializable {


    @Schema(description = "类型 1：七牛  2：阿里云  3：腾讯云   4：FastDFS   5：本地上传   6：MinIO")
    @Range(min = 1, max = 6, message = "{oss.type.range}")
    private Integer type;

    @Schema(description = "七牛绑定的域名")
    @NotBlank(message = "{qiniu.domain.require}", groups = QiniuGroup.class)
    @URL(message = "{qiniu.domain.url}", groups = QiniuGroup.class)
    private String qiniuDomain;

    @Schema(description = "七牛路径前缀")
    private String qiniuPrefix;

    @Schema(description = "七牛ACCESS_KEY")
    @NotBlank(message = "{qiniu.accesskey.require}", groups = QiniuGroup.class)
    private String qiniuAccessKey;

    @Schema(description = "七牛SECRET_KEY")
    @NotBlank(message = "{qiniu.secretkey.require}", groups = QiniuGroup.class)
    private String qiniuSecretKey;

    @Schema(description = "七牛存储空间名")
    @NotBlank(message = "{qiniu.bucketname.require}", groups = QiniuGroup.class)
    private String qiniuBucketName;

    @Schema(description = "阿里云绑定的域名")
    @NotBlank(message = "{aliyun.domain.require}", groups = AliyunGroup.class)
    @URL(message = "{aliyun.domain.url}", groups = AliyunGroup.class)
    private String aliyunDomain;

    @Schema(description = "阿里云路径前缀")
    private String aliyunPrefix;

    @Schema(description = "阿里云EndPoint")
    @NotBlank(message = "{aliyun.endPoint.require}", groups = AliyunGroup.class)
    private String aliyunEndPoint;

    @Schema(description = "阿里云AccessKeyId")
    @NotBlank(message = "{aliyun.accesskeyid.require}", groups = AliyunGroup.class)
    private String aliyunAccessKeyId;

    @Schema(description = "阿里云AccessKeySecret")
    @NotBlank(message = "{aliyun.accesskeysecret.require}", groups = AliyunGroup.class)
    private String aliyunAccessKeySecret;

    @Schema(description = "阿里云BucketName")
    @NotBlank(message = "{aliyun.bucketname.require}", groups = AliyunGroup.class)
    private String aliyunBucketName;

    @Schema(description = "腾讯云绑定的域名")
    @NotBlank(message = "{qcloud.domain.require}", groups = QcloudGroup.class)
    @URL(message = "{qcloud.domain.url}", groups = QcloudGroup.class)
    private String qcloudDomain;

    @Schema(description = "腾讯云路径前缀")
    private String qcloudPrefix;

    @Schema(description = "腾讯云AppId")
    @NotNull(message = "{qcloud.appid.require}", groups = QcloudGroup.class)
    private Integer qcloudAppId;

    @Schema(description = "腾讯云SecretId")
    @NotBlank(message = "{qcloud.secretId.require}", groups = QcloudGroup.class)
    private String qcloudSecretId;

    @Schema(description = "腾讯云SecretKey")
    @NotBlank(message = "{qcloud.secretkey.require}", groups = QcloudGroup.class)
    private String qcloudSecretKey;

    @Schema(description = "腾讯云BucketName")
    @NotBlank(message = "{qcloud.bucketname.require}", groups = QcloudGroup.class)
    private String qcloudBucketName;

    @Schema(description = "腾讯云COS所属地区")
    @NotBlank(message = "{qcloud.region.require}", groups = QcloudGroup.class)
    private String qcloudRegion;

    @Schema(description = "FastDFS绑定的域名")
    @NotBlank(message = "{fastdfs.domain.require}", groups = FastDFSGroup.class)
    @URL(message = "{fastdfs.domain.url}", groups = FastDFSGroup.class)
    private String fastdfsDomain;

    @Schema(description = "本地上传绑定的域名")
    @NotBlank(message = "{local.domain.require}", groups = LocalGroup.class)
    @URL(message = "{local.domain.url}", groups = LocalGroup.class)
    private String localDomain;

    @Schema(description = "本地上传路径前缀")
    private String localPrefix;

    @Schema(description = "本地上传存储目录")
    @NotBlank(message = "{local.path.url}", groups = LocalGroup.class)
    private String localPath;

    @Schema(description = "Minio EndPoint")
    @NotBlank(message = "{minio.endPoint.require}", groups = MinioGroup.class)
    private String minioEndPoint;

    @Schema(description = "accessKey")
    @NotBlank(message = "{minio.accesskey.require}", groups = MinioGroup.class)
    private String minioAccessKey;

    @Schema(description = "secretKey")
    @NotBlank(message = "{minio.secretkey.require}", groups = MinioGroup.class)
    private String minioSecretKey;

    @Schema(description = "BucketName")
    @NotBlank(message = "{minio.bucketname.require}", groups = MinioGroup.class)
    private String minioBucketName;

    @Schema(description = "MinIO上传路径前缀")
    private String minioPrefix;

}