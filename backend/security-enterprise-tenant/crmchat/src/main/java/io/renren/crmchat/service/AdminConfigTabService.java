package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    private final FormBuilder formBuilder;

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
     * PHP Reference: SystemConfigTabServices::createForm()
     *
     * PHP代码:
     * public function createForm()
     * {
     *     return create_form('添加配置分类', $this->createConfigTabForm(), $this->url('/setting/config_class'));
     * }
     *
     * @return FormBuilder生成的表单配置
     */
    public Map<String, Object> getCreateForm() {
        List<BaseComponent> rules = createConfigTabFormRules(null);

        return FormHelper.createForm(
            "Add Configuration Category",
            rules,
            "setting/config_class",
            "POST"
        );
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
        tab.setStatus(parseInteger(data.get("status"), 1));
        tab.setIcon((String) data.get("icon"));
        tab.setType(parseInteger(data.get("type"), 0));
        tab.setSort(parseInteger(data.get("sort"), 0));
        tab.setPid(parseInteger(data.get("pid"), 0));

        return systemConfigTabMapper.insert(tab) > 0;
    }

    /**
     * 获取编辑表单数据
     * PHP Reference: SystemConfigTabServices::updateForm()
     *
     * PHP代码:
     * public function updateForm(int $id)
     * {
     *     $configTabInfo = $this->dao->get($id);
     *     if (!$configTabInfo) {
     *         throw new AdminException('没有查到数据,无法修改!');
     *     }
     *     return create_form('编辑配置分类', $this->createConfigTabForm($configTabInfo->toArray()), $this->url('/setting/config_class/' . $id), 'PUT');
     * }
     *
     * @param id 配置分类ID
     * @return FormBuilder生成的表单配置
     */
    public Map<String, Object> getEditForm(Integer id) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing update parameter");
        }

        SystemConfigTabEntity tab = systemConfigTabMapper.selectById(id);
        if (tab == null) {
            throw new CrmChatException("No matching record found, update cannot continue.");
        }

        List<BaseComponent> rules = createConfigTabFormRules(tab);

        return FormHelper.createForm(
            "Edit Configuration Category",
            rules,
            "setting/config_class/" + id,
            "PUT"
        );
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
        tab.setStatus(parseInteger(data.get("status"), tab.getStatus()));
        tab.setIcon((String) data.get("icon"));
        tab.setType(parseInteger(data.get("type"), tab.getType()));
        tab.setSort(parseInteger(data.get("sort"), tab.getSort()));
        tab.setPid(parseInteger(data.get("pid"), tab.getPid()));

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

    /**
     * 获取分类选择下拉树 (public方法供外部调用)
     * PHP Reference: SystemConfigTabServices::getSelectForm()
     *
     * @return 下拉选项列表
     */
    public List<Map<String, Object>> getConfigTabSelectOptions() {
        return getSelectForm();
    }

    /**
     * 获取分类选择下拉树
     * PHP Reference: SystemConfigTabServices::getSelectForm()
     *
     * @return 下拉选项列表
     */
    private List<Map<String, Object>> getSelectForm() {
        QueryWrapper<SystemConfigTabEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort", "id");

        List<SystemConfigTabEntity> menuList = systemConfigTabMapper.selectList(wrapper);

        // PHP: sort_list_tier($menuList, 0, 'pid', 'id')
        List<Map<String, Object>> sortedList = sortListTier(menuList, 0, 0);

        List<Map<String, Object>> menus = new ArrayList<>();
        // PHP: 顶级按钮
        Map<String, Object> topOption = new HashMap<>();
        topOption.put("value", "0");
        topOption.put("label", "Top-level Button");
        menus.add(topOption);

        for (Map<String, Object> menu : sortedList) {
            Map<String, Object> option = new HashMap<>();
            option.put("value", String.valueOf(menu.get("id")));
            option.put("label", menu.get("html").toString() + menu.get("title"));
            menus.add(option);
        }

        return menus;
    }

    /**
     * 层级排序
     * PHP Reference: sort_list_tier() helper function
     */
    private List<Map<String, Object>> sortListTier(List<SystemConfigTabEntity> list, int pid, int level) {
        List<Map<String, Object>> result = new ArrayList<>();
        String html = "";
        for (int i = 0; i < level; i++) {
            html += "　　";
        }
        if (level > 0) {
            html += "├─ ";
        }

        for (SystemConfigTabEntity entity : list) {
            if (entity.getPid().equals(pid)) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", entity.getId());
                item.put("title", entity.getTitle());
                item.put("html", html);
                result.add(item);
                result.addAll(sortListTier(list, entity.getId(), level + 1));
            }
        }

        return result;
    }

    /**
     * 创建配置分类表单规则
     * PHP Reference: SystemConfigTabServices::createConfigTabForm()
     *
     * PHP代码:
     * $form[] = Form::select('pid', '父级分类', isset($formData['pid']) ? (string)$formData['pid'] : '')->setOptions($this->getSelectForm())->filterable(true);
     * $form[] = Form::input('title', '分类名称', $formData['title'] ?? '');
     * $form[] = Form::input('eng_title', '分类字段英文', $formData['eng_title'] ?? '');
     * $form[] = Form::frameInput('icon', '图标', $this->url('admin/widget.widgets/icon', ['fodder' => 'icon'], true), $formData['icon'] ?? '')->icon('ios-ionic')->height('435px');
     * $form[] = Form::radio('type', '类型', $formData['type'] ?? 0)->options([
     *     ['value' => 0, 'label' => '系统'],
     *     ['value' => 3, 'label' => '其它']
     * ]);
     * $form[] = Form::radio('status', '状态', $formData['status'] ?? 1)->options([['value' => 1, 'label' => '显示'], ['value' => 2, 'label' => '隐藏']]);
     * $form[] = Form::number('sort', '排序', (int)($formData['sort'] ?? 0));
     */
    private List<BaseComponent> createConfigTabFormRules(SystemConfigTabEntity formData) {
        List<BaseComponent> rules = new ArrayList<>();

        // 1. 父级分类下拉选择
        String pidValue = formData != null ? String.valueOf(formData.getPid()) : "0";
        rules.add(formBuilder.select("pid", "Parent Category", pidValue)
            .options(getSelectForm())
            .required());

        // 2. 分类名称
        String titleValue = formData != null ? formData.getTitle() : "";
        rules.add(formBuilder.input("title", "Category Name", titleValue)
            .required()
            .placeholder("Enter category name"));

        // 3. 分类字段英文
        String engTitleValue = formData != null ? formData.getEngTitle() : "";
        rules.add(formBuilder.input("eng_title", "Category Field (English)", engTitleValue)
            .required()
            .placeholder("Enter category field name"));

        // 4. 图标 (暂时用input代替frameInput)
        String iconValue = formData != null ? formData.getIcon() : "";
        rules.add(formBuilder.input("icon", "Icon", iconValue)
            .placeholder("Enter icon"));

        // 5. 类型
        List<Map<String, Object>> typeOptions = new ArrayList<>();
        Map<String, Object> typeOption1 = new HashMap<>();
        typeOption1.put("value", "0");
        typeOption1.put("label", "System");
        Map<String, Object> typeOption2 = new HashMap<>();
        typeOption2.put("value", "3");
        typeOption2.put("label", "Other");
        typeOptions.add(typeOption1);
        typeOptions.add(typeOption2);

        String typeValue = formData != null ? String.valueOf(formData.getType()) : "0";
        rules.add(formBuilder.radio("type", "Type", typeValue)
            .options(typeOptions));

        // 6. 状态
        List<Map<String, Object>> statusOptions = new ArrayList<>();
        Map<String, Object> statusOption1 = new HashMap<>();
        statusOption1.put("value", "1");
        statusOption1.put("label", "Visible");
        Map<String, Object> statusOption2 = new HashMap<>();
        statusOption2.put("value", "2");
        statusOption2.put("label", "Hidden");
        statusOptions.add(statusOption1);
        statusOptions.add(statusOption2);

        String statusValue = formData != null ? String.valueOf(formData.getStatus()) : "1";
        rules.add(formBuilder.radio("status", "Status", statusValue)
            .options(statusOptions));

        // 7. 排序
        Integer sortValue = formData != null ? formData.getSort() : 0;
        rules.add(formBuilder.number("sort", "Sort Order", String.valueOf(sortValue)));

        return rules;
    }

    /**
     * 解析Integer值,处理String和Integer两种类型
     * @param value 值对象(可能是String或Integer)
     * @param defaultValue 默认值
     * @return Integer值
     */
    private Integer parseInteger(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            String str = (String) value;
            if (str.trim().isEmpty()) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
