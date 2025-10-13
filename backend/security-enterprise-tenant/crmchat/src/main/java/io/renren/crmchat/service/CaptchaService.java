package io.renren.crmchat.service;

import java.util.Map;

/**
 * 公共验证码服务 - 提供滑块验证码和简单图形验证码
 * 所有角色（Admin、Tenant、Kefu）共用
 */
public interface CaptchaService {

    /**
     * 生成AJ滑块验证码
     *
     * @param captchaType 验证码类型（blockPuzzle）
     * @return 验证码数据
     */
    Map<String, Object> createAjCaptcha(String captchaType);

    /**
     * 验证AJ滑块验证码（一次验证）
     *
     * @param token      验证码token
     * @param pointJson  点选坐标JSON
     * @param captchaType 验证码类型
     * @return 验证结果
     */
    Map<String, Object> checkAjCaptcha(String captchaType, String token, String pointJson);

    /**
     * 生成简单图形验证码
     * PHP参考: Login::captcha() -> Captcha::create([], true)
     *
     * @return 验证码数据 {img: base64图片, key: 唯一标识}
     */
    Map<String, Object> createSimpleCaptcha();

    /**
     * 验证简单图形验证码
     *
     * @param key  验证码key
     * @param code 用户输入的验证码
     * @return true=验证成功, false=验证失败
     */
    boolean verifySimpleCaptcha(String key, String code);
}
