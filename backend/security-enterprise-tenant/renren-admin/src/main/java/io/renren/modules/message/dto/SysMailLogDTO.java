/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.message.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.renren.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 邮件发送记录
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "邮件发送记录")
public class SysMailLogDTO implements Serializable {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "邮件模板ID")
    private Long templateId;

    @Schema(description = "发送者")
    private String mailFrom;

    @Schema(description = "收件人")
    private String mailTo;

    @Schema(description = "抄送者")
    private String mailCc;

    @Schema(description = "邮件主题")
    private String subject;

    @Schema(description = "邮件正文")
    private String content;

    @Schema(description = "发送状态  0：失败  1：成功")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

}
