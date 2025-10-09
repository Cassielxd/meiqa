package ${package}.modules.${moduleName}.excel<#if subModuleName??>.${subModuleName}</#if>;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
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
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ${ClassName}Excel {
<#assign index = 0>
<#list columnList as column>
<#if column.list>
    <#if column.comment!?length gt 0>
    @ExcelProperty(value = "${column.comment}", index = ${index})
    <#else>
    @ExcelProperty(value = "${column.attrType}", index = ${index})
    </#if>
    private ${column.attrType} ${column.attrName};
    <#assign index = index+1>
</#if>    
</#list>
}