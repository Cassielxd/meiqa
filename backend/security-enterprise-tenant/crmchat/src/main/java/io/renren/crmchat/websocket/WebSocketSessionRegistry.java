package io.renren.crmchat.websocket;

import io.renren.common.redis.RedisUtils;
import io.renren.common.utils.SpringContextUtils;

import jakarta.websocket.Session;
import java.io.Serial;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * In-memory registry of active WebSocket sessions keyed by appid/userId.
 */
public final class WebSocketSessionRegistry {

    private WebSocketSessionRegistry() {
    }

    private static final ConcurrentHashMap<String, CopyOnWriteArraySet<SessionHolder>> SESSIONS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, SessionHolder> SESSION_LOOKUP = new ConcurrentHashMap<>();
    private static final String REGISTRY_KEY_PREFIX = "crmchat:ws:sessions";
    private static final long SESSION_META_TTL_SECONDS = 120;
    private static final String NODE_ID = resolveNodeId();

    static SessionHolder register(Session session,
                                  String appid,
                                  int userId,
                                  String userType,
                                  int formType,
                                  boolean tourist,
                                  Integer initialTarget,
                                  Integer serviceId) {
        SessionHolder holder = new SessionHolder(session, appid, userId, userType, formType, tourist, serviceId);
        holder.setCurrentTargetUserId(initialTarget);
        SESSION_LOOKUP.put(session, holder);
        SESSIONS.computeIfAbsent(key(appid, userId), k -> new CopyOnWriteArraySet<>()).add(holder);
        persistSession(holder);
        return holder;
    }

    static SessionHolder get(Session session) {
        return SESSION_LOOKUP.get(session);
    }

    static SessionHolder remove(Session session) {
        SessionHolder holder = SESSION_LOOKUP.remove(session);
        if (holder != null) {
            Set<SessionHolder> holders = SESSIONS.get(key(holder.getAppid(), holder.getUserId()));
            if (holders != null) {
                holders.remove(holder);
                if (holders.isEmpty()) {
                    SESSIONS.remove(key(holder.getAppid(), holder.getUserId()));
                }
            }
            removeSessionMetadata(holder);
        }
        return holder;
    }

    static void touch(Session session) {
        SessionHolder holder = SESSION_LOOKUP.get(session);
        if (holder != null) {
            persistSession(holder);
        }
    }

    public static Set<SessionHolder> getSessions(String appid, int userId) {
        Set<SessionHolder> holders = SESSIONS.get(key(appid, userId));
        return holders == null ? Collections.emptySet() : holders;
    }

    public static boolean isOnline(String appid, int userId) {
        return !getSessions(appid, userId).isEmpty();
    }

    public static boolean isEngaged(String appid, int userId, int targetUserId) {
        for (SessionHolder holder : getSessions(appid, userId)) {
            Integer currentTarget = holder.getCurrentTargetUserId();
            if (currentTarget != null && currentTarget == targetUserId) {
                return true;
            }
        }
        return false;
    }

    static void updateCurrentTarget(Session session, Integer targetUserId) {
        SessionHolder holder = SESSION_LOOKUP.get(session);
        if (holder != null) {
            holder.setCurrentTargetUserId(targetUserId);
        }
    }

    static void updateFormType(Session session, Integer formType) {
        SessionHolder holder = SESSION_LOOKUP.get(session);
        if (holder != null && formType != null) {
            holder.setFormType(formType);
        }
    }

    private static String key(String appid, int userId) {
        return appid + "::" + userId;
    }

    private static void persistSession(SessionHolder holder) {
        RedisUtils redis = redis();
        if (redis == null) {
            return;
        }
        String key = redisKey(holder.getAppid(), holder.getUserId());
        SessionDescriptor descriptor = new SessionDescriptor(
                NODE_ID,
                holder.getUserType(),
                holder.isTourist(),
                holder.getServiceId(),
                System.currentTimeMillis());
        redis.hSet(key, holder.getSession().getId(), descriptor, SESSION_META_TTL_SECONDS);
    }

    private static void removeSessionMetadata(SessionHolder holder) {
        RedisUtils redis = redis();
        if (redis == null) {
            return;
        }
        String key = redisKey(holder.getAppid(), holder.getUserId());
        redis.hDel(key, holder.getSession().getId());
    }

    private static String redisKey(String appid, int userId) {
        return REGISTRY_KEY_PREFIX + ":" + appid + ":" + userId;
    }

    private static RedisUtils redis() {
        return SpringContextUtils.getBean(RedisUtils.class);
    }

    private static String resolveNodeId() {
        String host = System.getenv("HOSTNAME");
        if (host != null && !host.isBlank()) {
            return host;
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
            return runtimeName != null ? runtimeName : "unknown-node";
        }
    }

    /**
     * Metadata wrapper for a WebSocket session.
     */
    public static final class SessionHolder {
        private final Session session;
        private final String appid;
        private final int userId;
        private final String userType;
        private volatile Integer currentTargetUserId;
        private volatile Integer formType;
        private volatile boolean tourist;
        private final Integer serviceId;

        SessionHolder(Session session,
                      String appid,
                      int userId,
                      String userType,
                      int formType,
                      boolean tourist,
                      Integer serviceId) {
            this.session = session;
            this.appid = appid;
            this.userId = userId;
            this.userType = userType;
            this.formType = formType;
            this.tourist = tourist;
            this.serviceId = serviceId;
        }

        public Session getSession() {
            return session;
        }

        public String getAppid() {
            return appid;
        }

        public int getUserId() {
            return userId;
        }

        public String getUserType() {
            return userType;
        }

        public Integer getCurrentTargetUserId() {
            return currentTargetUserId;
        }

        public void setCurrentTargetUserId(Integer currentTargetUserId) {
            this.currentTargetUserId = currentTargetUserId;
        }

        public Integer getFormType() {
            return formType;
        }

        public void setFormType(Integer formType) {
            this.formType = formType;
        }

        public boolean isTourist() {
            return tourist;
        }

        public void setTourist(boolean tourist) {
            this.tourist = tourist;
        }

        public Integer getServiceId() {
            return serviceId;
        }
    }

    private static final class SessionDescriptor implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final String nodeId;
        private final String userType;
        private final boolean tourist;
        private final Integer serviceId;
        private final long connectedAt;

        private SessionDescriptor(String nodeId, String userType, boolean tourist, Integer serviceId, long connectedAt) {
            this.nodeId = nodeId;
            this.userType = userType;
            this.tourist = tourist;
            this.serviceId = serviceId;
            this.connectedAt = connectedAt;
        }

        public String getNodeId() {
            return nodeId;
        }

        public String getUserType() {
            return userType;
        }

        public boolean isTourist() {
            return tourist;
        }

        public Integer getServiceId() {
            return serviceId;
        }

        public long getConnectedAt() {
            return connectedAt;
        }
    }
}
