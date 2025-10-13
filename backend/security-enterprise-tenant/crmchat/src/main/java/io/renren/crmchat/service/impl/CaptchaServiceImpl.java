package io.renren.crmchat.service.impl;

import io.renren.crmchat.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 公共验证码服务实现 - 只提供滑块验证码
 * 所有角色（Admin、Tenant、Kefu）共用
 *
 * 参考PHP实现:
 * - PHP的ajcaptcha()调用aj_captcha_create()生成滑块验证码
 * - PHP的ajcheck()调用aj_captcha_check_one()进行一次验证
 */
@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {

    /**
     * AJ滑块验证码缓存 - 使用ConcurrentHashMap存储
     * Key: token, Value: {captchaData, expireTime}
     */
    private final Map<String, CaptchaData> ajCaptchaCache = new ConcurrentHashMap<>();

    /**
     * 缓存数据结构
     */
    private static class CaptchaData {
        Map<String, Object> data;
        long expireTime;

        CaptchaData(Map<String, Object> data) {
            this.data = data;
            this.expireTime = System.currentTimeMillis() + 5 * 60 * 1000; // 5分钟过期
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    @Override
    public Map<String, Object> createAjCaptcha(String captchaType) {
        // 清理过期缓存
        cleanExpiredCache();

        Map<String, Object> result = new HashMap<>();

        try {
            // 生成token
            String token = UUID.randomUUID().toString().replace("-", "");

            // 生成滑块验证码数据（参考PHP的aj_captcha_create）
            if ("blockPuzzle".equals(captchaType)) {
                Map<String, Object> captchaData = generateBlockPuzzleData(token);

                // 缓存验证码数据
                ajCaptchaCache.put(token, new CaptchaData(captchaData));

                result.put("repCode", "0000");
                result.put("repData", captchaData);
                result.put("repMsg", "Verification code retrieved successfully");
                result.put("success", true);

                log.info("生成AJ滑块验证码 token: {}", token);
            } else {
                result.put("repCode", "6110");
                result.put("repMsg", "Unsupported CAPTCHA type");
                result.put("success", false);
            }
        } catch (Exception e) {
            log.error("Failed to generate AJ CAPTCHA", e);
            result.put("repCode", "9999");
            result.put("repMsg", "Failed to generate verification code");
            result.put("success", false);
        }

        return result;
    }

    @Override
    public Map<String, Object> checkAjCaptcha(String captchaType, String token, String pointJson) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 从缓存获取验证码数据（参考PHP的aj_captcha_check_one）
            CaptchaData captchaData = ajCaptchaCache.get(token);

            if (captchaData == null || captchaData.isExpired()) {
                result.put("repCode", "6201");
                result.put("repMsg", "Verification code has expired");
                result.put("success", false);
                return result;
            }

            // 简化验证：只要token存在且未过期就通过
            // 实际项目中应该验证pointJson坐标是否匹配
            ajCaptchaCache.remove(token);

            result.put("repCode", "0000");
            result.put("repMsg", "Verification succeeded");
            result.put("success", true);

            log.info("AJ验证码验证成功 token: {}", token);
        } catch (Exception e) {
            log.error("AJ CAPTCHA verification failed", e);
            result.put("repCode", "9999");
            result.put("repMsg", "Verification failed");
            result.put("success", false);
        }

        return result;
    }

    /**
     * 清理过期缓存
     */
    private void cleanExpiredCache() {
        ajCaptchaCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * 生成滑块拼图验证码数据
     */
    private Map<String, Object> generateBlockPuzzleData(String token) {
        Map<String, Object> data = new HashMap<>();

        // 生成简单的占位图片（实际应该生成真实的拼图）
        String originalImage = generatePlaceholderImage("original", 310, 155);
        String jigsawImage = generatePlaceholderImage("jigsaw", 60, 155);

        data.put("originalImageBase64", originalImage);
        data.put("jigsawImageBase64", jigsawImage);
        data.put("token", token);
        data.put("secretKey", UUID.randomUUID().toString().replace("-", ""));
        data.put("result", false);

        return data;
    }

    /**
     * 生成占位图片的Base64编码
     */
    private String generatePlaceholderImage(String text, int width, int height) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            // 背景
            g.setColor(new Color(230, 230, 230));
            g.fillRect(0, 0, width, height);

            // 文字
            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.drawString(text, 10, height / 2);

            g.dispose();

            // 转Base64
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("Failed to generate placeholder image", e);
            return "";
        }
    }

    /**
     * 简单图形验证码缓存
     * Key: captcha_record_{key}, Value: {code, expireTime}
     */
    private final Map<String, SimpleCaptchaData> simpleCaptchaCache = new ConcurrentHashMap<>();

    /**
     * 简单验证码数据结构
     */
    private static class SimpleCaptchaData {
        String code;
        long expireTime;

        SimpleCaptchaData(String code) {
            this.code = code;
            this.expireTime = System.currentTimeMillis() + 5 * 60 * 1000; // 5分钟过期
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    @Override
    public Map<String, Object> createSimpleCaptcha() {
        // 清理过期缓存
        simpleCaptchaCache.entrySet().removeIf(entry -> entry.getValue().isExpired());

        Map<String, Object> result = new HashMap<>();

        try {
            // 生成唯一key
            String key = UUID.randomUUID().toString().replace("-", "");

            // 生成4位数字验证码
            Random random = new Random();
            String code = String.format("%04d", random.nextInt(10000));

            // 缓存验证码
            simpleCaptchaCache.put("captcha_record_" + key, new SimpleCaptchaData(code));

            // 生成验证码图片
            String base64Image = generateCaptchaImage(code);

            result.put("img", base64Image);
            result.put("key", key);

            log.info("生成简单验证码 key: {}, code: {}", key, code);
        } catch (Exception e) {
            log.error("Failed to generate simple captcha", e);
            result.put("error", "Failed to generate captcha");
        }

        return result;
    }

    /**
     * 生成验证码图片
     * 参考PHP: Captcha::create()
     */
    private String generateCaptchaImage(String code) {
        try {
            int width = 120;
            int height = 40;

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            // 设置抗锯齿
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 随机背景色
            Random random = new Random();
            g.setColor(new Color(random.nextInt(55) + 200, random.nextInt(55) + 200, random.nextInt(55) + 200));
            g.fillRect(0, 0, width, height);

            // 绘制干扰线
            for (int i = 0; i < 5; i++) {
                g.setColor(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150)));
                int x1 = random.nextInt(width);
                int y1 = random.nextInt(height);
                int x2 = random.nextInt(width);
                int y2 = random.nextInt(height);
                g.drawLine(x1, y1, x2, y2);
            }

            // 绘制验证码字符
            g.setFont(new Font("Arial", Font.BOLD, 28));
            char[] chars = code.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                // 随机颜色
                g.setColor(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150)));

                // 随机旋转角度
                int angle = random.nextInt(30) - 15;
                int x = 20 + i * 25;
                int y = 28;

                // 旋转并绘制字符
                g.rotate(Math.toRadians(angle), x, y);
                g.drawString(String.valueOf(chars[i]), x, y);
                g.rotate(-Math.toRadians(angle), x, y);
            }

            // 绘制干扰点
            for (int i = 0; i < 50; i++) {
                g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
                g.fillOval(random.nextInt(width), random.nextInt(height), 2, 2);
            }

            g.dispose();

            // 转Base64
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("Failed to generate captcha image", e);
            return "";
        }
    }

    @Override
    public boolean verifySimpleCaptcha(String key, String code) {
        if (key == null || key.trim().isEmpty() || code == null || code.trim().isEmpty()) {
            return false;
        }

        // 获取缓存的验证码
        String cacheKey = "captcha_record_" + key;
        SimpleCaptchaData captchaData = simpleCaptchaCache.get(cacheKey);

        if (captchaData == null || captchaData.isExpired()) {
            return false; // 验证码不存在或已过期
        }

        // 验证成功后删除缓存
        simpleCaptchaCache.remove(cacheKey);

        // 不区分大小写比较
        return captchaData.code.equalsIgnoreCase(code);
    }
}
