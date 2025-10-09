package io.renren.crmchat.websocket;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.common.utils.SpringContextUtils;
import io.renren.crmchat.dao.ChatServiceMapper;
import io.renren.crmchat.dao.ChatServiceRecordMapper;
import io.renren.crmchat.dao.ChatUserMapper;
import io.renren.crmchat.entity.ChatServiceEntity;
import io.renren.crmchat.entity.ChatServiceRecordEntity;
import io.renren.crmchat.entity.ChatUserEntity;
import io.renren.crmchat.common.constant.TenantConstants;
import io.renren.crmchat.service.common.TokenService;
import io.renren.crmchat.service.ChatCacheService;
import io.renren.crmchat.security.CrmChatUser;
import io.renren.crmchat.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Minimal WebSocket gateway that emulates the legacy PHP Swoole behaviour.
 */
@Slf4j
@Component
@ServerEndpoint("/ws")
public class ChatWebSocketServer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session) {
        try {
            Map<String, String> params = parseQuery(session.getRequestURI());
            SessionIdentity identity = authenticate(params);
            if (identity == null) {
                closeSilently(session);
                return;
            }

            WebSocketSessionRegistry.SessionHolder holder = WebSocketSessionRegistry.register(
                    session,
                    identity.appid,
                    identity.userId,
                    identity.userType,
                    identity.formType,
                    identity.tourist,
                    identity.currentTargetUserId,
                    identity.serviceId
            );

            runWithUserContext(identity.appid, identity.userId, identity.userType,
                    () -> markUserOnline(holder, true));
            sendLoginSuccess(session, identity);
            sendEnvelope(session, "ping", Map.of("now", System.currentTimeMillis() / 1000));
        } catch (Exception ex) {
            log.error("WebSocket handshake failure", ex);
            closeSilently(session);
        }
    }

    @OnClose
    public void onClose(Session session) {
        WebSocketSessionRegistry.SessionHolder holder = WebSocketSessionRegistry.remove(session);
        if (holder != null) {
            runWithUserContext(holder.getAppid(), holder.getUserId(), holder.getUserType(),
                    () -> markUserOnline(holder, false));
            log.info("WebSocket connection closed: appid={}, userId={}, type={}",
                    holder.getAppid(), holder.getUserId(), holder.getUserType());
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("WebSocket error: sessionId={}", session != null ? session.getId() : "n/a", throwable);
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            JsonNode node = OBJECT_MAPPER.readTree(message);
            String type = node.path("type").asText("");
            if (type.isEmpty()) {
                return;
            }

            switch (type) {
                case "ping" -> {
                    sendEnvelope(session, "pong", Map.of(
                            "timestamp", System.currentTimeMillis() / 1000));
                    WebSocketSessionRegistry.touch(session);
                }
                case "to_chat" -> {
                    Integer target = parseIntNullable(node.path("id").asText(null));
                    WebSocketSessionRegistry.updateCurrentTarget(session, target);
                }
                case "open" -> {
                    Integer openTarget = parseIntNullable(node.path("open").asText(null));
                    WebSocketSessionRegistry.updateCurrentTarget(session, openTarget);
                }
                case "set_form_type" -> {
                    Integer newFormType = parseIntNullable(node.path("form_type").asText(null));
                    WebSocketSessionRegistry.updateFormType(session, newFormType);
                }
                default -> log.debug("WebSocket ignore type={} payload={}", type, message);
            }
        } catch (Exception ex) {
            log.error("Failed to process WebSocket message: {}", message, ex);
        }
    }

    static void broadcast(String appid, int userId, String type, Object payload) {
        Set<WebSocketSessionRegistry.SessionHolder> holders = WebSocketSessionRegistry.getSessions(appid, userId);
        if (holders.isEmpty()) {
            return;
        }
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("type", type);
        envelope.put("data", payload);
        String message;
        try {
            message = OBJECT_MAPPER.writeValueAsString(envelope);
        } catch (Exception ex) {
            log.error("Failed to encode WebSocket payload", ex);
            return;
        }

        for (WebSocketSessionRegistry.SessionHolder holder : holders) {
            send(holder.getSession(), message);
        }
    }

    private static void send(Session session, String message) {
        try {
            synchronized (session) {
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException ex) {
            log.warn("WebSocket send failure: {}", ex.getMessage());
            WebSocketSessionRegistry.remove(session);
            closeSilently(session);
        }
    }

    private static void sendEnvelope(Session session, String type, Object data) {
        sendEnvelope(session, type, data, null);
    }

    private static void sendEnvelope(Session session, String type, Object data, Integer status) {
        try {
            Map<String, Object> envelope = new HashMap<>();
            envelope.put("type", type);
            if (status != null) {
                envelope.put("status", status);
            }
            if (data != null) {
                envelope.put("data", data);
            }
            send(session, OBJECT_MAPPER.writeValueAsString(envelope));
        } catch (Exception ex) {
            log.warn("WebSocket send envelope failure", ex);
        }
    }

    private SessionIdentity authenticate(Map<String, String> params) {
        String userType = params.getOrDefault("type", "").toLowerCase(Locale.ROOT);
        if (userType.isEmpty()) {
            log.warn("WebSocket handshake rejected: missing user type. params={}", params);
            return null;
        }

        // DEBUG: 打印token参数
        String token = params.get("token");
        log.info("========== WebSocket authenticate 入口 ==========");
        log.info("userType={}, token参数={}, token为null={}, token为空={}",
                userType,
                token,
                token == null,
                token == null ? "N/A" : token.isBlank());
        log.info("完整params={}", params);
        log.info("================================================");

        TokenService tokenService = SpringContextUtils.getBean(TokenService.class);
        ChatCacheService cacheService = SpringContextUtils.getBean(ChatCacheService.class);
        boolean tokenProvided = token != null && !token.isBlank();
        boolean tokenValid = tokenProvided && tokenService != null && tokenService.validateToken(token);

        Long tokenUserId = null;
        String tokenAppid = null;
        if (tokenValid) {
            tokenUserId = tokenService.extractUserId(token);
            tokenAppid = tokenService.extractAppid(token);
        }

        String appid = params.getOrDefault("appid", params.getOrDefault("app", ""));
        if (tokenValid && tokenAppid != null && !tokenAppid.isBlank()) {
            appid = tokenAppid;
        }

        int formType = parseInt(params.get("form_type"), 0);
        Integer initialTarget = parseIntNullable(params.get("to_user_id"));

        switch (userType) {
            case "kefu" -> {
                if (!tokenValid || tokenUserId == null || tokenUserId <= 0) {
                    log.warn("WebSocket kefu handshake rejected: invalid token. params={}", params);
                    return null;
                }
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

                if (cacheService != null) {
                    cacheService.cacheServiceProfile(service);
                }

                int online = service.getOnline() != null ? service.getOnline() : 0;
                return new SessionIdentity(appid, chatUserId, userType, formType, false, initialTarget, service.getId(), online);
            }
            case "user" -> {
                if (!tokenValid || tokenUserId == null || tokenUserId <= 0) {
                    log.warn("WebSocket user handshake rejected: invalid token. params={}", params);
                    return null;
                }
                if (appid == null || appid.isBlank()) {
                    log.warn("WebSocket user handshake rejected: token missing appid. params={}", params);
                    return null;
                }

                ChatUserMapper userMapper = SpringContextUtils.getBean(ChatUserMapper.class);
                if (userMapper == null) {
                    log.error("ChatUserMapper bean not available");
                    return null;
                }
                ChatUserEntity chatUser = userMapper.selectOne(new QueryWrapper<ChatUserEntity>()
                        .eq("id", tokenUserId.intValue())
                        .eq("appid", appid));
                if (chatUser == null) {
                    log.warn("WebSocket user handshake rejected: user not found. uid={}", tokenUserId);
                    return null;
                }

                String userAppid = Optional.ofNullable(chatUser.getAppid()).orElse("");
                if (!userAppid.isEmpty() && !appid.equals(userAppid)) {
                    log.warn("WebSocket user handshake rejected: appid mismatch. expected={}, actual={}", userAppid, appid);
                    return null;
                }

                if (cacheService != null) {
                    cacheService.cacheUser(chatUser);
                }

                boolean tourist = chatUser.getIsTourist() != null && chatUser.getIsTourist() == 1;
                int inferredFormType = formType != 0 ? formType : (chatUser.getType() != null ? chatUser.getType() : 0);
                int online = chatUser.getOnline() != null ? chatUser.getOnline() : 0;
                return new SessionIdentity(appid, chatUser.getId(), userType, inferredFormType, tourist, initialTarget, null, online);
            }
            case "admin" -> {
                if (!tokenValid || tokenUserId == null) {
                    log.warn("WebSocket admin handshake rejected: invalid token. params={}", params);
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
                return new SessionIdentity(appid, tokenUserId.intValue(), userType, formType, false, initialTarget, null, 1);
            }
            default -> {
                log.warn("WebSocket handshake rejected: unsupported type='{}'. params={}", userType, params);
                return null;
            }
        }
    }

    private void sendLoginSuccess(Session session, SessionIdentity identity) {
        Map<String, Object> data = new HashMap<>();
        data.put("appid", identity.appid);
        data.put("uid", identity.userId);
        data.put("type", identity.userType);
        data.put("tourist", identity.tourist ? 1 : 0);
        data.put("form_type", identity.formType);
        if (identity.serviceId != null) {
            data.put("service_id", identity.serviceId);
        }
        data.put("online", identity.onlineFlag);
        sendEnvelope(session, "login", data, 200);
    }

    private static void markUserOnline(WebSocketSessionRegistry.SessionHolder holder, boolean online) {
        String userType = holder.getUserType();
        String appid = holder.getAppid();
        int userId = holder.getUserId();
        int flag = online ? 1 : 0;

        ChatUserMapper chatUserMapper = SpringContextUtils.getBean(ChatUserMapper.class);
        ChatServiceMapper chatServiceMapper = SpringContextUtils.getBean(ChatServiceMapper.class);
        ChatServiceRecordMapper chatServiceRecordMapper = SpringContextUtils.getBean(ChatServiceRecordMapper.class);
        ChatCacheService cacheService = SpringContextUtils.getBean(ChatCacheService.class);

        if (chatUserMapper != null) {
            UpdateWrapper<ChatUserEntity> wrapper = new UpdateWrapper<>();
            wrapper.eq("appid", appid).eq("id", userId);
            ChatUserEntity update = new ChatUserEntity();
            update.setOnline(flag);
            update.setUpdateTime(LocalDateTime.now());
            chatUserMapper.update(update, wrapper);
        }

        if ("kefu".equals(userType) && chatServiceMapper != null) {
            UpdateWrapper<ChatServiceEntity> wrapper = new UpdateWrapper<>();
            if (holder.getServiceId() != null && holder.getServiceId() > 0) {
                wrapper.eq("id", holder.getServiceId()).eq("appid", appid);
            } else {
                wrapper.eq("appid", appid).eq("user_id", userId);
            }
            ChatServiceEntity update = new ChatServiceEntity();
            update.setOnline(flag);
            update.setIsBackstage(flag);
            update.setUpdateTime((int) (System.currentTimeMillis() / 1000));
            chatServiceMapper.update(update, wrapper);
        }

        if (chatServiceRecordMapper != null) {
            int now = (int) (System.currentTimeMillis() / 1000);
            UpdateWrapper<ChatServiceRecordEntity> userRecordWrapper = new UpdateWrapper<>();
            userRecordWrapper.eq("appid", appid).eq("user_id", userId);
            ChatServiceRecordEntity recordUpdate = new ChatServiceRecordEntity();
            recordUpdate.setOnline(flag);
            recordUpdate.setUpdateTime(now);
            chatServiceRecordMapper.update(recordUpdate, userRecordWrapper);

            UpdateWrapper<ChatServiceRecordEntity> toUserWrapper = new UpdateWrapper<>();
            toUserWrapper.eq("appid", appid).eq("to_user_id", userId);
            chatServiceRecordMapper.update(recordUpdate, toUserWrapper);
        }

        if (cacheService != null) {
            cacheService.invalidateUserProfile(appid, userId);
            cacheService.invalidateOnlineServices(appid);
        }
    }

    private static void runWithUserContext(String appid, int userId, String userType, Runnable action) {
        CrmChatUser user = new CrmChatUser();
        user.setAppid(appid);
        user.setUserId((long) userId);
        user.setUsername(userType + ":" + userId);
        user.setUserType(userType);
        UserContext.setUser(user);
        try {
            action.run();
        } finally {
            UserContext.clear();
        }
    }

    private static Map<String, String> parseQuery(URI uri) {
        if (uri == null || uri.getQuery() == null || uri.getQuery().isBlank()) {
            return Collections.emptyMap();
        }
        Map<String, String> params = new HashMap<>();
        String[] pairs = uri.getQuery().split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx >= 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                params.put(key, value);
            } else {
                params.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
            }
        }
        return params;
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

    private static void closeSilently(Session session) {
        try {
            session.close();
        } catch (Exception ignored) {
        }
    }

    private static final class SessionIdentity {
        private final String appid;
        private final int userId;
        private final String userType;
        private final int formType;
        private final boolean tourist;
        private final Integer currentTargetUserId;
        private final Integer serviceId;
        private final int onlineFlag;

        private SessionIdentity(String appid,
                                int userId,
                                String userType,
                                int formType,
                                boolean tourist,
                                Integer currentTargetUserId,
                                Integer serviceId,
                                int onlineFlag) {
            this.appid = appid;
            this.userId = userId;
            this.userType = userType;
            this.formType = formType;
            this.tourist = tourist;
            this.currentTargetUserId = currentTargetUserId;
            this.serviceId = serviceId;
            this.onlineFlag = onlineFlag;
        }
    }
}
