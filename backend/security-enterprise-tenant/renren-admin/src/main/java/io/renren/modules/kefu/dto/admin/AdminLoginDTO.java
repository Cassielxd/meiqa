package io.renren.modules.kefu.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员登录请求DTO
 */
@Data
@Schema(description = "管理员登录请求")
public class AdminLoginDTO {

    @Schema(description = "账号")
    private String account;

    @Schema(description = "密码")
    private String pwd;

    @Schema(description = "图片验证码(可选)")
    private String imgcode;

    @Schema(description = "滑块验证(可选)")
    private String captchaVerification;

    @Schema(description = "验证码类型(可选)")
    private String captchaType;
}
