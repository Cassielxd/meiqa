package io.renren.modules.kefu.service;

import io.renren.modules.kefu.dto.admin.AjCheckDTO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Admin验证码服务
 */
public interface AdminCaptchaService {

    /**
     * 生成图片验证码
     *
     * @param response HTTP响应
     * @param uuid     验证码UUID
     * @throws IOException IO异常
     */
    void createCaptcha(HttpServletResponse response, String uuid) throws IOException;

    /**
     * 生成AJ滑块验证码
     *
     * @param captchaType 验证码类型
     * @return 验证码数据
     */
    Map<String, Object> createAjCaptcha(String captchaType);

    /**
     * 验证AJ滑块验证码
     *
     * @param dto 验证请求
     * @return 验证结果
     */
    Map<String, Object> checkAjCaptcha(AjCheckDTO dto);

    /**
     * 验证图片验证码
     *
     * @param uuid 验证码UUID
     * @param code 验证码
     * @return 验证结果
     */
    boolean validateCaptcha(String uuid, String code);
}
