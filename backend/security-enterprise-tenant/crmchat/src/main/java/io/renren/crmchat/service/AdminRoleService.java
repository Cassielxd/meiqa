package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.dao.SystemRoleMapper;
import io.renren.crmchat.entity.SystemRoleEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin Role Service - 管理员角色管理
 * PHP Reference: SystemRoleServices.php
 *
 * 业务逻辑说明:
 * - 角色通过 level 层级关系管理
 * - 当前管理员只能管理 level = currentLevel + 1 的角色
 * - rules字段存储菜单权限ID（逗号分隔）
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class AdminRoleService {

    private final SystemRoleMapper systemRoleMapper;

    /**
     * 获取角色列表
     * PHP Reference: SystemRoleServices::getRoleList()
     *
     * @param filters 查询条件
     * @param currentAdminLevel 当前管理员等级
     * @return 角色列表和总数
     */
    public Map<String, Object> getRoleList(Map<String, Object> filters, Integer currentAdminLevel) {
        // PHP: $where['level'] = $this->adminInfo['level'] + 1;
        Integer targetLevel = currentAdminLevel + 1;

        QueryWrapper<SystemRoleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("level", targetLevel);

        // 角色名称筛选
        if (filters.containsKey("role_name") && filters.get("role_name") != null && !filters.get("role_name").toString().trim().isEmpty()) {
            wrapper.like("role_name", filters.get("role_name").toString());
        }

        // 状态筛选
        if (filters.containsKey("status") && filters.get("status") != null && !filters.get("status").toString().trim().isEmpty()) {
            wrapper.eq("status", filters.get("status"));
        }

        // 分页
        Integer page = (Integer) filters.getOrDefault("page", 1);
        Integer limit = (Integer) filters.getOrDefault("limit", 20);
        int offset = (page - 1) * limit;

        wrapper.orderByDesc("id");
        wrapper.last("LIMIT " + offset + ", " + limit);

        List<SystemRoleEntity> list = systemRoleMapper.selectList(wrapper);

        // 计算总数
        QueryWrapper<SystemRoleEntity> countWrapper = new QueryWrapper<>();
        countWrapper.eq("level", targetLevel);
        if (filters.containsKey("role_name") && filters.get("role_name") != null && !filters.get("role_name").toString().trim().isEmpty()) {
            countWrapper.like("role_name", filters.get("role_name").toString());
        }
        if (filters.containsKey("status") && filters.get("status") != null && !filters.get("status").toString().trim().isEmpty()) {
            countWrapper.eq("status", filters.get("status"));
        }
        long count = systemRoleMapper.selectCount(countWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("count", count);

        return result;
    }

    /**
     * 创建角色
     * PHP Reference: Role.php::save($id=null)
     *
     * @param data 角色数据
     * @param currentAdminLevel 当前管理员等级
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createRole(Map<String, Object> data, Integer currentAdminLevel) {
        // PHP: if (!$data['role_name']) return $this->fail('请输入身份名称');
        String roleName = (String) data.get("role_name");
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new CrmChatException("Please enter role name");
        }

        // PHP: if (!is_array($data['rules']) || !count($data['rules']))
        String rules = (String) data.get("rules");
        if (rules == null || rules.trim().isEmpty()) {
            throw new CrmChatException("Please select at least one permission");
        }

        SystemRoleEntity role = new SystemRoleEntity();
        role.setRoleName(roleName);
        role.setRules(rules);
        role.setStatus((Integer) data.getOrDefault("status", 1));
        role.setLevel(currentAdminLevel + 1); // PHP: $data['level'] = $this->adminInfo['level'] + 1;

        return systemRoleMapper.insert(role) > 0;
    }

    /**
     * 获取角色详情（编辑表单数据）
     * PHP Reference: Role.php::edit()
     *
     * @param id 角色ID
     * @param currentAdminLevel 当前管理员等级
     * @return 角色信息
     */
    public SystemRoleEntity getRoleInfo(Integer id, Integer currentAdminLevel) {
        SystemRoleEntity role = systemRoleMapper.selectById(id);

        if (role == null) {
            throw new CrmChatException("Role to modify does not exist");
        }

        // 验证层级权限
        if (!role.getLevel().equals(currentAdminLevel + 1)) {
            throw new CrmChatException("No permission to access this role");
        }

        return role;
    }

    /**
     * 更新角色信息
     * PHP Reference: Role.php::save($id)
     *
     * @param id 角色ID
     * @param data 更新数据
     * @param currentAdminLevel 当前管理员等级
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(Integer id, Map<String, Object> data, Integer currentAdminLevel) {
        SystemRoleEntity role = systemRoleMapper.selectById(id);

        if (role == null) {
            throw new CrmChatException("Role to modify does not exist");
        }

        // 验证层级权限
        if (!role.getLevel().equals(currentAdminLevel + 1)) {
            throw new CrmChatException("No permission to modify this role");
        }

        // PHP: if (!$data['role_name']) return $this->fail('请输入身份名称');
        String roleName = (String) data.get("role_name");
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new CrmChatException("Please enter role name");
        }

        // PHP: if (!is_array($data['rules']) || !count($data['rules']))
        String rules = (String) data.get("rules");
        if (rules == null || rules.trim().isEmpty()) {
            throw new CrmChatException("Please select at least one permission");
        }

        role.setRoleName(roleName);
        role.setRules(rules);
        role.setStatus((Integer) data.getOrDefault("status", role.getStatus()));

        return systemRoleMapper.updateById(role) > 0;
    }

    /**
     * 删除角色
     * PHP Reference: Role.php::delete()
     *
     * @param id 角色ID
     * @param currentAdminLevel 当前管理员等级
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Integer id, Integer currentAdminLevel) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        SystemRoleEntity role = systemRoleMapper.selectById(id);

        if (role == null) {
            throw new CrmChatException("Role to delete does not exist");
        }

        // 验证层级权限
        if (!role.getLevel().equals(currentAdminLevel + 1)) {
            throw new CrmChatException("No permission to delete this role");
        }

        return systemRoleMapper.deleteById(id) > 0;
    }

    /**
     * 修改角色状态
     * PHP Reference: Role.php::set_status()
     *
     * @param id 角色ID
     * @param status 状态值
     * @param currentAdminLevel 当前管理员等级
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Integer id, Integer status, Integer currentAdminLevel) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        SystemRoleEntity role = systemRoleMapper.selectById(id);

        if (role == null) {
            throw new CrmChatException("This role was not found");
        }

        // 验证层级权限
        if (!role.getLevel().equals(currentAdminLevel + 1)) {
            throw new CrmChatException("No permission to modify this role status");
        }

        role.setStatus(status);

        return systemRoleMapper.updateById(role) > 0;
    }
}
