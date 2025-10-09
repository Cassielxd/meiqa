package io.renren.modules.devtools.entity;

import lombok.Data;

/**
 * 项目名修改
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
public class ProjectEntity {
    /**
     * 原项目路径
     */
    private String projectPath;

    /**
     * 原项目名称
     */
    private String projectName;

    /**
     * 新项目名称
     */
    private String newProjectName;

    /**
     * 新项目包名
     */
    private String newProjectPackage;

    /**
     * 新项目标识
     */
    private String newProjectCode;

}
