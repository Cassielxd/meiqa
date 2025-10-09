/**
 * Copyright (c) 2021 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */
package io.renren.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户岗位关系
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@TableName("sys_user_post")
public class SysUserPostEntity implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 岗位ID
     */
    private Long postId;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)
    private Long creator;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;
}