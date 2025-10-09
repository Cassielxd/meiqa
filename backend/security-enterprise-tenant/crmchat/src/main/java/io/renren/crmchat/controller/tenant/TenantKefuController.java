package io.renren.crmchat.controller.tenant;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.ChatServiceEntity;
import io.renren.crmchat.entity.ChatServiceGroupEntity;
import io.renren.crmchat.service.TenantKefuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.renren.crmchat.security.TenantSecurityUtils.requireAppid;

/**
 * Tenant API - 客服管理
 * PHP Reference: /app/controller/tenant/Service.php
 *
 * @author CRMChat Team
 */
@Slf4j
@RestController
@RequestMapping("/api/tenant/chat/kefu")
@Tag(name = "Tenant - Customer Service Management")
@AllArgsConstructor
public class TenantKefuController {

    private final TenantKefuService tenantKefuService;

    /**
     * 更新客服
     * PUT /api/tenant/kefu/:id
     *
     * PHP Reference: Service.php::update()
     *
     * Request Body:
     * {
     *   "group_id": 1,               // 可选
     *   "nickname": "Customer Service Agent Wang",       // 可选
     *   "password": "newpass",       // 可选，修改密码时提供
     *   "true_password": "newpass",  // password不为空时必须
     *   "phone": "13800138000",      // 可选
     *   "avatar": "http://...",      // 可选
     *   "welcome_words": "您好",     // 可选
     *   "auto_reply": 1,             // 可选
     *   "status": 1                  // 可选
     * }
     *
     * Response: { "code": 0, "msg": "更新成功" }
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update Customer Service Agent")
    public ApiResult<String> update(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> data) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        String appid = requireAppid();
        log.info("[租户API] kefu/update - appid={}, kefuId={}", appid, id);

        tenantKefuService.updateKefu(id, data);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 删除客服
     * DELETE /api/tenant/kefu/:id
     *
     * PHP Reference: Service.php::delete()
     *
     * Response: { "code": 0, "msg": "删除成功" }
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Customer Service Agent")
    public ApiResult<String> delete(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        String appid = requireAppid();
        log.info("[租户API] kefu/delete - appid={}, kefuId={}", appid, id);

        tenantKefuService.deleteKefu(id);
        return ApiResult.ok("Deleted successfully", "success");
    }

    /**
     * 更新客服状态
     * PUT /api/tenant/kefu/status/:id
     *
     * PHP Reference: Service.php::updateStatus()
     *
     * Request Body:
     * {
     *   "status": 1  // 0-禁用，1-启用
     * }
     *
     * Response: { "code": 0, "msg": "状态更新成功" }
     */
    @PutMapping("/status/{id}")
    @Operation(summary = "更新客服状态")
    public ApiResult<String> updateStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> data) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        if (!data.containsKey("status") || data.get("status") == null) {
            return ApiResult.fail("Please provide status value");
        }

        Integer status = Integer.parseInt(data.get("status").toString());
        tenantKefuService.updateKefuStatus(id, status);
        String msg = status == 0 ? "Hidden successfully" : "Displayed successfully";
        return ApiResult.ok(msg, "success");
    }

    /**
     * 获取客服分组列表
     * GET /api/tenant/kefu/groups
     *
     * PHP Reference: Service.php::groups()
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "name": "Technical Support Group",
     *     "sort": 100,
     *     "appid": "wx123",
     *     "create_time": "2025-01-01 10:00:00",
     *     "update_time": "2025-01-01 10:00:00"
     *   }
     * ]
     */
    @GetMapping("/groups")
    @Operation(summary = "获取客服分组列表")
    public ApiResult<List<ChatServiceGroupEntity>> groups() {
        String appid = requireAppid();
        log.info("[租户API] kefu/groups - appid={}", appid);

        List<ChatServiceGroupEntity> groups = tenantKefuService.getKefuGroups();
        return ApiResult.ok(groups);
    }

    /**
     * 创建客服分组
     * POST /api/tenant/kefu/group
     *
     * PHP Reference: ServiceGroup.php::save($id=null)
     *
     * Request Body:
     * {
     *   "name": "Technical Support Group",  // 必填
     *   "sort": 100          // 可选，默认0
     * }
     *
     * Response: { "code": 0, "msg": "Added successfully" }
     */
    @PostMapping("/group")
    @Operation(summary = "创建客服分组")
    public ApiResult<String> createGroup(@RequestBody Map<String, Object> data) {
        String appid = requireAppid();
        log.info("[租户API] kefu/group/create - appid={}", appid);

        tenantKefuService.createKefuGroup(data);
        return ApiResult.ok("Added successfully", "success");
    }

    /**
     * 更新客服分组
     * PUT /api/tenant/kefu/group/:id
     *
     * PHP Reference: ServiceGroup.php::save($id)
     *
     * Request Body:
     * {
     *   "name": "技术支持组",  // 必填
     *   "sort": 200          // 可选
     * }
     *
     * Response: { "code": 0, "msg": "修改成功" }
     */
    @PutMapping("/group/{id}")
    @Operation(summary = "更新客服分组")
    public ApiResult<String> updateGroup(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> data) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        tenantKefuService.updateKefuGroup(id, data);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 删除客服分组
     * DELETE /api/tenant/kefu/group/:id
     *
     * PHP Reference: ServiceGroup.php::delete($id)
     *
     * 业务逻辑:
     * - 检查是否有客服关联此分组
     * - 如果有，返回错误"请先解除客服关联"
     * - 如果没有，删除分组
     *
     * Response: { "code": 0, "msg": "删除成功" }
     */
    @DeleteMapping("/group/{id}")
    @Operation(summary = "Delete Customer Service Group")
    public ApiResult<String> deleteGroup(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        tenantKefuService.deleteKefuGroup(id);
        return ApiResult.ok("Deleted successfully", "success");
    }

    /**
     * 获取客服分组详情
     * GET /api/tenant/kefu/group/:id
     *
     * PHP Reference: ServiceGroup.php::create($id)
     *
     * Response:
     * {
     *   "id": 1,
     *   "name": "技术支持组",
     *   "sort": 100,
     *   "appid": "wx123",
     *   "create_time": "2025-01-01 10:00:00",
     *   "update_time": "2025-01-01 10:00:00"
     * }
     */
    @GetMapping("/group/{id}")
    @Operation(summary = "获取客服分组详情")
    public ApiResult<ChatServiceGroupEntity> getGroupDetail(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        ChatServiceGroupEntity group = tenantKefuService.getKefuGroupDetail(id);
        return ApiResult.ok(group);
    }

    /**
     * 获取客服列表
     * GET /api/tenant/kefu/list
     *
     * PHP Reference: Service.php::list()
     *
     * Query Parameters:
     * - nickname: 昵称（可选，模糊搜索）
     * - status: 状态（可选，0-禁用，1-启用）
     * - group_id: 分组ID（可选）
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "nickname": "Customer Service Agent Wang",
     *     "account": "kefu001",
     *     "group_id": 1,
     *     "phone": "13800138000",
     *     "avatar": "http://...",
     *     "welcome_words": "您好",
     *     "auto_reply": 1,
     *     "status": 1,
     *     "appid": "wx123",
     *     "create_time": "2025-01-01 10:00:00"
     *   }
     * ]
     */
    @GetMapping
    @Operation(summary = "获取客服列表")
    public ApiResult<Map<String, Object>> list(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer group_id,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "15") Integer limit) {

        String appid = requireAppid();
        log.info("[租户API] kefu/list - appid={}, page={}, limit={}", appid, page, limit);

        // 构建过滤条件
        Map<String, Object> filters = new HashMap<>();
        if (nickname != null) {
            filters.put("nickname", nickname);
        }
        if (status != null) {
            filters.put("status", status);
        }
        if (group_id != null) {
            filters.put("group_id", group_id);
        }

        List<ChatServiceEntity> list = tenantKefuService.getKefuList(filters);
        long count = tenantKefuService.getKefuCount(filters);

        // 返回格式与PHP一致：{ list: [...], count: 123 }
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("count", count);

        return ApiResult.ok(result);
    }

    /**
     * 获取客服详情
     * GET /api/tenant/kefu/:id
     *
     * PHP Reference: Service.php::read()
     *
     * Response:
     * {
     *   "id": 1,
     *   "nickname": "Customer Service Agent Wang",
     *   "account": "kefu001",
     *   "group_id": 1,
     *   "phone": "13800138000",
     *   "avatar": "http://...",
     *   "welcome_words": "您好",
     *   "auto_reply": 1,
     *   "status": 1,
     *   "appid": "wx123",
     *   "create_time": "2025-01-01 10:00:00",
     *   "update_time": "2025-01-01 10:00:00"
     * }
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取客服详情")
    public ApiResult<ChatServiceEntity> detail(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        String appid = requireAppid();
        log.info("[租户API] kefu/detail - appid={}, kefuId={}", appid, id);

        ChatServiceEntity kefu = tenantKefuService.getKefuDetail(id);
        return ApiResult.ok(kefu);
    }

    /**
     * 创建客服
     * POST /api/tenant/kefu/create
     *
     * PHP Reference: Service.php::save()
     *
     * Request Body:
     * {
     *   "nickname": "客服小王",          // 必填
     *   "account": "kefu001",           // 必填
     *   "password": "123456",           // 必填
     *   "true_password": "123456",      // 必填
     *   "group_id": 1,                  // 可选
     *   "phone": "13800138000",         // 可选
     *   "avatar": "http://...",         // 可选
     *   "welcome_words": "您好",        // 可选
     *   "auto_reply": 1,                // 可选
     *   "status": 1                     // 可选
     * }
     *
     * Response: { "code": 0, "msg": "创建成功", "data": { "id": 1 } }
     */
    /**
     * 获取创建客服表单配置
     * GET /api/tenant/chat/kefu/add
     *
     * PHP Reference: Service.php::add() -> createKefuForTent()
     *
     * Response: {
     *   "status": 200,
     *   "msg": "ok",
     *   "data": {
     *     "rules": [...],      // 表单规则
     *     "title": "添加客服",
     *     "action": "/api/tenant/kefu/create",
     *     "method": "POST",
     *     "info": "",
     *     "status": true
     *   }
     * }
     */
    @GetMapping("/add")
    @Operation(summary = "获取创建客服表单配置")
    public ApiResult<Map<String, Object>> getCreateForm() {
        Map<String, Object> formConfig = tenantKefuService.getCreateFormConfig();
        return ApiResult.ok(formConfig);
    }

    /**
     * 获取编辑客服表单配置
     * GET /api/tenant/chat/kefu/{id}/edit
     *
     * PHP Reference: Service.php::edit($id) -> ChatServiceServices::edit()
     *
     * Response: {
     *   "status": 200,
     *   "msg": "ok",
     *   "data": {
     *     "rules": [...],      // 表单规则（带现有数据作为默认值）
     *     "title": "编辑客服",
     *     "action": "/chat/kefu/16",
     *     "method": "PUT",
     *     "info": "",
     *     "status": true
     *   }
     * }
     */
    @GetMapping("/{id}/edit")
    @Operation(summary = "获取编辑客服表单配置")
    public ApiResult<Map<String, Object>> getEditForm(@PathVariable Integer id) {
        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        Map<String, Object> formConfig = tenantKefuService.getEditFormConfig(id);
        return ApiResult.ok(formConfig);
    }

    @PostMapping("/create")
    @Operation(summary = "Create Customer Service Agent")
    public ApiResult<Map<String, Object>> create(@RequestBody Map<String, Object> data) {

        // 从UserContext获取当前租户的appid
        String tenantAppid = io.renren.crmchat.security.UserContext.getAppid();
        if (tenantAppid == null || tenantAppid.isEmpty()) {
            return ApiResult.fail("Unable to retrieve tenant information, please log in again");
        }

        log.info("[租户API] kefu/create - appid={}", tenantAppid);

        Integer newId = tenantKefuService.createKefu(data);

        Map<String, Object> result = new HashMap<>();
        result.put("id", newId);
        return ApiResult.ok("Customer service representative added successfully", result);
    }

    /**
     * 客服登录（管理员代登录）
     * GET /api/tenant/chat/kefu/login/:id
     *
     * PHP Reference: Service.php::keufLogin($id)
     *
     * Response: {
     *   "code": 0,
     *   "msg": "ok",
     *   "data": {
     *     "token": "jwt_token_string",
     *     "exp_time": 1234567890,
     *     "kefuInfo": {...}
     *   }
     * }
     */
    @GetMapping("/login/{id}")
    @Operation(summary = "Customer Service Login (Admin Proxy Login)")
    public ApiResult<Map<String, Object>> login(@PathVariable Integer id) {

        if (id == null || id <= 0) {
            return ApiResult.fail("Invalid parameters");
        }

        String appid = requireAppid();
        log.info("[租户API] kefu/login - appid={}, kefuId={}", appid, id);

        Map<String, Object> loginResult = tenantKefuService.kefuLogin(id);
        return ApiResult.ok(loginResult);
    }
}
