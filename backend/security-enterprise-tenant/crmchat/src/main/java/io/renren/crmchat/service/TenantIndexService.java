package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatUserMapper;
import io.renren.crmchat.dao.SystemConfigMapper;
import io.renren.crmchat.dao.SystemMenusMapper;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.entity.SystemConfigEntity;
import io.renren.crmchat.entity.SystemMenusEntity;
import io.renren.crmchat.security.TenantSecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Tenant 首页服务
 * PHP Reference: /app/controller/tenant/Index.php
 * PHP Service: /app/services/chat/ChatUserServices.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getLogo(): 获取logo信息（从配置读取）
 * 2. getJnotice(): 获取通知（返回空数组）
 * 3. getMenusList(): 获取菜单列表（需要SystemMenusServices）
 * 4. getCustomerSum(): 客户统计（调用ChatUserServices.getKefuSum）
 * 5. getIndexStatistics(): 首页统计（调用ChatUserServices.getKefuStatistics）
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantIndexService {

    private final ChatUserMapper chatUserMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final SystemMenusMapper systemMenusMapper;
    private final ObjectMapper objectMapper;

    /**
     * 获取Logo信息
     * GET /api/tenant/logo
     *
     * PHP Reference: Index.php::logo()
     *
     * 业务逻辑:
     * 1. 从系统配置读取logo、logo_square、site_name
     *
     * PHP代码:
     * return $this->success([
     *     'logo'        => link_url(sys_config('site_logo')),
     *     'logo_square' => link_url(sys_config('site_logo_square')),
     *     'site_name'   => sys_config('site_name')
     * ]);
     *
     * 注意: 这里简化实现，返回默认值。实际使用时应从配置表读取
     *
     * @return Map包含logo、logo_square、site_name
     */
    public Map<String, String> getLogo() {
        TenantSecurityUtils.requireAppid();

        List<String> keys = Arrays.asList("site_logo", "site_logo_square", "site_name");
        QueryWrapper<SystemConfigEntity> wrapper = new QueryWrapper<>();
        wrapper.in("menu_name", keys);

        List<SystemConfigEntity> configs = systemConfigMapper.selectList(wrapper);
        Map<String, String> valueMap = new HashMap<>();
        configs.forEach(config -> valueMap.put(config.getMenuName(), decodeConfigValue(config.getValue())));

        Map<String, String> result = new HashMap<>();
        result.put("logo", valueMap.getOrDefault("site_logo", ""));
        result.put("logo_square", valueMap.getOrDefault("site_logo_square", ""));
        result.put("site_name", valueMap.getOrDefault("site_name", ""));
        return result;
    }

    /**
     * 获取通知
     * GET /api/tenant/jnotice
     *
     * PHP Reference: Index.php::jnotice()
     *
     * PHP代码:
     * public function jnotice()
     * {
     *     return $this->success([]);
     * }
     *
     * @return 空列表
     */
    public List<Object> getJnotice() {
        // PHP: return $this->success([]);
        return new ArrayList<>();
    }

    /**
     * 获取菜单列表
     * GET /api/tenant/menus
     *
     * PHP Reference: Index.php::getMenusList()
     *
     * 业务逻辑:
     * 1. 调用SystemMenusServices.getSearchList()获取菜单列表
     * 2. 处理菜单层级关系
     * 3. 返回树形结构
     *
     * PHP代码:
     * $menusServices = app()->make(SystemMenusServices::class);
     * $list = $menusServices->getSearchList();
     * ...
     * return app('json')->success(sort_list_tier($data));
     *
     * 注意: 这里简化实现，返回空列表。实际使用时应调用SystemMenusService
     *
     * @return 菜单列表
     */
    public List<Map<String, Object>> getMenusList() {
        TenantSecurityUtils.requireAppid();

        QueryWrapper<SystemMenusEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("is_del", 0);
        wrapper.eq("is_show", 1);
        wrapper.eq("auth_type", 1);
        wrapper.eq("is_show_path", 0);
        wrapper.orderByDesc("sort");
        wrapper.orderByDesc("id");

        List<SystemMenusEntity> menus = systemMenusMapper.selectList(wrapper);
        Set<Integer> parentIds = new HashSet<>();
        menus.stream()
                .map(SystemMenusEntity::getPid)
                .filter(pid -> pid != null && pid > 0)
                .forEach(parentIds::add);

        List<Map<String, Object>> nodes = new ArrayList<>();
        for (SystemMenusEntity menu : menus) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", menu.getId());
            node.put("pid", menu.getPid());
            node.put("menu_name", menu.getMenuName());
            node.put("controller", menu.getController());
            node.put("module", menu.getModule());
            node.put("action", menu.getAction());
            node.put("icon", menu.getIcon());
            node.put("params", menu.getParams());
            node.put("path", menu.getPath());
            node.put("menu_path", menu.getMenuPath());
            node.put("api_url", menu.getApiUrl());
            node.put("methods", menu.getMethods());
            node.put("unique_auth", menu.getUniqueAuth());
            node.put("header", menu.getHeader());
            node.put("is_header", menu.getIsHeader());
            node.put("sort", menu.getSort());
            node.put("auth_type", menu.getAuthType());
            node.put("access", menu.getAccess());
            node.put("is_show", menu.getIsShow());
            node.put("is_show_path", menu.getIsShowPath());
            node.put("is_del", menu.getIsDel());
            node.put("type", parentIds.contains(menu.getId()) ? 1 : 0);
            nodes.add(node);
        }

        return buildMenuTree(nodes);
    }

    /**
     * 客户统计
     * GET /api/tenant/sum
     *
     * PHP Reference:
     * - Controller: Index.php::sum()
     * - Service: ChatUserServices.php::getKefuSum()
     *
     * 业务逻辑:
     * 1. 统计全部客户数（all）
     * 2. 统计今日客户数（toDayKefu，排除游客）
     * 3. 统计本月客户数（month）
     * 4. 统计今日游客数（toDayTourist）
     *
     * PHP代码:
     * public function getKefuSum(string $appid = '')
     * {
     *     $all = $this->dao->count(['appid' => $appid]);
     *     $toDayKefu = $this->dao->count(['time' => 'today', 'appid' => $appid, 'is_tourist' => 0]);
     *     $month = $this->dao->count(['time' => 'month', 'appid' => $appid]);
     *     $toDayTourist = $this->dao->count(['time' => 'today', 'appid' => $appid, 'is_tourist' => 1]);
     *     return compact('all', 'toDayKefu', 'month', 'toDayTourist');
     * }
     *
     * @return Map包含all、toDayKefu、month、toDayTourist
     */
    public Map<String, Object> getCustomerSum() {
        TenantSecurityUtils.requireAppid();

        // PHP: $all = $this->dao->count(['appid' => $appid]);
        long all = chatUserMapper.selectCount(new QueryWrapper<>());

        // PHP: $toDayKefu = $this->dao->count(['time' => 'today', 'appid' => $appid, 'is_tourist' => 0]);
        LocalDate today = LocalDate.now();
        String todayStart = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00";
        String todayEnd = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 23:59:59";

        QueryWrapper<io.renren.crmchat.entity.ChatUserEntity> todayKefuWrapper = new QueryWrapper<>();
        todayKefuWrapper.eq("is_tourist", 0);
        todayKefuWrapper.ge("create_time", todayStart);
        todayKefuWrapper.le("create_time", todayEnd);
        long toDayKefu = chatUserMapper.selectCount(todayKefuWrapper);

        // PHP: $month = $this->dao->count(['time' => 'month', 'appid' => $appid]);
        YearMonth currentMonth = YearMonth.now();
        String monthStart = currentMonth.atDay(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00";
        String monthEnd = currentMonth.atEndOfMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 23:59:59";

        QueryWrapper<io.renren.crmchat.entity.ChatUserEntity> monthWrapper = new QueryWrapper<>();
        monthWrapper.ge("create_time", monthStart);
        monthWrapper.le("create_time", monthEnd);
        long month = chatUserMapper.selectCount(monthWrapper);

        // PHP: $toDayTourist = $this->dao->count(['time' => 'today', 'appid' => $appid, 'is_tourist' => 1]);
        QueryWrapper<io.renren.crmchat.entity.ChatUserEntity> todayTouristWrapper = new QueryWrapper<>();
        todayTouristWrapper.eq("is_tourist", 1);
        todayTouristWrapper.ge("create_time", todayStart);
        todayTouristWrapper.le("create_time", todayEnd);
        long toDayTourist = chatUserMapper.selectCount(todayTouristWrapper);

        // PHP: return compact('all', 'toDayKefu', 'month', 'toDayTourist');
        Map<String, Object> result = new HashMap<>();
        result.put("all", all);
        result.put("toDayKefu", toDayKefu);
        result.put("month", month);
        result.put("toDayTourist", toDayTourist);
        return result;
    }

    /**
     * 首页统计
     * GET /api/tenant/index
     *
     * PHP Reference:
     * - Controller: Index.php::index()
     * - Service: ChatUserServices.php::getKefuStatistics()
     *
     * 业务逻辑:
     * 1. 验证参数（year、month）
     * 2. 根据type（0=年度，1=月度）计算时间范围
     * 3. 统计正式客户和游客数据
     *
     * PHP代码:
     * public function index(ChatUserServices $services)
     * {
     *     $type = $this->request->get('type', 0);
     *     $year = $this->request->get('year', date('Y'));
     *     $month = $this->request->get('month', date('m'));
     *     if ($month <= 0 || $month > 12) return $this->fail('月份错误');
     *     if ($year[0] > 2) return $this->fail('年份错误');
     *     if (strlen($year) > 4) return $this->fail('年份错误');
     *     $appid = $this->request->tenantAppid();
     *     return $this->success($services->getKefuStatistics(0, (int)$type, (int)$year, (int)$month, $appid));
     * }
     *
     * @param type         类型（0=年度统计，1=月度统计）
     * @param year         年份
     * @param month        月份
     * @return Map包含list和tourist
     */
    public Map<String, Object> getIndexStatistics(Integer type, Integer year, Integer month) {
        TenantSecurityUtils.requireAppid();

        // PHP: if ($month <= 0 || $month > 12) return $this->fail('月份错误');
        if (month <= 0 || month > 12) {
            throw new CrmChatException("Invalid month");
        }

        // PHP: if ($year[0] > 2) return $this->fail('年份错误');
        String yearStr = year.toString();
        if (yearStr.charAt(0) > '2') {
            throw new CrmChatException("Invalid year");
        }

        // PHP: if (strlen($year) > 4) return $this->fail('年份错误');
        if (yearStr.length() > 4) {
            throw new CrmChatException("Invalid year");
        }

        // PHP: return $this->success($services->getKefuStatistics(0, (int)$type, (int)$year, (int)$month, $appid));

        // TODO: 实现完整的统计查询逻辑
        // PHP中调用 ChatUserDao::kefuStatistics() 进行复杂的聚合查询
        // 需要按时间维度（年/月）统计客户数据
        // 当前返回空结构

        Map<String, Object> result = new HashMap<>();
        result.put("list", new ArrayList<>());
        result.put("tourist", new ArrayList<>());
        return result;
    }

    private String decodeConfigValue(String rawValue) {
        if (rawValue == null) {
            return "";
        }
        String trimmed = rawValue.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        try {
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                JsonNode node = objectMapper.readTree(trimmed);
                if (node.isTextual()) {
                    return node.asText();
                }
                if (node.has("value")) {
                    return node.get("value").asText("");
                }
                return node.toString();
            }
            return objectMapper.readValue(trimmed, String.class);
        } catch (Exception ignore) {
            // fall back to simple unescape
        }

        String unescaped = trimmed.replace("\\/", "/");
        if (unescaped.startsWith("\"") && unescaped.endsWith("\"") && unescaped.length() >= 2) {
            unescaped = unescaped.substring(1, unescaped.length() - 1);
        }
        return unescaped;
    }

    private List<Map<String, Object>> buildMenuTree(List<Map<String, Object>> nodes) {
        Map<Integer, Map<String, Object>> indexed = new LinkedHashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();

        for (Map<String, Object> node : nodes) {
            Integer id = (Integer) node.get("id");
            if (id != null) {
                indexed.put(id, node);
            }
        }

        for (Map<String, Object> node : nodes) {
            Integer pid = (Integer) node.get("pid");
            if (pid != null && pid > 0 && indexed.containsKey(pid)) {
                Map<String, Object> parent = indexed.get(pid);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                if (children == null) {
                    children = new ArrayList<>();
                    parent.put("children", children);
                }
                children.add(node);
            } else {
                roots.add(node);
            }
        }

        return roots;
    }
}
