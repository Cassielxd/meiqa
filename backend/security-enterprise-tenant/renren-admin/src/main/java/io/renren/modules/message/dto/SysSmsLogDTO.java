package io.renren.modules.message.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 短信日志
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "短信日志")
public class SysSmsLogDTO implements Serializable {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "短信编码")
    private String smsCode;

    @Schema(description = "平台类型")
    private Integer platform;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "参数1")
    private String params1;

    @Schema(description = "参数2")
    private String params2;

    @Schema(description = "参数3")
    private String params3;

    @Schema(description = "参数4")
    private String params4;

    @Schema(description = "发送状态  0：失败  1：成功")
    private Integer status;

    @Schema(description = "创建者")
    private Long creator;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;


}