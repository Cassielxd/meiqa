package io.renren.crmchat.service.common;

import io.renren.crmchat.exception.CrmChatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;

/**
 * 邮件发送服务 - 单一职责：纯邮件发送功能
 *
 * 职责：
 * - sendHtmlEmail: 发送HTML格式邮件
 * - sendTextEmail: 发送纯文本邮件
 *
 * 注意：本服务只负责发送，不负责邮件内容的构建（内容构建由EmailTemplateService负责）
 *
 * @author System Architect
 * @date 2025-10-13
 */
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@example.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 发送HTML格式邮件
     *
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param htmlContent HTML格式的邮件内容
     * @throws CrmChatException 发送失败时抛出异常
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true表示HTML格式

            mailSender.send(message);
            log.info("HTML email sent successfully to: {}, subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}, error: {}", to, e.getMessage(), e);
            throw new CrmChatException("Failed to send email: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while sending email to: {}, error: {}", to, e.getMessage(), e);
            throw new CrmChatException("Failed to send email: " + e.getMessage());
        }
    }

    /**
     * 发送纯文本邮件
     *
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param textContent 纯文本格式的邮件内容
     * @throws CrmChatException 发送失败时抛出异常
     */
    public void sendTextEmail(String to, String subject, String textContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textContent, false); // false表示纯文本格式

            mailSender.send(message);
            log.info("Text email sent successfully to: {}, subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send text email to: {}, error: {}", to, e.getMessage(), e);
            throw new CrmChatException("Failed to send email: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while sending email to: {}, error: {}", to, e.getMessage(), e);
            throw new CrmChatException("Failed to send email: " + e.getMessage());
        }
    }
}
