package io.renren.crmchat.controller.mobile;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.MobileFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mobile Feedback Controller - 移动端反馈管理
 * PHP Reference: /app/controller/mobile/Feedback.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/mobile/service")
@Tag(name = "Mobile Feedback - 移动端反馈")
@AllArgsConstructor
public class MobileFeedbackController {

    private final MobileFeedbackService mobileFeedbackService;

    /**
     * 保存用户反馈
     * POST /api/mobile/service/feedback
     *
     * PHP Reference: Feedback.php::saveFeedback()
     *
     * Request Body:
     * {
     *   "rela_name": "张三",          // 姓名（必填）
     *   "phone": "13800138000",      // 联系方式（必填）
     *   "content": "反馈内容..."     // 反馈内容（必填）
     * }
     *
     * Response: { "code": 0, "msg": "保存成功" }
     */
    @PostMapping("/feedback")
    @Operation(summary = "Save User Feedback")
    public ApiResult<String> saveFeedback(@RequestBody Map<String, Object> data) {
        mobileFeedbackService.saveFeedback(data);
        return ApiResult.ok("Saved successfully", "success");
    }

    /**
     * 获取反馈页面头部文字
     * GET /api/mobile/service/feedback
     *
     * PHP Reference: Feedback.php::getFeedbackInfo()
     *
     * Response:
     * {
     *   "feedback": "欢迎提交您的宝贵意见..."
     * }
     */
    @GetMapping("/feedback")
    @Operation(summary = "Get Feedback Page Header Text")
    public ApiResult<Map<String, Object>> getFeedbackInfo() {
        Map<String, Object> result = mobileFeedbackService.getFeedbackInfo();
        return ApiResult.ok(result);
    }
}
