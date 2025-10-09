/**
 * Copyright (c) 2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */
package io.renren.modules.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * 租户列表
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "租户列表")
public class SysTenantListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "租户编码")
    private Long tenantCode;

    @Schema(description = "租户名称")
    private String tenantName;

    @Schema(description = "租户域名")
    private String tenantDomain;

    @Schema(description = "租户模式  0：字段模式   1：数据源模式")
    private Integer tenantMode;

    @Schema(description = "数据源id")
    private Long datasourceId;

    @Schema(description = "状态  0：停用    1：正常")
    private Integer status;

}