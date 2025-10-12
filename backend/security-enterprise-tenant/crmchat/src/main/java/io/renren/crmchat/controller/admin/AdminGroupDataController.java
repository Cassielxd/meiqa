package io.renren.crmchat.controller.admin;

import io.renren.crmchat.common.result.ApiResult;
import io.renren.crmchat.service.SystemGroupDataService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 组合数据管理控制器
 * PHP Reference: app/controller/admin/system/GroupData.php
 *
 * 功能说明:
 * - 获取客服页面广告 (kf_adv)
 * - 获取客服图标 (kf_icon)
 * - 获取隐私协议 (privacy)
 * - 其他组合数据的查询
 *
 * PHP路由示例:
 * - GET /api/admin/setting/group_data/kf_adv
 * - GET /api/admin/setting/group_data/privacy
 * - GET /api/admin/setting/group_data/kf_icon
 *
 * @author CRMChat Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/setting/group_data")
@AllArgsConstructor
public class AdminGroupDataController {

    private final SystemGroupDataService systemGroupDataService;

    /**
     * 根据配置名称获取组合数据列表
     * PHP Reference: GroupData::index() + getConfigNameValue()
     *
     * URL格式: /api/admin/setting/group_data/{configName}
     * 例如:
     * - /api/admin/setting/group_data/kf_adv  (客服页面广告)
     * - /api/admin/setting/group_data/privacy (隐私协议)
     * - /api/admin/setting/group_data/kf_icon (客服图标)
     *
     * @param configName 配置名称 (URL路径参数)
     * @param status 状态筛选 (可选,0:禁用 1:启用)
     * @param page 页码 (默认1)
     * @param limit 每页数量 (默认20)
     * @return {list: [], count: N}
     */
    @GetMapping("/{configName}")
    public ApiResult<Map<String, Object>> getGroupData(
            @PathVariable String configName,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {

        log.info("=== Querying group data === configName: {}, status: {}, page: {}, limit: {}",
                configName, status, page, limit);

        // PHP: $this->services->getGroupDataList($where)
        Map<String, Object> result = systemGroupDataService.getGroupDataByConfigName(
                configName, status, page, limit
        );

        return ApiResult.ok(result);
    }
}
