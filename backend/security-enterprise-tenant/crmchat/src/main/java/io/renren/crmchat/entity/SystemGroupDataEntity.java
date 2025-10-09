package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 组合数据详情表
 * PHP Reference: eb_system_group_data
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_system_group_data")
public class SystemGroupDataEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 组合数据详情ID
     */
    @TableId
    private Integer id;

    /**
     * 对应的数据组ID (关联eb_system_group表)
     */
    private Integer gid;

    /**
     * 数据内容 (JSON格式)
     */
    private String value;

    /**
     * 添加时间 (Unix timestamp)
     */
    private Integer addTime;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态(0:禁用 1:启用)
     */
    private Integer status;
}
