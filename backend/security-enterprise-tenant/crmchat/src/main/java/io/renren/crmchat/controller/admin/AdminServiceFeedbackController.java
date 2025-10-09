package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.AdminServiceFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin Service Feedback Controller - 管理员反馈管理
 * PHP Reference: /app/controller/admin/chat/ServiceFeedback.php
 *
 * 功能说明:
 * 1. GET /api/admin/chat/feedback - 获取反馈列表
 * 2. GET /api/admin/chat/feedback/:id/edit - 获取编辑表单
 * 3. PUT /api/admin/chat/feedback/:id - 更新反馈
 * 4. DELETE /api/admin/chat/feedback/:id - 删除反馈
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/chat/feedback")
@Tag(name = "Admin Service Feedback - 反馈管理")
@AllArgsConstructor
public class AdminServiceFeedbackController extends BaseController {

    private final AdminServiceFeedbackService adminServiceFeedbackService;

    /**
     * 获取反馈列表
     * GET /api/admin/chat/feedback
     *
     * PHP Reference: ServiceFeedback.php::index()
     *
     * Query Parameters:
     * - title: 昵称或内容搜索
     * - time: 时间范围
     * - page: 页码
     * - limit: 每页数量
     *
     * Response:
     * {
     *   "list": [...],
     *   "count": 100
     * }
     */
    @GetMapping
    @Operation(summary = "Get Feedback List")
    public ApiResult<Map<String, Object>> getFeedbackList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String time,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {

        String appid = currentAppid();

        Map<String, Object> where = new HashMap<>();
        if (title != null) where.put("title", title);
        if (time != null) where.put("time", time);
        where.put("page", page);
        where.put("limit", limit);

        Map<String, Object> result = adminServiceFeedbackService.getFeedbackList(where, appid);
        return ApiResult.ok(result);
    }

    /**
     * 获取编辑表单
     * GET /api/admin/chat/feedback/:id/edit
     *
     * PHP Reference: ServiceFeedback.php::edit()
     *
     * Response:
     * {
     *   "feedback": {...},
     *   "form_rules": []
     * }
     */
    @GetMapping("/{id}/edit")
    @Operation(summary = "Get Edit Form")
    public ApiResult<Map<String, Object>> getEditForm(@PathVariable Integer id) {
        String appid = currentAppid();

        Map<String, Object> result = adminServiceFeedbackService.getEditForm(id, appid);
        return ApiResult.ok(result);
    }

    /**
     * 更新反馈
     * PUT /api/admin/chat/feedback/:id
     *
     * PHP Reference: ServiceFeedback.php::update()
     *
     * Request Body:
     * {
     *   "make": "备注内容",
     *   "status": 1
     * }
     *
     * Response: { "code": 0, "msg": "修改成功" }
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update Feedback")
    public ApiResult<String> updateFeedback(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        String appid = currentAppid();

        boolean success = adminServiceFeedbackService.updateFeedback(id, data, appid);
        if (success) {
            return ApiResult.ok("Updated successfully", "success");
        } else {
            return ApiResult.fail("Update failed");
        }
    }

    /**
     * 删除反馈
     * DELETE /api/admin/chat/feedback/:id
     *
     * PHP Reference: ServiceFeedback.php::delete()
     *
     * Response: { "code": 0, "msg": "删除成功" }
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Feedback")
    public ApiResult<String> deleteFeedback(@PathVariable Integer id) {
        String appid = currentAppid();

        adminServiceFeedbackService.deleteFeedback(id, appid);
        return ApiResult.ok("Deleted successfully", "success");
    }
}
