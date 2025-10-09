package io.renren.modules.demo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * Excel导入演示
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@TableName("tb_excel_data")
public class ExcelDataEntity {
    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 学生姓名
     */
    private String realName;
    /**
     * 身份证
     */
    private String userIdentity;
    /**
     * 家庭地址
     */
    private String address;
    /**
     * 入学日期
     */
    private Date joinDate;
    /**
     * 班级名称
     */
    private String className;
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