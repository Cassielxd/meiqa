package io.renren.crmchat.controller.kefu;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatServiceSpeechcraftCateEntity;
import io.renren.crmchat.entity.ChatServiceSpeechcraftEntity;
import io.renren.crmchat.security.UserContext;
import io.renren.crmchat.service.KefuServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kefu Service Controller - 客服快捷话术管理
 * PHP Reference: /app/controller/kefu/Service.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/kefu/service")
@Tag(name = "Kefu Service - Quick Reply Management")
@AllArgsConstructor
public class KefuServiceController {

    private final KefuServiceService kefuServiceService;

    /**
     * 获取快捷话术列表
     * GET /api/kefu/service/speechcraft
     *
     * PHP Reference: Service.php::getSpeechcraftList()
     *
     * Query Parameters:
     * - title: 标题（可选，模糊查询）
     * - message: 内容（可选，模糊查询）
     * - cate_id: 分类ID（可选）
     *
     * Response: 个人快捷话术列表
     */
    @GetMapping("/speechcraft")
    @Operation(summary = "Get Quick Reply List")
    public ApiResult<List<ChatServiceSpeechcraftEntity>> getSpeechcraftList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String cate_id) {

        // 从JWT token中获取当前客服信息
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        Map<String, Object> filters = new HashMap<>();
        if (title != null && !title.trim().isEmpty()) {
            filters.put("title", title);
        }
        if (message != null && !message.trim().isEmpty()) {
            filters.put("message", message);
        }
        if (cate_id != null && !cate_id.trim().isEmpty()) {
            filters.put("cate_id", cate_id);
        }

        List<ChatServiceSpeechcraftEntity> list = kefuServiceService.getSpeechcraftList(filters, kefuId);
        return ApiResult.ok(list);
    }

    /**
     * 获取快捷话术详情
     * GET /api/kefu/service/speechcraft/:id
     *
     * PHP Reference: Service.php::getSpeechcraftDetail()
     *
     * Response: 快捷话术详情
     */
    @GetMapping("/speechcraft/{id}")
    @Operation(summary = "Get Quick Reply Details")
    public ApiResult<ChatServiceSpeechcraftEntity> getSpeechcraftDetail(@PathVariable Integer id) {
        // 从JWT token中获取当前客服信息
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        ChatServiceSpeechcraftEntity speechcraft = kefuServiceService.getSpeechcraftDetail(id, kefuId);
        return ApiResult.ok(speechcraft);
    }

    /**
     * 创建快捷话术
     * POST /api/kefu/service/speechcraft
     *
     * PHP Reference: Service.php::saveSpeechcraft()
     *
     * Request Body:
     * {
     *   "title": "Welcome Message",                // 必填
     *   "message": "您好，有什么可以帮您？",  // 必填
     *   "cate_id": 1,                    // 可选
     *   "sort": 0                        // 可选
     * }
     *
     * Response: 创建成功
     */
    @PostMapping("/speechcraft")
    @Operation(summary = "Create Quick Reply")
    public ApiResult<String> createSpeechcraft(@RequestBody Map<String, Object> data) {
        // 从JWT token中获取当前客服信息
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        kefuServiceService.createSpeechcraft(data, kefuId);
        return ApiResult.ok("Quick reply created successfully", "success");
    }

    /**
     * 更新快捷话术
     * PUT /api/kefu/service/speechcraft/:id
     *
     * PHP Reference: Service.php::editSpeechcraft()
     *
     * Request Body:
     * {
     *   "title": "Welcome Message",                // 必填
     *   "message": "您好，有什么可以帮您？",  // 必填
     *   "cate_id": 1,                    // 可选
     *   "sort": 0                        // 可选
     * }
     *
     * Response: 修改成功
     */
    @PutMapping("/speechcraft/{id}")
    @Operation(summary = "Update Quick Reply")
    public ApiResult<String> updateSpeechcraft(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        // 从JWT token中获取当前客服信息
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        kefuServiceService.updateSpeechcraft(id, data, kefuId);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 删除快捷话术
     * DELETE /api/kefu/service/speechcraft/:id
     *
     * PHP Reference: Service.php::deleteSpeechcraft()
     *
     * Response: 删除成功
     */
    @DeleteMapping("/speechcraft/{id}")
    @Operation(summary = "Delete Quick Reply")
    public ApiResult<String> deleteSpeechcraft(@PathVariable Integer id) {
        // 从JWT token中获取当前客服信息
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        kefuServiceService.deleteSpeechcraft(id, kefuId);
        return ApiResult.ok("Deleted successfully", "success");
    }

    /**
     * 获取分类列表
     * GET /api/kefu/service/cate
     *
     * PHP Reference: Service.php::getCateList()
     *
     * Query Parameters:
     * - name: 分类名称（可选，模糊查询）
     *
     * Response: 个人分类列表
     */
    @GetMapping("/cate")
    @Operation(summary = "Get Category List")
    public ApiResult<List<ChatServiceSpeechcraftCateEntity>> getCateList(
            @RequestParam(required = false) String name) {

        // 从JWT token中获取当前客服信息
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        Map<String, Object> filters = new HashMap<>();
        if (name != null && !name.trim().isEmpty()) {
            filters.put("name", name);
        }

        List<ChatServiceSpeechcraftCateEntity> list = kefuServiceService.getCateList(filters, kefuId);
        return ApiResult.ok(list);
    }

    /**
     * 创建分类
     * POST /api/kefu/service/cate
     *
     * PHP Reference: Service.php::saveCate()
     *
     * Request Body:
     * {
     *   "name": "Commonly Used Quick Replies",  // 必填
     *   "sort": 0          // 可选
     * }
     *
     * Response: 添加成功
     */
    @PostMapping("/cate")
    @Operation(summary = "Create Category")
    public ApiResult<String> createCate(@RequestBody Map<String, Object> data) {
        // 从JWT token中获取当前客服信息
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        kefuServiceService.createCate(data, kefuId);
        return ApiResult.ok("Added successfully", "success");
    }

    /**
     * 更新分类
     * PUT /api/kefu/service/cate/:id
     *
     * PHP Reference: Service.php::editCate()
     *
     * Request Body:
     * {
     *   "name": "常用话术",  // 必填
     *   "sort": 0          // 可选
     * }
     *
     * Response: 修改成功
     */
    @PutMapping("/cate/{id}")
    @Operation(summary = "更新分类")
    public ApiResult<String> updateCate(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        // 从JWT token中获取当前客服信息
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        kefuServiceService.updateCate(id, data, kefuId);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 删除分类
     * DELETE /api/kefu/service/cate/:id
     *
     * PHP Reference: Service.php::deleteCate()
     *
     * Response: 删除成功
     */
    @DeleteMapping("/cate/{id}")
    @Operation(summary = "删除分类")
    public ApiResult<String> deleteCate(@PathVariable Integer id) {
        // 从JWT token中获取当前客服信息
        Long userId = UserContext.getUserId();
        Integer kefuId = userId != null ? userId.intValue() : null;

        kefuServiceService.deleteCate(id, kefuId);
        return ApiResult.ok("Deleted successfully", "success");
    }
}
