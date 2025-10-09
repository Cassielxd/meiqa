package io.renren.crmchat.controller.mobile;

import io.renren.crmchat.common.result.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Mobile Comment Controller - 移动端评论/心跳管理
 * PHP Reference: /app/controller/mobile/Comment.php
 *
 * @author CRMChat Team
 */
@RestController
@RequestMapping("/api/mobile")
@Tag(name = "Mobile Comment - Mobile Comment Heartbeat")
@AllArgsConstructor
public class MobileCommentController {

    /**
     * 心跳检测
     * GET /api/mobile/service/ping
     *
     * PHP Reference: Comment.php::ping()
     *
     * 业务说明:
     * - 简单的心跳检测接口
     * - 用于保持连接活跃或检测服务可用性
     * - 无需参数，直接返回成功
     *
     * Response: { "code": 0, "msg": "pong" }
     */
    @GetMapping("/service/ping")
    @Operation(summary = "Heartbeat Detection")
    public ApiResult<String> ping() {
        return ApiResult.ok("pong", "success");
    }
}
