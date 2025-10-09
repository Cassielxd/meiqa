/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.tenant.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.common.enums.SuperAdminEnum;
import io.renren.common.enums.SuperTenantEnum;
import io.renren.common.enums.TenantModeEnum;
import io.renren.common.page.PageData;
import io.renren.common.service.impl.BaseServiceImpl;
import io.renren.common.utils.ConvertUtils;
import io.renren.commons.dynamic.datasource.config.DynamicContextHolder;
import io.renren.modules.security.password.PasswordUtils;
import io.renren.modules.sys.dao.SysUserDao;
import io.renren.modules.sys.entity.SysUserEntity;
import io.renren.modules.sys.enums.DeleteEnum;
import io.renren.modules.sys.enums.UserStatusEnum;
import io.renren.modules.sys.service.SysRoleUserService;
import io.renren.modules.tenant.cache.TenantCache;
import io.renren.modules.tenant.dao.SysTenantDao;
import io.renren.modules.tenant.dto.SysTenantDTO;
import io.renren.modules.tenant.dto.SysTenantListDTO;
import io.renren.modules.tenant.entity.SysTenantEntity;
import io.renren.modules.tenant.service.SysTenantService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 租户管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@AllArgsConstructor
@Service
public class SysTenantServiceImpl extends BaseServiceImpl<SysTenantDao, SysTenantEntity> implements SysTenantService {
    private final SysUserDao sysUserDao;
    private final TenantCache tenantCache;
    private final SysRoleUserService sysRoleUserService;

    @Override
    public PageData<SysTenantDTO> page(Map<String, Object> params) {
        //转换成like
        paramsToLike(params, "tenantName");

        //分页
        IPage<SysTenantEntity> page = getPage(params, "create_date", false);

        //查询
        List<SysTenantEntity> list = baseDao.getList(params);

        return getPageData(list, page.getTotal(), SysTenantDTO.class);
    }

    @Override
    public synchronized List<SysTenantListDTO> list() {
        // 判断缓存里面是否存在
        List<SysTenantListDTO> dtoList = tenantCache.getCache();
        if (dtoList != null && !dtoList.isEmpty()) {
            return dtoList;
        }

        dtoList = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("status", 1);
        List<SysTenantEntity> list = baseDao.getList(params);
        for (SysTenantEntity entity : list) {
            SysTenantListDTO dto = new SysTenantListDTO();
            dto.setTenantCode(entity.getId());
            dto.setTenantName(entity.getTenantName());
            dto.setTenantDomain(entity.getTenantDomain());
            dto.setTenantMode(entity.getTenantMode());
            dto.setDatasourceId(entity.getDatasourceId());
            dto.setStatus(entity.getStatus());

            dtoList.add(dto);
        }

        tenantCache.setCache(dtoList);
        return dtoList;
    }


    @Override
    public SysTenantDTO get(Long id) {
        SysTenantEntity entity = baseDao.getById(id);

        return ConvertUtils.sourceToTarget(entity, SysTenantDTO.class);
    }

    @Override
    public void save(SysTenantDTO dto) {
        SysTenantEntity entity = ConvertUtils.sourceToTarget(dto, SysTenantEntity.class);
        // 保存租户
        entity.setDelFlag(DeleteEnum.NO.value());
        insert(entity);

        SysUserEntity user = new SysUserEntity();
        user.setSuperAdmin(SuperAdminEnum.NO.value());

        // 切换数据源
        if (dto.getTenantMode() == TenantModeEnum.DATASOURCE.value()) {
            DynamicContextHolder.push(entity.getDatasourceId() + "");

            user.setSuperAdmin(SuperAdminEnum.YES.value());
        }

        // 保存用户
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getUsername());
        user.setPassword(PasswordUtils.encode(dto.getPassword()));
        user.setSuperTenant(SuperTenantEnum.YES.value());
        user.setStatus(UserStatusEnum.ENABLED.value());
        user.setTenantCode(entity.getId());
        user.setGender(2);

        sysUserDao.insert(user);

        // 清除数据源
        if (dto.getTenantMode() == TenantModeEnum.DATASOURCE.value()) {
            DynamicContextHolder.poll();
        }

        // 字段模式
        if (dto.getTenantMode() == TenantModeEnum.COLUMN.value()) {
            // 保存角色用户关系
            sysRoleUserService.saveOrUpdate(user.getId(), dto.getRoleIdList());
        }

        // 更新用户ID
        entity.setUserId(user.getId());
        baseDao.updateById(entity);

        // 清空缓存
        tenantCache.clear();
    }

    @Override
    public void update(SysTenantDTO dto) {
        //更新租户
        SysTenantEntity entity = ConvertUtils.sourceToTarget(dto, SysTenantEntity.class);
        updateById(entity);

        // 查询租户用户ID
        Long userId = baseDao.selectById(entity.getId()).getUserId();

        // 修改租户用户密码
        if (StringUtils.isNotBlank(dto.getPassword())) {
            // 切换数据源
            if (dto.getTenantMode() == TenantModeEnum.DATASOURCE.value()) {
                DynamicContextHolder.push(entity.getDatasourceId() + "");
            }

            // 修改密码
            sysUserDao.updatePassword(userId, PasswordUtils.encode(dto.getPassword()));

            // 清除数据源
            if (dto.getTenantMode() == TenantModeEnum.DATASOURCE.value()) {
                DynamicContextHolder.poll();
            }

        }

        // 字段模式
        if (dto.getTenantMode() == TenantModeEnum.COLUMN.value()) {
            // 保存角色用户关系
            sysRoleUserService.saveOrUpdate(userId, dto.getRoleIdList());
        }

        // 清空缓存
        tenantCache.clear();
    }

    @Override
    public void delete(Long[] ids) {
        baseDao.deleteBatch(ids);

        // 清空缓存
        tenantCache.clear();
    }
}
