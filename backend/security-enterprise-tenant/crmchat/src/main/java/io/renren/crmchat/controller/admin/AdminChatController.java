package io.renren.crmchat.controller.admin;

import io.renren.common.page.PageData;
import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.AdminChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin API - 客服管理
 * PHP Reference: /app/controller/admin/Chat.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/chat")
@Tag(name = "Admin - Customer Service Management")
@AllArgsConstructor
public class AdminChatController {

    private final AdminChatService adminChatService;

    /**
     * 4.1 站点统计
     * GET /api/admin/chat/statistics
     *
     * PHP Reference: Chat.php::statistics()
     *
     * Response:
     * {
     *   "total_services": 10,     // 总客服数
     *   "online_services": 3,      // 在线客服数
     *   "total_chats": 100,        // 总聊天次数
     *   "today_chats": 20,         // 今日聊天次数
     *   "total_users": 500,        // 总用户数
     *   "today_users": 15          // 今日新用户数
     * }
     */
    @GetMapping("/statistics")
    @Operation(summary = "Site Statistics")
    public ApiResult<Map<String, Object>> statistics() {
        Map<String, Object> stats = adminChatService.getChatStatistics();
        return ApiResult.ok(stats);
    }

    /**
     * 4.2 客服列表
     * GET /api/admin/chat/kefu
     *
     * PHP Reference: Chat.php::kefu()
     *
     * Query Params:
     * - page: 页码（默认1）
     * - limit: 每页记录数（默认10）
     * - keyword: 搜索关键词（账号、昵称、电话）
     * - group_id: 分组ID
     * - status: 状态筛选（0-禁用，1-启用）
     *
     * Response:
     * {
     *   "list": [...],
     *   "count": 10
     * }
     */
    @GetMapping("/kefu")
    @Operation(summary = "Customer Service List")
    public ApiResult<PageData<Map<String, Object>>> kefuList(@RequestParam Map<String, Object> params) {
        PageData<Map<String, Object>> result = adminChatService.getKefuList(params);
        return ApiResult.ok(result);
    }

    /**
     * 4.3 客服组列表
     * GET /api/admin/chat/group
     *
     * PHP Reference: Chat.php::group()
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "name": "Pre-sales Inquiry",
     *     "sort": 100,
     *     "kefu_count": 5
     *   }
     * ]
     */
    @GetMapping("/kefu_groups")
    @Operation(summary = "Customer Service Group List")
    public ApiResult<List<Map<String, Object>>> groupList() {
        List<Map<String, Object>> list = adminChatService.getKefuGroupList();
        return ApiResult.ok(list);
    }

    /**
     * 4.4 添加客服
     * POST /api/admin/chat/kefu
     *
     * PHP Reference: Chat.php::addKefu()
     *
     * Request Body:
     * {
     *   "account": "kefu001",
     *   "password": "123456",
     *   "nickname": "Agent Wang",
     *   "phone": "13800138000",
     *   "group_id": 1,
     *   "status": 1,
     *   "welcome_words": "Hello, how can we assist you today?"
     * }
     *
     * Response: { "code": 0, "msg": "Added successfully" }
     */
    @PostMapping("/kefu")
    @Operation(summary = "Add Customer Service Agent")
    public ApiResult<String> addKefu(@RequestBody Map<String, Object> data) {
        adminChatService.createKefu(data);
        return ApiResult.ok("Customer service representative added successfully", "success");
    }

    /**
     * 4.5 修改客服
     * PUT /api/admin/chat/kefu/:id
     *
     * PHP Reference: Chat.php::updateKefu($id)
     *
     * Request Body:
     * {
     *   "nickname": "Agent Li",
     *   "phone": "13900139000",
     *   "group_id": 2,
     *   "status": 1,
     *   "welcome_words": "Hello, welcome to our service!",
     *   "password": "newpass" // 可选
     * }
     *
     * Response: { "code": 0, "msg": "Updated successfully" }
     */
    @PutMapping("/kefu/{id}")
    @Operation(summary = "Update Customer Service Agent")
    public ApiResult<String> updateKefu(
            @Parameter(description = "Customer Service ID") @PathVariable("id") Integer id,
            @RequestBody Map<String, Object> data) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        adminChatService.updateKefu(id, data);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 4.6 删除客服
     * DELETE /api/admin/chat/kefu/:id
     *
     * PHP Reference: Chat.php::deleteKefu($id)
     *
     * Response: { "code": 0, "msg": "Deleted successfully" }
     */
    @DeleteMapping("/kefu/{id}")
    @Operation(summary = "Delete Customer Service Agent")
    public ApiResult<String> deleteKefu(@Parameter(description = "Customer Service ID") @PathVariable("id") Integer id) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        adminChatService.deleteKefu(id);
        return ApiResult.ok("Deleted successfully", "success");
    }

    /**
     * 4.7 修改客服状态
     * PUT /api/admin/chat/kefu/set_status/:id/:status
     *
     * PHP Reference: Chat.php::setKefuStatus($id, $status)
     *
     * Response: { "code": 0, "msg": "Status updated successfully" }
     */
    @PutMapping("/kefu/set_status/{id}/{status}")
    @Operation(summary = "Update Customer Service Agent Status")
    public ApiResult<String> setKefuStatus(
            @Parameter(description = "Customer Service ID") @PathVariable("id") Integer id,
            @Parameter(description = "Status: 0-Disabled, 1-Enabled") @PathVariable("status") Integer status) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        if (status != 0 && status != 1) {
            return ApiResult.fail("Invalid status parameter, must be 0 or 1");
        }

        adminChatService.updateKefuStatus(id, status);
        String msg = status == 0 ? "Hidden successfully" : "Displayed successfully";
        return ApiResult.ok(msg, "success");
    }

    /**
     * 获取客服聊天用户
     * GET /api/admin/chat/kefu/:id/chat_user
     *
     * PHP Reference: Service.php::chat_user()
     */
    @GetMapping("/kefu/{id}/chat_user")
    @Operation(summary = "Get Customer Service Chat User List")
    public ApiResult<List<Map<String, Object>>> getChatUser(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }
        List<Map<String, Object>> users = adminChatService.getChatUserList(id);
        return ApiResult.ok(users);
    }

    /**
     * 客服代登录
     * GET /api/admin/chat/kefu/login/:id
     *
     * PHP Reference: Service.php::keufLogin()
     */
    @GetMapping("/kefu/login/{id}")
    @Operation(summary = "Customer Service Proxy Login")
    public ApiResult<Map<String, Object>> kefuLogin(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        Map<String, Object> loginResult = adminChatService.kefuLogin(id);
        return ApiResult.ok(loginResult);
    }

    /**
     * 4.8 聊天记录
     * GET /api/admin/chat/record
     *
     * PHP Reference: Chat.php::record()
     *
     * Query Params:
     * - page: 页码
     * - limit: 每页数量
     * - keyword: 搜索关键词
     * - date: 日期范围
     *
     * Response:
     * {
     *   "list": [...],
     *   "count": 100
     * }
     */
    @GetMapping("/chat_records")
    @Operation(summary = "Chat History")
    public ApiResult<PageData<Map<String, Object>>> chatRecord(@RequestParam Map<String, Object> params) {
        PageData<Map<String, Object>> result = adminChatService.getChatRecords(params);
        return ApiResult.ok(result);
    }
}
