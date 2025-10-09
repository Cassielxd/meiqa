package io.renren.crmchat.service.common;

import io.renren.crmchat.exception.CrmChatException;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

/**
 * 密码服务 - 统一密码管理
 *
 * 职责：
 * 1. 密码验证（支持 PHP BCrypt $2y$ 格式）
 * 2. 密码加密（生成 BCrypt hash）
 * 3. 密码强度验证
 *
 * @author CRMChat Team
 */
@Service
public class PasswordService {

    /**
     * 验证密码是否匹配
     *
     * 特性：
     * - 兼容 PHP BCrypt 的 $2y$ 格式（自动转换为 $2a$）
     * - 安全的常量时间比较
     *
     * @param plainPassword 明文密码
     * @param hashedPassword 数据库中的加密密码
     * @return true=密码正确, false=密码错误
     */
    public boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        // PHP BCrypt 使用 $2y$，Java BCrypt 使用 $2a$
        // 需要转换才能正确验证
        String normalizedHash = normalizePhpBcryptHash(hashedPassword);

        return BCrypt.checkpw(plainPassword, normalizedHash);
    }

    /**
     * 验证密码并抛出异常（业务层常用）
     *
     * @param plainPassword 明文密码
     * @param hashedPassword 数据库中的加密密码
     * @param errorMessage 验证失败时的错误消息
     * @throws CrmChatException 密码验证失败时抛出
     */
    public void verifyOrThrow(String plainPassword, String hashedPassword, String errorMessage) {
        if (!verify(plainPassword, hashedPassword)) {
            throw new CrmChatException(errorMessage);
        }
    }

    /**
     * 加密密码（生成 BCrypt hash）
     *
     * 使用推荐的 workload factor：10
     *
     * @param plainPassword 明文密码
     * @return BCrypt 加密后的密码
     */
    public String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // BCrypt.gensalt() 默认使用 workload factor = 10
        // 生成格式：$2a$10$...
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * 使用指定的强度加密密码
     *
     * @param plainPassword 明文密码
     * @param logRounds BCrypt 的 workload factor (4-31)
     * @return BCrypt 加密后的密码
     */
    public String hash(String plainPassword, int logRounds) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (logRounds < 4 || logRounds > 31) {
            throw new IllegalArgumentException("logRounds must be between 4-31");
        }

        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(logRounds));
    }

    /**
     * 检查密码是否需要重新加密
     *
     * 使用场景：
     * - 升级密码强度（增加 workload factor）
     * - 密码策略变更
     *
     * @param hashedPassword 当前的加密密码
     * @param targetLogRounds 目标 workload factor
     * @return true=需要重新加密
     */
    public boolean needsRehash(String hashedPassword, int targetLogRounds) {
        if (hashedPassword == null || hashedPassword.length() < 7) {
            return true;
        }

        // 提取当前的 logRounds
        // BCrypt hash 格式: $2a$10$...
        //                         ^^
        String normalizedHash = normalizePhpBcryptHash(hashedPassword);

        try {
            String logRoundsPart = normalizedHash.substring(4, 6);
            int currentLogRounds = Integer.parseInt(logRoundsPart);
            return currentLogRounds < targetLogRounds;
        } catch (Exception e) {
            // 解析失败，建议重新加密
            return true;
        }
    }

    /**
     * 标准化 PHP BCrypt hash 格式
     *
     * 将 PHP 的 $2y$ 转换为 Java 的 $2a$
     *
     * @param hash BCrypt hash
     * @return 标准化后的 hash
     */
    private String normalizePhpBcryptHash(String hash) {
        if (hash == null) {
            return null;
        }

        // PHP BCrypt 使用 $2y$，Java BCrypt 使用 $2a$
        // 算法完全相同，只是标识符不同
        if (hash.startsWith("$2y$")) {
            return "$2a$" + hash.substring(4);
        }

        return hash;
    }

    /**
     * 验证密码强度（可选功能）
     *
     * 规则：
     * - 长度 >= 8
     * - 包含大写字母
     * - 包含小写字母
     * - 包含数字
     * - 可选：包含特殊字符
     *
     * @param password 待验证的密码
     * @return true=符合强度要求
     */
    public boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");

        return hasUpper && hasLower && hasDigit;
    }

    /**
     * 验证密码强度并返回详细信息
     *
     * @param password 待验证的密码
     * @return 强度验证结果描述
     */
    public String validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty";
        }

        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }

        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain uppercase letters";
        }

        if (!password.matches(".*[a-z].*")) {
            return "Password must contain lowercase letters";
        }

        if (!password.matches(".*\\d.*")) {
            return "Password must contain digits";
        }

        return "OK";
    }
}
