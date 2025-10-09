package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.common.redis.RedisUtils;
import io.renren.crmchat.dao.ChatServiceMapper;
import io.renren.crmchat.dao.ChatUserMapper;
import io.renren.crmchat.dao.SystemConfigMapper;
import io.renren.crmchat.entity.ChatServiceEntity;
import io.renren.crmchat.entity.ChatUserEntity;
import io.renren.crmchat.entity.SystemConfigEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 统一缓存管理：用户画像、在线客服、系统配置等高频数据。
 * 结合本地内存 + Redis，多租户隔离由 appid 控制。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatCacheService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final long PROFILE_TTL_SECONDS = 120;
    private static final long ONLINE_SERVICE_TTL_SECONDS = 10;
    private static final long CONFIG_TTL_SECONDS = 300;

    private final RedisUtils redisUtils;
    private final ChatUserMapper chatUserMapper;
    private final ChatServiceMapper chatServiceMapper;
    private final SystemConfigMapper systemConfigMapper;

    private final ConcurrentHashMap<String, LocalCacheEntry<?>> localCache = new ConcurrentHashMap<>();

    /* --------------------------- Site Name --------------------------- */

    public String getSiteName() {
        String localKey = "config::site_name";
        String redisKey = "crmchat:config:site_name";
        String cached = getLocal(localKey, String.class);
        if (cached != null) {
            return cached;
        }

        Object redisValue = redisUtils.get(redisKey, CONFIG_TTL_SECONDS);
        if (redisValue instanceof String str && !str.isBlank()) {
            putLocal(localKey, str, CONFIG_TTL_SECONDS);
            return str;
        }

        QueryWrapper<SystemConfigEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("menu_name", "site_name").last("LIMIT 1");
        SystemConfigEntity config = systemConfigMapper.selectOne(wrapper);
        String siteName = config == null ? "" : parseConfigString(config.getValue());
        redisUtils.set(redisKey, siteName, CONFIG_TTL_SECONDS);
        putLocal(localKey, siteName, CONFIG_TTL_SECONDS);
        return siteName;
    }

    public void invalidateSiteName() {
        localCache.remove("config::site_name");
        redisUtils.delete("crmchat:config:site_name");
    }

    /* --------------------------- Tourist Avatar --------------------------- */

    public String pickTouristAvatar(String appid) {
        List<String> avatars = getTouristAvatars(appid);
        if (avatars.isEmpty()) {
            return "";
        }
        int index = ThreadLocalRandom.current().nextInt(avatars.size());
        return avatars.get(index);
    }

    public List<String> getTouristAvatars(String appid) {
        String localKey = cacheKey(appid, "config", "tourist_avatar");
        @SuppressWarnings("unchecked")
        List<String> local = getLocal(localKey, List.class);
        if (local != null) {
            return local;
        }

        String redisKey = redisKey(appid, "config", "tourist_avatar");
        Object cached = redisUtils.get(redisKey, CONFIG_TTL_SECONDS);
        if (cached instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<String> converted = ((List<?>) cached).stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toCollection(ArrayList::new));
            putLocal(localKey, converted, CONFIG_TTL_SECONDS);
            return converted;
        }

        QueryWrapper<SystemConfigEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("menu_name", "tourist_avatar").last("LIMIT 1");
        SystemConfigEntity config = systemConfigMapper.selectOne(wrapper);
        List<String> avatars = config == null
                ? Collections.emptyList()
                : parseConfigArray(config.getValue());
        redisUtils.set(redisKey, avatars, CONFIG_TTL_SECONDS);
        putLocal(localKey, avatars, CONFIG_TTL_SECONDS);
        return avatars;
    }

    public void invalidateTouristAvatars(String appid) {
        String localKey = cacheKey(appid, "config", "tourist_avatar");
        localCache.remove(localKey);
        redisUtils.delete(redisKey(appid, "config", "tourist_avatar"));
    }

    /* --------------------------- Online Services --------------------------- */

    public List<ChatServiceEntity> getOnlineServices(String appid) {
        // 调试日志：检查租户隔离
        log.info("[租户隔离检查] Cache层 - 查询在线客服,appid: {}", appid);

        String localKey = cacheKey(appid, "service", "online_list");
        @SuppressWarnings("unchecked")
        List<ServiceSnapshot> local = getLocal(localKey, List.class);
        if (local != null) {
            log.info("[租户隔离检查] Cache层 - 从本地缓存返回,数量: {}", local.size());
            return toServiceEntities(local);
        }

        String redisKey = redisKey(appid, "service", "online_list");
        Object cached = redisUtils.get(redisKey, ONLINE_SERVICE_TTL_SECONDS);
        if (cached instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<ServiceSnapshot> snapshots = (List<ServiceSnapshot>) cached;
            putLocal(localKey, snapshots, ONLINE_SERVICE_TTL_SECONDS);
            log.info("[租户隔离检查] Cache层 - 从Redis缓存返回,数量: {}", snapshots.size());
            return toServiceEntities(snapshots);
        }

        log.info("[租户隔离检查] Cache层 - 执行数据库查询,appid: {}", appid);
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid).eq("status", 1).eq("online", 1);
        List<ChatServiceEntity> services = chatServiceMapper.selectList(wrapper);
        log.info("[租户隔离检查] Cache层 - 数据库查询结果,数量: {}", services.size());
        List<ServiceSnapshot> snapshots = services.stream()
                .map(ServiceSnapshot::fromEntity)
                .collect(Collectors.toCollection(ArrayList::new));
        redisUtils.set(redisKey, snapshots, ONLINE_SERVICE_TTL_SECONDS);
        putLocal(localKey, snapshots, ONLINE_SERVICE_TTL_SECONDS);
        return services;
    }

    public void invalidateOnlineServices(String appid) {
        String localKey = cacheKey(appid, "service", "online_list");
        localCache.remove(localKey);
        redisUtils.delete(redisKey(appid, "service", "online_list"));
    }

    /* --------------------------- User Profile --------------------------- */

    public UserProfile getUserProfile(String appid, int userId) {
        if (userId <= 0) {
            return null;
        }
        String localKey = cacheKey(appid, "profile", String.valueOf(userId));
        UserProfile localProfile = getLocal(localKey, UserProfile.class);
        if (localProfile != null) {
            return localProfile;
        }

        String redisKey = redisKey(appid, "profile", String.valueOf(userId));
        Object cached = redisUtils.get(redisKey, PROFILE_TTL_SECONDS);
        if (cached instanceof UserProfile profile) {
            putLocal(localKey, profile, PROFILE_TTL_SECONDS);
            return profile;
        }

        // 回表查询
        ChatUserEntity user = chatUserMapper.selectById(userId);
        if (user != null && appid.equals(user.getAppid())) {
            UserProfile profile = UserProfile.fromChatUser(user);
            cacheUserProfile(appid, profile);
            return profile;
        }

        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid).eq("user_id", userId).last("LIMIT 1");
        ChatServiceEntity service = chatServiceMapper.selectOne(wrapper);
        if (service != null) {
            UserProfile profile = UserProfile.fromChatService(service);
            cacheUserProfile(appid, profile);
            return profile;
        }

        return null;
    }

    public Map<Integer, UserProfile> getUserProfiles(String appid, Set<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userIds.parallelStream()
                .map(id -> Map.entry(id, getUserProfile(appid, id)))
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void cacheUser(ChatUserEntity entity) {
        if (entity == null || entity.getId() == null) {
            return;
        }
        cacheUserProfile(entity.getAppid(), UserProfile.fromChatUser(entity));
    }

    public void cacheServiceProfile(ChatServiceEntity entity) {
        if (entity == null || entity.getUserId() == null) {
            return;
        }
        cacheUserProfile(entity.getAppid(), UserProfile.fromChatService(entity));
    }

    public void invalidateUserProfile(String appid, int userId) {
        if (appid == null || appid.isBlank() || userId <= 0) {
            return;
        }
        String localKey = cacheKey(appid, "profile", String.valueOf(userId));
        localCache.remove(localKey);
        redisUtils.delete(redisKey(appid, "profile", String.valueOf(userId)));
    }

    private void cacheUserProfile(String appid, UserProfile profile) {
        if (appid == null || appid.isBlank() || profile == null) {
            return;
        }
        String localKey = cacheKey(appid, "profile", String.valueOf(profile.getUserId()));
        putLocal(localKey, profile, PROFILE_TTL_SECONDS);
        redisUtils.set(redisKey(appid, "profile", String.valueOf(profile.getUserId())), profile, PROFILE_TTL_SECONDS);
    }

    /* --------------------------- 工具方法 --------------------------- */

    private String cacheKey(String appid, String category, String suffix) {
        return appid == null || appid.isBlank()
                ? category + "::" + suffix
                : category + "::" + appid + "::" + suffix;
    }

    private String redisKey(String appid, String category, String suffix) {
        return appid == null || appid.isBlank()
                ? "crmchat:" + category + ":" + suffix
                : "crmchat:" + category + ":" + appid + ":" + suffix;
    }

    @SuppressWarnings("unchecked")
    private <T> T getLocal(String key, Class<T> type) {
        LocalCacheEntry<?> entry = localCache.get(key);
        if (entry == null || entry.isExpired()) {
            localCache.remove(key);
            return null;
        }
        Object value = entry.value;
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    private void putLocal(String key, Object value, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            localCache.remove(key);
            return;
        }
        long expireAt = System.currentTimeMillis() + ttlSeconds * 1000;
        localCache.put(key, new LocalCacheEntry<>(value, expireAt));
    }

    private List<ChatServiceEntity> toServiceEntities(List<ServiceSnapshot> snapshots) {
        if (snapshots == null || snapshots.isEmpty()) {
            return Collections.emptyList();
        }
        return snapshots.stream().map(ServiceSnapshot::toEntity).collect(Collectors.toList());
    }

    private String parseConfigString(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(cleaned);
            if (node.isTextual()) {
                return node.asText();
            }
        } catch (Exception ignored) {
            try {
                JsonNode node = OBJECT_MAPPER.readTree(cleaned.replace("\\\"", "\""));
                if (node.isTextual()) {
                    return node.asText();
                }
            } catch (Exception ignoredAgain) {
                // fallthrough
            }
        }
        cleaned = cleaned.replace("\\\"", "\"");
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) || (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            return cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned;
    }

    private List<String> parseConfigArray(String raw) {
        if (raw == null) {
            return Collections.emptyList();
        }
        String cleaned = raw.trim();
        if (cleaned.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(cleaned);
            if (node.isArray()) {
                List<String> list = new ArrayList<>();
                node.forEach(item -> {
                    if (item.isTextual()) {
                        list.add(item.asText());
                    }
                });
                return list;
            }
        } catch (Exception ignored) {
            try {
                JsonNode node = OBJECT_MAPPER.readTree(cleaned.replace("\\\"", "\""));
                if (node.isArray()) {
                    List<String> list = new ArrayList<>();
                    node.forEach(item -> {
                        if (item.isTextual()) {
                            list.add(item.asText());
                        }
                    });
                    return list;
                }
            } catch (Exception ignoredAgain) {
                // fallthrough
            }
        }

        cleaned = cleaned.replace("[", "").replace("]", "");
        if (cleaned.isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = cleaned.split(",");
        List<String> list = new ArrayList<>();
        for (String part : parts) {
            String item = part.trim();
            if (item.isEmpty()) {
                continue;
            }
            item = item.replace("\\\"", "\"");
            if ((item.startsWith("\"") && item.endsWith("\"")) || (item.startsWith("'") && item.endsWith("'"))) {
                item = item.substring(1, item.length() - 1);
            }
            if (!item.isEmpty()) {
                list.add(item);
            }
        }
        return list;
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    /* --------------------------- 内部类 --------------------------- */

    private static class LocalCacheEntry<T> {
        private final T value;
        private final long expireAt;

        LocalCacheEntry(T value, long expireAt) {
            this.value = value;
            this.expireAt = expireAt;
        }

        boolean isExpired() {
            return expireAt <= now();
        }
    }

    public static class UserProfile implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final int userId;
        private final String nickname;
        private final String avatar;
        private final String version;
        private final boolean tourist;

        private UserProfile(int userId, String nickname, String avatar, String version, boolean tourist) {
            this.userId = userId;
            this.nickname = nickname == null ? "" : nickname;
            this.avatar = avatar == null ? "" : avatar;
            this.version = version == null ? "" : version;
            this.tourist = tourist;
        }

        public static UserProfile fromChatUser(ChatUserEntity entity) {
            String nickname = entity.getRemarkNickname() != null && !entity.getRemarkNickname().isBlank()
                    ? entity.getRemarkNickname()
                    : entity.getNickname();
            return new UserProfile(
                    entity.getId(),
                    nickname,
                    entity.getAvatar(),
                    entity.getVersion(),
                    entity.getIsTourist() != null && entity.getIsTourist() == 1
            );
        }

        public static UserProfile fromChatService(ChatServiceEntity entity) {
            return new UserProfile(
                    entity.getUserId() != null ? entity.getUserId() : 0,
                    entity.getNickname(),
                    entity.getAvatar(),
                    "",
                    false
            );
        }

        public int getUserId() {
            return userId;
        }

        public String getNickname() {
            return nickname;
        }

        public String getAvatar() {
            return avatar;
        }

        public String getVersion() {
            return version;
        }

        public boolean isTourist() {
            return tourist;
        }
    }

    private static class ServiceSnapshot implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Integer id;
        private Integer userId;
        private Integer groupId;
        private Integer online;
        private String nickname;
        private String avatar;
        private String welcomeWords;
        private Integer autoReply;
        private Integer status;
        private String appid;

        static ServiceSnapshot fromEntity(ChatServiceEntity entity) {
            ServiceSnapshot snapshot = new ServiceSnapshot();
            snapshot.id = entity.getId();
            snapshot.userId = entity.getUserId();
            snapshot.groupId = entity.getGroupId();
            snapshot.online = entity.getOnline();
            snapshot.nickname = entity.getNickname();
            snapshot.avatar = entity.getAvatar();
            snapshot.welcomeWords = entity.getWelcomeWords();
            snapshot.autoReply = entity.getAutoReply();
            snapshot.status = entity.getStatus();
            snapshot.appid = entity.getAppid();
            return snapshot;
        }

        ChatServiceEntity toEntity() {
            ChatServiceEntity entity = new ChatServiceEntity();
            entity.setId(id);
            entity.setUserId(userId);
            entity.setGroupId(groupId);
            entity.setOnline(online);
            entity.setNickname(nickname);
            entity.setAvatar(avatar);
            entity.setWelcomeWords(welcomeWords);
            entity.setAutoReply(autoReply);
            entity.setStatus(status);
            entity.setAppid(appid);
            return entity;
        }

        // Getters for Jackson serialization
        public Integer getId() { return id; }
        public Integer getUserId() { return userId; }
        public Integer getGroupId() { return groupId; }
        public Integer getOnline() { return online; }
        public String getNickname() { return nickname; }
        public String getAvatar() { return avatar; }
        public String getWelcomeWords() { return welcomeWords; }
        public Integer getAutoReply() { return autoReply; }
        public Integer getStatus() { return status; }
        public String getAppid() { return appid; }
    }
}
