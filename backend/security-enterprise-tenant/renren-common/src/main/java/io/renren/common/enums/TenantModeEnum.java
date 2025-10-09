/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.common.enums;

/**
 * 租户模式
 *
 * @author Mark sunlightcs@gmail.com
 */
public enum TenantModeEnum {
    /**
     * 字段模式
     */
    COLUMN(0),
    /**
     * 数据源模式
     */
    DATASOURCE(1);

    private final int value;

    TenantModeEnum(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
