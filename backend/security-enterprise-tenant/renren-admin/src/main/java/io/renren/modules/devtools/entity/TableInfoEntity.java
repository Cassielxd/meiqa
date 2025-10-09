/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.devtools.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.utils.DateUtils;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 表信息
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@TableName("gen_table_info")
public class TableInfoEntity {
    @TableId
    private Long id;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 实体类名称
     */
    private String className;
    /**
     * 功能名
     */
    private String tableComment;
    /**
     * 项目包名
     */
    private String packageName;
    /**
     * 项目版本号
     */
    private String version;
    /**
     * 作者
     */
    private String author;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 后端生成路径
     */
    private String backendPath;
    /**
     * 前端生成路径
     */
    private String frontendPath;
    /**
     * 模块名
     */
    private String moduleName;
    /**
     * 子模块名
     */
    private String subModuleName;
    /**
     * 数据源ID
     */
    private Long datasourceId;
    /**
     * 基类ID
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long baseclassId;
    /**
     * 时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;
    /**
     * 表字段
     */
    @TableField(exist = false)
    private List<TableFieldEntity> fields;
    /**
     * 主键
     */
    @TableField(exist = false)
    private TableFieldEntity pk;

}
