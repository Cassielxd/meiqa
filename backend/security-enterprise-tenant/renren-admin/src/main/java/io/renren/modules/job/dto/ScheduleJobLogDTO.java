/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.job.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 定时任务日志
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Data
@Schema(description = "定时任务日志")
public class ScheduleJobLogDTO implements Serializable {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "任务id")
    private Long jobId;

    @Schema(description = "spring bean名称")
    private String beanName;

    @Schema(description = "参数")
    private String params;

    @Schema(description = "任务状态    0：失败    1：成功")
    private Integer status;

    @Schema(description = "失败信息")
    private String error;

    @Schema(description = "耗时(单位：毫秒)")
    private Integer times;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

}
