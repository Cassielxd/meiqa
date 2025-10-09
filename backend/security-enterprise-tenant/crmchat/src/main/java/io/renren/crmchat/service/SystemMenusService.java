package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.SystemMenusMapper;
import io.renren.crmchat.entity.SystemMenusEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统菜单服务
 * 参考PHP: app/services/system/SystemMenusServices.php
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
@Slf4j
public class SystemMenusService {

    private final SystemMenusMapper systemMenusMapper;

    /**
     * 获取菜单列表和权限数组
     * 参考PHP: SystemMenusServices::getMenusList($rouleId, int $level)
     * PHP返回树形结构：顶级菜单 + children数组
     *
     * @param roles 角色IDs（逗号分隔，如"1"或"1,2,3"）
     * @param level 管理员等级（0=超级管理员，拥有所有权限）
     * @return [菜单树形列表, 权限数组]
     */
    public MenusResult getMenusList(String roles, Integer level) {
        log.info("获取菜单列表 - roles: {}, level: {}", roles, level);

        // 参考PHP: SystemMenusDao::getMenusRoule() 的查询条件
        // 菜单树只包含：is_show=1 && auth_type=1 && is_show_path=0 的菜单
        // 注意：auth_type=2 是接口类型，不应该在菜单树中显示
        QueryWrapper<SystemMenusEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_del", 0);         // 未删除
        queryWrapper.eq("is_show", 1);        // 显示的菜单
        queryWrapper.eq("auth_type", 1);      // 菜单类型（不包括接口类型）
        queryWrapper.eq("is_show_path", 0);   // 非隐藏路径
        queryWrapper.orderByDesc("sort", "id");

        List<SystemMenusEntity> menusList = systemMenusMapper.selectList(queryWrapper);
        log.info("查询到 {} 条菜单记录", menusList.size());

        // 权限列表需要单独查询，包括所有unique_auth不为空的记录（包括按钮权限）
        // 参考PHP: SystemMenusDao::getMenusUnique() 的逻辑
        QueryWrapper<SystemMenusEntity> authQueryWrapper = new QueryWrapper<>();
        authQueryWrapper.eq("is_del", 0);
        authQueryWrapper.isNotNull("unique_auth");
        authQueryWrapper.ne("unique_auth", "");

        List<SystemMenusEntity> allMenusWithAuth = systemMenusMapper.selectList(authQueryWrapper);
        List<String> uniqueAuth = allMenusWithAuth.stream()
                .map(SystemMenusEntity::getUniqueAuth)
                .filter(auth -> auth != null && !auth.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        log.info("权限列表: {}", uniqueAuth);

        // 构建树形菜单结构（参考PHP返回格式）
        List<Map<String, Object>> menusTree = buildMenusTree(menusList);
        log.info("构建树形菜单，顶级菜单数: {}", menusTree.size());

        return new MenusResult(menusTree, uniqueAuth);
    }

    /**
     * 构建菜单树形结构
     * 参考PHP返回格式：顶级菜单数组，每个菜单可能包含children数组
     *
     * PHP实际返回示例：
     * [
     *   {"path":"/admin/home/","title":"统计","icon":"md-home","header":"home","is_header":1},
     *   {"path":"/admin/user","title":"用户管理","icon":"md-person","header":"user","is_header":1,
     *    "children":[
     *      {"path":"/admin/user/list","title":"用户列表","icon":"","header":"user","is_header":1},
     *      ...
     *    ]}
     * ]
     */
    private List<Map<String, Object>> buildMenusTree(List<SystemMenusEntity> menusList) {
        // 先将所有菜单按id索引
        Map<Integer, Map<String, Object>> menuMap = new HashMap<>();
        Map<Integer, SystemMenusEntity> entityMap = new HashMap<>();

        for (SystemMenusEntity menu : menusList) {
            Map<String, Object> menuData = new HashMap<>();
            menuData.put("path", menu.getMenuPath() != null ? menu.getMenuPath() : "");
            menuData.put("title", menu.getMenuName() != null ? menu.getMenuName() : "");
            menuData.put("icon", menu.getIcon() != null ? menu.getIcon() : "");
            menuData.put("header", menu.getHeader() != null ? menu.getHeader() : "");
            menuData.put("is_header", menu.getIsHeader() != null ? menu.getIsHeader() : 0);

            menuMap.put(menu.getId(), menuData);
            entityMap.put(menu.getId(), menu);
        }

        // 构建树形结构：遍历所有菜单，将子菜单添加到父菜单的children数组
        List<Map<String, Object>> rootMenus = new ArrayList<>();

        for (SystemMenusEntity menu : menusList) {
            Map<String, Object> menuData = menuMap.get(menu.getId());

            if (menu.getPid() == 0) {
                // 顶级菜单
                rootMenus.add(menuData);
            } else {
                // 子菜单：添加到父菜单的children数组
                Map<String, Object> parentMenu = menuMap.get(menu.getPid());
                if (parentMenu != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parentMenu.get("children");
                    if (children == null) {
                        children = new ArrayList<>();
                        parentMenu.put("children", children);
                    }
                    children.add(menuData);
                }
            }
        }

        return rootMenus;
    }

    /**
     * 菜单查询结果封装
     */
    public static class MenusResult {
        private final List<Map<String, Object>> menus;
        private final List<String> uniqueAuth;

        public MenusResult(List<Map<String, Object>> menus, List<String> uniqueAuth) {
            this.menus = menus;
            this.uniqueAuth = uniqueAuth;
        }

        public List<Map<String, Object>> getMenus() {
            return menus;
        }

        public List<String> getUniqueAuth() {
            return uniqueAuth;
        }
    }
}
