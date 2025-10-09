package io.renren.crmchat.service.common;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务 - 邮箱/短信验证码
 *
 * 功能：
 * - sendCaptcha: 生成并发送验证码（存入Redis）
 * - verifyCaptcha: 验证验证码是否正确
 * - clearCaptcha: 清除验证码缓存
 * - generateCaptcha: 生成6位数字验证码
 *
 * PHP参考: app/services/tenant/TenantServices.php::sendRegisterCaptcha()
 *
 * @author System Architect
 * @date 2025-10-02
 */
@Service
@AllArgsConstructor
public class CaptchaService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 验证码缓存前缀
     */
    private static final String CAPTCHA_PREFIX = "tenant_register_captcha_";

    /**
     * 验证码长度
     */
    private static final int CAPTCHA_LENGTH = 6;

    /**
     * 验证码有效期（秒）
     */
    private static final long CAPTCHA_EXPIRE_SECONDS = 600; // 10分钟

    /**
     * 发送验证码（生成并存入Redis）
     *
     * @param email 邮箱地址
     * @return 验证码（开发环境返回，生产环境应该通过邮件发送）
     */
    public String sendCaptcha(String email) {
        // 生成验证码
        String captcha = generateCaptcha();

        // 存入Redis，10分钟有效
        String cacheKey = getCacheKey(email);
        redisTemplate.opsForValue().set(cacheKey, captcha, CAPTCHA_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // TODO: 集成邮件发送服务（SendCloud、阿里云邮件推送等）
        // 开发环境直接返回验证码用于测试
        return captcha;
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
     * 获取验证码缓存Key
     *
     * @param email 邮箱地址
     * @return Redis缓存Key
     */
    private String getCacheKey(String email) {
        return CAPTCHA_PREFIX + md5(email);
    }

    /**
     * MD5哈希（简化版，实际应使用DigestUtils）
     *
     * @param input 输入字符串
     * @return MD5哈希值
     */
    private String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm unavailable", e);
        }
    }
}
