package io.renren.modules.mp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 公众号账号管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "公众号账号管理")
public class MpAccountDTO implements Serializable {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "AppID")
    private String appId;

    @Schema(description = "AppSecret")
    private String appSecret;

    @Schema(description = "Token")
    private String token;

    @Schema(description = "EncodingAESKey")
    private String aesKey;

    @Schema(description = "创建者")
    private Long creator;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;
}