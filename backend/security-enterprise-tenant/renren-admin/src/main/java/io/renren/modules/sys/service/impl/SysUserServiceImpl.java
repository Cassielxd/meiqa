/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.common.constant.Constant;
import io.renren.common.context.TenantContext;
import io.renren.common.enums.SuperTenantEnum;
import io.renren.common.exception.RenException;
import io.renren.common.page.PageData;
import io.renren.common.service.impl.BaseServiceImpl;
import io.renren.common.user.UserDetail;
import io.renren.common.utils.ConvertUtils;
import io.renren.modules.security.password.PasswordUtils;
import io.renren.modules.security.service.SysUserTokenService;
import io.renren.modules.security.user.SecurityUser;
import io.renren.modules.sys.dao.SysUserDao;
import io.renren.modules.sys.dto.SysUserDTO;
import io.renren.modules.sys.entity.SysUserEntity;
import io.renren.modules.sys.enums.SuperAdminEnum;
import io.renren.modules.sys.service.SysDeptService;
import io.renren.modules.sys.service.SysRoleUserService;
import io.renren.modules.sys.service.SysUserPostService;
import io.renren.modules.sys.service.SysUserService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 系统用户
 *
 * @author Mark sunlightcs@gmail.com
 */
@AllArgsConstructor
@Service
public class SysUserServiceImpl extends BaseServiceImpl<SysUserDao, SysUserEntity> implements SysUserService {
    private final SysRoleUserService sysRoleUserService;
    private final SysDeptService sysDeptService;
    private final SysUserPostService sysUserPostService;

    @Override
    public PageData<SysUserDTO> page(Map<String, Object> params) {
        //转换成like
        paramsToLike(params, "username");

        //分页
        IPage<SysUserEntity> page = getPage(params, "t1.create_date", false);

        //查询
        List<SysUserEntity> list = baseDao.getList(getQueryParams(params));

        return getPageData(list, page.getTotal(), SysUserDTO.class);
    }

    @Override
    public List<SysUserDTO> list(Map<String, Object> params) {
        List<SysUserEntity> entityList = baseDao.getList(getQueryParams(params));

        return ConvertUtils.sourceToTarget(entityList, SysUserDTO.class);
    }

    private Map<String, Object> getQueryParams(Map<String, Object> params) {
        String deptId = (String)params.get("deptId");
        List<Long> deptIdList = new ArrayList<>();
        if(StrUtil.isNotBlank(deptId)){
            deptIdList = sysDeptService.getSubDeptIdList(Long.valueOf(deptId));
            params.put("deptIdList", deptIdList);
        }

        //普通管理员，只能查询所属部门及子部门的数据
        UserDetail user = SecurityUser.getUser();
        if (user.getSuperAdmin() == io.renren.common.enums.SuperAdminEnum.NO.value()
                && user.getSuperTenant() == SuperTenantEnum.NO.value()) {
            if(deptIdList.isEmpty()){
                params.put("deptIdList", sysDeptService.getSubDeptIdList(user.getDeptId()));
            }else {
                // 交集
                List<Long> deptIdList2 = sysDeptService.getSubDeptIdList(user.getDeptId());
                params.put("deptIdList", CollUtil.intersectionDistinct(deptIdList, deptIdList2));
            }
        }

        //租户
        params.put(Constant.TENANT_CODE, TenantContext.getTenantCode(user));

        return params;
    }

    @Override
    public SysUserDTO get(Long id) {
        SysUserEntity entity = baseDao.getById(id);

        return ConvertUtils.sourceToTarget(entity, SysUserDTO.class);
    }

    @Override
    public SysUserDTO getByUsername(String username) {
        SysUserEntity entity = baseDao.getByUsername(username, TenantContext.getVisitorTenantCode());
        return ConvertUtils.sourceToTarget(entity, SysUserDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SysUserDTO dto) {
        SysUserEntity entity = ConvertUtils.sourceToTarget(dto, SysUserEntity.class);

        // 校验用户名是否存在
        SysUserDTO dbUser = getByUsername(entity.getUsername());
        if (dbUser != null) {
            throw new RenException("Username already exists, please use a different username");
        }

        //密码加密
        String password = PasswordUtils.encode(entity.getPassword());
        entity.setPassword(password);

        //保存用户
        entity.setSuperTenant(SuperTenantEnum.NO.value());
        entity.setSuperAdmin(SuperAdminEnum.NO.value());
        entity.setTenantCode(TenantContext.getTenantCode(SecurityUser.getUser()));
        insert(entity);

        //保存角色用户关系
        sysRoleUserService.saveOrUpdate(entity.getId(), dto.getRoleIdList());

        //保存用户岗位关系
        sysUserPostService.saveOrUpdate(entity.getId(), dto.getPostIdList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysUserDTO dto) {
        SysUserEntity entity = ConvertUtils.sourceToTarget(dto, SysUserEntity.class);

        //密码加密
        if (StringUtils.isBlank(dto.getPassword())) {
            entity.setPassword(null);
        } else {
            String password = PasswordUtils.encode(entity.getPassword());
            entity.setPassword(password);
        }

        //更新用户
        updateById(entity);

        //更新角色用户关系
        sysRoleUserService.saveOrUpdate(entity.getId(), dto.getRoleIdList());

        //保存用户岗位关系
        sysUserPostService.saveOrUpdate(entity.getId(), dto.getPostIdList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(SysUserDTO dto) {
        SysUserEntity entity = selectById(dto.getId());
        entity.setHeadUrl(dto.getHeadUrl());
        entity.setRealName(dto.getRealName());
        entity.setGender(dto.getGender());
        entity.setMobile(dto.getMobile());
        entity.setEmail(dto.getEmail());

        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long[] ids) {
        //删除用户
        baseDao.deleteBatchIds(Arrays.asList(ids));

        //删除角色用户关系
        sysRoleUserService.deleteByUserIds(ids);

        //删除用户岗位关系
        sysUserPostService.deleteByUserIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long id, String newPassword) {
        newPassword = PasswordUtils.encode(newPassword);

        baseDao.updatePassword(id, newPassword);
    }

    @Override
    public int getCountByDeptId(Long deptId) {
        return baseDao.getCountByDeptId(deptId);
    }

    @Override
    public List<Long> getUserIdListByDeptId(List<Long> deptIdList) {
        return baseDao.getUserIdListByDeptId(deptIdList);
    }

}
