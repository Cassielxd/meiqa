/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */
package io.renren.modules.devtools.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 代码生成参数配置
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
public class GenParam implements Serializable {
    private String packageName;
    private String version;
    private String author;
    private String email;
    private String backendPath;
    private String frontendPath;
}
