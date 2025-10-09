/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import io.renren.modules.sys.excel.converter.GenderConverter;
import io.renren.modules.sys.excel.converter.StatusConverter;
import lombok.Data;

import java.util.Date;

/**
 * 用户管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class SysUserExcel {
    @ColumnWidth(20)
    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("姓名")
    private String realName;

    @ColumnWidth(20)
    @ExcelProperty(value = "性别", converter = GenderConverter.class)
    private Integer gender;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("手机号")
    private String mobile;

    @ExcelProperty("部门名称")
    private String deptName;

    @ColumnWidth(20)
    @ExcelProperty(value = "状态", converter = StatusConverter.class)
    private Integer status;

    @ExcelProperty("备注")
    private String remark;

    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("创建时间")
    private Date createDate;


}
