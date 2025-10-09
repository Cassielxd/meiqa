package io.renren.modules.kefu.vo.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 管理员登录响应VO
 */
@Data
@Schema(description = "管理员登录响应")
public class AdminLoginVO {

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "Token过期时间(时间戳)")
    private Long expiresTime;

    @Schema(description = "菜单列表")
    private List<?> menus;

    @Schema(description = "权限标识列表")
    private List<String> uniqueAuth;

    @Schema(description = "用户信息")
    private UserInfo userInfo;

    @Schema(description = "Logo")
    private String logo;

    @Schema(description = "方形Logo")
    private String logoSquare;

    @Schema(description = "系统版本")
    private String version;

    @Schema(description = "新订单提示音链接")
    private String newOrderAudioLink;

    @Data
    @Schema(description = "用户信息")
    public static class UserInfo {
        @Schema(description = "用户ID")
        private Long id;

        @Schema(description = "账号")
        private String account;

        @Schema(description = "头像")
        private String headPic;
    }
}
