package io.renren.modules.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


/**
 * 产品管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "产品管理")
public class ProductDTO implements Serializable {
    @Schema(description = "id")
    private Long id;

    @Schema(description = "产品名称")
    private String name;

    @Schema(description = "产品介绍")
    private String content;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

    @Schema(description = "产品参数列表")
    private List<ProductParamsDTO> subList;

}