package io.renren.crmchat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper facade for pushing structured events to WebSocket clients.
 */
@Slf4j
@Service
public class WebSocketPushService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public boolean isOnline(String appid, int userId) {
        return WebSocketSessionRegistry.isOnline(appid, userId);
    }

    public boolean isEngaged(String appid, int userId, int targetUserId) {
        return WebSocketSessionRegistry.isEngaged(appid, userId, targetUserId);
    }

    public void sendChat(String appid, int userId, Map<String, Object> data) {
        log.info("=== sendChat被调用 === appid={}, userId={}, data={}", appid, userId, data);
        broadcast(appid, userId, "chat", data);
    }

    public void sendReply(String appid, int userId, Map<String, Object> data) {
        log.info("=== sendReply被调用 === appid={}, userId={}, data={}", appid, userId, data);
        broadcast(appid, userId, "reply", data);
    }

    public void sendMessageNum(String appid, int userId, int fromUserId, int unreadNum, int allUnread, Object recored) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", fromUserId);
        payload.put("num", unreadNum);
        payload.put("allNum", allUnread);
        payload.put("recored", recored);
        broadcast(appid, userId, "mssage_num", payload);
    }

    public void sendOnline(String appid, int userId, int onlineFlag) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId);
        payload.put("online", onlineFlag);
        broadcast(appid, userId, "online", payload);
    }

    /**
     * 广播用户上线消息给所有客服（用于更新左侧用户列表和在线状态）
     * Broadcast user online status to all customer service sessions
     *
     * @param appid 租户ID
     * @param userId 用户ID（游客的chat_user.id）
     * @param nickname 用户昵称
     * @param avatar 用户头像
     */
    public void broadcastUserOnline(String appid, int userId, String nickname, String avatar) {
        broadcastUserStatus(appid, userId, 1, nickname, avatar);
    }

    public void broadcastUserOffline(String appid, int userId, String nickname, String avatar) {
        broadcastUserStatus(appid, userId, 0, nickname, avatar);
    }

    public void broadcastUserStatus(String appid, int userId, int onlineFlag, String nickname, String avatar) {
        if (appid == null || appid.isEmpty()) {
            log.warn("broadcastUserStatus: appid is null or empty, skipping");
            return;
        }

        log.info("broadcastUserStatus: appid={}, userId={}, online={}, nickname={}, avatar={}",
                 appid, userId, onlineFlag, nickname, avatar);

        // 获取所有客服的session
        java.util.Set<WebSocketSessionRegistry.SessionHolder> kefuSessions =
                WebSocketSessionRegistry.getSessionsByType(appid, "kefu");

        if (kefuSessions.isEmpty()) {
            log.warn("broadcastUserStatus: no kefu sessions found for appid={}", appid);
            return;
        }

        // 构建user_online消息payload
        Map<String, Object> onlineData = new HashMap<>();
        onlineData.put("user_id", userId);
        onlineData.put("online", onlineFlag);
        onlineData.put("nickname", nickname != null ? nickname : "");
        onlineData.put("avatar", avatar != null ? avatar : "");

        // 向每个客服发送user_online消息
        int successCount = 0;
        for (WebSocketSessionRegistry.SessionHolder kefuHolder : kefuSessions) {
            try {
                ChatWebSocketServer.sendEnvelope(kefuHolder.getSession(), "user_online", onlineData);
                successCount++;
            } catch (Exception e) {
                log.warn("broadcastUserStatus: failed to send to kefu session userId={}, error: {}",
                         kefuHolder.getUserId(), e.getMessage());
            }
        }

        log.info("broadcastUserStatus: sent to {}/{} kefu sessions for userId={}, online={}",
                 successCount, kefuSessions.size(), userId, onlineFlag);
    }

    public void sendEvent(String appid, int userId, String type, Object payload) {
        broadcast(appid, userId, type, payload);
    }

    private void broadcast(String appid, int userId, String type, Object payload) {
        if (appid == null || appid.isEmpty()) {
            log.warn("broadcast: appid is null or empty, skipping");
            return;
        }
        log.info("broadcast: appid={}, userId={}, type={}", appid, userId, type);
        try {
            // Deep copy payload to avoid concurrent modifications when Jackson attempts to serialize later.
            Object safePayload = (payload instanceof Map || payload instanceof Iterable)
                    ? OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsBytes(payload), Object.class)
                    : payload;
            log.info("broadcast: calling ChatWebSocketServer.broadcast with safePayload");
            ChatWebSocketServer.broadcast(appid, userId, type, safePayload);
        } catch (Exception ex) {
            log.error("broadcast: payload serialization failed, using original payload", ex);
            ChatWebSocketServer.broadcast(appid, userId, type, payload);
        }
    }
}
