package io.renren.modules.kefu.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录页信息VO
 */
@Data
@Schema(description = "登录页信息")
public class LoginInfoVO {

    @Schema(description = "系统名称")
    private String systemName;

    @Schema(description = "Logo URL")
    private String logoUrl;

    @Schema(description = "是否启用验证码")
    private Boolean captchaEnabled;

    @Schema(description = "验证码类型 captcha:图片验证码 ajcaptcha:滑块验证码")
    private String captchaType;

    @Schema(description = "是否启用多租户")
    private Boolean tenantEnabled;

    @Schema(description = "背景图URL")
    private String backgroundUrl;
}
