/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.devtools.utils;

import lombok.Data;

/**
 * 模板管理
 *
 * @author Mark sunlightcs@gmail.com
 */

@Data
public class TemplateContent {
    /**
     * 模板名称
     */
    private String name;
    /**
     * 模板内容
     */
    private String content;
    /**
     * 生成代码的路径
     */
    private String path;

}