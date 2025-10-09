package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.dao.SystemAdminMapper;
import io.renren.crmchat.entity.SystemAdminEntity;
import io.renren.crmchat.service.common.PasswordService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tenant System Admin Service - 租户级系统管理员管理
 * PHP Reference: SystemAdminServices.php
 *
 * 业务逻辑说明:
 * - 租户管理员通过 level 层级关系管理下一级管理员
 * - 不使用 appid 隔离，而是通过 level 隔离
 * - 当前登录管理员只能管理 level = currentLevel + 1 的管理员
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class TenantSystemAdminService {

    private final SystemAdminMapper systemAdminMapper;
    private final PasswordService passwordService;

    /**
     * 获取管理员列表
     * PHP Reference: SystemAdminServices::getAdminList()
     *
     * @param filters 查询条件
     * @param currentAdminLevel 当前管理员等级
     * @return 管理员列表和总数
     */
    public Map<String, Object> getAdminList(Map<String, Object> filters, Integer currentAdminLevel) {
        // PHP: $where['level'] = $this->adminInfo['level'] + 1;
        Integer targetLevel = currentAdminLevel + 1;

        QueryWrapper<SystemAdminEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("level", targetLevel);
        wrapper.eq("is_del", filters.getOrDefault("is_del", 0));

        // 账号模糊查询
        if (filters.containsKey("name") && filters.get("name") != null && !filters.get("name").toString().trim().isEmpty()) {
            wrapper.like("account", filters.get("name").toString());
        }

        // 角色筛选
        if (filters.containsKey("roles") && filters.get("roles") != null && !filters.get("roles").toString().trim().isEmpty()) {
            wrapper.like("roles", filters.get("roles").toString());
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

        List<SystemAdminEntity> list = systemAdminMapper.selectList(wrapper);

        // 计算总数
        QueryWrapper<SystemAdminEntity> countWrapper = new QueryWrapper<>();
        countWrapper.eq("level", targetLevel);
        countWrapper.eq("is_del", filters.getOrDefault("is_del", 0));
        if (filters.containsKey("name") && filters.get("name") != null && !filters.get("name").toString().trim().isEmpty()) {
            countWrapper.like("account", filters.get("name").toString());
        }
        if (filters.containsKey("roles") && filters.get("roles") != null && !filters.get("roles").toString().trim().isEmpty()) {
            countWrapper.like("roles", filters.get("roles").toString());
        }
        if (filters.containsKey("status") && filters.get("status") != null && !filters.get("status").toString().trim().isEmpty()) {
            countWrapper.eq("status", filters.get("status"));
        }
        long count = systemAdminMapper.selectCount(countWrapper);

        // 隐藏密码字段
        for (SystemAdminEntity admin : list) {
            admin.setPwd(null);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("count", count);

        return result;
    }

    /**
     * 创建管理员
     * PHP Reference: SystemAdminServices::create()
     *
     * @param data 管理员数据
     * @param currentAdminLevel 当前管理员等级
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createAdmin(Map<String, Object> data, Integer currentAdminLevel) {
        // PHP: if ($data['conf_pwd'] != $data['pwd'])
        String pwd = (String) data.get("pwd");
        String confPwd = (String) data.get("conf_pwd");

        if (pwd == null || pwd.trim().isEmpty()) {
            throw new CrmChatException("Please enter administrator password");
        }

        if (confPwd == null || !confPwd.equals(pwd)) {
            throw new CrmChatException("Passwords do not match");
        }

        String account = (String) data.get("account");
        if (account == null || account.trim().isEmpty()) {
            throw new CrmChatException("Please enter administrator account");
        }

        // PHP: if ($this->dao->count(['account' => $data['account'], 'is_del' => 0]))
        QueryWrapper<SystemAdminEntity> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("account", account);
        checkWrapper.eq("is_del", 0);
        if (systemAdminMapper.selectCount(checkWrapper) > 0) {
            throw new CrmChatException("Administrator account already exists");
        }

        SystemAdminEntity admin = new SystemAdminEntity();
        admin.setAccount(account);
        admin.setPwd(passwordService.hash(pwd));
        admin.setRealName((String) data.get("real_name"));
        admin.setRoles((String) data.get("roles")); // 前端应该传逗号分隔的字符串
        admin.setStatus((Integer) data.getOrDefault("status", 1));
        admin.setLevel(currentAdminLevel + 1); // PHP: $data['level'] = $this->adminInfo['level'] + 1;
        admin.setAddTime((int) (System.currentTimeMillis() / 1000));
        admin.setIsDel(0);
        admin.setLoginCount(0);

        return systemAdminMapper.insert(admin) > 0;
    }

    /**
     * 获取管理员详情（编辑表单数据）
     * PHP Reference: SystemAdminServices::updateForm()
     *
     * @param id 管理员ID
     * @param currentAdminLevel 当前管理员等级
     * @return 管理员信息
     */
    public SystemAdminEntity getAdminInfo(Integer id, Integer currentAdminLevel) {
        SystemAdminEntity admin = systemAdminMapper.selectById(id);

        if (admin == null) {
            throw new CrmChatException("Administrator does not exist");
        }

        if (admin.getIsDel() == 1) {
            throw new CrmChatException("Administrator has been deleted");
        }

        // 验证层级权限：只能查看下一级管理员
        if (!admin.getLevel().equals(currentAdminLevel + 1)) {
            throw new CrmChatException("No permission to access this administrator");
        }

        // 隐藏密码
        admin.setPwd(null);

        return admin;
    }

    /**
     * 更新管理员信息
     * PHP Reference: SystemAdminServices::save()
     *
     * @param id 管理员ID
     * @param data 更新数据
     * @param currentAdminLevel 当前管理员等级
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAdmin(Integer id, Map<String, Object> data, Integer currentAdminLevel) {
        SystemAdminEntity admin = systemAdminMapper.selectById(id);

        if (admin == null) {
            throw new CrmChatException("Administrator does not exist, cannot modify");
        }

        if (admin.getIsDel() == 1) {
            throw new CrmChatException("Administrator has been deleted");
        }

        // 验证层级权限
        if (!admin.getLevel().equals(currentAdminLevel + 1)) {
            throw new CrmChatException("No permission to modify this administrator");
        }

        // PHP: 修改密码
        if (data.containsKey("pwd") && data.get("pwd") != null && !data.get("pwd").toString().trim().isEmpty()) {
            String pwd = data.get("pwd").toString();
            String confPwd = (String) data.get("conf_pwd");

            if (confPwd == null || confPwd.trim().isEmpty()) {
                throw new CrmChatException("Please enter confirmation password");
            }

            if (!confPwd.equals(pwd)) {
                throw new CrmChatException("Passwords do not match");
            }

            admin.setPwd(passwordService.hash(pwd));
        }

        // PHP: 修改账号
        if (data.containsKey("account") && data.get("account") != null) {
            String newAccount = data.get("account").toString();
            if (!newAccount.equals(admin.getAccount())) {
                QueryWrapper<SystemAdminEntity> checkWrapper = new QueryWrapper<>();
                checkWrapper.eq("account", newAccount);
                checkWrapper.eq("is_del", 0);
                checkWrapper.ne("id", id);
                if (systemAdminMapper.selectCount(checkWrapper) > 0) {
                    throw new CrmChatException("Administrator account already exists");
                }
                admin.setAccount(newAccount);
            }
        }

        // PHP: 修改其他字段
        if (data.containsKey("real_name")) {
            admin.setRealName(data.get("real_name").toString());
        }
        if (data.containsKey("roles")) {
            admin.setRoles(data.get("roles").toString());
        }
        if (data.containsKey("status")) {
            admin.setStatus((Integer) data.get("status"));
        }

        return systemAdminMapper.updateById(admin) > 0;
    }

    /**
     * 删除管理员（软删除）
     * PHP Reference: Admin.php::delete()
     *
     * @param id 管理员ID
     * @param currentAdminLevel 当前管理员等级
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAdmin(Integer id, Integer currentAdminLevel) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Deletion failed: missing parameter");
        }

        SystemAdminEntity admin = systemAdminMapper.selectById(id);

        if (admin == null) {
            throw new CrmChatException("Administrator does not exist");
        }

        // 验证层级权限
        if (!admin.getLevel().equals(currentAdminLevel + 1)) {
            throw new CrmChatException("No permission to delete this administrator");
        }

        // PHP: if ($this->services->update((int)$id, ['is_del' => 1, 'status' => 0]))
        admin.setIsDel(1);
        admin.setStatus(0);

        return systemAdminMapper.updateById(admin) > 0;
    }

    /**
     * 修改管理员状态
     * PHP Reference: Admin.php::set_status()
     *
     * @param id 管理员ID
     * @param status 状态值
     * @param currentAdminLevel 当前管理员等级
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Integer id, Integer status, Integer currentAdminLevel) {
        SystemAdminEntity admin = systemAdminMapper.selectById(id);

        if (admin == null) {
            throw new CrmChatException("Administrator does not exist");
        }

        // 验证层级权限
        if (!admin.getLevel().equals(currentAdminLevel + 1)) {
            throw new CrmChatException("No permission to modify this administrator status");
        }

        // PHP: $this->services->update((int)$id, ['status' => $status]);
        admin.setStatus(status);

        return systemAdminMapper.updateById(admin) > 0;
    }

    /**
     * 获取当前登录管理员信息
     * PHP Reference: Admin.php::info()
     *
     * 注意: 这里返回的是租户管理员自己的信息，不是system_admin表
     * 实际应该从 TenantsService 获取
     *
     * @param adminId 当前管理员ID（从JWT获取）
     * @return 管理员信息
     */
    public SystemAdminEntity getCurrentAdminInfo(Integer adminId) {
        // PHP: return $this->success($this->adminInfo);
        SystemAdminEntity admin = systemAdminMapper.selectById(adminId);

        if (admin == null) {
            throw new CrmChatException("Administrator does not exist");
        }

        if (admin.getIsDel() == 1) {
            throw new CrmChatException("Administrator has been deleted");
        }

        // 隐藏密码
        admin.setPwd(null);

        return admin;
    }
}
