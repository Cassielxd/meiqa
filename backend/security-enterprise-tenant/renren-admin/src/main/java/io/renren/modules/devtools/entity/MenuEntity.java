/**
 * Copyright (c) 2020 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */
package io.renren.modules.devtools.entity;

import lombok.Data;

/**
 * 创建菜单
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
public class MenuEntity {
    private Long pid;
    private String name;
    private String icon;
    private String moduleName;
    private String className;

}
