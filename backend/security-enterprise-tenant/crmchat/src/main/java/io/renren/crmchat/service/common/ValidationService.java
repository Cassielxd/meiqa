package io.renren.crmchat.service.common;

import io.renren.crmchat.exception.CrmChatException;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * 统一验证服务
 * 提供密码、邮箱、手机号、租户权限等公共验证逻辑
 *
 * 设计原则:
 * - Single Responsibility: 专注于验证逻辑
 * - DRY: 消除各 Service 中重复的验证代码
 *
 * @author CRMChat Team
 */
@Service
public class ValidationService {

    // 邮箱正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // 全球手机号允许的字符（可包含前导+、空格、连字符、括号）
    private static final Pattern PHONE_ALLOWED_CHARS_PATTERN = Pattern.compile(
            "^[+0-9\\s\\-()]{4,25}$"
    );

    /**
     * 验证密码匹配（用于注册、修改密码场景）
     *
     * @param password        密码
     * @param confirmPassword 确认密码
     * @param errorMsg        错误消息
     * @throws CrmChatException 密码不匹配时抛出
     */
    public void validatePasswordMatch(String password, String confirmPassword, String errorMsg) {
        if (password == null || confirmPassword == null) {
            throw new CrmChatException(errorMsg != null ? errorMsg : "Password cannot be empty");
        }
        if (!password.equals(confirmPassword)) {
            throw new CrmChatException(errorMsg != null ? errorMsg : "Passwords do not match");
        }
    }

    /**
     * 验证密码匹配（使用默认错误消息）
     *
     * @param password        密码
     * @param confirmPassword 确认密码
     */
    public void validatePasswordMatch(String password, String confirmPassword) {
        validatePasswordMatch(password, confirmPassword, null);
    }

    /**
     * 验证邮箱格式
     *
     * @param email    邮箱地址
     * @param errorMsg 错误消息
     * @throws CrmChatException 邮箱格式不合法时抛出
     */
    public void validateEmail(String email, String errorMsg) {
        if (email == null || email.trim().isEmpty()) {
            throw new CrmChatException(errorMsg != null ? errorMsg : "Email cannot be empty");
        }
        String trimmedEmail = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new CrmChatException(errorMsg != null ? errorMsg : "Invalid email format");
        }
    }

    /**
     * 验证邮箱格式（使用默认错误消息）
     *
     * @param email 邮箱地址
     */
    public void validateEmail(String email) {
        validateEmail(email, null);
    }

    /**
     * 验证手机号格式（支持全球号码）
     *
     * @param phone    手机号
     * @param errorMsg 错误消息
     * @throws CrmChatException 手机号格式不合法时抛出
     */
    public void validatePhone(String phone, String errorMsg) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new CrmChatException(errorMsg != null ? errorMsg : "Phone number cannot be empty");
        }

        String trimmedPhone = phone.trim();

        if (!PHONE_ALLOWED_CHARS_PATTERN.matcher(trimmedPhone).matches()) {
            throw new CrmChatException(errorMsg != null ? errorMsg : "Invalid phone number format");
        }

        long plusCount = trimmedPhone.chars().filter(ch -> ch == '+').count();
        if (plusCount > 1 || (plusCount == 1 && trimmedPhone.indexOf('+') != 0)) {
            throw new CrmChatException(errorMsg != null ? errorMsg : "Invalid phone number format");
        }

        String normalized = trimmedPhone.replaceAll("[\\s\\-()]", "");
        if (normalized.startsWith("+")) {
            normalized = normalized.substring(1);
        }

        if (!normalized.matches("\\d{4,20}")) {
            throw new CrmChatException(errorMsg != null ? errorMsg : "Invalid phone number format");
        }
    }

    /**
     * 验证手机号格式（使用默认错误消息）
     *
     * @param phone 手机号
     */
    public void validatePhone(String phone) {
        validatePhone(phone, null);
    }

    /**
     * 验证租户访问权限（多租户隔离核心方法）
     * 确保用户只能访问自己租户的数据
     *
     * @param requestAppId  请求者的 appid（来自 Token）
     * @param resourceAppId 资源的 appid（来自数据库记录）
     * @throws CrmChatException 无权访问时抛出
     */
    public void validateTenantAccess(String requestAppId, String resourceAppId) {
        // 管理员（appid="10000"）可以访问所有租户数据
        if ("10000".equals(requestAppId)) {
            return;
        }

        // 普通租户只能访问自己的数据
        if (requestAppId == null || !requestAppId.equals(resourceAppId)) {
            throw new CrmChatException("No permission to access this resource");
        }
    }

    /**
     * 验证非空字段
     *
     * @param value    字段值
     * @param fieldName 字段名称（用于错误提示）
     * @throws CrmChatException 字段为空时抛出
     */
    public void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new CrmChatException(fieldName + " cannot be empty");
        }
    }

    /**
     * 验证字符串长度范围
     *
     * @param value     字段值
     * @param fieldName 字段名称
     * @param minLength 最小长度
     * @param maxLength 最大长度
     * @throws CrmChatException 长度不符合要求时抛出
     */
    public void validateLength(String value, String fieldName, int minLength, int maxLength) {
        if (value == null) {
            throw new CrmChatException(fieldName + " cannot be empty");
        }
        int length = value.length();
        if (length < minLength || length > maxLength) {
            throw new CrmChatException(
                    String.format("%s length must be between %d-%d characters", fieldName, minLength, maxLength)
            );
        }
    }

    /**
     * 验证数值范围
     *
     * @param value     数值
     * @param fieldName 字段名称
     * @param min       最小值
     * @param max       最大值
     * @throws CrmChatException 数值超出范围时抛出
     */
    public void validateRange(Integer value, String fieldName, int min, int max) {
        if (value == null) {
            throw new CrmChatException(fieldName + " cannot be empty");
        }
        if (value < min || value > max) {
            throw new CrmChatException(
                    String.format("%s must be between %d-%d", fieldName, min, max)
            );
        }
    }

    /**
     * 验证密码强度
     * 要求: 至少8位，包含字母和数字
     *
     * @param password 密码
     * @throws CrmChatException 密码强度不足时抛出
     */
    public void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new CrmChatException("Password must be at least 8 characters");
        }

        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");

        if (!hasLetter || !hasDigit) {
            throw new CrmChatException("Password must contain both letters and numbers");
        }
    }

    /**
     * 验证状态值（0或1）
     *
     * @param status    状态值
     * @param fieldName 字段名称
     * @throws CrmChatException 状态值不合法时抛出
     */
    public void validateStatus(Integer status, String fieldName) {
        if (status == null) {
            throw new CrmChatException(fieldName + " cannot be empty");
        }
        if (status != 0 && status != 1) {
            throw new CrmChatException(fieldName + " must be 0 or 1");
        }
    }
}
