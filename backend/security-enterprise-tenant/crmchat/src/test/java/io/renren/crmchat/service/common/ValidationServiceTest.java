package io.renren.crmchat.service.common;

import io.renren.crmchat.exception.CrmChatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ValidationService 单元测试
 * 测试覆盖率目标: 100%
 *
 * @author CRMChat Team
 */
class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    // ==================== 密码匹配验证测试 ====================

    @Test
    void testValidatePasswordMatch_Success() {
        assertDoesNotThrow(() ->
            validationService.validatePasswordMatch("password123", "password123")
        );
    }

    @Test
    void testValidatePasswordMatch_NotMatch() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validatePasswordMatch("password123", "password456")
        );
        assertEquals("两次输入的密码不一致", exception.getMessage());
    }

    @Test
    void testValidatePasswordMatch_NullPassword() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validatePasswordMatch(null, "password123")
        );
        assertEquals("密码不能为空", exception.getMessage());
    }

    @Test
    void testValidatePasswordMatch_CustomErrorMsg() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validatePasswordMatch("pwd1", "pwd2", "自定义错误消息")
        );
        assertEquals("自定义错误消息", exception.getMessage());
    }

    // ==================== 邮箱验证测试 ====================

    @Test
    void testValidateEmail_Success() {
        assertDoesNotThrow(() -> validationService.validateEmail("test@example.com"));
        assertDoesNotThrow(() -> validationService.validateEmail("user.name+tag@example.co.uk"));
        assertDoesNotThrow(() -> validationService.validateEmail("admin@domain.org"));
    }

    @Test
    void testValidateEmail_InvalidFormat() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validateEmail("invalid-email")
        );
        assertEquals("邮箱格式不正确", exception.getMessage());
    }

    @Test
    void testValidateEmail_Empty() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validateEmail("")
        );
        assertEquals("邮箱不能为空", exception.getMessage());
    }

    @Test
    void testValidateEmail_Null() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validateEmail(null)
        );
        assertEquals("邮箱不能为空", exception.getMessage());
    }

    @Test
    void testValidateEmail_CustomErrorMsg() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validateEmail("bad-email", "自定义邮箱错误")
        );
        assertEquals("自定义邮箱错误", exception.getMessage());
    }

    // ==================== 手机号验证测试 ====================

    @Test
    void testValidatePhone_Success() {
        assertDoesNotThrow(() -> validationService.validatePhone("13800138000"));
        assertDoesNotThrow(() -> validationService.validatePhone("15912345678"));
        assertDoesNotThrow(() -> validationService.validatePhone("18591992168"));
    }

    @Test
    void testValidatePhone_InvalidFormat() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validatePhone("12345678901") // 不是1开头的有效号段
        );
        assertEquals("手机号格式不正确", exception.getMessage());
    }

    @Test
    void testValidatePhone_TooShort() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validatePhone("1380013800")
        );
        assertEquals("手机号格式不正确", exception.getMessage());
    }

    @Test
    void testValidatePhone_Empty() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validatePhone("")
        );
        assertEquals("手机号不能为空", exception.getMessage());
    }

    @Test
    void testValidatePhone_CustomErrorMsg() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validatePhone("bad-phone", "自定义手机错误")
        );
        assertEquals("自定义手机错误", exception.getMessage());
    }

    // ==================== 租户权限验证测试 ====================

    @Test
    void testValidateTenantAccess_Admin() {
        // 管理员（appid=10000）可以访问所有租户数据
        assertDoesNotThrow(() ->
            validationService.validateTenantAccess("10000", "202509251953426878")
        );
    }

    @Test
    void testValidateTenantAccess_SameTenant() {
        // 同一租户可以访问
        assertDoesNotThrow(() ->
            validationService.validateTenantAccess("202509251953426878", "202509251953426878")
        );
    }

    @Test
    void testValidateTenantAccess_DifferentTenant() {
        // 不同租户无法访问
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validateTenantAccess("202509251953426878", "202116257358989495")
        );
        assertEquals("无权访问该资源", exception.getMessage());
    }

    @Test
    void testValidateTenantAccess_NullRequestAppId() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validateTenantAccess(null, "202509251953426878")
        );
        assertEquals("无权访问该资源", exception.getMessage());
    }

    // ==================== 非空验证测试 ====================

    @Test
    void testValidateNotEmpty_Success() {
        assertDoesNotThrow(() -> validationService.validateNotEmpty("value", "字段"));
    }

    @Test
    void testValidateNotEmpty_Null() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validateNotEmpty(null, "用户名")
        );
        assertEquals("用户名不能为空", exception.getMessage());
    }

    @Test
    void testValidateNotEmpty_Empty() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validateNotEmpty("", "密码")
        );
        assertEquals("密码不能为空", exception.getMessage());
    }

    // ==================== 长度验证测试 ====================

    @Test
    void testValidateLength_Success() {
        assertDoesNotThrow(() -> validationService.validateLength("admin", "用户名", 3, 20));
    }

    @Test
    void testValidateLength_TooShort() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validateLength("ab", "用户名", 3, 20)
        );
        assertEquals("用户名长度必须在 3-20 个字符之间", exception.getMessage());
    }

    @Test
    void testValidateLength_TooLong() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validateLength("a".repeat(21), "用户名", 3, 20)
        );
        assertEquals("用户名长度必须在 3-20 个字符之间", exception.getMessage());
    }

    @Test
    void testValidateLength_Null() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validateLength(null, "昵称", 1, 50)
        );
        assertEquals("昵称不能为空", exception.getMessage());
    }

    // ==================== 数值范围验证测试 ====================

    @Test
    void testValidateRange_Success() {
        assertDoesNotThrow(() -> validationService.validateRange(50, "年龄", 1, 100));
    }

    @Test
    void testValidateRange_TooSmall() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validateRange(0, "年龄", 1, 100)
        );
        assertEquals("年龄必须在 1-100 之间", exception.getMessage());
    }

    @Test
    void testValidateRange_TooLarge() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validateRange(101, "年龄", 1, 100)
        );
        assertEquals("年龄必须在 1-100 之间", exception.getMessage());
    }

    @Test
    void testValidateRange_Null() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validateRange(null, "数量", 1, 999)
        );
        assertEquals("数量不能为空", exception.getMessage());
    }

    // ==================== 密码强度验证测试 ====================

    @Test
    void testValidatePasswordStrength_Success() {
        assertDoesNotThrow(() -> validationService.validatePasswordStrength("password123"));
        assertDoesNotThrow(() -> validationService.validatePasswordStrength("Admin@2024"));
    }

    @Test
    void testValidatePasswordStrength_TooShort() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validatePasswordStrength("pwd123")
        );
        assertEquals("密码长度至少为8位", exception.getMessage());
    }

    @Test
    void testValidatePasswordStrength_NoLetter() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validatePasswordStrength("12345678")
        );
        assertEquals("密码必须包含字母和数字", exception.getMessage());
    }

    @Test
    void testValidatePasswordStrength_NoDigit() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validatePasswordStrength("password")
        );
        assertEquals("密码必须包含字母和数字", exception.getMessage());
    }

    @Test
    void testValidatePasswordStrength_Null() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validatePasswordStrength(null)
        );
        assertEquals("密码长度至少为8位", exception.getMessage());
    }

    // ==================== 状态值验证测试 ====================

    @Test
    void testValidateStatus_Success() {
        assertDoesNotThrow(() -> validationService.validateStatus(0, "状态"));
        assertDoesNotThrow(() -> validationService.validateStatus(1, "状态"));
    }

    @Test
    void testValidateStatus_InvalidValue() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validateStatus(2, "状态")
        );
        assertEquals("状态只能为0或1", exception.getMessage());
    }

    @Test
    void testValidateStatus_Null() {
        CrmChatException exception = assertThrows(CrmChatException.class, () ->
            validationService.validateStatus(null, "在线状态")
        );
        assertEquals("在线状态不能为空", exception.getMessage());
    }
}
