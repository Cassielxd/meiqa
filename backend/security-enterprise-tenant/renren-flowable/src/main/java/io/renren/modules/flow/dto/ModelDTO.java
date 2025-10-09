/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.flow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

import java.util.Date;
import java.util.Map;


/**
 * flowable模型
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Tag(name = "flowable模型")
public class ModelDTO {

    @Schema(description = "id")
    private String id;

    @Schema(description = "流程key")
    private String key;

    @Schema(description = "流程名称")
    private String name;

    @Schema(description = "部署ID")
    private String deploymentId;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date created;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date lastUpdated;

    @Schema(description = "流程分类")
    private String category;

    @Schema(description = "BPMN XML")
    private String bpmnXml;

    private Map<String, String> metaInfo;

    private ProcessDefinitionDTO processDefinition;

}
