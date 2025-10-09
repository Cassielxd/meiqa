package io.renren.crmchat.service;

import java.util.Map;

/**
 * 公共验证码服务 - 只提供滑块验证码
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
}
