package io.renren.crmchat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper facade for pushing structured events to WebSocket clients.
 */
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
        broadcast(appid, userId, "chat", data);
    }

    public void sendReply(String appid, int userId, Map<String, Object> data) {
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

    public void sendEvent(String appid, int userId, String type, Object payload) {
        broadcast(appid, userId, type, payload);
    }

    private void broadcast(String appid, int userId, String type, Object payload) {
        if (appid == null || appid.isEmpty()) {
            return;
        }
        try {
            // Deep copy payload to avoid concurrent modifications when Jackson attempts to serialize later.
            Object safePayload = (payload instanceof Map || payload instanceof Iterable)
                    ? OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsBytes(payload), Object.class)
                    : payload;
            ChatWebSocketServer.broadcast(appid, userId, type, safePayload);
        } catch (Exception ignored) {
            ChatWebSocketServer.broadcast(appid, userId, type, payload);
        }
    }
}
