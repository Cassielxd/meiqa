package io.renren.crmchat.service.common;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
/**
 * 验证码服务 - 邮箱/短信验证码
 *
 * 功能：
 * - sendCaptcha: 生成验证码并通过邮件发送（存入Redis）
 * - verifyCaptcha: 验证验证码是否正确
 * - clearCaptcha: 清除验证码缓存
 * - generateCaptcha: 生成6位数字验证码
 *
 * 单一职责原则应用：
 * - 本服务负责：生成验证码、存储验证码、验证验证码
 * - EmailService负责：发送邮件
 * - EmailTemplateService负责：构建邮件内容
 *
 * PHP参考: app/services/tenant/TenantServices.php::sendRegisterCaptcha()
 *
 * @author System Architect
 * @date 2025-10-02
 */
@Slf4j
@Service
public class CaptchaService {

    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    /**
     * 验证码缓存前缀
     */
    private static final String CAPTCHA_PREFIX = "tenant_reg_captcha_";

    /**
     * 频率限制缓存前缀
     */
    private static final String RATE_LIMIT_PREFIX = "tenant_reg_captcha_limit_";

    /**
     * 验证码长度
     */
    private static final int CAPTCHA_LENGTH = 6;

    /**
     * 验证码有效期（秒）
     */
    @Value("${crmchat.captcha.expire-seconds:600}")
    private long captchaExpireSeconds;

    /**
     * 发送频率限制（秒）- 同一邮箱60秒内只能发送一次
     */
    @Value("${crmchat.captcha.rate-limit-seconds:30}")
    private long rateLimitSeconds;
    public CaptchaService(StringRedisTemplate redisTemplate,
                          EmailService emailService,
                          EmailTemplateService emailTemplateService) {
        this.redisTemplate = redisTemplate;
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
    }

    /**
     * 发送验证码（生成验证码、存入Redis、并通过邮件发送）
     * 频率限制：同一邮箱60秒内只能发送一次
     *
     * @param email 邮箱地址
     * @return 验证码（开发环境返回用于测试，生产环境建议返回null或成功标志）
     */
    public String sendCaptcha(String email) {
        // 1. 验证邮箱格式
        if (email == null || (email = email.trim()).isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be empty");
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        String rateLimitKey = getRateLimitKey(email);
        String cacheKey = getCacheKey(email);

        // 2. 检查频率限制（60秒内不能重复发送）
        String limitFlag = redisTemplate.opsForValue().get(rateLimitKey);
        if (limitFlag != null) {
            // 获取剩余时间
            Long ttl = redisTemplate.getExpire(rateLimitKey, TimeUnit.SECONDS);
            String message = String.format("Verification code has been sent, please try again in %d seconds", ttl != null ? ttl : rateLimitSeconds);
            log.warn("Rate limit exceeded for email: {}, remaining: {}s", email, ttl);
            throw new io.renren.crmchat.exception.CrmChatException(message);
        }

        // 3. 使用 SETNX 原子操作设置频率限制（防止并发请求）
        Boolean rateLimitSet = redisTemplate.opsForValue().setIfAbsent(rateLimitKey, "1", rateLimitSeconds, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(rateLimitSet)) {
            // 并发情况下，另一个请求已经设置了频率限制
            log.warn("Concurrent request detected for email: {}, rate limit already set", email);
            throw new io.renren.crmchat.exception.CrmChatException("Verification code is being sent, please wait");
        }

        try {
            // 4. 删除旧验证码（确保只有一个有效验证码）
            redisTemplate.delete(cacheKey);

            // 5. 生成新验证码
            String captcha = generateCaptcha();

            // 6. 存入Redis，10分钟有效
            redisTemplate.opsForValue().set(cacheKey, captcha, captchaExpireSeconds, TimeUnit.SECONDS);

            // 7. 构建邮件内容（委托给EmailTemplateService）
            int expireMinutes = (int) (captchaExpireSeconds  / 60);
            String emailContent = emailTemplateService.buildRegisterCaptchaEmail(email, captcha, expireMinutes);

            // 8. 发送邮件（委托给EmailService）
            emailService.sendHtmlEmail(email, "Registration Verification Code - CRMChat", emailContent);
            log.info("Verification code email sent successfully to: {}", email);
            log.info("[DEV] Email verification code for {}: {}", email, captcha);

            // 开发环境返回验证码用于测试，生产环境建议隐藏此返回值
            return captcha;

        } catch (Exception e) {
            // 如果任何步骤失败，删除频率限制，允许用户立即重试
            redisTemplate.delete(rateLimitKey);
            log.error("Failed to send verification code to: {}, rate limit removed for retry. Error: {}", email, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 验证验证码
     *
     * @param email 邮箱地址
     * @param captcha 用户输入的验证码
     * @return true=验证成功, false=验证失败
     */
    public boolean verifyCaptcha(String email, String captcha) {
        String cacheKey = getCacheKey(email);
        String cachedCaptcha = redisTemplate.opsForValue().get(cacheKey);

        if (cachedCaptcha == null) {
            return false; // 验证码已过期或不存在
        }

        // 不区分大小写比较
        return cachedCaptcha.equalsIgnoreCase(captcha);
    }

    /**
     * 清除验证码缓存（验证成功后调用）
     *
     * @param email 邮箱地址
     */
    public void clearCaptcha(String email) {
        String cacheKey = getCacheKey(email);
        redisTemplate.delete(cacheKey);
    }

    /**
     * 生成6位数字验证码
     *
     * @return 验证码字符串
     */
    private String generateCaptcha() {
        SecureRandom random = new SecureRandom();
        int captchaNumber = random.nextInt(900000) + 100000; // 100000 ~ 999999
        return String.valueOf(captchaNumber);
    }

    /**
     * 获取验证码缓存Key（直接使用邮箱地址）
     *
     * @param email 邮箱地址
     * @return Redis缓存Key (格式: tenant_reg_captcha_邮箱)
     */
    private String getCacheKey(String email) {
        return CAPTCHA_PREFIX + email;
    }

    /**
     * 获取频率限制Key
     *
     * @param email 邮箱地址
     * @return Redis频率限制Key (格式: tenant_reg_captcha_limit_邮箱)
     */
    private String getRateLimitKey(String email) {
        return RATE_LIMIT_PREFIX + email;
    }
}
