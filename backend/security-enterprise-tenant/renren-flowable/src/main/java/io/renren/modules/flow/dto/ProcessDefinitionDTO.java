package io.renren.modules.flow.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProcessDefinitionDTO {
    @Schema(description = "id")
    private String id;
    @Schema(description = "是否挂起")
    private boolean isSuspended;
}
