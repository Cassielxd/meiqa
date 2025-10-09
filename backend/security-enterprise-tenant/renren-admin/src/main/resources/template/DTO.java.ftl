package ${package}.modules.${moduleName}.dto<#if subModuleName??>.${subModuleName}</#if>;

import com.fasterxml.jackson.annotation.JsonFormat;
import ${package}.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
<#list imports as i>
import ${i!};
</#list>

/**
* ${tableComment}
*
* @author ${author} ${email}
* @since ${version} ${date}
*/
@Data
@Schema(description = "${tableComment}")
public class ${ClassName}DTO implements Serializable {
    private static final long serialVersionUID = 1L;

<#list columnList as column>
    <#if column.comment!?length gt 0>
    @Schema(description = "${column.comment}")
    </#if>
    <#if column.formType == 'date'>
    @JsonFormat(pattern = DateUtils.DATE_PATTERN)
    </#if>
    <#if column.formType == 'datetime'>
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    </#if>
    private ${column.attrType} ${column.attrName};
</#list>

}