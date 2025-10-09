package io.renren.modules.tenant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 租户数据源
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "租户数据源")
public class SysTenantDataSourceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "驱动")
    private String driverClassName;
    @Schema(description = "URL")
    private String url;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "密码")
    private String password;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;
}