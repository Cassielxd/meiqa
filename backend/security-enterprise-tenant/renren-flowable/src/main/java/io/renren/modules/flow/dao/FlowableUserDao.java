/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.flow.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * 用户
 *
 * @author Mark sunlightcs@gmail.com
 */
@Mapper
public interface FlowableUserDao {

    String getUserName(String userId);

    List<Long> getUserIdListByRoleIdList(List<Long> ids);

    List<Long> getUserIdListByPostIdList(List<Long> ids);

    /**
     * 查询部门领导列表
     *
     * @param ids 部门列表
     */
    List<Long> getLeaderIdListByDeptIdList(List<Long> ids);

    /**
     * 获取用户部门领导ID
     *
     * @param userId 用户ID
     */
    Long getLeaderIdListByUserId(Long userId);


}
