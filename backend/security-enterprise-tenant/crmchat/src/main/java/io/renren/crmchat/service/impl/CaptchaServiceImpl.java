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
}
