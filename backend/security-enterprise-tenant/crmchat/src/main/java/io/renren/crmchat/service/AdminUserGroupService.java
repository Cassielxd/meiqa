package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatUserGroupMapper;
import io.renren.crmchat.entity.ChatUserGroupEntity;
import io.renren.crmchat.exception.CrmChatException;
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
     * Returns form configuration for form-create library:
     * {
     *   "rules": [field definitions],
     *   "title": "Add Group",
     *   "action": "/api/admin/user/group",
     *   "method": "POST",
     *   "info": "",
     *   "status": true
     * }
     */
    public Map<String, Object> getCreateForm() {
        List<Map<String, Object>> rules = new ArrayList<>();

        // Create group_name input field
        Map<String, Object> groupNameField = new HashMap<>();
        groupNameField.put("type", "input");
        groupNameField.put("field", "group_name");
        groupNameField.put("title", "Group Name");
        groupNameField.put("value", "");

        // Add validation rules
        List<Map<String, Object>> validate = new ArrayList<>();
        Map<String, Object> requiredRule = new HashMap<>();
        requiredRule.put("required", true);
        requiredRule.put("message", "Please enter group name");
        requiredRule.put("trigger", "blur");
        validate.add(requiredRule);
        groupNameField.put("validate", validate);

        rules.add(groupNameField);

        // Build complete form configuration
        Map<String, Object> result = new HashMap<>();
        result.put("rules", rules);
        result.put("title", "添加分组");
        result.put("action", "user/group");  // Relative path, frontend adds /api/admin prefix
        result.put("method", "POST");
        result.put("info", "");
        result.put("status", true);

        return result;
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

        List<Map<String, Object>> rules = new ArrayList<>();

        // Add hidden id field
        Map<String, Object> idField = new HashMap<>();
        idField.put("type", "hidden");
        idField.put("field", "id");
        idField.put("value", id);
        rules.add(idField);

        // Add group_name input field with existing value
        Map<String, Object> groupNameField = new HashMap<>();
        groupNameField.put("type", "input");
        groupNameField.put("field", "group_name");
        groupNameField.put("title", "Group Name");
        groupNameField.put("value", group.getGroupName());

        // Add validation rules
        List<Map<String, Object>> validate = new ArrayList<>();
        Map<String, Object> requiredRule = new HashMap<>();
        requiredRule.put("required", true);
        requiredRule.put("message", "Please enter group name");
        requiredRule.put("trigger", "blur");
        validate.add(requiredRule);
        groupNameField.put("validate", validate);

        rules.add(groupNameField);

        // Build complete form configuration
        Map<String, Object> result = new HashMap<>();
        result.put("rules", rules);
        result.put("title", "Modify Group");
        result.put("action", "user/group/" + id);  // Relative path, frontend adds /api/admin prefix
        result.put("method", "PUT");
        result.put("info", "");
        result.put("status", true);

        return result;
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
