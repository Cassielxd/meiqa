/**
 * Copyright (c) 2021 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */
package io.renren.modules.sys.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 岗位管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "岗位管理")
public class SysPostDTO implements Serializable {

    @Schema(description = "id")
    private Long id;
    @Schema(description = "岗位编码")
    private String postCode;
    @Schema(description = "岗位名称")
    private String postName;
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "租户编码")
    private Long tenantCode;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

}