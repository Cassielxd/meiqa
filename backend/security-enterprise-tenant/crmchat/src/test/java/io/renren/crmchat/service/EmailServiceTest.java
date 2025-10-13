package io.renren.crmchat.service;

import io.renren.crmchat.service.common.EmailService;
import io.renren.crmchat.service.common.EmailTemplateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * EmailService 单元测试
 *
 * 注意：
 * - 测试前需要配置正确的SMTP邮箱信息（环境变量或application.yml）
 * - 测试会实际发送邮件，请使用测试邮箱
 * - @Disabled 注解表示默认不执行，需要时手动移除
 *
 * @author System Architect
 * @date 2025-10-13
 */
@SpringBootTest
public class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailTemplateService emailTemplateService;

    /**
     * 测试发送纯文本邮件
     */
    @Test
    @Disabled("需要配置SMTP才能运行此测试")
    public void testSendTextEmail() {
        String to = "test@example.com"; // 替换为真实测试邮箱
        String subject = "Test Text Email";
        String content = "This is a test email from CRMChat system.";

        emailService.sendTextEmail(to, subject, content);
        System.out.println("Text email sent successfully to: " + to);
    }

    /**
     * 测试发送HTML格式邮件
     */
    @Test
    @Disabled("需要配置SMTP才能运行此测试")
    public void testSendHtmlEmail() {
        String to = "test@example.com"; // 替换为真实测试邮箱
        String subject = "Test HTML Email";
        String htmlContent = """
                <html>
                <body>
                    <h1>Hello from CRMChat!</h1>
                    <p>This is a <strong>test</strong> HTML email.</p>
                </body>
                </html>
                """;

        emailService.sendHtmlEmail(to, subject, htmlContent);
        System.out.println("HTML email sent successfully to: " + to);
    }

    /**
     * 测试发送注册验证码邮件（完整流程）
     */
    @Test
    @Disabled("需要配置SMTP才能运行此测试")
    public void testSendRegisterCaptchaEmail() {
        String email = "test@example.com"; // 替换为真实测试邮箱
        String captcha = "123456";
        int expireMinutes = 10;

        // 1. 构建邮件内容（EmailTemplateService的职责）
        String emailContent = emailTemplateService.buildRegisterCaptchaEmail(email, captcha, expireMinutes);

        // 2. 发送邮件（EmailService的职责）
        emailService.sendHtmlEmail(email, "Registration Verification Code - CRMChat", emailContent);

        System.out.println("Verification code email sent successfully!");
        System.out.println("Email: " + email);
        System.out.println("Captcha: " + captcha);
    }

    /**
     * 测试发送欢迎邮件
     */
    @Test
    @Disabled("需要配置SMTP才能运行此测试")
    public void testSendWelcomeEmail() {
        String email = "test@example.com";
        String tenantName = "Test Company";

        String emailContent = emailTemplateService.buildWelcomeEmail(tenantName, email);
        emailService.sendHtmlEmail(email, "Welcome to CRMChat!", emailContent);

        System.out.println("Welcome email sent successfully to: " + email);
    }

    /**
     * 测试发送密码重置邮件
     */
    @Test
    @Disabled("需要配置SMTP才能运行此测试")
    public void testSendPasswordResetEmail() {
        String email = "test@example.com";
        String resetLink = "https://example.com/reset-password?token=abc123";
        int expireMinutes = 30;

        String emailContent = emailTemplateService.buildPasswordResetEmail(resetLink, expireMinutes);
        emailService.sendHtmlEmail(email, "Password Reset Request - CRMChat", emailContent);

        System.out.println("Password reset email sent successfully to: " + email);
    }
}
