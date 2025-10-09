package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.SystemMenusMapper;
import io.renren.crmchat.entity.SystemMenusEntity;
import io.renren.crmchat.exception.CrmChatException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin Menu Service - 管理员菜单管理
 * PHP Reference: SystemMenusServices.php, Menus.php
 *
 * 业务逻辑说明:
 * - 菜单系统是全局的，不区分appid或level
 * - path字段：前端传入数组，用"/"分隔存储
 * - params字段：JSON格式存储
 * - 支持父子层级关系（pid字段）
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class AdminMenuService {

    private final SystemMenusMapper systemMenusMapper;

    /**
     * 获取菜单列表
     * PHP Reference: Menus.php::index()
     *
     * @param filters 查询条件
     * @return 菜单列表
     */
    public List<SystemMenusEntity> getMenuList(Map<String, Object> filters) {
        QueryWrapper<SystemMenusEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("is_del", 0);

        // is_show筛选
        if (filters.containsKey("is_show") && filters.get("is_show") != null
            && !filters.get("is_show").toString().trim().isEmpty()) {
            wrapper.eq("is_show", filters.get("is_show"));
        }

        // keyword筛选（菜单名称）
        if (filters.containsKey("keyword") && filters.get("keyword") != null
            && !filters.get("keyword").toString().trim().isEmpty()) {
            wrapper.like("menu_name", filters.get("keyword").toString());
        }

        wrapper.orderByAsc("sort", "id");

        return systemMenusMapper.selectList(wrapper);
    }

    /**
     * 获取创建表单数据
     * PHP Reference: Menus.php::create()
     *
     * @return 表单数据（可能包含父级菜单列表）
     */
    public Map<String, Object> getCreateForm() {
        // PHP: return $this->services->createMenus();
        // 返回所有菜单用于选择父级
        List<SystemMenusEntity> allMenus = systemMenusMapper.selectList(
            new QueryWrapper<SystemMenusEntity>()
                .eq("is_del", 0)
                .orderByAsc("sort", "id")
        );

        Map<String, Object> result = new HashMap<>();
        result.put("menus", allMenus);
        return result;
    }

    /**
     * 创建菜单
     * PHP Reference: Menus.php::save()
     *
     * @param data 菜单数据
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createMenu(Map<String, Object> data) {
        // PHP: if (!$data['menu_name']) return $this->fail('请填写按钮名称');
        String menuName = (String) data.get("menu_name");
        if (menuName == null || menuName.trim().isEmpty()) {
            throw new CrmChatException("Please enter button name");
        }

        SystemMenusEntity menu = new SystemMenusEntity();
        menu.setMenuName(menuName);
        menu.setController((String) data.get("controller"));
        menu.setModule((String) data.getOrDefault("module", "admin"));
        menu.setAction((String) data.get("action"));
        menu.setIcon((String) data.get("icon"));
        menu.setParams((String) data.get("params"));

        // PHP: $data['path'] = implode('/', $data['path']);
        Object pathObj = data.get("path");
        if (pathObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> pathList = (List<String>) pathObj;
            menu.setPath(String.join("/", pathList));
        } else if (pathObj instanceof String) {
            menu.setPath((String) pathObj);
        }

        menu.setMenuPath((String) data.get("menu_path"));
        menu.setApiUrl((String) data.get("api_url"));
        menu.setMethods((String) data.get("methods"));
        menu.setUniqueAuth((String) data.get("unique_auth"));
        menu.setHeader((String) data.get("header"));
        menu.setIsHeader((Integer) data.getOrDefault("is_header", 0));
        menu.setPid((Integer) data.getOrDefault("pid", 0));
        menu.setSort((Integer) data.getOrDefault("sort", 0));
        menu.setAuthType((Integer) data.getOrDefault("auth_type", 0));
        menu.setAccess((Integer) data.getOrDefault("access", 1));
        menu.setIsShow((Integer) data.getOrDefault("is_show", 0));
        menu.setIsShowPath((Integer) data.getOrDefault("is_show_path", 0));
        menu.setIsDel(0);

        return systemMenusMapper.insert(menu) > 0;
    }

    /**
     * 获取菜单详情
     * PHP Reference: Menus.php::read()
     *
     * @param id 菜单ID
     * @return 菜单信息
     */
    public SystemMenusEntity getMenuInfo(Integer id) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Data does not exist");
        }

        SystemMenusEntity menu = systemMenusMapper.selectById(id);
        if (menu == null || menu.getIsDel() == 1) {
            throw new CrmChatException("Data does not exist");
        }

        return menu;
    }

    /**
     * 获取编辑表单数据
     * PHP Reference: Menus.php::edit()
     *
     * @param id 菜单ID
     * @return 表单数据
     */
    public Map<String, Object> getEditForm(Integer id) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing update parameter");
        }

        SystemMenusEntity menu = systemMenusMapper.selectById(id);
        if (menu == null || menu.getIsDel() == 1) {
            throw new CrmChatException("Data does not exist");
        }

        // 获取所有菜单用于选择父级
        List<SystemMenusEntity> allMenus = systemMenusMapper.selectList(
            new QueryWrapper<SystemMenusEntity>()
                .eq("is_del", 0)
                .ne("id", id)  // 排除自己
                .orderByAsc("sort", "id")
        );

        Map<String, Object> result = new HashMap<>();
        result.put("menu", menu);
        result.put("menus", allMenus);
        return result;
    }

    /**
     * 更新菜单
     * PHP Reference: Menus.php::update()
     *
     * @param id 菜单ID
     * @param data 更新数据
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMenu(Integer id, Map<String, Object> data) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Data does not exist");
        }

        SystemMenusEntity menu = systemMenusMapper.selectById(id);
        if (menu == null || menu.getIsDel() == 1) {
            throw new CrmChatException("Data does not exist");
        }

        // PHP: if (!$data['menu_name']) return $this->fail('请输入按钮名称');
        String menuName = (String) data.get("menu_name");
        if (menuName == null || menuName.trim().isEmpty()) {
            throw new CrmChatException("Please enter button name");
        }

        menu.setMenuName(menuName);
        menu.setController((String) data.get("controller"));
        menu.setModule((String) data.getOrDefault("module", "admin"));
        menu.setAction((String) data.get("action"));
        menu.setIcon((String) data.get("icon"));
        menu.setParams((String) data.get("params"));

        // PHP: $data['path'] = implode('/', $data['path']);
        Object pathObj = data.get("path");
        if (pathObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> pathList = (List<String>) pathObj;
            menu.setPath(String.join("/", pathList));
        } else if (pathObj instanceof String) {
            menu.setPath((String) pathObj);
        }

        menu.setMenuPath((String) data.get("menu_path"));
        menu.setApiUrl((String) data.get("api_url"));
        menu.setMethods((String) data.get("methods"));
        menu.setUniqueAuth((String) data.get("unique_auth"));
        menu.setHeader((String) data.get("header"));
        menu.setIsHeader((Integer) data.getOrDefault("is_header", menu.getIsHeader()));
        menu.setPid((Integer) data.getOrDefault("pid", menu.getPid()));
        menu.setSort((Integer) data.getOrDefault("sort", menu.getSort()));
        menu.setAuthType((Integer) data.getOrDefault("auth_type", menu.getAuthType()));
        menu.setAccess((Integer) data.getOrDefault("access", menu.getAccess()));
        menu.setIsShow((Integer) data.getOrDefault("is_show", menu.getIsShow()));
        menu.setIsShowPath((Integer) data.getOrDefault("is_show_path", menu.getIsShowPath()));

        return systemMenusMapper.updateById(menu) > 0;
    }

    /**
     * 删除菜单
     * PHP Reference: Menus.php::delete()
     *
     * @param id 菜单ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMenu(Integer id) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Parameter error, please reopen");
        }

        SystemMenusEntity menu = systemMenusMapper.selectById(id);
        if (menu == null) {
            throw new CrmChatException("Deletion failed, please try again later");
        }

        // 软删除
        menu.setIsDel(1);
        return systemMenusMapper.updateById(menu) > 0;
    }

    /**
     * 更新菜单显示状态
     * PHP Reference: Menus.php::show()
     *
     * @param id 菜单ID
     * @param isShow 显示状态
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMenuShow(Integer id, Integer isShow) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Parameter error, please reopen");
        }

        SystemMenusEntity menu = systemMenusMapper.selectById(id);
        if (menu == null) {
            throw new CrmChatException("Failed to modify");
        }

        menu.setIsShow(isShow);
        return systemMenusMapper.updateById(menu) > 0;
    }

    /**
     * 获取用户菜单列表（用于前端显示）
     * PHP Reference: Menus.php::menus()
     *
     * @param roles 角色权限字符串（逗号分隔的菜单ID）
     * @param level 管理员等级
     * @return 菜单树和权限标识
     */
    public Map<String, Object> getUserMenus(String roles, Integer level) {
        // TODO: 实现菜单权限过滤和树形结构构建
        // PHP: $this->services->getMenusList($this->adminInfo['roles'], (int)$this->adminInfo['level']);

        QueryWrapper<SystemMenusEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("is_del", 0);
        wrapper.eq("is_show", 1);
        wrapper.orderByAsc("sort", "id");

        List<SystemMenusEntity> allMenus = systemMenusMapper.selectList(wrapper);

        // 提取所有菜单的唯一权限标识
        List<String> uniqueAuth = allMenus.stream()
            .filter(menu -> menu.getUniqueAuth() != null && !menu.getUniqueAuth().isEmpty())
            .map(SystemMenusEntity::getUniqueAuth)
            .distinct()
            .collect(Collectors.toList());

        // TODO: 根据roles和level过滤菜单
        Map<String, Object> result = new HashMap<>();
        result.put("menus", allMenus);
        result.put("unique", uniqueAuth); // 唯一权限标识列表

        return result;
    }
}
