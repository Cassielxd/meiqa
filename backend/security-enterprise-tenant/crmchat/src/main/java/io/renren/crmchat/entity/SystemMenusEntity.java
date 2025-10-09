package io.renren.crmchat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统菜单表 system_menus
 *
 * @author CRMChat Team
 */
@Data
@TableName("eb_system_menus")
public class SystemMenusEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    private String menuName;      // 菜单名称
    private String controller;    // 控制器
    private String module;        // 模块（默认admin）
    private String action;        // 方法
    private String icon;          // 图标
    private String params;        // 参数（JSON）
    private String path;          // 路径
    private String menuPath;      // 菜单路径
    private String apiUrl;        // API地址
    private String methods;       // 请求方法
    private String uniqueAuth;    // 唯一权限标识
    private String header;        // 头部
    private Integer isHeader;     // 是否是头部（0否1是）
    private Integer pid;          // 父级ID
    private Integer sort;         // 排序
    private Integer authType;     // 权限类型
    private Integer access;       // 是否开启（0关闭1开启）
    private Integer isShow;       // 是否显示（0隐藏1显示）
    private Integer isShowPath;   // 是否显示路径（0否1是）
    private Integer isDel;        // 是否删除（0未删除1已删除）
}
