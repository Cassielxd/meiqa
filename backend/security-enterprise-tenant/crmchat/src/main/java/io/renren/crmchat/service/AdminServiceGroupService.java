package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatServiceGroupMapper;
import io.renren.crmchat.dao.ChatServiceMapper;
import io.renren.crmchat.entity.ChatServiceGroupEntity;
import io.renren.crmchat.exception.CrmChatException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin Service Group Service - 管理员客服组管理
 * PHP Reference: ServiceGroup.php
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class AdminServiceGroupService {

    private final ChatServiceGroupMapper chatServiceGroupMapper;
    private final ChatServiceMapper chatServiceMapper;

    /**
     * 获取客服组列表
     * PHP Reference: ServiceGroup.php::index()
     */
    public List<ChatServiceGroupEntity> getGroupList(String appid) {
        QueryWrapper<ChatServiceGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.orderByAsc("sort");
        wrapper.orderByDesc("id");
        return chatServiceGroupMapper.selectList(wrapper);
    }

    /**
     * 获取客服组表单
     * PHP Reference: ServiceGroup.php::create()
     *
     * PHP代码：
     * return $this->services->from((int)$id);
     *
     * from() 方法返回格式：
     * {
     *   "rules": [...],      // FormBuilder 规则
     *   "title": "修改组名",  // 表单标题
     *   "action": "/chat/group/1",  // 提交URL
     *   "method": "POST",    // 提交方法
     *   "info": "",         // 信息
     *   "status": true      // 状态
     * }
     */
    public Map<String, Object> getGroupForm(Integer id, String appid) {
        Map<String, Object> result = new HashMap<>();

        // 1. 如果是编辑，获取现有数据
        ChatServiceGroupEntity group = null;
        if (id != null && id > 0) {
            group = chatServiceGroupMapper.selectById(id);
            if (group == null || !group.getAppid().equals(appid)) {
                throw new CrmChatException("Group does not exist");
            }
        }

        // 2. 构建 FormBuilder 规则
        // PHP: FormBuilder::input('name', '组名', $data['name'] ?? '')
        // PHP: FormBuilder::number('sort', '排序', $data['sort'] ?? 0)
        List<Map<String, Object>> rules = new ArrayList<>();

        Map<String, Object> nameRule = new HashMap<>();
        nameRule.put("type", "input");
        nameRule.put("field", "name");
        nameRule.put("title", "组名");
        nameRule.put("value", group != null ? group.getName() : "");

        Map<String, Object> sortRule = new HashMap<>();
        sortRule.put("type", "inputNumber");  // PHP: lcfirst(basename('InputNumber')) = 'inputNumber'
        sortRule.put("field", "sort");
        sortRule.put("title", "排序");
        sortRule.put("value", group != null ? group.getSort() : 0);

        rules.add(nameRule);
        rules.add(sortRule);

        // 3. 构建返回结果（PHP create_form 格式）
        result.put("rules", rules);
        result.put("title", id != null && id > 0 ? "修改组名" : "添加分组");
        result.put("action", "/chat/group/" + (id != null ? id : 0));
        result.put("method", "POST");
        result.put("info", "");
        result.put("status", true);

        return result;
    }

    /**
     * 保存客服组(创建或更新)
     * PHP Reference: ServiceGroup.php::save()
     */
    @Transactional(rollbackFor = Exception.class)
    public String saveGroup(Integer id, Map<String, Object> data, String appid) {
        String name = (String) data.get("name");
        Integer sort = (Integer) data.getOrDefault("sort", 0);

        if (name == null || name.trim().isEmpty()) {
            throw new CrmChatException("Missing group name");
        }

        if (id != null && id > 0) {
            // 更新
            ChatServiceGroupEntity group = chatServiceGroupMapper.selectById(id);
            if (group == null || !group.getAppid().equals(appid)) {
                throw new CrmChatException("Group does not exist");
            }
            group.setName(name);
            group.setSort(sort);
            chatServiceGroupMapper.updateById(group);
            return "Modified successfully";
        } else {
            // 创建
            ChatServiceGroupEntity group = new ChatServiceGroupEntity();
            group.setAppid(appid);
            group.setName(name);
            group.setSort(sort);
            chatServiceGroupMapper.insert(group);
            return "Added successfully";
        }
    }

    /**
     * 删除客服组
     * PHP Reference: ServiceGroup.php::delete()
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteGroup(Integer id, String appid) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        ChatServiceGroupEntity group = chatServiceGroupMapper.selectById(id);
        if (group == null || !group.getAppid().equals(appid)) {
            throw new CrmChatException("Group does not exist");
        }

        // 检查是否有客服关联此分组
        long count = chatServiceMapper.selectCount(
                new QueryWrapper<io.renren.crmchat.entity.ChatServiceEntity>()
                        .eq("appid", appid)
                        .eq("group_id", id)
        );

        if (count > 0) {
            throw new CrmChatException("Please remove customer service agent association first");
        }

        boolean success = chatServiceGroupMapper.deleteById(id) > 0;
        if (!success) {
            throw new CrmChatException("Failed to delete");
        }

        return true;
    }
}
