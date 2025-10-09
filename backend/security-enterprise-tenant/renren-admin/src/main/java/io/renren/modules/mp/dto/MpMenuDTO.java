package io.renren.modules.mp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 公众号自定义菜单
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "公众号自定义菜单")
public class MpMenuDTO implements Serializable {
    @Schema(description = "id")
    private Long id;
    @Schema(description = "菜单json数据")
    private String menu;
    @Schema(description = "AppID")
    private String appId;
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

}