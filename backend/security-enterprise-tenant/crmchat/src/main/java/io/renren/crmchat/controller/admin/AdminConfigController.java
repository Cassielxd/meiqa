package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.entity.SystemConfigEntity;
import io.renren.crmchat.service.AdminConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin Config Controller - 管理员配置管理
 * PHP Reference: /app/controller/admin/system/Config.php
 *
 * 功能说明:
 * 1. GET /api/admin/setting/config - 配置列表
 * 2. GET /api/admin/setting/config/create - 获取创建表单
 * 3. POST /api/admin/setting/config - 保存配置
 * 4. GET /api/admin/setting/config/:id - 获取配置详情
 * 5. GET /api/admin/setting/config/:id/edit - 获取编辑表单
 * 6. PUT /api/admin/setting/config/:id - 更新配置
 * 7. DELETE /api/admin/setting/config/:id - 删除配置
 * 8. PUT /api/admin/setting/config/set_status/:id/:status - 修改状态
 * 9. GET /api/admin/setting/config/edit_basics - 基础配置表单
 * 10. POST /api/admin/setting/config/save_basics - 批量保存配置
 * 11. GET /api/admin/setting/config/header_basics - 配置分类头部
 * 12. POST /api/admin/setting/config/kefu_icon - 设置客服图标
 * 13. GET /api/admin/setting/config/kefu_icon - 获取客服图标
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/admin/setting")
@Tag(name = "Admin Setting - 配置管理")
@AllArgsConstructor
public class AdminConfigController {

    private final AdminConfigService adminConfigService;

    /**
     * 获取配置列表
     * GET /api/admin/setting/config
     *
     * PHP Reference: Config.php::index()
     *
     * Query Parameters:
     * - tab_id: 配置分类ID（必填）
     * - status: 状态筛选（-1=全部，0=隐藏，1=显示）
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "menu_name": "site_name",
     *     "info": "网站名称",
     *     ...
     *   }
     * ]
     */
    @GetMapping("/config")
    @Operation(summary = "Get Configuration List")
    public ApiResult<Map<String, Object>> getConfigList(
            @RequestParam Integer tab_id,
            @RequestParam(required = false, defaultValue = "-1") Integer status) {

        Map<String, Object> filters = new java.util.HashMap<>();
        filters.put("tab_id", tab_id);
        filters.put("status", status);

        Map<String, Object> result = adminConfigService.getConfigList(filters);
        return ApiResult.ok(result);
    }

    /**
     * 获取创建配置表单
     * GET /api/admin/setting/config/create
     *
     * PHP Reference: Config.php::create()
     *
     * Query Parameters:
     * - type: 类型
     * - tab_id: 分类ID（默认1）
     *
     * Response:
     * {
     *   "type": "text",
     *   "tab_id": 1,
     *   "form_rules": [...]
     * }
     */
    @GetMapping("/config/create")
    @Operation(summary = "Get Create Configuration Form")
    public ApiResult<Map<String, Object>> getCreateForm(
            @RequestParam(required = false, defaultValue = "") String type,
            @RequestParam(required = false, defaultValue = "1") Integer tab_id) {

        Map<String, Object> result = adminConfigService.getCreateForm(type, tab_id);
        return ApiResult.ok(result);
    }

    /**
     * 保存配置
     * POST /api/admin/setting/config
     *
     * PHP Reference: Config.php::save()
     *
     * Request Body:
     * {
     *   "menu_name": "site_name",
     *   "type": "text",
     *   "input_type": "",
     *   "config_tab_id": 1,
     *   "parameter": "",
     *   "upload_type": "",
     *   "required": "",
     *   "width": 100,
     *   "high": 0,
     *   "value": "My Website",
     *   "info": "网站名称",
     *   "desc": "网站名称配置",
     *   "sort": 0,
     *   "status": 1
     * }
     *
     * Response: { "code": 0, "msg": "Configuration added successfully" }
     */
    @PostMapping("/config")
    @Operation(summary = "保存配置")
    public ApiResult<String> saveConfig(@RequestBody Map<String, Object> data) {
        adminConfigService.saveConfig(data);
        return ApiResult.ok("Configuration added successfully", "success");
    }

    /**
     * 获取配置详情
     * GET /api/admin/setting/config/:id
     *
     * PHP Reference: Config.php::read()
     *
     * Response:
     * {
     *   "info": {
     *     "id": 1,
     *     "menu_name": "site_name",
     *     ...
     *   }
     * }
     */
    @GetMapping("/config/{id}")
    @Operation(summary = "获取配置详情")
    public ApiResult<Map<String, Object>> getConfigInfo(@PathVariable Integer id) {
        SystemConfigEntity config = adminConfigService.getConfigInfo(id);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("info", config);
        return ApiResult.ok(result);
    }

    /**
     * 获取编辑配置表单
     * GET /api/admin/setting/config/:id/edit
     *
     * PHP Reference: Config.php::edit()
     *
     * Response:
     * {
     *   "config": {...},
     *   "form_rules": [...]
     * }
     */
    @GetMapping("/config/{id}/edit")
    @Operation(summary = "Get Edit Configuration Form")
    public ApiResult<Map<String, Object>> getEditForm(@PathVariable Integer id) {
        Map<String, Object> result = adminConfigService.getEditForm(id);
        return ApiResult.ok(result);
    }

    /**
     * 更新配置
     * PUT /api/admin/setting/config/:id
     *
     * PHP Reference: Config.php::update()
     *
     * Request Body:
     * {
     *   "status": 1,
     *   "info": "网站名称",
     *   "desc": "网站名称配置",
     *   "sort": 0,
     *   "config_tab_id": 1,
     *   "required": "",
     *   "parameter": "",
     *   "value": "我的新网站",
     *   "upload_type": "",
     *   "input_type": ""
     * }
     *
     * Response: { "code": 0, "msg": "修改成功!" }
     */
    @PutMapping("/config/{id}")
    @Operation(summary = "更新配置")
    public ApiResult<String> updateConfig(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        boolean success = adminConfigService.updateConfig(id, data);
        if (success) {
            return ApiResult.ok("Updated successfully", "success");
        } else {
            return ApiResult.fail("Update failed");
        }
    }

    /**
     * 删除配置
     * DELETE /api/admin/setting/config/:id
     *
     * PHP Reference: Config.php::delete()
     *
     * Response: { "code": 0, "msg": "Deleted successfully" }
     */
    @DeleteMapping("/config/{id}")
    @Operation(summary = "删除配置")
    public ApiResult<String> deleteConfig(@PathVariable Integer id) {
        boolean success = adminConfigService.deleteConfig(id);
        if (success) {
            return ApiResult.ok("Deleted successfully", "success");
        } else {
            return ApiResult.fail("Delete failed, please try again later");
        }
    }

    /**
     * 修改配置状态
     * PUT /api/admin/setting/config/set_status/:id/:status
     *
     * PHP Reference: Config.php::set_status()
     *
     * Response: { "code": 0, "msg": "隐藏成功" } 或 { "code": 0, "msg": "显示成功" }
     */
    @PutMapping("/config/set_status/{id}/{status}")
    @Operation(summary = "修改配置状态")
    public ApiResult<String> updateStatus(@PathVariable Integer id, @PathVariable Integer status) {
        adminConfigService.updateStatus(id, status);
        return ApiResult.ok(status == 0 ? "Hidden successfully" : "Shown successfully", "success");
    }

    /**
     * 获取基础配置表单
     * GET /api/admin/setting/config/edit_basics
     *
     * PHP Reference: Config.php::edit_basics()
     *
     * Query Parameters:
     * - tab_id: 分类ID（默认1）
     *
     * Response:
     * {
     *   "configs": [...],
     *   "form_rules": [...]
     * }
     */
    @GetMapping("/config/edit_basics")
    @Operation(summary = "获取基础配置表单")
    public ApiResult<Map<String, Object>> getConfigForm(
            @RequestParam(required = false, defaultValue = "1") Integer tab_id) {

        Map<String, Object> result = adminConfigService.getConfigForm(tab_id);
        return ApiResult.ok(result);
    }

    /**
     * 批量保存基础配置
     * POST /api/admin/setting/config/save_basics
     *
     * PHP Reference: Config.php::save_basics()
     *
     * Request Body: 配置项的键值对
     * {
     *   "site_name": "新网站名称",
     *   "site_url": "https://example.com",
     *   "site_logo": "https://example.com/logo.png",
     *   ...
     * }
     *
     * Response: { "code": 0, "msg": "修改成功" }
     */
    @PostMapping("/config/save_basics")
    @Operation(summary = "批量保存基础配置")
    public ApiResult<String> saveBasics(@RequestBody Map<String, Object> data) {
        adminConfigService.saveBasics(data);
        return ApiResult.ok("Updated successfully", "success");
    }

    /**
     * 获取配置分类头部
     * GET /api/admin/setting/config/header_basics
     *
     * PHP Reference: Config.php::header_basics()
     *
     * Query Parameters:
     * - type: 类型（默认0）
     * - pid: 父级ID（默认0）
     *
     * Response:
     * {
     *   "config_tab": [...]
     * }
     */
    @GetMapping("/config/header_basics")
    @Operation(summary = "获取配置分类头部")
    public ApiResult<Map<String, Object>> getHeaderBasics(
            @RequestParam(required = false, defaultValue = "0") Integer type,
            @RequestParam(required = false, defaultValue = "0") Integer pid) {

        Map<String, Object> result = adminConfigService.getHeaderBasics(type, pid);
        return ApiResult.ok(result);
    }

    /**
     * 设置客服图标
     * POST /api/admin/setting/config/kefu_icon
     *
     * PHP Reference: Config.php::setKefuIcon()
     *
     * Request Body:
     * {
     *   "kefu_icon_url3": "https://example.com/icon.png",
     *   "kefu_icon_type": "1"
     * }
     *
     * Response: { "code": 0, "msg": "保存成功" }
     */
    @PostMapping("/config/kefu_icon")
    @Operation(summary = "设置客服图标")
    public ApiResult<String> setKefuIcon(@RequestBody Map<String, Object> data) {
        String kefuIconUrl3 = (String) data.get("kefu_icon_url3");
        String kefuIconType = (String) data.get("kefu_icon_type");

        adminConfigService.setKefuIcon(kefuIconUrl3, kefuIconType);
        return ApiResult.ok("Saved successfully", "success");
    }

    /**
     * 获取客服图标
     * GET /api/admin/setting/config/kefu_icon
     *
     * PHP Reference: Config.php::getKefuIcon()
     *
     * Response:
     * {
     *   "kefu_icon_url1": "...",
     *   "kefu_icon_url2": "...",
     *   "kefu_icon_url3": "...",
     *   "kefu_icon_type": "..."
     * }
     */
    @GetMapping("/config/kefu_icon")
    @Operation(summary = "获取客服图标")
    public ApiResult<Map<String, String>> getKefuIcon() {
        Map<String, String> result = adminConfigService.getKefuIcon();
        return ApiResult.ok(result);
    }
}
