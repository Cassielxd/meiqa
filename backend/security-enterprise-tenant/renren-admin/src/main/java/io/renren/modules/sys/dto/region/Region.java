/**
 * Copyright (c) 2019 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.dto.region;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 地区管理
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "地区管理")
public class Region implements Serializable {


    @Schema(description = "地区ID")
    private Long id;

    @JsonIgnore
    private Long pid;

    @Schema(description = "名称")
    private String name;
}
