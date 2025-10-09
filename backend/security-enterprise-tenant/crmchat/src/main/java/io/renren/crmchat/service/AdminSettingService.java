package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.page.PageData;
import io.renren.crmchat.dao.SystemAdminMapper;
import io.renren.crmchat.dao.SystemRoleMapper;
import io.renren.crmchat.entity.SystemAdminEntity;
import io.renren.crmchat.entity.SystemRoleEntity;
import io.renren.crmchat.service.common.PaginationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin 管理员设置服务
 * PHP Reference: /app/services/system/admin/SystemAdminServices.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getAdminList(): 获取管理员列表，支持多条件搜索
 *    - account_like: 账号或姓名的LIKE搜索
 *    - roles: 角色ID搜索（CONCAT(',',roles,',') LIKE '%,$roles,%'）
 *    - status: 状态过滤
 *    - level: 级别过滤（只能查看下级管理员，level = currentLevel + 1）
 * 2. 角色ID转角色名称
 * 3. 时间格式化
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class AdminSettingService {

    private final SystemAdminMapper systemAdminMapper;
    private final SystemRoleMapper systemRoleMapper;
    private final PaginationService paginationService;
    private final io.renren.crmchat.service.common.PasswordService passwordService;

    /**
     * 获取管理员列表
     * PHP Reference: SystemAdminServices::getAdminList()
     *
     * 业务逻辑:
     * 1. 分页查询
     * 2. account_like: account OR real_name LIKE查询
     * 3. roles: CONCAT(',',roles,',') LIKE '%,$roles,%'
     * 4. status: 状态过滤
     * 5. level: = currentLevel + 1 (只能查看下级管理员)
     * 6. is_del: 固定为0 (未删除)
     * 7. 角色ID转角色名称
     * 8. 时间格式化
     *
     * @param params 查询参数 (page, limit, name, roles, status)
     * @param currentLevel 当前管理员级别
     * @return PageData包含list和count
     */
    public PageData<Map<String, Object>> getAdminList(Map<String, Object> params, Integer currentLevel) {
        // 1. 构建查询条件
        QueryWrapper<SystemAdminEntity> wrapper = new QueryWrapper<>();

        // 2. PHP: is_del = 0 (固定查询未删除的)
        wrapper.eq("is_del", 0);

        // 3. PHP: level = currentLevel + 1 (只能查看下级管理员)
        // 注意: PHP是 $where['level'] = $this->adminInfo['level'] + 1
        wrapper.eq("level", currentLevel + 1);

        // 4. account_like 搜索 (PHP: searchAccountLikeAttr - account OR real_name LIKE)
        if (params.containsKey("name") && params.get("name") != null) {
            String name = params.get("name").toString().trim();
            if (!name.isEmpty()) {
                // PHP: whereLike('account|real_name', '%' . $value . '%')
                wrapper.and(w -> w.like("account", name).or().like("real_name", name));
            }
        }

        // 5. roles 搜索 (PHP: searchRolesAttr - CONCAT(',',roles,',') LIKE '%,$roles,%')
        if (params.containsKey("roles") && params.get("roles") != null) {
            String roles = params.get("roles").toString().trim();
            if (!roles.isEmpty()) {
                // PHP逻辑: CONCAT(',',roles,',') LIKE '%,$roles,%'
                // 确保精确匹配role ID（避免1匹配到11）
                wrapper.apply("CONCAT(',',roles,',') LIKE {0}", "%," + roles + ",%");
            }
        }

        // 6. status 搜索 (PHP: searchStatusAttr)
        if (params.containsKey("status") && params.get("status") != null && !params.get("status").toString().isEmpty()) {
            Integer status = Integer.parseInt(params.get("status").toString());
            wrapper.eq("status", status);
        }

        // 7. 分页查询
        PaginationService.PaginationParams paginationParams = paginationService.extractParams(params);

        // 查询总数
        Long count = systemAdminMapper.selectCount(wrapper);

        // 查询列表数据
        wrapper.last("LIMIT " + paginationParams.getOffset() + "," + paginationParams.getLimit());
        List<SystemAdminEntity> adminList = systemAdminMapper.selectList(wrapper);

        // 8. 获取所有角色信息 (PHP: getRoleArray())
        Map<Integer, String> allRoles = getAllRoles();

        // 9. 转换为Map格式并处理roles和时间字段
        List<Map<String, Object>> list = adminList.stream()
                .map(admin -> adminEntityToMap(admin, allRoles))
                .collect(Collectors.toList());

        // 10. 返回格式: { list: [...], count: 100 }
        return new PageData<>(list, count);
    }

    /**
     * 获取所有角色的映射 Map<RoleId, RoleName>
     * PHP Reference: SystemRoleServices::getRoleArray()
     */
    private Map<Integer, String> getAllRoles() {
        QueryWrapper<SystemRoleEntity> wrapper = new QueryWrapper<>();
        wrapper.select("id", "role_name");
        List<SystemRoleEntity> roles = systemRoleMapper.selectList(wrapper);

        return roles.stream()
                .collect(Collectors.toMap(
                        SystemRoleEntity::getId,
                        SystemRoleEntity::getRoleName,
                        (existing, replacement) -> existing // 处理重复key
                ));
    }

    /**
     * 将SystemAdminEntity转换为Map
     * 匹配PHP返回格式
     *
     * PHP逻辑:
     * - roles字段: 从"1,2,3"转换为"超级管理员,经理"
     * - _add_time: 格式化为"Y-m-d H:i:s"
     * - _last_time: 格式化为"Y-m-d H:i:s"（如果为空则为""）
     */
    private Map<String, Object> adminEntityToMap(SystemAdminEntity entity, Map<Integer, String> allRoles) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("account", entity.getAccount());
        map.put("real_name", entity.getRealName());
        map.put("head_pic", entity.getHeadPic());
        map.put("status", entity.getStatus());
        map.put("level", entity.getLevel());
        map.put("last_ip", entity.getLastIp());

        // PHP逻辑: 处理roles字段 - 将role id转换为role name
        String rolesStr = entity.getRoles();
        if (rolesStr != null && !rolesStr.trim().isEmpty()) {
            List<String> roleNames = new ArrayList<>();
            String[] roleIds = rolesStr.split(",");
            for (String roleIdStr : roleIds) {
                try {
                    Integer roleId = Integer.parseInt(roleIdStr.trim());
                    if (allRoles.containsKey(roleId)) {
                        roleNames.add(allRoles.get(roleId));
                    }
                } catch (NumberFormatException e) {
                    // 忽略无效的role id
                }
            }
            map.put("roles", String.join(",", roleNames));
        } else {
            map.put("roles", "");
        }

        // PHP逻辑: 时间格式化
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // _add_time: date('Y-m-d H:i:s', $item['add_time'])
        if (entity.getAddTime() != null) {
            map.put("_add_time", sdf.format(new Date(entity.getAddTime() * 1000L)));
        } else {
            map.put("_add_time", "");
        }

        // _last_time: $item['last_time'] ? date('Y-m-d H:i:s', $item['last_time']) : ''
        if (entity.getLastTime() != null && entity.getLastTime() > 0) {
            map.put("_last_time", sdf.format(new Date(entity.getLastTime() * 1000L)));
            map.put("last_time", sdf.format(new Date(entity.getLastTime() * 1000L)));
        } else {
            map.put("_last_time", "");
            map.put("last_time", "");
        }

        return map;
    }

    /**
     * 创建管理员
     * PHP Reference: SystemAdminServices::create()
     *
     * 业务逻辑:
     * 1. 验证两次密码是否相同
     * 2. 检查账号是否已存在
     * 3. 密码加密（BCrypt）
     * 4. 角色数组转逗号分隔字符串
     * 5. 设置level = currentLevel + 1
     * 6. 设置add_time = 当前时间戳
     * 7. 事务保存
     *
     * @param data 管理员数据 (account, pwd, conf_pwd, real_name, roles, status)
     * @param currentLevel 当前管理员级别
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void createAdmin(Map<String, Object> data, Integer currentLevel) {
        // 1. PHP: if ($data['conf_pwd'] != $data['pwd'])
        String pwd = data.get("pwd").toString();
        String confPwd = data.get("conf_pwd").toString();

        if (!pwd.equals(confPwd)) {
            throw new io.renren.crmchat.exception.CrmChatException("Passwords do not match");
        }

        // 2. PHP: if ($this->dao->count(['account' => $data['account'], 'is_del' => 0]))
        String account = data.get("account").toString();
        QueryWrapper<SystemAdminEntity> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("account", account);
        checkWrapper.eq("is_del", 0);
        Long count = systemAdminMapper.selectCount(checkWrapper);

        if (count > 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Administrator account already exists");
        }

        // 3. 创建实体
        SystemAdminEntity admin = new SystemAdminEntity();
        admin.setAccount(account);
        admin.setRealName(data.get("real_name").toString());

        // 4. PHP: $data['pwd'] = $this->passwordHash($data['pwd']);
        // 使用BCrypt加密（与PHP的password_hash兼容）
        admin.setPwd(passwordService.hash(pwd));

        // 5. PHP: $data['roles'] = implode(',', $data['roles']);
        // 角色处理：可能是数组或逗号分隔的字符串
        Object rolesObj = data.get("roles");
        String rolesStr;
        if (rolesObj instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<Object> rolesList = (java.util.List<Object>) rolesObj;
            rolesStr = rolesList.stream()
                    .map(Object::toString)
                    .collect(java.util.stream.Collectors.joining(","));
        } else {
            rolesStr = rolesObj.toString();
        }
        admin.setRoles(rolesStr);

        // 6. PHP: $data['level'] = $this->adminInfo['level'] + 1
        admin.setLevel(currentLevel + 1);

        // 7. PHP: $data['add_time'] = time()
        admin.setAddTime((int) (System.currentTimeMillis() / 1000));

        // 8. 状态
        if (data.containsKey("status")) {
            admin.setStatus(Integer.parseInt(data.get("status").toString()));
        } else {
            admin.setStatus(0); // 默认禁用
        }

        // 9. 其他字段默认值
        admin.setIsDel(0);
        admin.setLoginCount(0);
        admin.setLastTime(0);
        admin.setLastIp("");
        admin.setHeadPic("");

        // 10. PHP: if ($this->dao->save($data))
        int result = systemAdminMapper.insert(admin);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to add");
        }

        // PHP有清除缓存的逻辑：\crmeb\services\CacheService::clear()
        // Java中如果使用了缓存，这里也需要清除
    }

    /**
     * 更新管理员
     * PHP Reference: SystemAdminServices::save()
     *
     * 业务逻辑:
     * 1. 检查管理员是否存在
     * 2. 检查管理员是否已删除
     * 3. 修改密码（如果提供了pwd）
     *    - 必须提供conf_pwd
     *    - 两次密码必须相同
     *    - 使用BCrypt加密
     * 4. 修改账号（如果账号变更）
     *    - 检查新账号是否已被其他管理员使用
     * 5. 修改角色（数组转逗号分隔字符串）
     * 6. 修改其他字段：real_name, status
     * 7. 事务保存
     *
     * @param id 管理员ID
     * @param data 更新数据 (account, pwd, conf_pwd, real_name, roles, status)
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void updateAdmin(Integer id, Map<String, Object> data) {
        // 1. PHP: if (!$adminInfo = $this->dao->get($id))
        SystemAdminEntity adminInfo = systemAdminMapper.selectById(id);
        if (adminInfo == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Administrator does not exist, cannot modify");
        }

        // 2. PHP: if ($adminInfo->is_del)
        if (adminInfo.getIsDel() != null && adminInfo.getIsDel() == 1) {
            throw new io.renren.crmchat.exception.CrmChatException("Administrator has been deleted");
        }

        // 3. PHP: 修改密码
        // if ($data['pwd']) { ... }
        if (data.containsKey("pwd") && data.get("pwd") != null && !data.get("pwd").toString().trim().isEmpty()) {
            String pwd = data.get("pwd").toString();

            // PHP: if (!$data['conf_pwd'])
            if (!data.containsKey("conf_pwd") || data.get("conf_pwd") == null || data.get("conf_pwd").toString().trim().isEmpty()) {
                throw new io.renren.crmchat.exception.CrmChatException("Please enter confirmation password");
            }

            String confPwd = data.get("conf_pwd").toString();

            // PHP: if ($data['conf_pwd'] != $data['pwd'])
            if (!confPwd.equals(pwd)) {
                throw new io.renren.crmchat.exception.CrmChatException("Passwords do not match");
            }

            // PHP: $adminInfo->pwd = $this->passwordHash($data['pwd']);
            adminInfo.setPwd(passwordService.hash(pwd));
        }

        // 4. PHP: 修改账号
        // if (isset($data['account']) && $data['account'] != $adminInfo->account && $this->dao->isAccountUsable($data['account'], $id))
        if (data.containsKey("account") && data.get("account") != null) {
            String newAccount = data.get("account").toString();
            if (!newAccount.equals(adminInfo.getAccount())) {
                // 检查账号是否已被其他管理员使用
                QueryWrapper<SystemAdminEntity> checkWrapper = new QueryWrapper<>();
                checkWrapper.eq("account", newAccount);
                checkWrapper.eq("is_del", 0);
                checkWrapper.ne("id", id); // 排除当前管理员
                Long count = systemAdminMapper.selectCount(checkWrapper);

                if (count > 0) {
                    throw new io.renren.crmchat.exception.CrmChatException("Administrator account already exists");
                }

                adminInfo.setAccount(newAccount);
            }
        }

        // 5. PHP: if (isset($data['roles']))
        if (data.containsKey("roles") && data.get("roles") != null) {
            Object rolesObj = data.get("roles");
            String rolesStr;
            if (rolesObj instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Object> rolesList = (java.util.List<Object>) rolesObj;
                rolesStr = rolesList.stream()
                        .map(Object::toString)
                        .collect(java.util.stream.Collectors.joining(","));
            } else {
                rolesStr = rolesObj.toString();
            }
            adminInfo.setRoles(rolesStr);
        }

        // 6. PHP: $adminInfo->real_name = $data['real_name'] ?? $adminInfo->real_name;
        if (data.containsKey("real_name") && data.get("real_name") != null) {
            adminInfo.setRealName(data.get("real_name").toString());
        }

        // 7. PHP: $adminInfo->status = $data['status'];
        if (data.containsKey("status")) {
            adminInfo.setStatus(Integer.parseInt(data.get("status").toString()));
        }

        // 8. PHP: if ($adminInfo->save())
        int result = systemAdminMapper.updateById(adminInfo);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to modify");
        }

        // PHP有清除缓存的逻辑：\crmeb\services\CacheService::clear()
        // Java中如果使用了缓存，这里也需要清除
    }

    /**
     * 删除管理员（软删除）
     * PHP Reference: Admin.php::delete()
     *
     * 业务逻辑:
     * 1. 检查ID是否有效
     * 2. 软删除：设置is_del=1, status=0
     * 3. 使用update方法更新数据库
     *
     * @param id 管理员ID
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void deleteAdmin(Integer id) {
        // 1. PHP: if (!$id) return $this->fail('删除失败，缺少参数');
        if (id == null || id <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Deletion failed: missing parameter");
        }

        // 2. PHP: if ($this->services->update((int)$id, ['is_del' => 1, 'status' => 0]))
        // 软删除：设置is_del=1, status=0
        SystemAdminEntity admin = new SystemAdminEntity();
        admin.setId(id);
        admin.setIsDel(1);
        admin.setStatus(0);

        int result = systemAdminMapper.updateById(admin);

        // 3. PHP: else return $this->fail('删除失败');
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to delete");
        }

        // PHP有清除缓存的逻辑：\crmeb\services\CacheService::clear()
    }

    /**
     * 修改管理员状态
     * PHP Reference: Admin.php::set_status()
     *
     * 业务逻辑:
     * 1. 更新status字段
     * 2. 返回成功消息（根据status值）
     *
     * @param id 管理员ID
     * @param status 状态值（0-禁用，1-启用）
     * @return 成功消息
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public String setStatus(Integer id, Integer status) {
        // PHP: $this->services->update((int)$id, ['status' => $status]);
        SystemAdminEntity admin = new SystemAdminEntity();
        admin.setId(id);
        admin.setStatus(status);

        int result = systemAdminMapper.updateById(admin);

        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to modify");
        }

        // PHP: return $this->success($status == 0 ? '关闭成功' : '开启成功');
        return status == 0 ? "Closed successfully" : "Enabled successfully";
    }

    /**
     * 获取当前管理员信息
     * PHP Reference: Admin.php::info()
     *
     * 业务逻辑:
     * 1. 根据admin ID获取管理员详细信息
     * 2. 返回管理员信息（不包含密码）
     *
     * @param adminId 当前登录管理员ID
     * @return 管理员信息
     */
    public Map<String, Object> getAdminInfo(Integer adminId) {
        // PHP: return $this->success($this->adminInfo);
        SystemAdminEntity admin = systemAdminMapper.selectById(adminId);
        if (admin == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Administrator does not exist");
        }

        if (admin.getIsDel() != null && admin.getIsDel() == 1) {
            throw new io.renren.crmchat.exception.CrmChatException("Administrator has been deleted");
        }

        // 转换为Map并排除密码
        Map<String, Object> result = new HashMap<>();
        result.put("id", admin.getId());
        result.put("account", admin.getAccount());
        result.put("real_name", admin.getRealName());
        result.put("head_pic", admin.getHeadPic());
        result.put("roles", admin.getRoles());
        result.put("level", admin.getLevel());
        result.put("status", admin.getStatus());
        result.put("last_ip", admin.getLastIp());
        result.put("last_time", admin.getLastTime());
        result.put("add_time", admin.getAddTime());
        result.put("login_count", admin.getLoginCount());

        return result;
    }

    /**
     * 修改当前管理员信息
     * PHP Reference: SystemAdminServices::updateAdmin()
     *
     * 业务逻辑:
     * 1. 验证管理员存在且未删除
     * 2. real_name必填
     * 3. 修改密码（可选）:
     *    - 验证原密码正确
     *    - new_pwd和conf_pwd必填
     *    - 两次新密码必须一致
     *    - 使用BCrypt加密新密码
     * 4. 更新real_name和head_pic
     * 5. 保存更新
     *
     * @param adminId 当前登录管理员ID
     * @param data 更新数据 (real_name, head_pic, pwd, new_pwd, conf_pwd)
     */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void updateCurrentAdmin(Integer adminId, Map<String, Object> data) {
        // 1. PHP: $adminInfo = $this->dao->get($id);
        SystemAdminEntity adminInfo = systemAdminMapper.selectById(adminId);
        if (adminInfo == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Administrator information not found");
        }

        // 2. PHP: if ($adminInfo->is_del)
        if (adminInfo.getIsDel() != null && adminInfo.getIsDel() == 1) {
            throw new io.renren.crmchat.exception.CrmChatException("Administrator has been deleted");
        }

        // 3. PHP: if (!$data['real_name'])
        if (!data.containsKey("real_name") || data.get("real_name") == null || data.get("real_name").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Administrator name cannot be empty");
        }

        // 4. PHP: 密码修改逻辑
        if (data.containsKey("pwd") && data.get("pwd") != null && !data.get("pwd").toString().trim().isEmpty()) {
            String pwd = data.get("pwd").toString();

            // PHP: if (!password_verify($data['pwd'], $adminInfo['pwd']))
            if (!passwordService.verify(pwd, adminInfo.getPwd())) {
                throw new io.renren.crmchat.exception.CrmChatException("Original password is incorrect");
            }

            // PHP: if (!$data['new_pwd'])
            if (!data.containsKey("new_pwd") || data.get("new_pwd") == null || data.get("new_pwd").toString().trim().isEmpty()) {
                throw new io.renren.crmchat.exception.CrmChatException("Please enter new password");
            }

            String newPwd = data.get("new_pwd").toString();

            // PHP: if (!$data['conf_pwd'])
            if (!data.containsKey("conf_pwd") || data.get("conf_pwd") == null || data.get("conf_pwd").toString().trim().isEmpty()) {
                throw new io.renren.crmchat.exception.CrmChatException("Please enter confirmation password");
            }

            String confPwd = data.get("conf_pwd").toString();

            // PHP: if ($data['new_pwd'] != $data['conf_pwd'])
            if (!newPwd.equals(confPwd)) {
                throw new io.renren.crmchat.exception.CrmChatException("Passwords do not match");
            }

            // PHP: $adminInfo->pwd = $this->passwordHash($data['new_pwd']);
            adminInfo.setPwd(passwordService.hash(newPwd));
        }

        // 5. PHP: $adminInfo->real_name = $data['real_name'];
        adminInfo.setRealName(data.get("real_name").toString());

        // 6. PHP: $adminInfo->head_pic = $data['head_pic'];
        if (data.containsKey("head_pic") && data.get("head_pic") != null) {
            adminInfo.setHeadPic(data.get("head_pic").toString());
        }

        // 7. PHP: if ($adminInfo->save())
        int result = systemAdminMapper.updateById(adminInfo);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to modify");
        }
    }

    /**
     * 获取管理员身份列表
     * PHP Reference: SystemRoleServices::getRoleFormSelect()
     *
     * 业务逻辑:
     * 1. 根据level和status查询角色列表
     * 2. 转换为前端选择器格式 [{label: roleName, value: id}]
     *
     * @param currentLevel 当前管理员级别
     * @return 角色选项列表
     */
    public java.util.List<Map<String, Object>> getRoleList(Integer currentLevel) {
        // PHP: $list = $this->getRoleArray(['level' => $level, 'status' => 1]);
        QueryWrapper<SystemRoleEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("level", currentLevel);
        wrapper.eq("status", 1);
        wrapper.select("id", "role_name");

        List<SystemRoleEntity> roles = systemRoleMapper.selectList(wrapper);

        // PHP: foreach ($list as $id => $roleName) { $options[] = ['label' => $roleName, 'value' => $id]; }
        return roles.stream()
                .map(role -> {
                    Map<String, Object> option = new HashMap<>();
                    option.put("label", role.getRoleName());
                    option.put("value", role.getId());
                    return option;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
