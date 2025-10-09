/**
 * Copyright (c) 2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.tenant.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.renren.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 租户管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_tenant")
public class SysTenantEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 租户数据源
     */
    private Long datasourceId;
    /**
     * 租户名称
     */
    private String tenantName;
    /**
     * 域名
     */
    private String tenantDomain;
    /**
     * 状态  0：停用    1：正常
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 登录账号ID
     */
    private Long userId;
    /**
     * 登录账号
     */
    private String username;
    /**
     * 租户模式  0：字段模式   1：数据源模式
     */
    private Integer tenantMode;
    /**
     * 删除标识 0：未删除    1：删除
     */
    private Integer delFlag;
    /**
     * 更新者
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;
    /**
     * 真实姓名
     */
    @TableField(exist = false)
    private String realName;
}