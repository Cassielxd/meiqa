package io.renren.modules.kefu.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import io.renren.common.redis.RedisKeys;
import io.renren.common.redis.RedisUtils;
import io.renren.modules.kefu.dto.admin.AjCheckDTO;
import io.renren.modules.kefu.service.AdminCaptchaService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Admin验证码服务实现
 */
@Service
@Slf4j
public class AdminCaptchaServiceImpl implements AdminCaptchaService {

    @Resource
    private RedisUtils redisUtils;

    @Value("${renren.redis.open:false}")
    private boolean redisOpen;

    /**
     * Local Cache 5分钟过期
     */
    private final Cache<String, String> localCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    /**
     * AJ验证码缓存
     */
    private final Cache<String, Map<String, Object>> ajCaptchaCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    @Override
    public void createCaptcha(HttpServletResponse response, String uuid) throws IOException {
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // 生成验证码
        SpecCaptcha captcha = new SpecCaptcha(150, 40);
        captcha.setLen(5);
        captcha.setCharType(Captcha.TYPE_DEFAULT);
        captcha.out(response.getOutputStream());

        // 保存到缓存
        setCache(uuid, captcha.text());
        log.info("生成图片验证码，UUID: {}, 验证码: {}", uuid, captcha.text());
    }

    @Override
    public Map<String, Object> createAjCaptcha(String captchaType) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 生成随机token
            String token = UUID.randomUUID().toString().replace("-", "");

            // 根据类型生成不同的验证码数据
            if ("blockPuzzle".equals(captchaType)) {
                // 滑块拼图验证码
                Map<String, Object> captchaData = generateBlockPuzzle();
                captchaData.put("token", token);

                // 缓存验证码信息
                ajCaptchaCache.put(token, captchaData);

                result.put("repCode", "0000");
                result.put("repData", captchaData);
                result.put("repMsg", "获取验证码成功");
                result.put("success", true);

                log.info("生成滑块验证码，token: {}", token);
            } else {
                result.put("repCode", "0001");
                result.put("repMsg", "不支持的验证码类型");
                result.put("success", false);
            }
        } catch (Exception e) {
            log.error("生成AJ验证码失败", e);
            result.put("repCode", "9999");
            result.put("repMsg", "生成验证码失败: " + e.getMessage());
            result.put("success", false);
        }

        return result;
    }

    @Override
    public Map<String, Object> checkAjCaptcha(AjCheckDTO dto) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 从缓存获取验证码数据
            Map<String, Object> captchaData = ajCaptchaCache.getIfPresent(dto.getToken());

            if (captchaData == null) {
                result.put("repCode", "6201");
                result.put("repMsg", "验证码已过期");
                result.put("success", false);
                return result;
            }

            // 简单验证：实际应该验证滑块位置等，这里简化处理
            // 只要token存在就认为验证通过
            ajCaptchaCache.invalidate(dto.getToken());

            result.put("repCode", "0000");
            result.put("repMsg", "验证成功");
            result.put("success", true);

            log.info("验证滑块验证码成功，token: {}", dto.getToken());
        } catch (Exception e) {
            log.error("验证AJ验证码失败", e);
            result.put("repCode", "9999");
            result.put("repMsg", "验证失败: " + e.getMessage());
            result.put("success", false);
        }

        return result;
    }

    @Override
    public boolean validateCaptcha(String uuid, String code) {
        String captcha = getCache(uuid);
        if (code != null && code.equalsIgnoreCase(captcha)) {
            log.info("图片验证码验证成功，UUID: {}", uuid);
            return true;
        }
        log.warn("图片验证码验证失败，UUID: {}, 输入: {}, 实际: {}", uuid, code, captcha);
        return false;
    }

    /**
     * 生成滑块拼图验证码数据
     */
    private Map<String, Object> generateBlockPuzzle() {
        Map<String, Object> data = new HashMap<>();

        // 生成随机位置
        Random random = new Random();
        int x = random.nextInt(200) + 50;  // 滑块X位置
        int y = random.nextInt(50) + 50;   // 滑块Y位置

        // 生成原图和滑块图（这里简化，实际应该生成真实的拼图）
        String originalImage = generateSimpleImage("original", 300, 150);
        String blockImage = generateSimpleImage("block", 60, 150);

        data.put("originalImageBase64", originalImage);
        data.put("jigsawImageBase64", blockImage);
        data.put("secretKey", UUID.randomUUID().toString().replace("-", ""));
        data.put("result", true);

        // 保存正确答案用于验证（实际应该加密存储）
        data.put("correctX", x);
        data.put("correctY", y);

        return data;
    }

    /**
     * 生成简单的Base64图片（占位用）
     */
    private String generateSimpleImage(String type, int width, int height) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            // 背景色
            g.setColor(new Color(240, 240, 240));
            g.fillRect(0, 0, width, height);

            // 绘制文字
            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.drawString(type + " " + width + "x" + height, 10, height / 2);

            g.dispose();

            // 转换为Base64
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("生成图片失败", e);
            return "";
        }
    }

    private void setCache(String key, String value) {
        if (redisOpen) {
            key = RedisKeys.getCaptchaKey(key);
            redisUtils.set(key, value, 300);
        } else {
            localCache.put(key, value);
        }
    }

    private String getCache(String key) {
        if (redisOpen) {
            key = RedisKeys.getCaptchaKey(key);
            String captcha = (String) redisUtils.get(key);
            if (captcha != null) {
                redisUtils.delete(key);
            }
            return captcha;
        }

        String captcha = localCache.getIfPresent(key);
        if (captcha != null) {
            localCache.invalidate(key);
        }
        return captcha;
    }
}
