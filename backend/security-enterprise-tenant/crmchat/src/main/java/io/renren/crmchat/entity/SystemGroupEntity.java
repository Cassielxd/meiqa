package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 组合数据表 eb_system_group
 * PHP Reference: app/models/system/config/SystemGroup.php
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_system_group")
public class SystemGroupEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    private Integer cateId;      // 分类ID
    private String name;         // 数据组名称
    private String info;         // 简介
    private String configName;   // 配置名称(唯一标识)
    private String fields;       // 字段信息(JSON格式)
}
