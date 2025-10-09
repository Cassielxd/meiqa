package io.renren.modules.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Excel导入演示
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "Excel导入演示")
public class ExcelDataDTO implements Serializable {
    @Schema(description = "id")
    private Long id;
    @Schema(description = "学生姓名")
    private String realName;
    @Schema(description = "身份证")
    private String userIdentity;
    @Schema(description = "家庭地址")
    private String address;
    @Schema(description = "入学日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date joinDate;
    @Schema(description = "班级名称")
    private String className;
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

}