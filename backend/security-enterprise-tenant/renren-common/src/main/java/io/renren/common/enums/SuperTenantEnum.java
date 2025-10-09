/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.common.enums;

/**
 * 租户管理员枚举
 *
 * @author Mark sunlightcs@gmail.com
 */
public enum SuperTenantEnum {
    YES(1),
    NO(0);

    private final int value;

    SuperTenantEnum(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}