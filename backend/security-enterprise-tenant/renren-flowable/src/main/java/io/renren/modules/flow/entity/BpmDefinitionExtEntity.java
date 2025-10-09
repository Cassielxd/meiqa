package io.renren.modules.flow.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.renren.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 流程定义扩展
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bpm_definition_ext")
public class BpmDefinitionExtEntity extends BaseEntity {

    /**
     * 模型ID
     */
    private String modelId;
    /**
     * 流程定义ID
     */
    private String processDefinitionId;
    /**
     * 表单类型
     */
    private String formType;
    /**
     * 表单ID
     */
    private String formId;
    /**
     * 表单内容
     */
    private String formContent;
    /**
     * 更新者
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;
}