package io.renren.crmchat.service.common;

import org.springframework.stereotype.Service;

/**
 * 邮件模板服务 - 单一职责：构建各种类型的邮件内容
 *
 * 职责：
 * - buildRegisterCaptchaEmail: 构建注册验证码邮件内容
 * - buildWelcomeEmail: 构建欢迎邮件内容
 * - buildPasswordResetEmail: 构建密码重置邮件内容
 * - buildNotificationEmail: 构建通知类邮件内容
 *
 * 注意：本服务只负责构建邮件内容，不负责发送（发送由EmailService负责）
 *
 * @author System Architect
 * @date 2025-10-13
 */
@Service
public class EmailTemplateService {

    /**
     * 构建注册验证码邮件内容（HTML格式）
     *
     * @param email 收件人邮箱
     * @param captcha 验证码
     * @param expireMinutes 验证码有效期（分钟）
     * @return HTML格式的邮件内容
     */
    public String buildRegisterCaptchaEmail(String email, String captcha, int expireMinutes) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Registration Verification Code</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f5f5f5;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f5f5f5; padding: 40px 20px;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center;">
                                            <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 600;">Verification Code</h1>
                                        </td>
                                    </tr>

                                    <!-- Body -->
                                    <tr>
                                        <td style="padding: 40px 30px;">
                                            <p style="color: #333333; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">Hello,</p>

                                            <p style="color: #666666; font-size: 14px; line-height: 1.6; margin: 0 0 30px 0;">
                                                You are receiving this email because you requested a verification code to register your account at CRMChat.
                                            </p>

                                            <div style="background-color: #f8f9fa; border-left: 4px solid #667eea; padding: 20px; margin: 0 0 30px 0; border-radius: 4px;">
                                                <p style="color: #666666; font-size: 14px; margin: 0 0 10px 0;">Your verification code is:</p>
                                                <p style="color: #667eea; font-size: 36px; font-weight: bold; margin: 0; letter-spacing: 8px; font-family: 'Courier New', monospace;">%s</p>
                                            </div>

                                            <p style="color: #999999; font-size: 13px; line-height: 1.6; margin: 0;">
                                                <strong>Important:</strong> This verification code will expire in <strong>%d minutes</strong>.
                                                If you did not request this code, please ignore this email.
                                            </p>
                                        </td>
                                    </tr>

                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #e9ecef;">
                                            <p style="color: #999999; font-size: 12px; margin: 0; line-height: 1.6;">
                                                This is an automated message, please do not reply.<br>
                                                © 2025 CRMChat. All rights reserved.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, captcha, expireMinutes);
    }

    /**
     * 构建欢迎邮件内容（HTML格式）
     *
     * @param tenantName 租户名称
     * @param accountEmail 账号邮箱
     * @return HTML格式的邮件内容
     */
    public String buildWelcomeEmail(String tenantName, String accountEmail) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Welcome to CRMChat</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f5f5f5;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f5f5f5; padding: 40px 20px;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center;">
                                            <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 600;">Welcome to CRMChat!</h1>
                                        </td>
                                    </tr>

                                    <!-- Body -->
                                    <tr>
                                        <td style="padding: 40px 30px;">
                                            <p style="color: #333333; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">Hello %s,</p>

                                            <p style="color: #666666; font-size: 14px; line-height: 1.6; margin: 0 0 20px 0;">
                                                Your registration has been successfully completed! We're excited to have you on board.
                                            </p>

                                            <p style="color: #666666; font-size: 14px; line-height: 1.6; margin: 0 0 20px 0;">
                                                <strong>Account Information:</strong><br>
                                                Email: %s
                                            </p>

                                            <p style="color: #999999; font-size: 13px; line-height: 1.6; margin: 0;">
                                                Your account is currently pending approval by our admin team. You will receive a notification once your account is approved.
                                            </p>
                                        </td>
                                    </tr>

                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #e9ecef;">
                                            <p style="color: #999999; font-size: 12px; margin: 0; line-height: 1.6;">
                                                © 2025 CRMChat. All rights reserved.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, tenantName, accountEmail);
    }

    /**
     * 构建密码重置邮件内容（HTML格式）
     *
     * @param resetLink 密码重置链接
     * @param expireMinutes 链接有效期（分钟）
     * @return HTML格式的邮件内容
     */
    public String buildPasswordResetEmail(String resetLink, int expireMinutes) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Password Reset Request</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f5f5f5;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f5f5f5; padding: 40px 20px;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center;">
                                            <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 600;">Password Reset</h1>
                                        </td>
                                    </tr>

                                    <!-- Body -->
                                    <tr>
                                        <td style="padding: 40px 30px;">
                                            <p style="color: #333333; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">Hello,</p>

                                            <p style="color: #666666; font-size: 14px; line-height: 1.6; margin: 0 0 30px 0;">
                                                We received a request to reset your password. Click the button below to reset it.
                                            </p>

                                            <div style="text-align: center; margin: 0 0 30px 0;">
                                                <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: #ffffff; text-decoration: none; padding: 15px 40px; border-radius: 8px; font-size: 16px; font-weight: 600;">Reset Password</a>
                                            </div>

                                            <p style="color: #999999; font-size: 13px; line-height: 1.6; margin: 0;">
                                                <strong>Important:</strong> This link will expire in <strong>%d minutes</strong>.
                                                If you did not request a password reset, please ignore this email.
                                            </p>
                                        </td>
                                    </tr>

                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #e9ecef;">
                                            <p style="color: #999999; font-size: 12px; margin: 0; line-height: 1.6;">
                                                This is an automated message, please do not reply.<br>
                                                © 2025 CRMChat. All rights reserved.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, resetLink, expireMinutes);
    }

    /**
     * 构建通知类邮件内容（HTML格式）
     *
     * @param title 通知标题
     * @param message 通知消息
     * @return HTML格式的邮件内容
     */
    public String buildNotificationEmail(String title, String message) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Notification</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f5f5f5;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f5f5f5; padding: 40px 20px;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center;">
                                            <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 600;">%s</h1>
                                        </td>
                                    </tr>

                                    <!-- Body -->
                                    <tr>
                                        <td style="padding: 40px 30px;">
                                            <p style="color: #666666; font-size: 14px; line-height: 1.6; margin: 0;">
                                                %s
                                            </p>
                                        </td>
                                    </tr>

                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #e9ecef;">
                                            <p style="color: #999999; font-size: 12px; margin: 0; line-height: 1.6;">
                                                © 2025 CRMChat. All rights reserved.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, title, message);
    }
}
