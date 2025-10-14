package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatUserGroupMapper;
import io.renren.crmchat.dao.ChatUserMapper;
import io.renren.crmchat.entity.ChatUserGroupEntity;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.formbuilder.FormBuilder;
import io.renren.crmchat.formbuilder.FormHelper;
import io.renren.crmchat.formbuilder.components.BaseComponent;
import io.renren.crmchat.security.TenantGuard;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tenant 用户分组管理服务
 * PHP Reference: /app/controller/tenant/user/Group.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getGroupList(): 获取分组列表
 *    - 根据appid查询
 *    - 返回id和group_name字段
 * 2. createGroup(): 创建分组
 *    - 验证group_name非空
 *    - 检查group_name唯一性
 *    - 保存分组
 * 3. updateGroup(): 更新分组
 *    - 验证group_name非空
 *    - 验证分组存在
 *    - 检查group_name唯一性（排除当前分组）
 *    - 更新分组
 * 4. deleteGroup(): 删除分组
 *    - 检查是否有用户关联此分组
 *    - 若存在关联用户，则返回错误"Please remove the associated user groups first"
 *    - 若不存在关联用户，则删除分组
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantUserGroupService {

    private final ChatUserGroupMapper chatUserGroupMapper;
    private final ChatUserMapper chatUserMapper;
    private final FormBuilder formBuilder;

    /**
     * 获取分组列表（分页）
     * GET /api/tenant/user/group
     *
     * PHP Reference: Group.php::index()
     *
     * 业务逻辑:
     * 1. 根据appid查询分组列表
     * 2. 支持分页
     * 3. 返回 data 和 count 字段
     *
     * @param page  页码
     * @param limit 每页数量
     * @return Map<String, Object> 包含 data 和 count
     */
    public Map<String, Object> getGroupList(Integer page, Integer limit) {
        // PHP: $this->services->getGroupList(['*'], true, $appid)

        QueryWrapper<ChatUserGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");
        List<ChatUserGroupEntity> allGroups = chatUserGroupMapper.selectList(wrapper);

        // 计算分页
        int total = allGroups.size();
        int start = (page - 1) * limit;
        int end = Math.min(start + limit, total);

        List<ChatUserGroupEntity> paginatedList;
        if (start >= total) {
            paginatedList = new ArrayList<>();
        } else {
            paginatedList = allGroups.subList(start, end);
        }

        // 前端期望 res.data.list 和 res.data.count 结构
        Map<String, Object> result = new HashMap<>();
        result.put("list", paginatedList);
        result.put("count", total);
        return result;
    }

    /**
     * 获取创建表单数据
     * GET /api/tenant/user/group/create
     *
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
            "/user/group",
            "POST"
        );
    }

    /**
     * 获取编辑表单数据
     * GET /api/tenant/user/group/:id/edit
     *
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
    public Map<String, Object> getEditForm(Integer id) {
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
            "/user/group/" + id,
            "PUT"
        );
    }

    /**
     * 创建分组
     * POST /api/tenant/user/group
     *
     * PHP Reference: Group.php::save()
     *
     * 业务逻辑:
     * 1. 验证group_name非空
     * 2. 检查group_name唯一性
     * 3. 保存分组
     *
     * @param data          分组数据（group_name）
     * @return 新创建的分组ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createGroup(Map<String, Object> data) {
        // 1. PHP: if (!$data['group_name']) return $this->fail('请输入分组名称');
        if (!data.containsKey("group_name") || data.get("group_name") == null || data.get("group_name").toString().trim().isEmpty()) {
            throw new CrmChatException("Please enter group name");
        }

        String groupName = data.get("group_name").toString();

        // 2. PHP: if ($groupName) throw new AdminException('该分组已经存在');
        QueryWrapper<ChatUserGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("group_name", groupName);
        Long count = chatUserGroupMapper.selectCount(wrapper);

        if (count > 0) {
            throw new CrmChatException("This group already exists");
        }

        // 3. 保存分组
        ChatUserGroupEntity group = new ChatUserGroupEntity();
        group.setGroupName(groupName);

        int result = chatUserGroupMapper.insert(group);
        if (result <= 0) {
            throw new CrmChatException("Failed to add");
        }

        return group.getId();
    }

    /**
     * 更新分组
     * PUT /api/tenant/user/group/:id
     *
     * PHP Reference: Group.php::update()
     *
     * 业务逻辑:
     * 1. 验证group_name非空
     * 2. 验证分组存在
     * 3. 检查group_name唯一性（排除当前分组）
     * 4. 更新分组
     *
     * @param id           分组ID
     * @param data         分组数据（group_name）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateGroup(Integer id, Map<String, Object> data) {
        // 1. PHP: if (!$data['group_name']) return $this->fail('请输入分组名称');
        if (!data.containsKey("group_name") || data.get("group_name") == null || data.get("group_name").toString().trim().isEmpty()) {
            throw new CrmChatException("Please enter group name");
        }

        String groupName = data.get("group_name").toString();

        // 2. PHP: if (!$this->getGroup($id)) throw new AdminException('数据不存在');
        ChatUserGroupEntity group = chatUserGroupMapper.selectById(id);
        if (group == null) {
            throw new CrmChatException("Data does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(group.getAppid(), "Data does not exist");

        // 3. PHP: if ($groupName && $id != $groupName['id']) throw new AdminException('该分组已经存在');
        QueryWrapper<ChatUserGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("group_name", groupName);
        wrapper.ne("id", id); // 排除当前分组
        Long count = chatUserGroupMapper.selectCount(wrapper);

        if (count > 0) {
            throw new CrmChatException("This group already exists");
        }

        // 4. 更新分组
        group.setGroupName(groupName);

        int result = chatUserGroupMapper.updateById(group);
        if (result <= 0) {
            throw new CrmChatException("Modification failed or nothing was changed");
        }
    }

    /**
     * 删除分组
     * DELETE /api/tenant/user/group
     *
     * PHP Reference: Group.php::delete()
     *
     * 业务逻辑:
     * 1. 验证分组存在
     * 2. 检查是否有用户关联此分组
     * 3. 如果有，返回错误
     * 4. 如果没有，删除分组
     *
     * @param id           分组ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Integer id) {
        // 1. 验证分组存在
        ChatUserGroupEntity group = chatUserGroupMapper.selectById(id);
        if (group == null) {
            throw new CrmChatException("Data does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(group.getAppid(), "Data does not exist");

        // 2. PHP: if ($userServices->count(['group_id' => $id])) throw new AdminException('请先清除掉,关联的用户分组');
        QueryWrapper<io.renren.crmchat.entity.ChatUserEntity> userWrapper = new QueryWrapper<>();
        userWrapper.eq("group_id", id);
        Long userCount = chatUserMapper.selectCount(userWrapper);

        if (userCount > 0) {
            throw new CrmChatException("Please remove associated user groups first");
        }

        // 3. 删除分组
        int result = chatUserGroupMapper.deleteById(id);
        if (result <= 0) {
            throw new CrmChatException("Deletion failed, please try again later");
        }
    }
}
