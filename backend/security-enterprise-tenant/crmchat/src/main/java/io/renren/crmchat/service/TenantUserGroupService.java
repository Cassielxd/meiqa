package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatUserGroupMapper;
import io.renren.crmchat.dao.ChatUserMapper;
import io.renren.crmchat.entity.ChatUserGroupEntity;
import io.renren.crmchat.security.TenantGuard;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 获取分组列表
     * GET /api/tenant/user/group
     *
     * PHP Reference: Group.php::index()
     *
     * 业务逻辑:
     * 1. 根据appid查询分组列表
     * 2. 返回所有字段
     *
     * @return 分组列表
     */
    public List<ChatUserGroupEntity> getGroupList() {
        // PHP: $this->services->getGroupList(['*'], true, $appid)

        QueryWrapper<ChatUserGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("id");

        return chatUserGroupMapper.selectList(wrapper);
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
            throw new io.renren.crmchat.exception.CrmChatException("Please enter group name");
        }

        String groupName = data.get("group_name").toString();

        // 2. PHP: if ($groupName) throw new AdminException('该分组已经存在');
        QueryWrapper<ChatUserGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("group_name", groupName);
        Long count = chatUserGroupMapper.selectCount(wrapper);

        if (count > 0) {
            throw new io.renren.crmchat.exception.CrmChatException("This group already exists");
        }

        // 3. 保存分组
        ChatUserGroupEntity group = new ChatUserGroupEntity();
        group.setGroupName(groupName);

        int result = chatUserGroupMapper.insert(group);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to add");
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
            throw new io.renren.crmchat.exception.CrmChatException("Please enter group name");
        }

        String groupName = data.get("group_name").toString();

        // 2. PHP: if (!$this->getGroup($id)) throw new AdminException('数据不存在');
        ChatUserGroupEntity group = chatUserGroupMapper.selectById(id);
        if (group == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Data does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(group.getAppid(), "Data does not exist");

        // 3. PHP: if ($groupName && $id != $groupName['id']) throw new AdminException('该分组已经存在');
        QueryWrapper<ChatUserGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("group_name", groupName);
        wrapper.ne("id", id); // 排除当前分组
        Long count = chatUserGroupMapper.selectCount(wrapper);

        if (count > 0) {
            throw new io.renren.crmchat.exception.CrmChatException("This group already exists");
        }

        // 4. 更新分组
        group.setGroupName(groupName);

        int result = chatUserGroupMapper.updateById(group);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Modification failed or nothing was changed");
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
            throw new io.renren.crmchat.exception.CrmChatException("Data does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(group.getAppid(), "Data does not exist");

        // 2. PHP: if ($userServices->count(['group_id' => $id])) throw new AdminException('请先清除掉,关联的用户分组');
        QueryWrapper<io.renren.crmchat.entity.ChatUserEntity> userWrapper = new QueryWrapper<>();
        userWrapper.eq("group_id", id);
        Long userCount = chatUserMapper.selectCount(userWrapper);

        if (userCount > 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Please remove associated user groups first");
        }

        // 3. 删除分组
        int result = chatUserGroupMapper.deleteById(id);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Deletion failed, please try again later");
        }
    }
}
