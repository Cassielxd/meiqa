/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录表单
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "登录表单")
public class LoginDTO implements Serializable {

    @Schema(description = "用户名", required = true)
    @NotBlank(message = "{sysuser.username.require}")
    private String username;

    @Schema(description = "密码")
    @NotBlank(message = "{sysuser.password.require}")
    private String password;

    @Schema(description = "验证码")
    @NotBlank(message = "{sysuser.captcha.require}")
    private String captcha;

    @Schema(description = "唯一标识")
    @NotBlank(message = "{sysuser.uuid.require}")
    private String uuid;

}