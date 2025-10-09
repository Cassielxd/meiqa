package io.renren.modules.demo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.util.Date;

/**
 * Excel导入演示
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ExcelDataExcel {
    @ExcelProperty(value = "学生姓名", index = 0)
    private String realName;
    @ExcelProperty(value = "身份证", index = 1)
    private String userIdentity;
    @ExcelProperty(value = "家庭地址", index = 2)
    private String address;
    @ExcelProperty(value = "入学日期", index = 3)
    @DateTimeFormat("yyyy-MM-dd")
    private Date joinDate;
    @ExcelProperty(value = "班级名称", index = 4)
    private String className;
}