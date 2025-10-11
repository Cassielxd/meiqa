package io.renren.crmchat.websocket.handler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.utils.SpringContextUtils;
import io.renren.crmchat.dao.ChatServiceMapper;
import io.renren.crmchat.entity.ChatServiceEntity;
import io.renren.crmchat.service.ChatCacheService;
import io.renren.crmchat.service.common.TokenService;
import io.renren.crmchat.websocket.BaseHandler;
import io.renren.crmchat.websocket.ChatWebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class KefuHandler implements BaseHandler {

    @Override
    public ChatWebSocketServer.SessionIdentity authenticate(Map<String, String> params) {
        String token = params.get("token");
        String appid = params.getOrDefault("appid", params.getOrDefault("app", ""));
        int formType = parseInt(params.get("form_type"), 0);
        Integer initialTarget = parseIntNullable(params.get("to_user_id"));

        // 验证token
        TokenService tokenService = SpringContextUtils.getBean(TokenService.class);
        if (tokenService == null || !tokenService.validateToken(token)) {
            log.warn("WebSocket kefu handshake rejected: invalid token. params={}", params);
            return null;
        }

        Long tokenUserId = tokenService.extractUserId(token);
        String tokenAppid = tokenService.extractAppid(token);

        if (tokenUserId == null || tokenUserId <= 0) {
            log.warn("WebSocket kefu handshake rejected: invalid token user id. params={}", params);
            return null;
        }

        if (tokenAppid != null && !tokenAppid.isBlank()) {
            appid = tokenAppid;
        }

        // 查找客服信息
        ChatServiceMapper serviceMapper = SpringContextUtils.getBean(ChatServiceMapper.class);
        if (serviceMapper == null) {
            log.error("ChatServiceMapper bean not available");
            return null;
        }

        ChatServiceEntity service = serviceMapper.selectOne(new QueryWrapper<ChatServiceEntity>()
                .eq("id", tokenUserId.intValue())
                .eq("appid", appid));

        if (service == null) {
            log.warn("WebSocket kefu handshake rejected: service not found. serviceId={}", tokenUserId);
            return null;
        }

        String serviceAppid = Optional.ofNullable(service.getAppid()).orElse("");
        if (appid.isEmpty()) {
            appid = serviceAppid;
        } else if (!Objects.equals(appid, serviceAppid)) {
            log.warn("WebSocket kefu handshake rejected: appid mismatch. expected={}, actual={}", serviceAppid, appid);
            return null;
        }

        Integer chatUserId = service.getUserId();
        if (chatUserId == null || chatUserId <= 0) {
            log.warn("WebSocket kefu handshake rejected: service missing user_id. serviceId={}", service.getId());
            return null;
        }

        // 缓存客服信息
        ChatCacheService cacheService = SpringContextUtils.getBean(ChatCacheService.class);
        if (cacheService != null) {
            cacheService.cacheServiceProfile(service);
        }

        int online = service.getOnline() != null ? service.getOnline() : 0;
        return new ChatWebSocketServer.SessionIdentity(
                appid,
                chatUserId,
                "kefu",
                formType,
                false,
                initialTarget,
                service.getId(),
                online
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
