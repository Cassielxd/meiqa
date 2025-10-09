package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.SystemConfigMapper;
import io.renren.crmchat.dao.SystemConfigTabMapper;
import io.renren.crmchat.entity.SystemConfigEntity;
import io.renren.crmchat.entity.SystemConfigTabEntity;
import io.renren.crmchat.exception.CrmChatException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Admin Config Tab Service - 管理员配置分类管理
 * PHP Reference: ConfigTab.php
 *
 * 业务逻辑说明:
 * - 配置分类是全局的，不区分appid或level
 * - 支持父子层级关系（pid字段）
 * - 删除前需检查是否有下级配置
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class AdminConfigTabService {

    private final SystemConfigTabMapper systemConfigTabMapper;
    private final SystemConfigMapper systemConfigMapper;

    /**
     * 获取配置分类列表
     * PHP Reference: ConfigTab.php::index()
     * PHP逻辑: $list = get_tree_children($menusValue); return compact('list', 'count');
     *
     * @param filters 查询条件
     * @return 配置分类列表(树形结构)
     */
    public List<Map<String, Object>> getConfigTabList(Map<String, Object> filters) {
        QueryWrapper<SystemConfigTabEntity> wrapper = new QueryWrapper<>();

        // status筛选
        if (filters.containsKey("status") && filters.get("status") != null
            && !filters.get("status").toString().trim().isEmpty()) {
            wrapper.eq("status", filters.get("status"));
        }

        // title筛选
        if (filters.containsKey("title") && filters.get("title") != null
            && !filters.get("title").toString().trim().isEmpty()) {
            wrapper.like("title", filters.get("title").toString());
        }

        wrapper.orderByAsc("sort", "id");

        List<SystemConfigTabEntity> entityList = systemConfigTabMapper.selectList(wrapper);

        // PHP: foreach ($list as $item) { $menusValue[] = $item->getData(); }
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (SystemConfigTabEntity entity : entityList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", entity.getId());
            map.put("title", entity.getTitle());
            map.put("eng_title", entity.getEngTitle());
            map.put("status", entity.getStatus());
            map.put("type", entity.getType());
            map.put("sort", entity.getSort());
            map.put("pid", entity.getPid());
            map.put("icon", entity.getIcon());
            dataList.add(map);
        }

        // PHP: $list = get_tree_children($menusValue);
        return buildTree(dataList, 0);
    }

    /**
     * 构建树形结构
     * PHP: get_tree_children($list, $pid = 0)
     */
    private List<Map<String, Object>> buildTree(List<Map<String, Object>> list, Integer pid) {
        List<Map<String, Object>> tree = new ArrayList<>();

        for (Map<String, Object> item : list) {
            Integer itemPid = (Integer) item.get("pid");
            if (Objects.equals(itemPid, pid)) {
                List<Map<String, Object>> children = buildTree(list, (Integer) item.get("id"));
                if (!children.isEmpty()) {
                    item.put("children", children);
                }
                tree.add(item);
            }
        }

        return tree;
    }

    /**
     * 获取创建表单数据
     * PHP Reference: ConfigTab.php::create()
     *
     * @return 表单数据（父级分类列表）
     */
    public Map<String, Object> getCreateForm() {
        // 返回所有分类用于选择父级
        List<SystemConfigTabEntity> allTabs = systemConfigTabMapper.selectList(
            new QueryWrapper<SystemConfigTabEntity>()
                .eq("status", 1)
                .orderByAsc("sort", "id")
        );

        Map<String, Object> result = new HashMap<>();
        result.put("tabs", allTabs);
        return result;
    }

    /**
     * 创建配置分类
     * PHP Reference: ConfigTab.php::save()
     *
     * @param data 配置分类数据
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createConfigTab(Map<String, Object> data) {
        // PHP: if (!$data['title']) return $this->fail('请输入按钮名称');
        String title = (String) data.get("title");
        if (title == null || title.trim().isEmpty()) {
            throw new CrmChatException("Please enter button name");
        }

        SystemConfigTabEntity tab = new SystemConfigTabEntity();
        tab.setTitle(title);
        tab.setEngTitle((String) data.get("eng_title"));
        tab.setStatus((Integer) data.getOrDefault("status", 1));
        tab.setIcon((String) data.get("icon"));
        tab.setType((Integer) data.getOrDefault("type", 0));
        tab.setSort((Integer) data.getOrDefault("sort", 0));
        tab.setPid((Integer) data.getOrDefault("pid", 0));

        return systemConfigTabMapper.insert(tab) > 0;
    }

    /**
     * 获取编辑表单数据
     * PHP Reference: ConfigTab.php::edit()
     *
     * @param id 配置分类ID
     * @return 表单数据
     */
    public Map<String, Object> getEditForm(Integer id) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing update parameter");
        }

        SystemConfigTabEntity tab = systemConfigTabMapper.selectById(id);
        if (tab == null) {
            throw new CrmChatException("Data does not exist");
        }

        // 获取所有分类用于选择父级
        List<SystemConfigTabEntity> allTabs = systemConfigTabMapper.selectList(
            new QueryWrapper<SystemConfigTabEntity>()
                .eq("status", 1)
                .ne("id", id)  // 排除自己
                .orderByAsc("sort", "id")
        );

        Map<String, Object> result = new HashMap<>();
        result.put("tab", tab);
        result.put("tabs", allTabs);
        return result;
    }

    /**
     * 更新配置分类
     * PHP Reference: ConfigTab.php::update()
     *
     * @param id 配置分类ID
     * @param data 更新数据
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfigTab(Integer id, Map<String, Object> data) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Data does not exist");
        }

        SystemConfigTabEntity tab = systemConfigTabMapper.selectById(id);
        if (tab == null) {
            throw new CrmChatException("Data does not exist");
        }

        // PHP: if (!$data['title']) return $this->fail('请输入分类昵称');
        String title = (String) data.get("title");
        if (title == null || title.trim().isEmpty()) {
            throw new CrmChatException("Please enter category nickname");
        }

        // PHP: if (!$data['eng_title']) return $this->fail('请输入分类字段');
        String engTitle = (String) data.get("eng_title");
        if (engTitle == null || engTitle.trim().isEmpty()) {
            throw new CrmChatException("Please enter category field");
        }

        tab.setTitle(title);
        tab.setEngTitle(engTitle);
        tab.setStatus((Integer) data.getOrDefault("status", tab.getStatus()));
        tab.setIcon((String) data.get("icon"));
        tab.setType((Integer) data.getOrDefault("type", tab.getType()));
        tab.setSort((Integer) data.getOrDefault("sort", tab.getSort()));
        tab.setPid((Integer) data.getOrDefault("pid", tab.getPid()));

        return systemConfigTabMapper.updateById(tab) > 0;
    }

    /**
     * 删除配置分类
     * PHP Reference: ConfigTab.php::delete()
     *
     * @param id 配置分类ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConfigTab(Integer id) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Parameter error, please reopen");
        }

        // PHP: if ($services->count(['tab_id' => $id])) return $this->fail('存在下级配置，无法删除！');
        long count = systemConfigMapper.selectCount(
            new QueryWrapper<SystemConfigEntity>().eq("config_tab_id", id)
        );
        if (count > 0) {
            throw new CrmChatException("Subconfiguration exists, cannot delete");
        }

        SystemConfigTabEntity tab = systemConfigTabMapper.selectById(id);
        if (tab == null) {
            throw new CrmChatException("Deletion failed, please try again later");
        }

        return systemConfigTabMapper.deleteById(id) > 0;
    }

    /**
     * 更新配置分类状态
     * PHP Reference: ConfigTab.php::set_status()
     *
     * @param id 配置分类ID
     * @param status 状态值
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Integer id, Integer status) {
        if (id == null || id <= 0 || status == null) {
            throw new CrmChatException("Invalid parameter");
        }

        SystemConfigTabEntity tab = systemConfigTabMapper.selectById(id);
        if (tab == null) {
            throw new CrmChatException("Failed to modify");
        }

        tab.setStatus(status);
        return systemConfigTabMapper.updateById(tab) > 0;
    }
}
