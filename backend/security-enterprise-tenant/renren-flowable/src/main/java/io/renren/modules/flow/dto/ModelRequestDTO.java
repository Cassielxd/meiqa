/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.flow.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;


/**
 * flowable请求模型
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Tag(name = "flowable请求模型")
public class ModelRequestDTO {

    @Schema(description = "id")
    private String id;

    @Schema(description = "流程key")
    private String key;

    @Schema(description = "流程名称")
    private String name;

    @Schema(description = "BPMN XML")
    private String bpmnXml;

    @Schema(description = "表单类型  0：流程表单  1：自定义表单")
    private String formType;

    @Schema(description = "表单ID")
    private String formId;

    @Schema(description = "表单名称")
    private String formName;
}
