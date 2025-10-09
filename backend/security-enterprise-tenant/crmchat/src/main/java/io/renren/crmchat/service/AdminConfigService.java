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
 * Admin Config Service - 管理员配置管理
 * PHP Reference: Config.php
 *
 * 业务逻辑说明:
 * - 配置项存储在system_config表，按config_tab_id分组
 * - value字段存储JSON格式数据
 * - 支持多种类型：text, textarea, radio, checkbox, upload等
 * - save()方法：如果menu_name存在则更新，不存在则插入
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class AdminConfigService {

    private final SystemConfigMapper systemConfigMapper;
    private final SystemConfigTabMapper systemConfigTabMapper;

    /**
     * 获取配置列表
     * PHP Reference: Config.php::index()
     *
     * @param filters 查询条件
     * @return 配置列表
     */
    public List<SystemConfigEntity> getConfigList(Map<String, Object> filters) {
        // PHP: if (!$where['tab_id']) return $this->fail('参数错误');
        Object tabId = filters.get("tab_id");
        if (tabId == null || Integer.parseInt(tabId.toString()) == 0) {
            throw new CrmChatException("Invalid parameter");
        }

        QueryWrapper<SystemConfigEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("config_tab_id", tabId);

        // status筛选
        if (filters.containsKey("status") && filters.get("status") != null) {
            Integer status = Integer.parseInt(filters.get("status").toString());
            if (status != -1) {
                wrapper.eq("status", status);
            }
        }

        wrapper.orderByAsc("sort", "id");

        return systemConfigMapper.selectList(wrapper);
    }

    /**
     * 获取创建表单数据
     * PHP Reference: Config.php::create()
     *
     * @param type 类型
     * @param tabId 分类ID
     * @return 表单数据
     */
    public Map<String, Object> getCreateForm(String type, Integer tabId) {
        // TODO: 实现表单规则构建逻辑
        // PHP: $this->services->createFormRule($type, $tabId)
        Map<String, Object> result = new HashMap<>();
        result.put("type", type);
        result.put("tab_id", tabId);
        // 临时返回空表单规则
        result.put("form_rules", new ArrayList<>());
        return result;
    }

    /**
     * 保存配置（新建或更新）
     * PHP Reference: Config.php::save()
     *
     * @param data 配置数据
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveConfig(Map<String, Object> data) {
        // PHP: if (!$data['info']) return $this->fail('请输入配置名称');
        String info = (String) data.get("info");
        if (info == null || info.trim().isEmpty()) {
            throw new CrmChatException("Please enter configuration name");
        }

        // PHP: if (!$data['menu_name']) return $this->fail('请输入字段名称');
        String menuName = (String) data.get("menu_name");
        if (menuName == null || menuName.trim().isEmpty()) {
            throw new CrmChatException("Please enter field name");
        }

        // PHP: if (!$data['desc']) return $this->fail('请输入配置简介');
        String desc = (String) data.get("desc");
        if (desc == null || desc.trim().isEmpty()) {
            throw new CrmChatException("Please enter configuration description");
        }

        String type = (String) data.get("type");
        Integer width = (Integer) data.getOrDefault("width", 0);
        Integer high = (Integer) data.getOrDefault("high", 0);

        // PHP: if ($data['type'] == 'text')
        if ("text".equals(type)) {
            if (width == null || width == 0) {
                throw new CrmChatException("Please enter textbox width");
            }
            if (width <= 0) {
                throw new CrmChatException("Please enter a valid textbox width");
            }
        }

        // PHP: if ($data['type'] == 'textarea')
        if ("textarea".equals(type)) {
            if (width == null || width == 0) {
                throw new CrmChatException("Please enter textarea width");
            }
            if (high == null || high == 0) {
                throw new CrmChatException("Please enter textarea height");
            }
            if (width < 0 || high < 0) {
                throw new CrmChatException("Please enter a valid textarea width");
            }
        }

        // PHP: if ($data['type'] == 'radio' || $data['type'] == 'checkbox')
        if ("radio".equals(type) || "checkbox".equals(type)) {
            String parameter = (String) data.get("parameter");
            if (parameter == null || parameter.trim().isEmpty()) {
                throw new CrmChatException("Please enter configuration parameters");
            }
        }

        // PHP: $config = $this->services->getOne(['menu_name' => $data['menu_name']]);
        SystemConfigEntity existingConfig = systemConfigMapper.selectOne(
            new QueryWrapper<SystemConfigEntity>().eq("menu_name", menuName)
        );

        if (existingConfig != null) {
            // 更新现有配置
            existingConfig.setInfo(info);
            existingConfig.setType(type);
            existingConfig.setInputType((String) data.get("input_type"));
            existingConfig.setConfigTabId((Integer) data.get("config_tab_id"));
            existingConfig.setParameter((String) data.get("parameter"));
            existingConfig.setUploadType((String) data.get("upload_type"));
            existingConfig.setRequired((String) data.get("required"));
            existingConfig.setWidth(width);
            existingConfig.setHigh(high);
            existingConfig.setValue(data.get("value").toString()); // JSON格式
            existingConfig.setDesc(desc);
            existingConfig.setSort((Integer) data.getOrDefault("sort", 0));
            existingConfig.setStatus((Integer) data.get("status"));

            return systemConfigMapper.updateById(existingConfig) > 0;
        } else {
            // 创建新配置
            SystemConfigEntity config = new SystemConfigEntity();
            config.setMenuName(menuName);
            config.setInfo(info);
            config.setType(type);
            config.setInputType((String) data.get("input_type"));
            config.setConfigTabId((Integer) data.get("config_tab_id"));
            config.setParameter((String) data.get("parameter"));
            config.setUploadType((String) data.get("upload_type"));
            config.setRequired((String) data.get("required"));
            config.setWidth(width);
            config.setHigh(high);
            config.setValue(data.get("value").toString()); // JSON格式
            config.setDesc(desc);
            config.setSort((Integer) data.getOrDefault("sort", 0));
            config.setStatus((Integer) data.get("status"));

            return systemConfigMapper.insert(config) > 0;
        }
    }

    /**
     * 获取配置详情
     * PHP Reference: Config.php::read()
     *
     * @param id 配置ID
     * @return 配置信息
     */
    public SystemConfigEntity getConfigInfo(Integer id) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Parameter error, please reopen");
        }

        SystemConfigEntity config = systemConfigMapper.selectById(id);
        if (config == null) {
            throw new CrmChatException("Data does not exist");
        }

        return config;
    }

    /**
     * 获取编辑表单数据
     * PHP Reference: Config.php::edit()
     *
     * @param id 配置ID
     * @return 表单数据
     */
    public Map<String, Object> getEditForm(Integer id) {
        SystemConfigEntity config = getConfigInfo(id);

        // TODO: 实现表单规则构建逻辑
        // PHP: $this->services->editConfigForm((int)$id)
        Map<String, Object> result = new HashMap<>();
        result.put("config", config);
        result.put("form_rules", new ArrayList<>()); // 临时返回空表单规则
        return result;
    }

    /**
     * 更新配置
     * PHP Reference: Config.php::update()
     *
     * @param id 配置ID
     * @param data 更新数据
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfig(Integer id, Map<String, Object> data) {
        // PHP: if (!$this->services->get($id)) return $this->fail('编辑的记录不存在!');
        SystemConfigEntity config = systemConfigMapper.selectById(id);
        if (config == null) {
            throw new CrmChatException("Record to edit does not exist");
        }

        // PHP: $data['value'] = json_encode($data['value']);
        config.setStatus((Integer) data.get("status"));
        config.setInfo((String) data.get("info"));
        config.setDesc((String) data.get("desc"));
        config.setSort((Integer) data.get("sort"));
        config.setConfigTabId((Integer) data.get("config_tab_id"));
        config.setRequired((String) data.get("required"));
        config.setParameter((String) data.get("parameter"));
        config.setValue(data.get("value").toString()); // JSON格式
        config.setUploadType((String) data.get("upload_type"));
        config.setInputType((String) data.get("input_type"));

        return systemConfigMapper.updateById(config) > 0;
    }

    /**
     * 删除配置
     * PHP Reference: Config.php::delete()
     *
     * @param id 配置ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConfig(Integer id) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Parameter error, please reopen");
        }

        SystemConfigEntity config = systemConfigMapper.selectById(id);
        if (config == null) {
            throw new CrmChatException("Deletion failed, please try again later");
        }

        return systemConfigMapper.deleteById(id) > 0;
    }

    /**
     * 更新配置状态
     * PHP Reference: Config.php::set_status()
     *
     * @param id 配置ID
     * @param status 状态值
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Integer id, Integer status) {
        if (id == null || id == 0 || status == null) {
            throw new CrmChatException("Invalid parameter");
        }

        SystemConfigEntity config = systemConfigMapper.selectById(id);
        if (config == null) {
            throw new CrmChatException("Failed to modify");
        }

        config.setStatus(status);
        return systemConfigMapper.updateById(config) > 0;
    }

    /**
     * 获取基础配置表单
     * PHP Reference: Config.php::edit_basics()
     *
     * @param tabId 分类ID
     * @return 配置表单数据
     */
    public Map<String, Object> getConfigForm(Integer tabId) {
        if (tabId == null || tabId == 0) {
            throw new CrmChatException("Invalid parameter");
        }

        // TODO: 实现配置表单构建逻辑
        // PHP: $this->services->getConfigForm($url, $tabId)
        List<SystemConfigEntity> configs = systemConfigMapper.selectList(
            new QueryWrapper<SystemConfigEntity>()
                .eq("config_tab_id", tabId)
                .eq("status", 1)
                .orderByAsc("sort", "id")
        );

        Map<String, Object> result = new HashMap<>();
        result.put("configs", configs);
        result.put("form_rules", new ArrayList<>()); // 临时返回空表单规则
        return result;
    }

    /**
     * 批量保存基础配置
     * PHP Reference: Config.php::save_basics()
     *
     * @param data 配置数据（多个字段的键值对）
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBasics(Map<String, Object> data) {
        // PHP: foreach ($post as $k => $v) { ... }
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String menuName = entry.getKey();
            Object value = entry.getValue();

            // PHP: $config_one = $this->services->getOne(['menu_name' => $k]);
            SystemConfigEntity config = systemConfigMapper.selectOne(
                new QueryWrapper<SystemConfigEntity>().eq("menu_name", menuName)
            );

            if (config != null) {
                // PHP: $this->services->update($k, ['value' => json_encode($v)], 'menu_name');
                config.setValue(value.toString()); // JSON格式
                systemConfigMapper.updateById(config);
            }
        }

        return true;
    }

    /**
     * 获取配置分类头部
     * PHP Reference: Config.php::header_basics()
     *
     * @param type 类型
     * @param pid 父级ID
     * @return 分类列表
     */
    public Map<String, Object> getHeaderBasics(Integer type, Integer pid) {
        Map<String, Object> result = new HashMap<>();

        if (type == 3) {
            // PHP: if ($type == 3) { $config_tab = []; }
            result.put("config_tab", new ArrayList<>());
        } else {
            // PHP: $config_tab = $services->getConfigTab($pid);
            List<SystemConfigTabEntity> tabs = systemConfigTabMapper.selectList(
                new QueryWrapper<SystemConfigTabEntity>()
                    .eq("pid", pid)
                    .eq("status", 1)
                    .orderByAsc("sort", "id")
            );

            // Convert to Map to add 'value' and 'label' fields for frontend compatibility
            // PHP: ['id', 'id as value', 'title as label', 'pid', 'icon', 'type']
            // Frontend expects: config[0].value and config[0].label
            List<Map<String, Object>> tabList = tabs.stream()
                .map(tab -> {
                    Map<String, Object> tabMap = new HashMap<>();
                    tabMap.put("id", tab.getId());
                    tabMap.put("value", tab.getId()); // PHP: 'id as value'
                    tabMap.put("label", tab.getTitle()); // PHP: 'title as label'
                    tabMap.put("pid", tab.getPid());
                    tabMap.put("icon", tab.getIcon());
                    tabMap.put("type", tab.getType());
                    tabMap.put("sort", tab.getSort());
                    tabMap.put("eng_title", tab.getEngTitle());
                    tabMap.put("status", tab.getStatus());
                    // PHP uses get_tree_children() to build nested structure with 'children' array
                    return tabMap;
                })
                .collect(java.util.stream.Collectors.toList());

            // Build tree structure with children (mimicking PHP's get_tree_children)
            List<Map<String, Object>> treeList = buildTreeStructure(tabList);
            result.put("config_tab", treeList);
        }

        return result;
    }

    /**
     * 设置客服图标
     * PHP Reference: Config.php::setKefuIcon()
     *
     * @param kefuIconUrl3 客服图标URL
     * @param kefuIconType 客服图标类型
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean setKefuIcon(String kefuIconUrl3, String kefuIconType) {
        // PHP: $this->services->update('kefu_icon_url3', ['value' => json_encode($kefuIconUrl)], 'menu_name');
        SystemConfigEntity config1 = systemConfigMapper.selectOne(
            new QueryWrapper<SystemConfigEntity>().eq("menu_name", "kefu_icon_url3")
        );
        if (config1 != null) {
            config1.setValue(kefuIconUrl3);
            systemConfigMapper.updateById(config1);
        }

        // PHP: $this->services->update('kefu_icon_type', ['value' => json_encode($kefuIconType)], 'menu_name');
        SystemConfigEntity config2 = systemConfigMapper.selectOne(
            new QueryWrapper<SystemConfigEntity>().eq("menu_name", "kefu_icon_type")
        );
        if (config2 != null) {
            config2.setValue(kefuIconType);
            systemConfigMapper.updateById(config2);
        }

        return true;
    }

    /**
     * 获取客服图标
     * PHP Reference: Config.php::getKefuIcon()
     *
     * @return 客服图标配置
     */
    public Map<String, String> getKefuIcon() {
        Map<String, String> result = new HashMap<>();

        // PHP: 'kefu_icon_url1' => sys_config('kefu_icon_url1')
        String[] keys = {"kefu_icon_url1", "kefu_icon_url2", "kefu_icon_url3", "kefu_icon_type"};
        for (String key : keys) {
            SystemConfigEntity config = systemConfigMapper.selectOne(
                new QueryWrapper<SystemConfigEntity>().eq("menu_name", key)
            );
            result.put(key, config != null ? config.getValue() : "");
        }

        return result;
    }

    /**
     * Build tree structure with children (mimicking PHP's get_tree_children)
     * PHP Reference: app/services/system/config/SystemConfigTabServices.php::getConfigTab()
     *
     * @param flatList flat list of config tabs
     * @return tree structure with children arrays
     */
    private List<Map<String, Object>> buildTreeStructure(List<Map<String, Object>> flatList) {
        // Group by pid
        Map<Integer, List<Map<String, Object>>> grouped = new HashMap<>();
        List<Map<String, Object>> rootItems = new ArrayList<>();

        for (Map<String, Object> item : flatList) {
            Integer pid = (Integer) item.get("pid");
            if (pid == null || pid == 0) {
                rootItems.add(item);
            } else {
                grouped.computeIfAbsent(pid, k -> new ArrayList<>()).add(item);
            }
        }

        // Attach children to parents recursively
        attachChildren(rootItems, grouped);

        return rootItems;
    }

    /**
     * Recursively attach children to parent items
     */
    private void attachChildren(List<Map<String, Object>> items, Map<Integer, List<Map<String, Object>>> grouped) {
        for (Map<String, Object> item : items) {
            Integer id = (Integer) item.get("id");
            List<Map<String, Object>> children = grouped.get(id);
            if (children != null && !children.isEmpty()) {
                item.put("children", children);
                // Recursively attach children to these children
                attachChildren(children, grouped);
            }
        }
    }
}
