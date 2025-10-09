package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统配置表 system_config
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_system_config")
public class SystemConfigEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    private String menuName;        // 字段名称
    private String type;            // 类型（text/textarea/radio/upload等）
    private String inputType;       // 表单类型
    private Integer configTabId;    // 配置分类ID
    private String parameter;       // 参数（radio/checkbox的选项）
    private String uploadType;      // 上传类型
    private String required;        // 是否必填
    private Integer width;          // 宽度
    private Integer high;           // 高度
    private String value;           // 配置值（JSON格式）
    private String info;            // 配置名称
    @TableField("`desc`")          // Escape reserved keyword
    private String desc;            // 配置简介
    private Integer sort;           // 排序
    private Integer status;         // 状态（0隐藏1显示）
}
