package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.AppVersionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * APP版本管理控制器
 * PHP Reference: app/controller/admin/system/AppVersion.php
 */
@RestController
@RequestMapping("/api/admin/setting/app")
@AllArgsConstructor
public class AdminAppVersionController {

    private final AppVersionService appVersionService;

    @GetMapping("/version")
    public ApiResult<Map<String, Object>> getList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        return ApiResult.ok(appVersionService.getList(page, limit));
    }
}
