package io.renren.crmchat.websocket;

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
import io.renren.crmchat.service.ChatCacheService;
import io.renren.crmchat.security.CrmChatUser;
import io.renren.crmchat.security.UserContext;
import io.renren.crmchat.websocket.handler.AuthHandlerFactory;
import io.renren.crmchat.websocket.BaseHandler;
import io.renren.crmchat.websocket.handler.UserHandler;
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

            // 对应PHP UserHandler::login()的业务逻辑
            if ("user".equals(identity.userType)) {
                handleUserLogin(identity);
            }

            sendLoginSuccess(session, identity);

            // 对应PHP Manager::onOpen()第109行: 发送success消息触发前端user消息发送
            Map<String, Object> successData = new HashMap<>();
            successData.put("appid", identity.appid);
            if (identity.onlineFlag > 0) {
                successData.put("uid", identity.userId);
            }
            sendEnvelope(session, "success", successData);

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

            JsonNode dataNode = node.path("data");

            switch (type) {
                case "ping" -> {
                    sendEnvelope(session, "pong", Map.of(
                            "timestamp", System.currentTimeMillis() / 1000));
                    WebSocketSessionRegistry.touch(session);
                }
                case "user" -> {
                    // 处理用户信息同步消息，委托给UserHandler
                    UserHandler userHandler =
                            SpringContextUtils.getBean(UserHandler.class);
                    if (userHandler != null) {
                        userHandler.handleUserMessage(session, dataNode);
                    } else {
                        log.error("UserHandler bean not available");
                    }
                }
                case "to_chat" -> {
                    Integer target = parseIntNullable(dataNode.path("id").asText(null));
                    WebSocketSessionRegistry.updateCurrentTarget(session, target);
                }
                case "open" -> {
                    Integer openTarget = parseIntNullable(dataNode.path("open").asText(null));
                    WebSocketSessionRegistry.updateCurrentTarget(session, openTarget);
                }
                case "set_form_type" -> {
                    Integer newFormType = parseIntNullable(dataNode.path("form_type").asText(null));
                    WebSocketSessionRegistry.updateFormType(session, newFormType);
                }
                default -> log.debug("WebSocket ignore type={} payload={}", type, message);
            }
        } catch (Exception ex) {
            log.error("Failed to process WebSocket message: {}", message, ex);
        }
    }

    static void broadcast(String appid, int userId, String type, Object payload) {
        log.info("=== ChatWebSocketServer.broadcast === appid={}, userId={}, type={}", appid, userId, type);
        Set<WebSocketSessionRegistry.SessionHolder> holders = WebSocketSessionRegistry.getSessions(appid, userId);
        log.info("Found {} WebSocket sessions for user {}", holders.size(), userId);
        if (holders.isEmpty()) {
            log.warn("No WebSocket sessions found for appid={}, userId={}, message will NOT be pushed", appid, userId);
            return;
        }
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("type", type);
        envelope.put("data", payload);
        String message;
        try {
            message = OBJECT_MAPPER.writeValueAsString(envelope);
            log.info("Encoded WebSocket message: {}", message.substring(0, Math.min(200, message.length())));
        } catch (Exception ex) {
            log.error("Failed to encode WebSocket payload", ex);
            return;
        }

        for (WebSocketSessionRegistry.SessionHolder holder : holders) {
            log.info("Sending message to session: sessionId={}", holder.getSession().getId());
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

    public static void sendEnvelope(Session session, String type, Object data) {
        sendEnvelope(session, type, data, null);
    }

    static void sendEnvelope(Session session, String type, Object data, Integer status) {
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

        // 使用工厂模式获取对应的认证处理器
        AuthHandlerFactory factory = SpringContextUtils.getBean(AuthHandlerFactory.class);
        if (factory == null) {
            log.error("AuthHandlerFactory bean not available");
            return null;
        }

        BaseHandler handler = factory.getHandler(userType);
        if (handler == null) {
            log.warn("WebSocket handshake rejected: unsupported or invalid user type='{}'. params={}", userType, params);
            return null;
        }

        // 委托给具体的处理器进行认证
        return handler.authenticate(params);
    }

    private void sendLoginSuccess(Session session, SessionIdentity identity) {
        Map<String, Object> data = new HashMap<>();
        data.put("appid", identity.appid);
        // 对应PHP: 只有当token中有用户信息时才返回uid字段
        // 前端通过检查uid字段是否存在来判断是否需要发送user消息
        if (identity.onlineFlag > 0) {
            data.put("uid", identity.userId);
        }
        data.put("type", identity.userType);
        data.put("tourist", identity.tourist ? 1 : 0);
        data.put("form_type", identity.formType);
        if (identity.serviceId != null) {
            data.put("service_id", identity.serviceId);
        }
        data.put("online", identity.onlineFlag);
        sendEnvelope(session, "login", data, 200);
    }

    /**
     * 处理用户登录业务逻辑
     * 对应PHP: UserHandler::login() 第54-63行
     *
     * 功能：
     * 1. 更新chat_service_record的在线状态
     * 2. 广播用户上线消息给所有客服（仅限已存在用户）
     *
     * 注意：新用户的广播在handleUserMessage()中处理
     */
    private static void handleUserLogin(SessionIdentity identity) {
        try {
            ChatServiceRecordMapper recordMapper = SpringContextUtils.getBean(ChatServiceRecordMapper.class);
            ChatUserMapper userMapper = SpringContextUtils.getBean(ChatUserMapper.class);

            // 1. 更新chat_service_record的在线状态 (对应PHP第58行)
            if (recordMapper != null) {
                int now = (int) (System.currentTimeMillis() / 1000);
                UpdateWrapper<ChatServiceRecordEntity> wrapper = new UpdateWrapper<>();
                wrapper.eq("appid", identity.appid)
                        .eq("to_user_id", identity.userId);
                ChatServiceRecordEntity update = new ChatServiceRecordEntity();
                update.setOnline(1);
                update.setType(identity.formType);
                update.setUpdateTime(now);
                recordMapper.update(update, wrapper);
                log.info("Updated service record online status for user login: userId={}", identity.userId);
            }

            // 2. 广播用户上线消息给所有客服 (对应PHP第60-63行)
            // 只有当用户已存在时才广播（onlineFlag > 0表示是已存在用户）
            if (identity.onlineFlag > 0 && userMapper != null) {
                ChatUserEntity chatUser = userMapper.selectById(identity.userId);
                if (chatUser != null) {
                    // 获取所有客服的session
                    Set<WebSocketSessionRegistry.SessionHolder> kefuSessions =
                            WebSocketSessionRegistry.getSessionsByType(identity.appid, "kefu");

                    Map<String, Object> onlineData = new HashMap<>();
                    onlineData.put("user_id", identity.userId);
                    onlineData.put("online", 1);

                    // 向每个客服发送消息
                    for (WebSocketSessionRegistry.SessionHolder kefuHolder : kefuSessions) {
                        sendEnvelope(kefuHolder.getSession(), "user_online", onlineData);
                    }

                    log.info("Broadcasted user login to {} kefu sessions: userId={}", kefuSessions.size(), identity.userId);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to handle user login business logic", ex);
        }
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

    public static final class SessionIdentity {
        private final String appid;
        private final int userId;
        private final String userType;
        private final int formType;
        private final boolean tourist;
        private final Integer currentTargetUserId;
        private final Integer serviceId;
        private final int onlineFlag;

        public SessionIdentity(String appid,
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
