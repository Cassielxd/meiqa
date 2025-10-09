/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.log.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 操作日志
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Data
@Schema(description = "操作日志")
public class SysLogOperationDTO implements Serializable {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "用户操作")
    private String operation;

    @Schema(description = "请求URI")
    private String requestUri;

    @Schema(description = "请求方式")
    private String requestMethod;

    @Schema(description = "请求参数")
    private String requestParams;

    @Schema(description = "请求时长(毫秒)")
    private Integer requestTime;

    @Schema(description = "用户代理")
    private String userAgent;

    @Schema(description = "操作IP")
    private String ip;

    @Schema(description = "状态  0：失败   1：成功")
    private Integer status;

    @Schema(description = "用户名")
    private String creatorName;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

}