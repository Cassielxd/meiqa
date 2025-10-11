package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatUserGroupMapper;
import io.renren.crmchat.entity.ChatUserGroupEntity;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.formbuilder.FormBuilder;
import io.renren.crmchat.formbuilder.FormHelper;
import io.renren.crmchat.formbuilder.components.BaseComponent;
import io.renren.crmchat.security.TenantContextUtils;
import io.renren.crmchat.security.TenantGuard;
import io.renren.crmchat.security.TenantQueryHelper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Admin User Group Service - 管理员用户分组管理
 * PHP Reference: Group.php
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class AdminUserGroupService {

    private final ChatUserGroupMapper chatUserGroupMapper;
    private final FormBuilder formBuilder;

    /**
     * 获取分组列表
     * PHP Reference: Group.php::index() -> ChatUserGroupServices::getGroupList()
     * PHP returns: compact('list', 'count')
     */
    public Map<String, Object> getGroupList(String appid, Integer page, Integer limit) {
        QueryWrapper<ChatUserGroupEntity> wrapper = new QueryWrapper<>();
        TenantQueryHelper.applyAppid(wrapper, appid);
        wrapper.orderByAsc("id");

        // 计算总数
        Long count = chatUserGroupMapper.selectCount(wrapper);

        // 获取分页数据
        List<ChatUserGroupEntity> list;
        if (limit != null && limit > 0) {
            int offset = (page - 1) * limit;
            wrapper.last("LIMIT " + offset + ", " + limit);
        }
        list = chatUserGroupMapper.selectList(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("count", count);
        return result;
    }

    /**
     * 获取创建表单数据
     * PHP Reference: Group.php::create() -> ChatUserGroupServices::add()
     *
     * PHP代码:
     * public function add()
     * {
     *     $field[] = FormBuilder::input('group_name', '分组名称')->required();
     *     return create_form('添加分组', $field, $this->url('/user/group'), 'POST');
     * }
     */
    public Map<String, Object> getCreateForm() {
        List<BaseComponent> rules = new ArrayList<>();

        // 分组名称输入框
        rules.add(formBuilder.input("group_name", "分组名称", "")
            .required()
            .placeholder("请输入分组名称"));

        return FormHelper.createForm(
            "添加分组",
            rules,
            "user/group",
            "POST"
        );
    }

    /**
     * 保存分组
     * PHP Reference: Group.php::save()
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveGroup(Map<String, Object> data, String appid) {
        String groupName = (String) data.get("group_name");
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new CrmChatException("Please enter group name");
        }

        ChatUserGroupEntity group = new ChatUserGroupEntity();
        group.setAppid(TenantContextUtils.resolveAppid(appid));
        group.setGroupName(groupName);

        return chatUserGroupMapper.insert(group) > 0;
    }

    /**
     * 获取编辑表单数据
     * PHP Reference: Group.php::edit() -> ChatUserGroupServices::add()
     *
     * PHP代码:
     * public function add(int $id = 0)
     * {
     *     $groupInfo = $id ? $this->services->get($id) : [];
     *     $field[] = FormBuilder::input('group_name', '分组名称', $groupInfo['group_name'] ?? '')->required();
     *     return create_form($id ? '修改分组' : '添加分组', $field, $this->url('/user/group' . ($id ? ('/' . $id) : '')), $id ? 'PUT' : 'POST');
     * }
     */
    public Map<String, Object> getEditForm(Integer id, String appid) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        ChatUserGroupEntity group = chatUserGroupMapper.selectById(id);
        if (group == null) {
            throw new CrmChatException("Group does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(group.getAppid(), "Group does not exist");

        List<BaseComponent> rules = new ArrayList<>();

        // 隐藏字段：id
        rules.add(formBuilder.hidden("id", id));

        // 分组名称输入框（带默认值）
        rules.add(formBuilder.input("group_name", "分组名称", group.getGroupName())
            .required()
            .placeholder("请输入分组名称"));

        return FormHelper.createForm(
            "修改分组",
            rules,
            "user/group/" + id,
            "PUT"
        );
    }

    /**
     * 更新分组
     * PHP Reference: Group.php::update()
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateGroup(Integer id, Map<String, Object> data, String appid) {
        ChatUserGroupEntity group = chatUserGroupMapper.selectById(id);
        if (group == null) {
            throw new CrmChatException("Group does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(group.getAppid(), "Group does not exist");

        String groupName = (String) data.get("group_name");
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new CrmChatException("Please enter group name");
        }

        group.setGroupName(groupName);
        return chatUserGroupMapper.updateById(group) > 0;
    }

    /**
     * 删除分组
     * PHP Reference: Group.php::delete()
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteGroup(Integer id, String appid) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Data does not exist");
        }

        ChatUserGroupEntity group = chatUserGroupMapper.selectById(id);
        if (group == null) {
            throw new CrmChatException("Deletion failed, please try again later");
        }
        TenantGuard.ensureOwnedByCurrentTenant(group.getAppid(), "Deletion failed, please try again later");

        return chatUserGroupMapper.deleteById(id) > 0;
    }
}
