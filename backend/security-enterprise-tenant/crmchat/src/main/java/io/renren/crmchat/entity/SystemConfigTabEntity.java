package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统配置分类表 system_config_tab
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_system_config_tab")
public class SystemConfigTabEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    private String title;       // 分类名称
    private String engTitle;    // 英文名称
    private Integer status;     // 状态（0隐藏1显示）
    private Integer type;       // 类型
    private Integer sort;       // 排序
    private Integer pid;        // 父级ID
    private String icon;        // 图标
}
