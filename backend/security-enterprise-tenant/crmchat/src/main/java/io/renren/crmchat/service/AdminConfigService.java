package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.crmchat.dao.SystemConfigMapper;
import io.renren.crmchat.dao.SystemConfigTabMapper;
import io.renren.crmchat.entity.SystemConfigEntity;
import io.renren.crmchat.entity.SystemConfigTabEntity;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.formbuilder.FormBuilder;
import io.renren.crmchat.formbuilder.FormHelper;
import io.renren.crmchat.formbuilder.components.BaseComponent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    private final FormBuilder formBuilder;
    private final AdminConfigTabService adminConfigTabService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // PHP: protected $cuttingStr = '=>';
    private static final String CUTTING_STR = "=>";

    /**
     * 获取配置列表
     * PHP Reference: Config.php::index() + SystemConfigServices::getConfigList()
     *
     * PHP逻辑:
     * 1. 查询配置列表
     * 2. 对每个配置项：
     *    - 解码value JSON字符串
     *    - 如果是radio/checkbox，转换value为显示文本
     *    - 如果是upload，处理文件URL
     * 3. 返回 {count: 总数, list: [配置列表]}
     *
     * @param filters 查询条件
     * @return {count: 总数, list: [配置列表]}
     */
    public Map<String, Object> getConfigList(Map<String, Object> filters) {
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

        List<SystemConfigEntity> list = systemConfigMapper.selectList(wrapper);
        Long count = systemConfigMapper.selectCount(wrapper);

        // PHP: foreach ($list as &$item) { 处理value字段 }
        List<Map<String, Object>> processedList = new ArrayList<>();
        for (SystemConfigEntity item : list) {
            Map<String, Object> itemMap = entityToMap(item);

            // PHP: $item['value'] = $item['value'] ? json_decode($item['value'], true) ?: '' : '';
            String valueStr = item.getValue();
            Object value = "";
            if (valueStr != null && !valueStr.isEmpty()) {
                try {
                    // 尝试JSON解码
                    value = objectMapper.readValue(valueStr, Object.class);
                    if (value == null) {
                        value = "";
                    }
                } catch (Exception e) {
                    // 如果解码失败，保持原字符串
                    value = valueStr;
                }
            }
            itemMap.put("value", value);

            // TODO: 实现radio/checkbox值转换和upload文件URL处理
            // PHP: if ($item['type'] == 'radio' || $item['type'] == 'checkbox')
            // PHP: if ($item['type'] == 'upload' && !empty($item['value']))

            processedList.add(itemMap);
        }

        // PHP: return compact('count', 'list');
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("list", processedList);
        return result;
    }

    /**
     * Entity转Map
     */
    private Map<String, Object> entityToMap(SystemConfigEntity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("menu_name", entity.getMenuName());
        map.put("type", entity.getType());
        map.put("input_type", entity.getInputType());
        map.put("config_tab_id", entity.getConfigTabId());
        map.put("parameter", entity.getParameter());
        map.put("upload_type", entity.getUploadType());
        map.put("required", entity.getRequired());
        map.put("width", entity.getWidth());
        map.put("high", entity.getHigh());
        map.put("info", entity.getInfo());
        map.put("desc", entity.getDesc());
        map.put("sort", entity.getSort());
        map.put("status", entity.getStatus());
        return map;
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
     * PHP Reference: Config.php::edit() + SystemConfigServices::editConfigForm()
     *
     * PHP代码:
     * public function editConfigForm(int $id)
     * {
     *     $configInfo = $this->dao->get($id);
     *     $f = [];
     *     $f[] = Form::input('menu_name', '字段变量')->disabled(true);
     *     $f[] = Form::input('type', '类型')->disabled(true);
     *     $f[] = Form::select('config_tab_id', '分类');
     *     $f[] = Form::input('info', '配置名称', $configInfo->getData('info'));
     *     $f[] = Form::textarea('desc', '配置简介', $configInfo->getData('desc'));
     *
     *     switch ($configInfo->getData('type')) {
     *         case 'text':
     *             $f[] = Form::select('input_type', '类型', $configInfo->getData('input_type'));
     *             $f[] = Form::input('value', '默认值', $configInfo->getData('value'));
     *             $f[] = Form::number('width', '文本框宽度', (int)$configInfo->getData('width'));
     *             $f[] = Form::radio('required', '是否必填', $configInfo->getData('required'));
     *             break;
     *         case 'textarea':
     *             $f[] = Form::textarea('value', '默认值', $configInfo->getData('value'));
     *             $f[] = Form::number('width', '文本框宽度', (int)$configInfo->getData('width'));
     *             $f[] = Form::number('high', '文本框高度', (int)$configInfo->getData('high'));
     *             break;
     *         case 'radio':
     *             $f[] = Form::radio('value', '默认值', $configInfo->getData('value'));
     *             $f[] = Form::textarea('parameter', '配置参数');
     *             break;
     *         case 'checkbox':
     *             $f[] = Form::checkbox('value', '默认值', $configInfo->getData('value'));
     *             $f[] = Form::textarea('parameter', '配置参数');
     *             break;
     *         case 'upload':
     *             if ($configInfo->getData('upload_type') == 1) {
     *                 $f[] = Form::frameImage(...);
     *             } else if ($configInfo->getData('upload_type') == 2) {
     *                 $f[] = Form::frameImages(...);
     *             } else if ($configInfo->getData('upload_type') == 3) {
     *                 $f[] = Form::frameFile(...);
     *             }
     *             $f[] = Form::radio('upload_type', '上传类型', (string)$configInfo->getData('upload_type'));
     *             break;
     *     }
     *
     *     $f[] = Form::number('sort', '排序', (int)$configInfo->getData('sort'));
     *     $f[] = Form::radio('status', '状态', (string)$configInfo->getData('status'));
     *     return $f;
     * }
     *
     * @param id 配置ID
     * @return 表单数据
     */
    public Map<String, Object> getEditForm(Integer id) {
        SystemConfigEntity config = getConfigInfo(id);

        // PHP: $this->services->editConfigForm((int)$id)
        List<BaseComponent> rules = new ArrayList<>();

        // 1. 字段变量（禁用）
        rules.add(formBuilder.input("menu_name", "Field Variable", config.getMenuName())
            .disabled(true));

        // 2. 类型（禁用）
        rules.add(formBuilder.input("type", "Type", config.getType())
            .disabled(true));

        // 3. 分类选择
        List<Map<String, Object>> tabOptions = adminConfigTabService.getConfigTabSelectOptions();
        String tabIdValue = config.getConfigTabId() != null ? String.valueOf(config.getConfigTabId()) : "1";
        rules.add(formBuilder.select("config_tab_id", "Category", tabIdValue)
            .options(tabOptions)
            .required());

        // 4. 配置名称
        rules.add(formBuilder.input("info", "Configuration Name", config.getInfo())
            .required());

        // 5. 配置简介
        rules.add(formBuilder.textarea("desc", "Configuration Description", config.getDesc())
            .required());

        // 6. 根据类型添加特定字段
        String type = config.getType();

        // 解码value字段
        String valueStr = config.getValue();
        Object decodedValue = "";
        if (valueStr != null && !valueStr.isEmpty()) {
            try {
                decodedValue = objectMapper.readValue(valueStr, Object.class);
                if (decodedValue == null) {
                    decodedValue = "";
                }
            } catch (Exception e) {
                decodedValue = valueStr;
            }
        }

        switch (type) {
            case "text":
                rules.addAll(createTextFormRules(config, decodedValue));
                break;
            case "textarea":
                rules.addAll(createTextareaFormRules(config, decodedValue));
                break;
            case "radio":
                rules.addAll(createRadioFormRules(config, decodedValue));
                break;
            case "checkbox":
                rules.addAll(createCheckboxFormRules(config, decodedValue));
                break;
            case "upload":
                rules.addAll(createUploadFormRules(config, decodedValue));
                break;
            default:
                // 其他类型默认添加value输入框
                rules.add(formBuilder.input("value", "Default Value", decodedValue.toString()));
                break;
        }

        // 7. 排序
        rules.add(formBuilder.number("sort", "Sort Order", String.valueOf(config.getSort())));

        // 8. 状态
        List<Map<String, Object>> statusOptions = new ArrayList<>();
        Map<String, Object> statusOption1 = new HashMap<>();
        statusOption1.put("value", "1");
        statusOption1.put("label", "Visible");
        Map<String, Object> statusOption2 = new HashMap<>();
        statusOption2.put("value", "0");
        statusOption2.put("label", "Hidden");
        statusOptions.add(statusOption1);
        statusOptions.add(statusOption2);

        String statusValue = String.valueOf(config.getStatus());
        rules.add(formBuilder.radio("status", "Status", statusValue)
            .options(statusOptions));

        // PHP: return create_form('修改配置', $f, $this->url('/setting/config/' . $id), 'PUT');
        return FormHelper.createForm(
            "Edit Configuration",
            rules,
            "setting/config/" + id,
            "PUT"
        );
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
     * PHP Reference: Config.php::edit_basics() + SystemConfigServices::getConfigForm()
     *
     * PHP代码:
     * public function getConfigForm($url, int $tabId)
     * {
     *     $service    = app()->make(SystemConfigTabServices::class);
     *     $title      = $service->value(['id' => $tabId], 'title');
     *     $list       = $this->dao->getConfigTabAllList($tabId);
     *     $formbuider = $this->createForm($list);
     *     return create_form($title, $formbuider, $this->url($postUrl), 'POST');
     * }
     *
     * @param tabId 分类ID
     * @return 配置表单数据
     */
    public Map<String, Object> getConfigForm(Integer tabId) {
        if (tabId == null || tabId == 0) {
            throw new CrmChatException("Invalid parameter");
        }

        // PHP: $service->value(['id' => $tabId], 'title');
        SystemConfigTabEntity tab = systemConfigTabMapper.selectById(tabId);
        String title = tab != null ? tab.getTitle() : "System Configuration";

        // 检查是否有子菜单
        List<SystemConfigTabEntity> children = systemConfigTabMapper.selectList(
            new QueryWrapper<SystemConfigTabEntity>()
                .eq("pid", tabId)
                .eq("status", 1)
        );

        List<SystemConfigEntity> configs;

        if (children != null && !children.isEmpty()) {
            // 如果有子菜单，查询所有子菜单的配置
            List<Integer> childIds = children.stream()
                .map(SystemConfigTabEntity::getId)
                .collect(java.util.stream.Collectors.toList());

            configs = systemConfigMapper.selectList(
                new QueryWrapper<SystemConfigEntity>()
                    .in("config_tab_id", childIds)
                    .eq("status", 1)
                    .orderByAsc("sort", "id")
            );
        } else {
            // 如果没有子菜单，查询当前tab的配置
            configs = systemConfigMapper.selectList(
                new QueryWrapper<SystemConfigEntity>()
                    .eq("config_tab_id", tabId)
                    .eq("status", 1)
                    .orderByAsc("sort", "id")
            );
        }

        // PHP: $formbuider = $this->createForm($list);
        List<BaseComponent> formRules = createConfigFormRules(configs);

        // PHP: return create_form($title, $formbuider, $this->url($postUrl), 'POST');
        return FormHelper.createForm(
            title,
            formRules,
            "setting/config/save_basics",
            "POST"
        );
    }

    /**
     * 根据配置列表创建表单规则
     * PHP Reference: SystemConfigServices::createForm()
     *
     * PHP代码中会根据每个config的type创建不同的表单组件:
     * - text: 根据input_type创建input/number/dateTime/color组件
     * - textarea: 创建textarea组件
     * - radio: 创建radio组件,解析parameter生成options
     * - checkbox: 创建checkbox组件,解析parameter生成options
     * - upload: 根据upload_type创建frameImage/frameImages/uploadFile组件
     *
     * @param configs 配置列表
     * @return 表单规则列表
     */
    private List<BaseComponent> createConfigFormRules(List<SystemConfigEntity> configs) {
        List<BaseComponent> rules = new ArrayList<>();

        // PHP: $list = array_combine(array_column($list, 'menu_name'), $list);
        // 去重：使用menu_name作为key，后面的覆盖前面的
        Map<String, SystemConfigEntity> configMap = new LinkedHashMap<>();
        for (SystemConfigEntity config : configs) {
            configMap.put(config.getMenuName(), config);
        }

        // 遍历去重后的配置
        for (SystemConfigEntity config : configMap.values()) {
            String menuName = config.getMenuName();
            String info = config.getInfo();
            String desc = config.getDesc();
            String type = config.getType();

            // 解码value字段
            String valueStr = config.getValue();
            Object decodedValue = "";
            if (valueStr != null && !valueStr.isEmpty()) {
                try {
                    decodedValue = objectMapper.readValue(valueStr, Object.class);
                    if (decodedValue == null) {
                        decodedValue = "";
                    }
                } catch (Exception e) {
                    decodedValue = valueStr;
                }
            }

            switch (type) {
                case "text":
                    rules.addAll(createBasicsTextFormRules(config, menuName, info, desc, decodedValue));
                    break;
                case "textarea":
                    rules.addAll(createBasicsTextareaFormRules(menuName, info, desc, decodedValue));
                    break;
                case "radio":
                    rules.addAll(createBasicsRadioFormRules(config, menuName, info, desc, decodedValue));
                    break;
                case "checkbox":
                    rules.addAll(createBasicsCheckboxFormRules(config, menuName, info, desc, decodedValue));
                    break;
                case "upload":
                    rules.addAll(createBasicsUploadFormRules(config, menuName, info, desc, decodedValue));
                    break;
            }
        }

        return rules;
    }

    /**
     * 创建基础配置text类型表单
     */
    private List<BaseComponent> createBasicsTextFormRules(SystemConfigEntity config, String menuName, String info, String desc, Object decodedValue) {
        List<BaseComponent> rules = new ArrayList<>();
        String inputType = config.getInputType() != null ? config.getInputType() : "input";
        String valueStr = decodedValue != null ? decodedValue.toString() : "";

        switch (inputType) {
            case "number":
                rules.add(formBuilder.inputNumber(menuName, info, parseDouble(valueStr))
                    .placeholder(desc)
                    .col(13));
                break;
            case "dateTime":
                rules.add(formBuilder.datePicker(menuName, info, valueStr)
                    .col(13));
                break;
            case "color":
                rules.add(formBuilder.input(menuName, info, valueStr)
                    .placeholder(desc)
                    .col(13));
                break;
            default: // input
                rules.add(formBuilder.input(menuName, info, valueStr)
                    .placeholder(desc)
                    .col(13));
                break;
        }

        return rules;
    }

    /**
     * 创建基础配置textarea类型表单
     */
    private List<BaseComponent> createBasicsTextareaFormRules(String menuName, String info, String desc, Object decodedValue) {
        List<BaseComponent> rules = new ArrayList<>();
        String valueStr = decodedValue != null ? decodedValue.toString() : "";

        rules.add(formBuilder.textarea(menuName, info, valueStr)
            .placeholder(desc)
            .col(13));

        return rules;
    }

    /**
     * 创建基础配置radio类型表单
     */
    private List<BaseComponent> createBasicsRadioFormRules(SystemConfigEntity config, String menuName, String info, String desc, Object decodedValue) {
        List<BaseComponent> rules = new ArrayList<>();

        // 解析parameter生成选项
        String parameter = config.getParameter();
        List<Map<String, Object>> options = parseParameterOptions(parameter);

        // PHP中radio的value是整数
        String valueStr = decodedValue != null ? decodedValue.toString() : "0";

        rules.add(formBuilder.radio(menuName, info, valueStr)
            .options(options)
            .col(13));

        return rules;
    }

    /**
     * 创建基础配置checkbox类型表单
     */
    private List<BaseComponent> createBasicsCheckboxFormRules(SystemConfigEntity config, String menuName, String info, String desc, Object decodedValue) {
        List<BaseComponent> rules = new ArrayList<>();

        // 解析parameter生成选项
        String parameter = config.getParameter();
        List<Map<String, Object>> options = parseParameterOptions(parameter);

        rules.add(formBuilder.checkbox(menuName, info, decodedValue)
            .options(options)
            .col(13));

        return rules;
    }

    /**
     * 创建基础配置upload类型表单
     */
    private List<BaseComponent> createBasicsUploadFormRules(SystemConfigEntity config, String menuName, String info, String desc, Object decodedValue) {
        List<BaseComponent> rules = new ArrayList<>();
        String uploadType = config.getUploadType();
        String valueStr = decodedValue != null ? decodedValue.toString() : "";

        if (uploadType != null && !uploadType.isEmpty()) {
            if ("1".equals(uploadType)) {
                // 单图上传
                rules.add(formBuilder.frameImage(menuName, info,
                    "admin/widget.images/index?fodder=" + menuName, valueStr)
                    .icon("ios-image")
                    .width("950px")
                    .height("420px")
                    .col(13));
            } else if ("2".equals(uploadType)) {
                // 多图上传
                rules.add(formBuilder.frameImage(menuName, info,
                    "admin/widget.images/index?fodder=" + menuName, valueStr)
                    .icon("ios-images")
                    .width("950px")
                    .height("420px")
                    .col(13));
            } else if ("3".equals(uploadType)) {
                // 文件上传
                rules.add(formBuilder.frameImage(menuName, info,
                    "admin/widget.files/index?fodder=" + menuName, valueStr)
                    .icon("ios-folder")
                    .width("950px")
                    .height("420px")
                    .col(13));
            }
        }

        return rules;
    }

    /**
     * 解析Double值
     */
    private Number parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
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

    /**
     * 创建text类型的表单规则
     * PHP: case 'text': ...
     */
    private List<BaseComponent> createTextFormRules(SystemConfigEntity config, Object decodedValue) {
        List<BaseComponent> rules = new ArrayList<>();

        // input_type选择
        List<Map<String, Object>> inputTypeOptions = new ArrayList<>();
        Map<String, Object> inputType1 = new HashMap<>();
        inputType1.put("value", "input");
        inputType1.put("label", "input");
        Map<String, Object> inputType2 = new HashMap<>();
        inputType2.put("value", "dateTime");
        inputType2.put("label", "dateTime");
        Map<String, Object> inputType3 = new HashMap<>();
        inputType3.put("value", "color");
        inputType3.put("label", "color");
        Map<String, Object> inputType4 = new HashMap<>();
        inputType4.put("value", "number");
        inputType4.put("label", "number");
        inputTypeOptions.add(inputType1);
        inputTypeOptions.add(inputType2);
        inputTypeOptions.add(inputType3);
        inputTypeOptions.add(inputType4);

        String inputTypeValue = config.getInputType() != null ? config.getInputType() : "input";
        rules.add(formBuilder.select("input_type", "Input Type", inputTypeValue)
            .options(inputTypeOptions));

        // 默认值
        rules.add(formBuilder.input("value", "Default Value", decodedValue.toString()));

        // 文本框宽度
        rules.add(formBuilder.number("width", "Input Width", String.valueOf(config.getWidth())));

        // 是否必填
        List<Map<String, Object>> requiredOptions = new ArrayList<>();
        Map<String, Object> requiredOption1 = new HashMap<>();
        requiredOption1.put("value", "");
        requiredOption1.put("label", "No");
        Map<String, Object> requiredOption2 = new HashMap<>();
        requiredOption2.put("value", "required");
        requiredOption2.put("label", "Yes");
        requiredOptions.add(requiredOption1);
        requiredOptions.add(requiredOption2);

        String requiredValue = config.getRequired() != null ? config.getRequired() : "";
        rules.add(formBuilder.radio("required", "Required", requiredValue)
            .options(requiredOptions));

        return rules;
    }

    /**
     * 创建textarea类型的表单规则
     * PHP: case 'textarea': ...
     */
    private List<BaseComponent> createTextareaFormRules(SystemConfigEntity config, Object decodedValue) {
        List<BaseComponent> rules = new ArrayList<>();

        // 默认值
        rules.add(formBuilder.textarea("value", "Default Value", decodedValue.toString()));

        // 文本框宽度
        rules.add(formBuilder.number("width", "Input Width", String.valueOf(config.getWidth())));

        // 文本框高度
        rules.add(formBuilder.number("high", "Input Height", String.valueOf(config.getHigh())));

        return rules;
    }

    /**
     * 创建radio类型的表单规则
     * PHP: case 'radio': ...
     *
     * PHP代码:
     * $f[] = Form::radio('value', '默认值', $configInfo->getData('value'))
     *     ->setOptions(function () use ($configInfo) {
     *         $str = $configInfo->getData('parameter') ?: '';
     *         $options = [];
     *         if ($str) {
     *             preg_match_all('/(.*)' . $this->cuttingStr . '(.*)[\n\r]/', $str, $match);
     *             if (isset($match[1]) && $match[1]) {
     *                 foreach ($match[1] as $index => $value) {
     *                     $options[] = ['value' => $value, 'label' => $match[2][$index] ?? ''];
     *                 }
     *             }
     *         }
     *         return $options;
     *     });
     * $f[] = Form::textarea('parameter', '配置参数', $configInfo->getData('parameter'))
     *     ->placeholder("参数方式例如:\n1=>白色\n2=>红色\n3=>黑色");
     */
    private List<BaseComponent> createRadioFormRules(SystemConfigEntity config, Object decodedValue) {
        List<BaseComponent> rules = new ArrayList<>();

        // 解析parameter生成选项
        String parameter = config.getParameter();
        List<Map<String, Object>> options = parseParameterOptions(parameter);

        // 默认值
        String valueStr = decodedValue != null ? decodedValue.toString() : "";
        rules.add(formBuilder.radio("value", "Default Value", valueStr)
            .options(options));

        // 配置参数
        String parameterValue = parameter != null ? parameter : "";
        rules.add(formBuilder.textarea("parameter", "Configuration Options", parameterValue)
            .placeholder("Example:\n1=>White\n2=>Red\n3=>Black"));

        return rules;
    }

    /**
     * 创建checkbox类型的表单规则
     * PHP: case 'checkbox': ...
     */
    private List<BaseComponent> createCheckboxFormRules(SystemConfigEntity config, Object decodedValue) {
        List<BaseComponent> rules = new ArrayList<>();

        // 解析parameter生成选项
        String parameter = config.getParameter();
        List<Map<String, Object>> options = parseParameterOptions(parameter);

        // 默认值
        Object valueObj = decodedValue;
        rules.add(formBuilder.checkbox("value", "Default Value", valueObj)
            .options(options));

        // 配置参数
        String parameterValue = parameter != null ? parameter : "";
        rules.add(formBuilder.textarea("parameter", "Configuration Options", parameterValue)
            .placeholder("Example:\n1=>White\n2=>Red\n3=>Black"));

        return rules;
    }

    /**
     * 创建upload类型的表单规则
     * PHP: case 'upload': ...
     *
     * PHP代码:
     * if ($configInfo->getData('upload_type') == 1) {
     *     $f[] = Form::frameImage('value', '默认值', $this->url('admin/widget.images/index', ['fodder' => 'value'], true), $configInfo->getData('value'))
     *         ->icon('ios-image')->width('960px')->height('505px')->modal(['footer-hide' => true]);
     * } else if ($configInfo->getData('upload_type') == 2) {
     *     $f[] = Form::frameImages('value', '默认值', $this->url('admin/widget.images/index', ['fodder' => 'value'], true), $configInfo->getData('value'))
     *         ->maxLength(5)->icon('ios-images')->width('960px')->height('505px')->modal(['footer-hide' => true]);
     * } else if ($configInfo->getData('upload_type') == 3) {
     *     $f[] = Form::frameFile('value', '默认值', $this->url('admin/widget.files/index', ['fodder' => 'value'], true), $configInfo->getData('value'))
     *         ->maxLength(5)->icon('ios-folder')->width('960px')->height('505px')->modal(['footer-hide' => true]);
     * }
     * $f[] = Form::radio('upload_type', '上传类型', (string)$configInfo->getData('upload_type'))
     *     ->options([
     *         ['value' => '1', 'label' => '单图'],
     *         ['value' => '2', 'label' => '多图'],
     *         ['value' => '3', 'label' => '文件']
     *     ]);
     */
    private List<BaseComponent> createUploadFormRules(SystemConfigEntity config, Object decodedValue) {
        List<BaseComponent> rules = new ArrayList<>();

        String uploadType = config.getUploadType();
        String valueStr = decodedValue != null ? decodedValue.toString() : "";

        // 根据upload_type创建不同的上传组件
        if (uploadType != null && !uploadType.isEmpty()) {
            if ("1".equals(uploadType)) {
                // 单图上传
                rules.add(formBuilder.frameImage("value", "Default Value",
                    "admin/widget.images/index?fodder=value", valueStr)
                    .icon("ios-image")
                    .width("960px")
                    .height("505px"));
            } else if ("2".equals(uploadType)) {
                // 多图上传（暂用frameImage代替frameImages）
                rules.add(formBuilder.frameImage("value", "Default Value",
                    "admin/widget.images/index?fodder=value", valueStr)
                    .icon("ios-images")
                    .width("960px")
                    .height("505px"));
            } else if ("3".equals(uploadType)) {
                // 文件上传（暂用frameImage代替frameFile）
                rules.add(formBuilder.frameImage("value", "Default Value",
                    "admin/widget.files/index?fodder=value", valueStr)
                    .icon("ios-folder")
                    .width("960px")
                    .height("505px"));
            }
        } else {
            // 默认单图上传
            rules.add(formBuilder.frameImage("value", "Default Value",
                "admin/widget.images/index?fodder=value", valueStr)
                .icon("ios-image")
                .width("960px")
                .height("505px"));
        }

        // 上传类型选择
        List<Map<String, Object>> uploadTypeOptions = new ArrayList<>();
        Map<String, Object> uploadTypeOption1 = new HashMap<>();
        uploadTypeOption1.put("value", "1");
        uploadTypeOption1.put("label", "Single Image");
        Map<String, Object> uploadTypeOption2 = new HashMap<>();
        uploadTypeOption2.put("value", "2");
        uploadTypeOption2.put("label", "Multiple Images");
        Map<String, Object> uploadTypeOption3 = new HashMap<>();
        uploadTypeOption3.put("value", "3");
        uploadTypeOption3.put("label", "File");
        uploadTypeOptions.add(uploadTypeOption1);
        uploadTypeOptions.add(uploadTypeOption2);
        uploadTypeOptions.add(uploadTypeOption3);

        String uploadTypeValue = (uploadType != null && !uploadType.isEmpty()) ? uploadType : "1";
        rules.add(formBuilder.radio("upload_type", "Upload Type", uploadTypeValue)
            .options(uploadTypeOptions));

        return rules;
    }

    /**
     * 解析parameter参数生成选项列表
     * PHP正则: preg_match_all('/(.*)' . $this->cuttingStr . '(.*)[\n\r]/', $str, $match);
     *
     * 格式示例:
     * 1=>白色
     * 2=>红色
     * 3=>黑色
     *
     * @param parameter 参数字符串
     * @return 选项列表 [{value: "1", label: "白色"}, ...]
     */
    private List<Map<String, Object>> parseParameterOptions(String parameter) {
        List<Map<String, Object>> options = new ArrayList<>();

        if (parameter != null && !parameter.trim().isEmpty()) {
            // PHP: preg_match_all('/(.*)' . $this->cuttingStr . '(.*)[\n\r]/', $str, $match);
            String[] lines = parameter.split("[\n\r]+");
            for (String line : lines) {
                if (line.contains(CUTTING_STR)) {
                    String[] parts = line.split(CUTTING_STR, 2);
                    if (parts.length == 2) {
                        Map<String, Object> option = new HashMap<>();
                        option.put("value", parts[0].trim());
                        option.put("label", parts[1].trim());
                        options.add(option);
                    }
                }
            }
        }

        return options;
    }
}
