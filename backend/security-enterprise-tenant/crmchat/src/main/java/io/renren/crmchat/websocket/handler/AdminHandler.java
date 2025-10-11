package io.renren.crmchat.websocket.handler;

import io.renren.common.utils.SpringContextUtils;
import io.renren.crmchat.common.constant.TenantConstants;
import io.renren.crmchat.service.common.TokenService;
import io.renren.crmchat.websocket.BaseHandler;
import io.renren.crmchat.websocket.ChatWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class AdminHandler implements BaseHandler {

    @Override
    public ChatWebSocketServer.SessionIdentity authenticate(Map<String, String> params) {
        String token = params.get("token");
        String appid = params.getOrDefault("appid", params.getOrDefault("app", ""));
        int formType = parseInt(params.get("form_type"), 0);
        Integer initialTarget = parseIntNullable(params.get("to_user_id"));

        // 验证token
        TokenService tokenService = SpringContextUtils.getBean(TokenService.class);
        if (tokenService == null || !tokenService.validateToken(token)) {
            log.warn("WebSocket admin handshake rejected: invalid token. params={}", params);
            return null;
        }

        Long tokenUserId = tokenService.extractUserId(token);
        String tokenAppid = tokenService.extractAppid(token);

        if (tokenUserId == null) {
            log.warn("WebSocket admin handshake rejected: invalid token user id. params={}", params);
            return null;
        }

        String resolvedAppid = (tokenAppid != null && !tokenAppid.isBlank())
                ? tokenAppid
                : TenantConstants.SUPER_APPID;

        if (!appid.isBlank() && !appid.equals(resolvedAppid)) {
            log.warn("WebSocket admin handshake rejected: appid mismatch. expected={}, actual={}", resolvedAppid, appid);
            return null;
        }

        appid = resolvedAppid;

        return new ChatWebSocketServer.SessionIdentity(
                appid,
                tokenUserId.intValue(),
                "admin",
                formType,
                false,
                initialTarget,
                null,
                1
        );
    }

    private static int parseInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static Integer parseIntNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
