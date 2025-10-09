package io.renren.modules.kefu.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 验证码验证请求DTO
 */
@Data
@Schema(description = "验证码验证请求")
public class AjCheckDTO {

    @Schema(description = "验证码token")
    private String token;

    @Schema(description = "点选坐标JSON")
    private String pointJson;

    @Schema(description = "验证码类型")
    private String captchaType;
}
