package io.renren.crmchat.dto.tenant;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 租户注册请求DTO，与 PHP 表单字段保持一致。
 */
@Data
@Schema(description = "Tenant Registration Request")
public class TenantRegisterDTO implements Serializable {

    @Schema(description = "Login Account (Email Recommended)", example = "tenant@example.com")
    @NotBlank(message = "Please enter account")
    @Email(message = "Please enter a valid email address")
    private String account;

    @Schema(description = "Tenant Name", example = "Test Tenant")
    private String tenantName;

    @Schema(description = "Contact Name", example = "张三")
    private String contactName;

    @Schema(description = "Contact Phone", example = "13800138000")
    @Size(max = 20, message = "Contact phone length cannot exceed 20 characters")
    private String contactPhone;

    @Schema(description = "Contact Email (Optional if Same as Account)", example = "tenant@example.com")
    @Email(message = "Please enter a valid email address")
    @JsonProperty("contact_email")
    private String contactEmail;

    @Schema(description = "验证码", example = "123456", required = true)
    @NotBlank(message = "Please enter verification code")
    @Size(min = 6, max = 6, message = "Verification code length error")
    private String captcha;

    @Schema(description = "密码", example = "123456", required = true)
    @NotBlank(message = "Please enter password")
    @Size(min = 6, max = 32, message = "Password length must be between 6-32 characters")
    private String pwd;

    @Schema(description = "Confirm Password", example = "123456", required = true)
    @NotBlank(message = "Please enter confirmation password")
    @JsonProperty("confirm_pwd")
    private String confirmPwd;
}
