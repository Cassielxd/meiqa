package io.renren.crmchat.common.constant;

import java.util.Arrays;

/**
 * 租户状态枚举，与 PHP 端保持一致。
 */
public enum TenantStatus {

    PENDING(0, "待审核"),
    APPROVED(1, "已批准"),
    REJECTED(2, "已拒绝"),
    DISABLED(3, "已禁用");

    private final int code;
    private final String label;

    TenantStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static TenantStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElse(null);
    }
}
