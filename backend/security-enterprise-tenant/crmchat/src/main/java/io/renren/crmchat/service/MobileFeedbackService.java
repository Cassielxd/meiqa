package io.renren.crmchat.service;

import io.renren.crmchat.dao.ChatServiceFeedbackMapper;
import io.renren.crmchat.entity.ChatServiceFeedbackEntity;
import io.renren.crmchat.exception.CrmChatException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Mobile Feedback Service - 移动端反馈服务
 * PHP Reference: /app/controller/mobile/Feedback.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. saveFeedback(): 保存用户反馈（姓名、电话、内容）
 * 2. getFeedbackInfo(): 获取反馈页面头部文字配置
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class MobileFeedbackService {

    private final ChatServiceFeedbackMapper chatServiceFeedbackMapper;

    /**
     * 保存用户反馈
     * POST /api/mobile/feedback
     *
     * PHP Reference: Feedback.php::saveFeedback()
     *
     * 业务逻辑:
     * 1. 验证必填字段：rela_name, phone, content（使用ChatServiceFeedbackValidate）
     * 2. HTML转义content字段（防止XSS）
     * 3. 设置add_time为当前时间
     * 4. 保存到ChatServiceFeedback表
     *
     * @param data 反馈数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveFeedback(Map<String, Object> data) {
        // 1. 验证必填字段
        if (!data.containsKey("rela_name") || data.get("rela_name") == null || data.get("rela_name").toString().trim().isEmpty()) {
            throw new CrmChatException("Name cannot be empty");
        }
        if (!data.containsKey("phone") || data.get("phone") == null || data.get("phone").toString().trim().isEmpty()) {
            throw new CrmChatException("Contact information cannot be empty");
        }
        if (!data.containsKey("content") || data.get("content") == null || data.get("content").toString().trim().isEmpty()) {
            throw new CrmChatException("Feedback content cannot be empty");
        }

        String relaName = data.get("rela_name").toString();
        String phone = data.get("phone").toString();
        String content = data.get("content").toString();

        // 2. HTML转义content（PHP: htmlspecialchars）
        content = htmlEscape(content);

        // 3. 创建反馈记录
        ChatServiceFeedbackEntity feedback = new ChatServiceFeedbackEntity();
        feedback.setRelaName(relaName);
        feedback.setPhone(phone);
        feedback.setContent(content);
        feedback.setAddTime((int) (System.currentTimeMillis() / 1000));

        int result = chatServiceFeedbackMapper.insert(feedback);
        if (result <= 0) {
            throw new CrmChatException("Failed to save");
        }

        log.info("保存反馈成功: relaName={}, phone={}", relaName, phone);
    }

    /**
     * 获取反馈页面头部文字
     * GET /api/mobile/feedback/info
     *
     * PHP Reference: Feedback.php::getFeedbackInfo()
     *
     * 业务逻辑:
     * 1. 从系统配置中获取'service_feedback'配置
     * 2. 返回反馈页面头部提示文字
     *
     * @return 反馈页面配置信息
     */
    public Map<String, Object> getFeedbackInfo() {
        // TODO: 从系统配置表获取
        // PHP: sys_config('service_feedback')
        String feedback = "";  // 默认为空，实际应从配置表获取

        Map<String, Object> result = new HashMap<>();
        result.put("feedback", feedback);

        log.info("获取反馈页面信息: feedback={}", feedback);
        return result;
    }

    /**
     * HTML转义工具方法
     * 模拟PHP的htmlspecialchars函数
     *
     * @param input 输入字符串
     * @return 转义后的字符串
     */
    private String htmlEscape(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
