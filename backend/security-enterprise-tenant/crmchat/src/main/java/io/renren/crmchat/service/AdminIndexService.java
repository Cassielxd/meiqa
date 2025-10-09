package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatUserMapper;
import io.renren.crmchat.dao.SystemMenusMapper;
import io.renren.crmchat.dao.SystemRoleMapper;
import io.renren.crmchat.dto.ChartDataDTO;
import io.renren.crmchat.dto.ChartStatisticsDTO;
import io.renren.crmchat.dto.ChartSumDTO;
import io.renren.crmchat.entity.SystemMenusEntity;
import io.renren.crmchat.entity.SystemRoleEntity;
import io.renren.crmchat.security.UserContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin Index Service - 管理员首页/统计
 * PHP Reference: Index.php
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class AdminIndexService {

    private final ChatUserMapper chatUserMapper;
    private final SystemMenusMapper systemMenusMapper;
    private final SystemRoleMapper systemRoleMapper;
    private final AdminChartService adminChartService;

    /**
     * 获取logo
     * PHP Reference: Index.php::logo()
     */
    public Map<String, Object> getLogo() {
        Map<String, Object> result = new HashMap<>();
        // TODO: 从系统配置中获取logo
        result.put("logo", "");
        result.put("logo_square", "");
        result.put("site_name", "CRMChat");
        return result;
    }

    /**
     * 消息通知
     * PHP Reference: Index.php::jnotice()
     */
    public List<Object> getJNotice() {
        // TODO: 实现消息通知逻辑
        return new ArrayList<>();
    }

    /**
     * 获取菜单列表
     * PHP Reference: Index.php::getMenusList()
     */
    public List<Map<String, Object>> getMenusList() {
        List<SystemMenusEntity> menus = systemMenusMapper.selectList(
                new QueryWrapper<SystemMenusEntity>()
                        .eq("is_show", 1)
                        .eq("auth_type", 1)
                        .eq("is_del", 0)
                        .eq("is_show_path", 0)
        );

        Map<Integer, List<SystemMenusEntity>> childrenMap = new HashMap<>();
        for (SystemMenusEntity menu : menus) {
            Integer parentId = menu.getPid() == null ? 0 : menu.getPid();
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(menu);
        }

        Comparator<SystemMenusEntity> comparator = (a, b) -> {
            int sortCompare = Integer.compare(b.getSort(), a.getSort());
            if (sortCompare != 0) {
                return sortCompare;
            }
            return Integer.compare(a.getId(), b.getId());
        };

        menus.sort(comparator);

        for (List<SystemMenusEntity> list : childrenMap.values()) {
            list.sort(comparator);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        List<SystemMenusEntity> roots = childrenMap.getOrDefault(0, Collections.emptyList());
        for (SystemMenusEntity root : roots) {
            buildMenuList(root, 1, childrenMap, result, visited);
        }

        // 防止存在未挂载父级的菜单导致缺失
        if (result.size() < menus.size()) {
            for (SystemMenusEntity menu : menus) {
                if (!visited.contains(menu.getId())) {
                    buildMenuList(menu, 1, childrenMap, result, visited);
                }
            }
        }

        return result;
    }

    private void buildMenuList(SystemMenusEntity menu,
                               int depth,
                               Map<Integer, List<SystemMenusEntity>> childrenMap,
                               List<Map<String, Object>> result,
                               Set<Integer> visited) {

        if (!visited.add(menu.getId())) {
            return;
        }

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", menu.getId());
        item.put("pid", menu.getPid() == null ? 0 : menu.getPid());
        item.put("menu_name", menu.getMenuName());
        item.put("menu_path", menu.getMenuPath());
        item.put("unique_auth", menu.getUniqueAuth());
        item.put("sort", menu.getSort());
        item.put("type", childrenMap.getOrDefault(menu.getId(), Collections.emptyList()).isEmpty() ? 0 : 1);
        item.put("html", buildHtmlString(depth));
        result.add(item);

        List<SystemMenusEntity> children = childrenMap.get(menu.getId());
        if (children == null || children.isEmpty()) {
            return;
        }

        // children already sorted
        for (SystemMenusEntity child : children) {
            buildMenuList(child, depth + 1, childrenMap, result, visited);
        }
    }

    private String buildHtmlString(int depth) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            builder.append("|-----");
        }
        return builder.toString();
    }

    /**
     * 获取所有菜单的唯一权限标识（基于用户角色过滤）
     * 用于前端v-auth指令进行权限控制
     *
     * PHP Reference: SystemMenusServices::getMenusList()
     * 核心逻辑：
     * 1. 获取当前用户的角色IDs（roles字段）
     * 2. 根据角色IDs查询对应的菜单权限（system_role表的rules字段）
     * 3. 根据菜单IDs查询unique_auth字段
     *
     * @return 权限标识列表
     */
    public List<String> getMenusUniqueAuth() {
        // 获取当前用户的角色和等级
        String rolesStr = UserContext.getRoles();
        Integer level = UserContext.getLevel();

        // 如果level为0（超级管理员）或roles为空，返回所有菜单权限
        if (level != null && level == 0) {
            return getAllMenusUniqueAuth();
        }

        if (rolesStr == null || rolesStr.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 解析角色IDs（逗号分隔）
        List<Integer> roleIds = Arrays.stream(rolesStr.split(","))
                .filter(s -> !s.trim().isEmpty())
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询这些角色对应的菜单权限（rules字段）
        List<SystemRoleEntity> roles = systemRoleMapper.selectList(
                new QueryWrapper<SystemRoleEntity>()
                        .in("id", roleIds)
                        .eq("status", 1)
        );

        // 合并所有角色的rules字段（菜单ID列表）
        Set<Integer> menuIds = new HashSet<>();
        for (SystemRoleEntity role : roles) {
            String rules = role.getRules();
            if (rules != null && !rules.trim().isEmpty()) {
                Arrays.stream(rules.split(","))
                        .filter(s -> !s.trim().isEmpty())
                        .map(String::trim)
                        .map(Integer::parseInt)
                        .forEach(menuIds::add);
            }
        }

        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 根据菜单IDs查询unique_auth（这里才是关键 - 只查询用户有权限的菜单）
        List<SystemMenusEntity> menus = systemMenusMapper.selectList(
                new QueryWrapper<SystemMenusEntity>()
                        .in("id", menuIds)
                        .isNotNull("unique_auth")
                        .ne("unique_auth", "")
        );

        // 提取unique_auth字段
        return menus.stream()
                .map(SystemMenusEntity::getUniqueAuth)
                .filter(auth -> auth != null && !auth.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取所有菜单的权限标识（超级管理员使用）
     * 注意：包括is_show=0的按钮权限（如"添加标签"、"添加分类"等按钮操作）
     */
    private List<String> getAllMenusUniqueAuth() {
        List<SystemMenusEntity> menus = systemMenusMapper.selectList(
                new QueryWrapper<SystemMenusEntity>()
                        .eq("is_del", 0)
                        // 不过滤is_show，因为按钮权限is_show=0也需要返回
                        .isNotNull("unique_auth")
                        .ne("unique_auth", "")
        );

        return menus.stream()
                .map(SystemMenusEntity::getUniqueAuth)
                .filter(auth -> auth != null && !auth.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 客户统计
     * PHP Reference: Index.php::sum()
     */
    public ChartSumDTO getKefuSum(String appid) {
        return adminChartService.getKefuSum(appid);
    }

    /**
     * 客户首页统计
     * PHP Reference: Index.php::index()
     */
    public ChartStatisticsDTO getKefuStatistics(String appid, Integer type, Integer year, Integer month) {
        if (month <= 0 || month > 12) {
            throw new IllegalArgumentException("Invalid month");
        }
        if (year < 2000 || year > 2100) {
            throw new IllegalArgumentException("Invalid year");
        }

        return adminChartService.getKefuStatistics(type, year, month, appid);
    }
}
