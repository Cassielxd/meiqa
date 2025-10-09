/**
 * /**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


/**
 * 登录表单
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "登录表单")
public class LoginDTO {
    @Schema(description = "手机号")
    @NotBlank(message = "Mobile number cannot be empty")
    private String mobile;

    @Schema(description = "密码")
    @NotBlank(message = "Password cannot be empty")
    private String password;

}