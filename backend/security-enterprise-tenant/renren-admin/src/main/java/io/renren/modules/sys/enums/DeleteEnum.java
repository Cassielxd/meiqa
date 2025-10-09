/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.enums;

/**
 * 删除标记枚举
 *
 * @author Mark sunlightcs@gmail.com
 */
public enum DeleteEnum {
    /**
     * 是（删除）
     */
    YES(1),
    /**
     * 否（未删除）
     */
    NO(0);

    private int value;

    DeleteEnum(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
