package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.crmchat.dao.*;
import io.renren.crmchat.entity.*;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.mapper.QrcodeMapper;
import io.renren.crmchat.service.common.FileService;
import io.renren.crmchat.websocket.WebSocketPushService;
import io.renren.crmchat.service.ChatCacheService.UserProfile;
import io.renren.crmchat.service.common.TokenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Mobile Service - 移动端客服服务
 * PHP Reference: /app/controller/mobile/Service.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getRecordList(): 获取聊天记录（支持用户信息同步、客服分配）
 * 2. getKfAdv(): 获取客服页面广告内容
 * 3. getCache(): 获取缓存数据
 * 4. setCache(): 设置缓存数据
 * 5. upload(): 图片上传
 * 6. getKefuConfig(): 获取客服图标配置
 * 7. getSendId(): 获取消息发送ID
 * 8. sendMessage(): 发送消息
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class MobileServiceService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DateTimeFormatter FULL_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter RECORD_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ChatServiceMapper chatServiceMapper;
    private final ChatServiceRecordMapper chatServiceRecordMapper;
    private final ChatServiceDialogueRecordMapper chatServiceDialogueRecordMapper;
    private final ChatUserMapper chatUserMapper;
    private final AuxiliaryMapper auxiliaryMapper;
    private final QrcodeMapper qrcodeMapper;
    private final WebSocketPushService webSocketPushService;
    private final ChatCacheService chatCacheService;
    private final TokenService tokenService;
    private final FileService fileService;
    private final SystemAttachmentMapper systemAttachmentMapper;

    private static final int PARALLEL_THRESHOLD = 12;

    /**
     * 获取聊天记录
     * POST /api/mobile/service/record
     *
     * PHP Reference: Service.php::getRecordList()
     *
     * 业务逻辑:
     * 1. 接收用户信息（uid, nickname, phone, sex, avatar, openid, type）
     * 2. 处理客服分配逻辑：
     *    - 优先使用指定客服(kefu_id)
     *    - 其次使用toUserId
     *    - 最后使用随机客服(kefu_rand)
     * 3. 返回聊天记录列表（分页）
     *
     * @param params 请求参数
     * @param appid  租户ID
     * @return 聊天记录列表
     */
    public Map<String, Object> getRecordList(Map<String, Object> params, String appid) {
        // 调试日志：检查租户隔离
        log.info("[Tenant isolation check] Service layer received appid: {}", appid);

        int idTo = parseInt(params.get("idTo"), 0);
        int limit = parseInt(params.get("limit"), 10);
        limit = limit <= 0 ? 10 : Math.min(limit, 100);
        int toUserId = parseInt(params.get("toUserId"), 0);
        int cookieUid = parseInt(params.get("uid"), 0);
        int kefuId = parseInt(params.get("kefu_id"), 0);
        int kefuRand = parseInt(params.get("kefu_rand"), 0);

        Map<String, Object> userPayload = new HashMap<>();
        userPayload.put("uid", params.getOrDefault("uid", ""));
        userPayload.put("nickname", params.getOrDefault("nickname", ""));
        userPayload.put("phone", params.getOrDefault("phone", ""));
        userPayload.put("sex", params.getOrDefault("sex", ""));
        userPayload.put("avatar", params.getOrDefault("avatar", ""));
        userPayload.put("openid", params.getOrDefault("openid", ""));
        userPayload.put("type", params.getOrDefault("type", ""));

        if (kefuId > 0 && toUserId > 0) {
            toUserId = 0;
        }
        if (kefuId > 0 && kefuRand > 0) {
            kefuRand = 0;
        }
        if (toUserId > 0 && kefuRand > 0) {
            kefuRand = 0;
            kefuId = 0;
        }

        EnsureUserResult userResult = ensureChatUser(appid, userPayload, cookieUid);
        ChatUserEntity chatUser = userResult.user;
        if (chatUser == null || chatUser.getId() == null) {
            throw new CrmChatException("User does not exist");
        }
        int userId = chatUser.getId();
        int uid = chatUser.getUid() != null ? chatUser.getUid() : 0;

        toUserId = resolvePreferredServiceUserId(appid, toUserId, kefuId, kefuRand, userId);

        // 获取在线客服列表（与PHP保持一致：必须有在线客服）
        List<ChatServiceEntity> onlineServices = chatCacheService.getOnlineServices(appid);
        if (onlineServices.isEmpty()) {
            throw new CrmChatException("No support agents are online right now. Please try again later.");
        }

        Map<Integer, ChatServiceEntity> onlineServiceMap = new HashMap<>();
        for (ChatServiceEntity service : onlineServices) {
            Integer serviceUserId = service.getUserId();
            if (serviceUserId != null) {
                onlineServiceMap.put(serviceUserId, service);
            }
        }

        // 如果指定的客服不在线，重置为0
        if (toUserId > 0 && !onlineServiceMap.containsKey(toUserId)) {
            toUserId = 0;
        }

        // 尝试找最近聊天的客服（如果在线）
        if (toUserId == 0) {
            Integer latelyUserId = findLatestChatServiceUserId(appid, userId);
            if (latelyUserId != null && onlineServiceMap.containsKey(latelyUserId)) {
                toUserId = latelyUserId;
            }
        }

        // 如果还是没有，随机选择一个在线客服
        if (toUserId == 0 && !onlineServiceMap.isEmpty()) {
            toUserId = pickRandomUserId(onlineServiceMap.keySet());
        }

        ChatServiceEntity assignedService = onlineServiceMap.get(toUserId);
        if (assignedService == null || toUserId <= 0) {
            throw new CrmChatException("No support agents are online right now. Please try again later.");
        }

        chatCacheService.cacheServiceProfile(assignedService);
        int resolvedKefuId = assignedService.getId() != null ? assignedService.getId() : 0;
        String toUserNickname = Optional.ofNullable(assignedService.getNickname()).orElse("");
        String toUserAvatar = Optional.ofNullable(assignedService.getAvatar()).orElse("");

        // 首次对话时发送欢迎语
        Object welcomeData = Boolean.FALSE;
        if (idTo <= 0 && shouldSendWelcome(appid, userId, toUserId, assignedService.getWelcomeWords())) {
            ChatServiceDialogueRecordEntity welcomeRecord = createWelcomeMessage(appid, userId, chatUser, assignedService);
            welcomeData = buildWelcomePayload(appid, welcomeRecord, assignedService, chatUser);
        }

        // 查询历史聊天记录
        List<ChatServiceDialogueRecordEntity> records = queryDialogueRecords(appid, userId, toUserId, idTo, limit);
        Collections.reverse(records);
        List<Map<String, Object>> serviceList = tidyChatRecords(appid, records);

        Map<String, Object> result = new HashMap<>();
        result.put("serviceList", serviceList);
        result.put("toUserId", toUserId);
        result.put("to_user_id", toUserId);
        result.put("kefuId", resolvedKefuId);
        result.put("kefu_id", resolvedKefuId);
        result.put("is_tourist", chatUser.getIsTourist() != null ? chatUser.getIsTourist() : 0);
        result.put("uid", uid);
        result.put("user_id", userId);
        result.put("site_name", chatCacheService.getSiteName());
        result.put("nickname", Optional.ofNullable(chatUser.getNickname()).orElse(""));
        result.put("avatar", Optional.ofNullable(chatUser.getAvatar()).orElse(""));
        result.put("to_user_nickname", toUserNickname);
        result.put("to_user_avatar", toUserAvatar);
        result.put("welcome", idTo > 0 ? Boolean.FALSE : welcomeData);

        log.info("Fetching chat history: appid={}, userId={}, toUserId={}, records={}",
                appid, userId, toUserId, serviceList.size());
        return result;
    }

    /**
     * 游客自动登录：根据传入的用户信息自动创建/更新用户，并签发 JWT token。
     */
    public Map<String, Object> autoLogin(Map<String, Object> params, String appid) {
        Map<String, Object> userPayload = new HashMap<>();
        userPayload.put("uid", params.getOrDefault("uid", ""));
        userPayload.put("nickname", params.getOrDefault("nickname", ""));
        userPayload.put("phone", params.getOrDefault("phone", ""));
        userPayload.put("sex", params.getOrDefault("sex", ""));
        userPayload.put("avatar", params.getOrDefault("avatar", ""));
        userPayload.put("openid", params.getOrDefault("openid", ""));
        userPayload.put("type", params.getOrDefault("type", ""));

        int cookieUid = parseInt(params.get("cookieUid"), 0);

        EnsureUserResult ensureResult = ensureChatUser(appid, userPayload, cookieUid);
        ChatUserEntity chatUser = ensureResult.user;
        if (chatUser == null || chatUser.getId() == null) {
            throw new CrmChatException("Failed to create user");
        }

        String username = Optional.ofNullable(chatUser.getNickname())
                .filter(n -> !n.isBlank())
                .orElse("user-" + (chatUser.getUid() != null ? chatUser.getUid() : chatUser.getId()));

        String token = tokenService.generateToken(
                chatUser.getId().longValue(),
                username,
                chatUser.getAppid()
        );
        Long expiresTime = tokenService.getTokenExpireAt(token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("user_id", chatUser.getId());
        userInfo.put("uid", chatUser.getUid());
        userInfo.put("nickname", Optional.ofNullable(chatUser.getNickname()).orElse(""));
        userInfo.put("avatar", Optional.ofNullable(chatUser.getAvatar()).orElse(""));
        userInfo.put("is_tourist", chatUser.getIsTourist() != null ? chatUser.getIsTourist() : 0);
        userInfo.put("appid", chatUser.getAppid());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("expires_time", expiresTime);
        result.put("user_info", userInfo);
        result.put("is_new_user", ensureResult.created);

        log.info("Guest auto-login: appid={}, userId={}, isNew={}", chatUser.getAppid(), chatUser.getId(), ensureResult.created);
        return result;
    }

    private int parseInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String str = value.toString().trim();
        if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private String optionalString(Object value) {
        if (value == null) {
            return "";
        }
        String str = value.toString().trim();
        if ("null".equalsIgnoreCase(str)) {
            return "";
        }
        return str;
    }

    private EnsureUserResult ensureChatUser(String appid, Map<String, Object> payload, int fallbackUserId) {
        int uidValue = parseInt(payload.get("uid"), 0);
        ChatUserEntity user = null;
        if (uidValue > 0) {
            QueryWrapper<ChatUserEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("uid", uidValue);
            user = chatUserMapper.selectOne(wrapper);
        }
        if (user == null && fallbackUserId > 0) {
            QueryWrapper<ChatUserEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("id", fallbackUserId);
            user = chatUserMapper.selectOne(wrapper);
        }

        boolean created = false;
        boolean updated = false;
        if (user == null) {
            int generatedUid = uidValue > 0 ? uidValue : generateTouristUid();
            boolean isTourist = uidValue <= 0;

            user = new ChatUserEntity();
            user.setUid(generatedUid);
            user.setAppid(appid);
            user.setNickname(resolveNickname(payload, generatedUid));
            user.setAvatar(resolveAvatar(appid, payload));
            user.setPhone(optionalString(payload.get("phone")));
            user.setOpenid(optionalString(payload.get("openid")));
            user.setType(parseInt(payload.get("type"), 0));
            user.setSex(parseInt(payload.get("sex"), 0));
            user.setIsTourist(isTourist ? 1 : 0);
            user.setGroupId(0);
            user.setIsDelete(0);
            user.setIsKefu(0);
            user.setOnline(0);
            user.setVersion("0");
            user.setRemarkNickname(Optional.ofNullable(user.getRemarkNickname()).orElse(""));
            user.setRemarks(Optional.ofNullable(user.getRemarks()).orElse(""));
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            chatUserMapper.insert(user);
            created = true;
        } else {
            boolean needUpdate = false;
            String nickname = optionalString(payload.get("nickname"));
            if (!nickname.isEmpty() && !nickname.equals(Optional.ofNullable(user.getNickname()).orElse(""))) {
                user.setNickname(nickname);
                needUpdate = true;
            }
            String avatar = optionalString(payload.get("avatar"));
            if (!avatar.isEmpty() && !avatar.equals(Optional.ofNullable(user.getAvatar()).orElse(""))) {
                user.setAvatar(avatar);
                needUpdate = true;
            }
            String phone = optionalString(payload.get("phone"));
            if (!phone.isEmpty() && !phone.equals(Optional.ofNullable(user.getPhone()).orElse(""))) {
                user.setPhone(phone);
                needUpdate = true;
            }
            String openid = optionalString(payload.get("openid"));
            if (!openid.isEmpty() && !openid.equals(Optional.ofNullable(user.getOpenid()).orElse(""))) {
                user.setOpenid(openid);
                needUpdate = true;
            }
            int type = parseInt(payload.get("type"), user.getType() != null ? user.getType() : 0);
            if (!Objects.equals(user.getType(), type)) {
                user.setType(type);
                needUpdate = true;
            }
            int sex = parseInt(payload.get("sex"), user.getSex() != null ? user.getSex() : 0);
            if (!Objects.equals(user.getSex(), sex)) {
                user.setSex(sex);
                needUpdate = true;
            }
            if (needUpdate) {
                user.setUpdateTime(LocalDateTime.now());
                chatUserMapper.updateById(user);
                updated = true;
            }
        }

        if (created || updated) {
            chatCacheService.invalidateUserProfile(appid, user.getId());
            chatCacheService.cacheUser(user);
        }

        if (created) {
            chatCacheService.invalidateOnlineServices(appid);
        }

        return new EnsureUserResult(user, created);
    }

    private int generateTouristUid() {
        int year = LocalDateTime.now().getYear();
        int rand1 = ThreadLocalRandom.current().nextInt(10, 100);
        int rand2 = ThreadLocalRandom.current().nextInt(1000, 10000);
        String uidStr = String.valueOf(year) + rand1 + rand2;
        try {
            return Integer.parseInt(uidStr);
        } catch (NumberFormatException ex) {
            return ThreadLocalRandom.current().nextInt(100000, 999999);
        }
    }

    private String resolveNickname(Map<String, Object> payload, int uid) {
        String nickname = optionalString(payload.get("nickname"));
        if (!nickname.isEmpty()) {
            return nickname;
        }
        return "Guest" + uid;
    }

    private String resolveAvatar(String appid, Map<String, Object> payload) {
        String avatar = optionalString(payload.get("avatar"));
        if (!avatar.isEmpty()) {
            return avatar;
        }
        return chatCacheService.pickTouristAvatar(appid);
    }

    private int resolvePreferredServiceUserId(String appid, int toUserId, int kefuId, int kefuRand, int chatUserId) {
        int resolved = 0;
        if (toUserId > 0 && isActiveServiceUser(appid, toUserId)) {
            resolved = toUserId;
        }
        if (resolved == 0 && kefuId > 0) {
            resolved = resolveFromKefuId(appid, kefuId);
        }
        if (resolved == 0 && kefuRand > 0) {
            resolved = resolveFromQrcode(appid, kefuRand);
        }
        if (resolved > 0) {
            resolved = applyTransferRelation(appid, resolved, chatUserId);
        }
        return Math.max(resolved, 0);
    }

    private boolean isActiveServiceUser(String appid, int userId) {
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("status", 1);
        wrapper.eq("user_id", userId);
        return chatServiceMapper.selectCount(wrapper) > 0;
    }

    private int resolveFromKefuId(String appid, int kefuId) {
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("status", 1);
        wrapper.eq("id", kefuId);
        ChatServiceEntity service = chatServiceMapper.selectOne(wrapper);
        if (service != null && service.getUserId() != null) {
            chatCacheService.cacheServiceProfile(service);
            return service.getUserId();
        }
        return 0;
    }

    private int resolveFromQrcode(String appid, int qrcodeId) {
        QrcodeEntity qrcode = qrcodeMapper.selectById(qrcodeId);
        if (qrcode == null) {
            return 0;
        }
        if (qrcode.getAppid() != null && !qrcode.getAppid().isEmpty() && !Objects.equals(qrcode.getAppid(), appid)) {
            return 0;
        }
        List<Integer> userIds = qrcode.getUserIds();
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        List<Integer> candidates = new ArrayList<>();
        for (Integer id : userIds) {
            if (id != null && id > 0) {
                candidates.add(id);
            }
        }
        if (candidates.isEmpty()) {
            return 0;
        }
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    private int applyTransferRelation(String appid, int candidateUserId, int chatUserId) {
        QueryWrapper<AuxiliaryEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("binding_id", chatUserId);
        wrapper.eq("type", 0);
        wrapper.orderByDesc("update_time");
        wrapper.last("LIMIT 1");
        AuxiliaryEntity auxiliary = auxiliaryMapper.selectOne(wrapper);
        if (auxiliary == null || auxiliary.getRelationId() == null || auxiliary.getRelationId() <= 0) {
            return candidateUserId;
        }

        int relationUserId = auxiliary.getRelationId();
        if (isServiceOnline(appid, relationUserId)) {
            return relationUserId;
        }
        return 0;
    }

    private boolean isServiceOnline(String appid, int userId) {
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("status", 1);
        wrapper.eq("online", 1);
        wrapper.eq("user_id", userId);
        return chatServiceMapper.selectCount(wrapper) > 0;
    }

    /**
     * 查找游客最近聊天的客服
     * 根据PHP实现: eb_chat_service_record表中 user_id=游客, to_user_id=客服
     * @param appid 租户ID
     * @param chatUserId 游客的user_id
     * @return 客服的user_id，如果没有历史记录则返回null
     */
    private Integer findLatestChatServiceUserId(String appid, int chatUserId) {
        QueryWrapper<ChatServiceRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("user_id", chatUserId);  // user_id = 游客 (per PHP implementation)
        wrapper.orderByDesc("update_time");
        wrapper.last("LIMIT 1");
        ChatServiceRecordEntity record = chatServiceRecordMapper.selectOne(wrapper);
        return record != null ? record.getToUserId() : null;  // to_user_id = 客服 (per PHP implementation)
    }

    /**
     * 查找系统中的任意一个启用状态的客服(即使离线)
     * 用于新游客发送第一条消息时,没有在线客服也没有历史记录的情况
     */
    private Integer findAnyActiveServiceUserId(String appid) {
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("status", 1);  // 只查询启用状态的客服
        wrapper.last("LIMIT 1");  // 取第一个
        ChatServiceEntity service = chatServiceMapper.selectOne(wrapper);
        return service != null && service.getUserId() != null ? service.getUserId() : null;
    }

    private int pickRandomUserId(Set<Integer> userIds) {
        List<Integer> candidates = new ArrayList<>();
        for (Integer id : userIds) {
            if (id != null && id > 0) {
                candidates.add(id);
            }
        }
        if (candidates.isEmpty()) {
            throw new CrmChatException("No customer service agents are currently online, please try again later");
        }
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    private boolean shouldSendWelcome(String appid, int chatUserId, int serviceUserId, String welcomeWords) {
        if (welcomeWords == null || welcomeWords.trim().isEmpty()) {
            return false;
        }
        QueryWrapper<ChatServiceDialogueRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.and(w -> w.eq("user_id", serviceUserId).eq("to_user_id", chatUserId)
                .or().eq("user_id", chatUserId).eq("to_user_id", serviceUserId));
        return chatServiceDialogueRecordMapper.selectCount(wrapper) == 0;
    }

    private ChatServiceDialogueRecordEntity createWelcomeMessage(String appid, int chatUserId, ChatUserEntity chatUser, ChatServiceEntity service) {
        ChatServiceDialogueRecordEntity record = new ChatServiceDialogueRecordEntity();
        record.setAppid(appid);
        record.setUserId(Optional.ofNullable(service.getUserId()).orElse(0));
        record.setToUserId(chatUserId);
        record.setMsn(Optional.ofNullable(service.getWelcomeWords()).orElse(""));
        record.setMsnType(1);
        record.setOther("");
        record.setGuid(UUID.randomUUID().toString().replace("-", ""));
        record.setType(1);
        record.setIsTourist(chatUser.getIsTourist() != null ? chatUser.getIsTourist() : 0);
        record.setAddTime((int) (System.currentTimeMillis() / 1000));
        chatServiceDialogueRecordMapper.insert(record);
        return record;
    }

    private Map<String, Object> buildWelcomePayload(String appid, ChatServiceDialogueRecordEntity record, ChatServiceEntity service, ChatUserEntity chatUser) {
        List<Map<String, Object>> tidy = tidyChatRecords(appid, Collections.singletonList(record));
        Map<String, Object> payload = tidy.isEmpty()
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(tidy.get(0));
        int isTourist = chatUser.getIsTourist() != null ? chatUser.getIsTourist() : 0;
        payload.put("is_tourist", isTourist);

        Map<String, Object> recored = Collections.emptyMap();
        if (record.getUserId() != null && record.getUserId() > 0 && record.getToUserId() != null && record.getToUserId() > 0) {
            int unreadCount = getUnreadCount(appid, record.getUserId(), record.getToUserId());
            recored = saveConversationRecord(
                    appid,
                    record.getUserId(),
                    record.getToUserId(),
                    record.getMsn(),
                    record.getMsnType() != null ? record.getMsnType() : 1,
                    unreadCount,
                    isTourist,
                    Optional.ofNullable(service.getNickname()).orElse(""),
                    Optional.ofNullable(service.getAvatar()).orElse(""),
                    service.getOnline() != null ? service.getOnline() : 0,
                    service.getGroupId() != null ? service.getGroupId() : 0
            );
        }
        payload.put("recored", recored);
        return payload;
    }

    private List<ChatServiceDialogueRecordEntity> queryDialogueRecords(String appid, int chatUserId, int serviceUserId, int idTo, int limit) {
        QueryWrapper<ChatServiceDialogueRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.and(w -> w.eq("user_id", chatUserId).eq("to_user_id", serviceUserId)
                .or().eq("user_id", serviceUserId).eq("to_user_id", chatUserId));
        if (idTo > 0) {
            wrapper.lt("id", idTo);
        }
        wrapper.orderByDesc("id");
        wrapper.last("LIMIT " + limit);
        return chatServiceDialogueRecordMapper.selectList(wrapper);
    }

    private List<Map<String, Object>> tidyChatRecords(String appid, List<ChatServiceDialogueRecordEntity> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Integer> userIds = records.stream()
                .flatMap(record -> Stream.of(record.getUserId(), record.getToUserId()))
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .collect(Collectors.toSet());

        Map<Integer, UserProfile> profiles = chatCacheService.getUserProfiles(appid, userIds);

        Stream<ChatServiceDialogueRecordEntity> stream = records.size() >= PARALLEL_THRESHOLD
                ? records.parallelStream()
                : records.stream();

        return stream.map(record -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", record.getId());
            item.put("user_id", record.getUserId());
            item.put("to_user_id", record.getToUserId());
            item.put("msn", Optional.ofNullable(record.getMsn()).orElse(""));
            int msnType = record.getMsnType() != null ? record.getMsnType() : 1;
            item.put("msn_type", msnType);
            item.put("type", record.getType() != null ? record.getType() : 0);
            item.put("guid", Optional.ofNullable(record.getGuid()).orElse(""));
            item.put("is_tourist", record.getIsTourist() != null ? record.getIsTourist() : 0);
            item.put("other", parseOtherField(record.getOther()));
            item.put("_add_time", formatTimestamp(record.getAddTime()));
            item.put("add_time", record.getAddTime());
            item.put("time", formatTimestamp(record.getAddTime()));

            UserProfile senderProfile = profiles.get(record.getUserId());
            UserProfile receiverProfile = profiles.get(record.getToUserId());
            String nickname = resolveNickname(senderProfile, receiverProfile);
            item.put("nickname", nickname);
            String avatar = resolveAvatar(senderProfile, receiverProfile);
            item.put("avatar", avatar);

            if (msnType == 5 || msnType == 6) {
                Object other = item.get("other");
                if (!(other instanceof Map) && !(other instanceof List)) {
                    item.put("other", Collections.emptyMap());
                }
                item.put("orderInfo", Collections.emptyMap());
            }

            return item;
        }).collect(Collectors.toList());
    }

    private UserProfile getUserProfile(String appid, Integer userId, Map<Integer, UserProfile> cache) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return cache.computeIfAbsent(userId, id -> chatCacheService.getUserProfile(appid, id));
    }

    private Object parseOtherField(String other) {
        if (other == null || other.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(other);
            if (node.isObject()) {
                return OBJECT_MAPPER.convertValue(node, Map.class);
            }
            if (node.isArray()) {
                return OBJECT_MAPPER.convertValue(node, List.class);
            }
            if (node.isTextual()) {
                return node.asText();
            }
        } catch (Exception ignored) {
        }
        return other;
    }

    private String encodeOtherPayload(Object other) {
        if (other == null) {
            return "";
        }
        if (other instanceof String str) {
            return str.trim();
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(other);
        } catch (Exception ex) {
            return other.toString();
        }
    }

    private String sanitizeMessage(String message) {
        if (message == null) {
            return "";
        }
        String sanitized = message.replaceAll("<[^>]+>", "");
        sanitized = sanitized.replace("\n", "").replace("\r", "").replace("\t", "");
        sanitized = sanitized.replace("&nbsp;", " ");
        return sanitized.trim();
    }

    private String formatTimestamp(Integer epochSeconds) {
        if (epochSeconds == null || epochSeconds <= 0) {
            return "";
        }
        return FULL_TIME_FORMAT.format(Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.systemDefault()));
    }

    private String resolveNickname(UserProfile senderProfile, UserProfile receiverProfile) {
        if (senderProfile != null && !senderProfile.getNickname().isBlank()) {
            return applyVersionPrefix(senderProfile, senderProfile.getNickname());
        }
        if (receiverProfile != null && !receiverProfile.getNickname().isBlank()) {
            return applyVersionPrefix(receiverProfile, receiverProfile.getNickname());
        }
        return "";
    }

    private String resolveAvatar(UserProfile senderProfile, UserProfile receiverProfile) {
        if (senderProfile != null && senderProfile.getAvatar() != null && !senderProfile.getAvatar().isBlank()) {
            return senderProfile.getAvatar();
        }
        if (receiverProfile != null && receiverProfile.getAvatar() != null && !receiverProfile.getAvatar().isBlank()) {
            return receiverProfile.getAvatar();
        }
        return "";
    }

    private String applyVersionPrefix(UserProfile profile, String nickname) {
        String base = nickname == null ? "" : nickname;
        if (profile != null) {
            String version = profile.getVersion();
            if (version != null && !version.isBlank() && !"0".equals(version)) {
                return "[" + version + "]" + base;
            }
        }
        return base;
    }

    private int getUnreadCount(String appid, int senderUserId, int recipientUserId) {
        if (senderUserId <= 0 || recipientUserId <= 0) {
            return 0;
        }
        QueryWrapper<ChatServiceDialogueRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("user_id", senderUserId);
        wrapper.eq("to_user_id", recipientUserId);
        wrapper.eq("type", 0);
        Long count = chatServiceDialogueRecordMapper.selectCount(wrapper);
        return count != null ? count.intValue() : 0;
    }

    private int getTotalUnreadCount(String appid, int recipientUserId) {
        if (recipientUserId <= 0) {
            return 0;
        }
        QueryWrapper<ChatServiceDialogueRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("to_user_id", recipientUserId);
        wrapper.eq("type", 0);
        Long count = chatServiceDialogueRecordMapper.selectCount(wrapper);
        return count != null ? count.intValue() : 0;
    }

    /**
     * 保存会话记录到eb_chat_service_record表
     * 根据PHP实现: user_id=游客(消息发送者), to_user_id=客服(消息接收者)
     *
     * @param appid 租户ID
     * @param senderUserId 消息发送者ID（游客）
     * @param recipientUserId 消息接收者ID（客服）
     * @param message 消息内容
     * @param messageType 消息类型
     * @param unreadCount 未读数量
     * @param isTourist 是否游客
     * @param nickname 昵称
     * @param avatar 头像
     * @param online 在线状态
     * @param channelType 渠道类型
     * @return 会话记录Map
     */
    private Map<String, Object> saveConversationRecord(String appid,
                                                       int senderUserId,
                                                       int recipientUserId,
                                                       String message,
                                                       int messageType,
                                                       int unreadCount,
                                                       int isTourist,
                                                       String nickname,
                                                       String avatar,
                                                       int online,
                                                       int channelType) {
        if (senderUserId <= 0 || recipientUserId <= 0) {
            return Collections.emptyMap();
        }

        String summary = summarizeMessage(message, messageType);
        int now = (int) (System.currentTimeMillis() / 1000);
        Set<Integer> profileIds = new HashSet<>();
        profileIds.add(senderUserId);
        profileIds.add(recipientUserId);
        Map<Integer, UserProfile> profileCache = chatCacheService.getUserProfiles(appid, profileIds);

        // 根据PHP实现: user_id=游客, to_user_id=客服
        QueryWrapper<ChatServiceRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("user_id", senderUserId);      // senderUserId = 游客
        wrapper.eq("to_user_id", recipientUserId); // recipientUserId = 客服
        ChatServiceRecordEntity record = chatServiceRecordMapper.selectOne(wrapper);

        if (record == null) {
            record = new ChatServiceRecordEntity();
            record.setAppid(appid);
            record.setUserId(senderUserId);       // 游客ID
            record.setToUserId(recipientUserId);   // 客服ID
            record.setNickname(nickname);
            record.setAvatar(avatar);
            record.setIsTourist(isTourist);
            record.setType(channelType);
            record.setMsn(summary);
            record.setMessageType(messageType);
            record.setNum(unreadCount);
            record.setOnline(online);
            record.setAddTime(now);
            record.setUpdateTime(now);
            chatServiceRecordMapper.insert(record);
        } else {
            if (nickname != null && !nickname.isEmpty()) {
                record.setNickname(nickname);
            }
            if (avatar != null && !avatar.isEmpty()) {
                record.setAvatar(avatar);
            }
            record.setIsTourist(isTourist);
            record.setType(channelType);
            record.setMsn(summary);
            record.setMessageType(messageType);
            record.setNum(unreadCount);
            record.setOnline(online);
            record.setUpdateTime(now);
            chatServiceRecordMapper.updateById(record);
        }

        // 反向记录也需要更新（如果存在）
        QueryWrapper<ChatServiceRecordEntity> reverseWrapper = new QueryWrapper<>();
        reverseWrapper.eq("appid", appid);
        reverseWrapper.eq("user_id", recipientUserId);  // 客服作为user_id
        reverseWrapper.eq("to_user_id", senderUserId);  // 游客作为to_user_id
        ChatServiceRecordEntity reverse = chatServiceRecordMapper.selectOne(reverseWrapper);
        if (reverse != null) {
            reverse.setMsn(summary);
            reverse.setMessageType(messageType);
            reverse.setUpdateTime(now);
            chatServiceRecordMapper.updateById(reverse);
        }

        return convertRecordToMap(appid, record, profileCache);
    }

    private Map<String, Object> convertRecordToMap(String appid, ChatServiceRecordEntity record) {
        return convertRecordToMap(appid, record, new HashMap<>());
    }

    private Map<String, Object> convertRecordToMap(String appid, ChatServiceRecordEntity record, Map<Integer, UserProfile> cache) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", record.getId());
        map.put("user_id", record.getUserId());
        map.put("to_user_id", record.getToUserId());

        UserProfile senderProfile = getUserProfile(appid, record.getUserId(), cache);
        UserProfile receiverProfile = getUserProfile(appid, record.getToUserId(), cache);

        String nickname = record.getNickname();
        if (nickname == null || nickname.isBlank()) {
            nickname = senderProfile != null ? senderProfile.getNickname() : "";
            if (nickname.isBlank() && receiverProfile != null) {
                nickname = receiverProfile.getNickname();
            }
        }
        nickname = applyVersionPrefix(senderProfile != null ? senderProfile : receiverProfile, nickname);
        map.put("nickname", nickname);

        String avatar = record.getAvatar();
        if (avatar == null || avatar.isBlank()) {
            avatar = resolveAvatar(senderProfile, receiverProfile);
        }
        map.put("avatar", avatar != null ? avatar : "");

        map.put("message", record.getMsn());
        map.put("message_type", record.getMessageType());
        map.put("mssage_num", record.getNum());
        map.put("update_time", record.getUpdateTime());
        map.put("_update_time", formatUpdateTime(record.getUpdateTime()));
        map.put("online", record.getOnline());
        map.put("is_tourist", record.getIsTourist());
        map.put("type", record.getType());
        return map;
    }

    private String summarizeMessage(String message, int messageType) {
        if (messageType == 2) {
            return "[Emoji]";
        }
        if (messageType == 3) {
            return "[Image]";
        }
        if (messageType == 4) {
            return "[Voice]";
        }
        if (messageType == 5 || messageType == 6) {
            return "[Rich Media]";
        }
        return message == null ? "" : message;
    }

    private String formatUpdateTime(Integer epochSeconds) {
        if (epochSeconds == null || epochSeconds <= 0) {
            return "";
        }
        return RECORD_TIME_FORMAT.format(Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.systemDefault()));
    }

    private static class EnsureUserResult {
        private final ChatUserEntity user;
        private final boolean created;

        private EnsureUserResult(ChatUserEntity user, boolean created) {
            this.user = user;
            this.created = created;
        }
    }

    /**
     * 获取客服页面广告内容
     * GET /api/mobile/service/adv
     *
     * PHP Reference: Service.php::getKfAdv()
     *
     * 业务逻辑:
     * 1. 从缓存中获取'kf_adv'配置
     * 2. 返回广告内容
     *
     * @return 广告内容
     */
    public Map<String, Object> getKfAdv() {
        // TODO: 从CacheServices获取'kf_adv'配置
        // PHP: $cache->getDbCache('kf_adv', '');
        String content = "";  // 简化实现，实际应从配置表获取

        Map<String, Object> result = new HashMap<>();
        result.put("content", content);

        log.info("Retrieved agent banner content: content={}", content);
        return result;
    }

    /**
     * 获取缓存数据
     * GET /api/mobile/service/cache/:key
     *
     * PHP Reference: Service.php::getCache()
     *
     * 业务逻辑:
     * 1. 根据key从缓存中获取value
     * 2. 返回缓存值
     *
     * @param key 缓存键
     * @return 缓存值
     */
    public Map<String, Object> getCache(String key) {
        // TODO: 从CacheServices获取缓存
        // PHP: $cache->getDbCache($key, []);
        Object value = new HashMap<>();  // 简化实现，实际应从Redis或配置表获取

        Map<String, Object> result = new HashMap<>();
        result.put("value", value);

        log.info("Fetching cache entry: key={}", key);
        return result;
    }

    /**
     * 设置缓存数据
     * POST /api/mobile/service/cache
     *
     * PHP Reference: Service.php::setCache()
     *
     * 业务逻辑:
     * 1. 验证key必须存在
     * 2. 将value存入缓存（10分钟过期）
     * 3. 返回成功消息
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    @Transactional(rollbackFor = Exception.class)
    public void setCache(String key, Object value) {
        if (key == null || key.trim().isEmpty()) {
            throw new CrmChatException("key must exist");
        }

        // TODO: 使用CacheServices设置缓存
        // PHP: $cache->setDbCache($key, $value, 600);

        log.info("Setting cache entry: key={}, value={}", key, value);
    }

    /**
     * 图片上传
     * POST /api/mobile/service/upload
     *
     * PHP Reference: Service.php::upload()
     *
     * 业务逻辑:
     * 1. 验证filename参数
     * 2. 检查上传频率限制（每天最多500次）
     * 3. 上传到store/comment目录
     * 4. 保存附件记录到SystemAttachment表
     * 5. 返回文件名和URL
     *
     * @param file  上传文件
     * @param appid 租户ID
     * @return 上传结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> upload(MultipartFile file, String appid,Long userid) {
        if (file == null || file.isEmpty()) {
            throw new CrmChatException("Invalid parameter");
        }
        String fileUrl = fileService.uploadFile(file, appid, "kefu");
        int now = (int) (System.currentTimeMillis() / 1000);
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "upload_" + now;
        }
        long fileSize = file.getSize();
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }
        String completeUrl = fileUrl;
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            // 如果fileUrl不包含http协议，使用getFileUrl确保返回完整URL
            completeUrl = fileService.getFileUrl(fileUrl);
        }
        SystemAttachmentEntity attachment = new SystemAttachmentEntity();
        attachment.setName(originalFilename);
        attachment.setRealName(originalFilename);
        attachment.setAttDir(completeUrl);
        attachment.setSattDir(completeUrl);
        attachment.setAttSize(String.valueOf(fileSize));
        attachment.setAttType(contentType);
        attachment.setImageType(1);
        attachment.setModuleType(1);
        attachment.setPid(0);
        attachment.setTime(now);
        systemAttachmentMapper.insert(attachment);
        // TODO: 实现上传逻辑
        // 1. 检查上传频率限制
        // 2. 使用UploadService上传到store/comment
        // 3. 保存附件记录
        // 4. 返回文件信息

        Map<String, Object> result = new HashMap<>();
        result.put("name", originalFilename);
        result.put("url", completeUrl);
        result.put("att_id", attachment.getAttId());
        result.put("att_size", attachment.getAttSize());
        result.put("att_type", attachment.getAttType());

        log.info("Uploading image: appid={}, filename={}", appid, file.getOriginalFilename());
        return result;
    }

    /**
     * 获取客服图标配置
     * GET /api/mobile/service/config
     *
     * PHP Reference: Service.php::getKefuConfig()
     *
     * 业务逻辑:
     * 1. 获取kefu_icon_type配置（图标类型）
     * 2. 获取kefu_icon_url{type}配置（图标URL）
     * 3. 返回图标信息
     *
     * @return 客服图标配置
     */
    public Map<String, Object> getKefuConfig() {
        // TODO: 从系统配置表获取
        // PHP: sys_config('kefu_icon_type')
        // PHP: sys_config('kefu_icon_url' . $type)
        String type = "1";  // 默认类型
        String icon = "";   // 图标URL

        Map<String, Object> result = new HashMap<>();
        result.put("icon", icon);
        result.put("type", type);

        log.info("Fetching agent configuration: type={}, icon={}", type, icon);
        return result;
    }

    /**
     * 获取消息发送ID
     * GET /api/mobile/service/send_id
     *
     * PHP Reference: Service.php::getSendId()
     *
     * 业务逻辑:
     * 1. 使用Snowflake算法生成唯一ID（Java简化为UUID）
     * 2. 存入Redis
     * 3. 返回send_id
     *
     * @return 消息发送ID
     */
    public Map<String, Object> getSendId() {
        // PHP使用Snowflake算法生成分布式ID
        // Java简化版：使用UUID去掉横线
        String sendId = UUID.randomUUID().toString().replace("-", "");

        // TODO: 存入Redis
        // CacheService.redisHandler().set($sendId, 1);

        Map<String, Object> result = new HashMap<>();
        result.put("send_id", sendId);

        log.info("Generated message send ID: sendId={}", sendId);
        return result;
    }

    /**
     * 发送消息
     * POST /api/mobile/service/message
     *
     * PHP Reference: Service.php::sendMessage()
     *
     * 业务逻辑:
     * 1. 验证必填字段：to_user_id, msn, guid, user_id
     * 2. 验证不能和自己聊天
     * 3. 保存消息到ChatServiceDialogueRecord表
     * 4. TODO: 通过WebSocket推送消息
     * 5. 返回消息记录（附带guid）
     *
     * @param data   消息数据
     * @param appid  租户ID
     * @return 发送结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> sendMessage(Map<String, Object> data, String appid) {
        String guid = optionalString(data.get("guid"));
        if (guid.isEmpty()) {
            throw new CrmChatException("Message ID does not exist");
        }

        int userId = parseInt(data.get("user_id"), 0);
        if (userId <= 0) {
            throw new CrmChatException("Missing user_id");
        }

        int toUserId = parseInt(data.get("to_user_id"), 0);

        ChatUserEntity chatUser = chatUserMapper.selectOne(
                new QueryWrapper<ChatUserEntity>().eq("appid", appid).eq("id", userId)
        );
        if (chatUser == null) {
            throw new CrmChatException("User does not exist");
        }

        // 如果toUserId为0，自动分配客服（优先在线客服，如果没有则找历史客服）
        if (toUserId <= 0) {
            List<ChatServiceEntity> onlineServices = chatCacheService.getOnlineServices(appid);
            boolean hasOnlineService = !onlineServices.isEmpty();

            if (hasOnlineService) {
                // 有在线客服：优先分配在线客服
                // 尝试找到最近聊天的客服（如果在线）
                Integer recentServiceUserId = findLatestChatServiceUserId(appid, userId);
                if (recentServiceUserId != null && recentServiceUserId > 0) {
                    boolean isOnline = onlineServices.stream()
                            .anyMatch(s -> Objects.equals(s.getUserId(), recentServiceUserId));
                    if (isOnline) {
                        toUserId = recentServiceUserId;
                    }
                }

                // 如果还没有分配，随机选择一个在线客服
                if (toUserId <= 0) {
                    Set<Integer> onlineUserIds = onlineServices.stream()
                            .map(ChatServiceEntity::getUserId)
                            .filter(Objects::nonNull)
                            .filter(id -> id > 0)
                            .collect(Collectors.toSet());
                    if (!onlineUserIds.isEmpty()) {
                        toUserId = pickRandomUserId(onlineUserIds);
                    }
                }
            } else {
                // 没有在线客服：尝试找历史聊天的客服（允许离线发送消息）
                Integer recentServiceUserId = findLatestChatServiceUserId(appid, userId);
                if (recentServiceUserId != null && recentServiceUserId > 0) {
                    toUserId = recentServiceUserId;
                    log.info("No agent online; falling back to last agent: toUserId={}", toUserId);
                } else {
                    // 如果连历史记录也没有，选择系统中的任意一个客服（允许留言给离线客服）
                    Integer anyServiceUserId = findAnyActiveServiceUserId(appid);
                    if (anyServiceUserId != null && anyServiceUserId > 0) {
                        toUserId = anyServiceUserId;
                        log.info("No agent online and no history; using default agent: toUserId={}", toUserId);
                    } else {
                        // 系统中完全没有可用的客服
                        throw new CrmChatException("No customer service agents available in the system");
                    }
                }
            }
        }

        if (userId == toUserId) {
            throw new CrmChatException("Cannot chat with yourself");
        }

        ChatServiceEntity service = chatServiceMapper.selectOne(
                new QueryWrapper<ChatServiceEntity>()
                        .eq("appid", appid)
                        .eq("user_id", toUserId)
                        .eq("status", 1)
        );
        if (service == null) {
            throw new CrmChatException("Customer service representative does not exist or is not enabled");
        }

        int msnType = parseInt(data.get("msn_type"), 1);
        String msn = sanitizeMessage(optionalString(data.get("msn")));
        if (msnType == 1 && msn.isEmpty()) {
            throw new CrmChatException("Message content cannot be empty");
        }

        String otherJson = encodeOtherPayload(data.get("other"));
        int isTourist = chatUser.getIsTourist() != null ? chatUser.getIsTourist() : parseInt(data.get("is_tourist"), 0);
        int now = (int) (System.currentTimeMillis() / 1000);

        ChatServiceDialogueRecordEntity record = new ChatServiceDialogueRecordEntity();
        record.setAppid(appid);
        record.setUserId(userId);
        record.setToUserId(toUserId);
        record.setMsn(msn);
        record.setMsnType(msnType);
        record.setOther((msnType == 5 || msnType == 6) ? otherJson : "");
        record.setGuid(guid);
        record.setIsTourist(isTourist);
        record.setType(service.getOnline() != null && service.getOnline() == 1 ? 1 : 0);
        record.setAddTime(now);

        if (chatServiceDialogueRecordMapper.insert(record) <= 0) {
            throw new CrmChatException("Failed to send");
        }

        // ✅ 修改：为所有客服创建会话记录（实现客服协同）
        // 不再只为被分配的客服创建记录，而是为所有启用的客服创建记录
        saveConversationRecordForAllKefu(appid, userId, msn, msnType, isTourist, chatUser);

        List<Map<String, Object>> tidyMessages = tidyChatRecords(appid, Collections.singletonList(record));
        Map<String, Object> response = tidyMessages.isEmpty()
                ? new HashMap<>()
                : new LinkedHashMap<>(tidyMessages.get(0));

        // ⚠️ 注意：不再返回 recored 字段，因为每个客服的 recored 不同
        // 前端会通过 WebSocket 的 user_online 消息更新左侧列表
        response.put("guid", guid);
        response.put("nickname", Optional.ofNullable(chatUser.getNickname()).orElse(""));
        response.put("avatar", Optional.ofNullable(chatUser.getAvatar()).orElse(""));
        response.put("is_tourist", isTourist);

        int totalUnread = getTotalUnreadCount(appid, toUserId);

        // 不推送给游客自己，避免前端重复显示（前端HTTP API成功后已主动添加）
        // webSocketPushService.sendChat(appid, userId, response);

        // ⭐ FIX: 先广播用户上线消息,确保客服端左侧列表有该用户,再发送消息内容
        // 这样避免首次游客发消息时,客服端收到reply但左侧列表还没有该用户的竞态问题
        log.info("📡 [FIRST_VISITOR_FIX] Broadcasting user_online before reply: appid={}, userId={}, nickname={}",
                 appid, userId, chatUser.getNickname());
        webSocketPushService.broadcastUserStatus(appid, userId, 1,
            Optional.ofNullable(chatUser.getNickname()).orElse(""),
            Optional.ofNullable(chatUser.getAvatar()).orElse(""));

        // 短暂延迟(100ms),确保客服端先处理user_online消息,再处理reply消息
        // 这样客服端左侧用户列表会先更新,然后消息才到达,避免找不到对应会话
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Message ordering delay interrupted: {}", e.getMessage());
        }

        // ✅ 修改：发送消息给所有在线客服（实现客服协同）
        broadcastVisitorMessageToAllKefu(appid, userId, response);

        log.info("Message sent successfully: userId={}, toUserId={}, guid={}", userId, toUserId, guid);
        return response;
    }

    /**
     * 为所有客服创建会话记录（实现客服协同）
     *
     * @param appid 租户ID
     * @param visitorUserId 游客user_id
     * @param msn 消息内容
     * @param msnType 消息类型
     * @param isTourist 是否游客
     * @param chatUser 游客用户实体
     */
    private void saveConversationRecordForAllKefu(String appid, Integer visitorUserId, String msn,
                                                   int msnType, int isTourist, ChatUserEntity chatUser) {
        try {
            // 1. 查询同一个appid下的所有启用状态的客服
            QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("appid", appid);
            wrapper.eq("status", 1);  // 只查询启用状态的客服
            wrapper.isNotNull("user_id");  // 必须有user_id

            List<ChatServiceEntity> kefuList = chatServiceMapper.selectList(wrapper);

            log.info("📝 Creating conversation records for {} customer service agents (appid={}, visitor={})",
                kefuList.size(), appid, visitorUserId);

            // 2. 为每个客服创建会话记录
            int recordCount = 0;
            for (ChatServiceEntity kefu : kefuList) {
                Integer kefuUserId = kefu.getUserId();
                if (kefuUserId != null) {
                    // 计算该客服与游客之间的未读数
                    int unreadCount = getUnreadCount(appid, visitorUserId, kefuUserId);

                    // 保存会话记录
                    saveConversationRecord(
                        appid,
                        visitorUserId,  // 发送者：游客
                        kefuUserId,     // 接收者：客服
                        msn,
                        msnType,
                        unreadCount,
                        isTourist,
                        Optional.ofNullable(chatUser.getNickname()).orElse(""),
                        Optional.ofNullable(chatUser.getAvatar()).orElse(""),
                        chatUser.getOnline() != null ? chatUser.getOnline() : 1,  // ✅ 使用实际在线状态
                        chatUser.getType() != null ? chatUser.getType() : 0
                    );
                    recordCount++;
                    log.debug("📝 Created conversation record for kefu: {} (user_id={})", kefu.getNickname(), kefuUserId);
                }
            }

            log.info("✅ Successfully created {} conversation records", recordCount);

        } catch (Exception e) {
            // 创建记录失败不影响主流程，只记录日志
            log.error("❌ Failed to create conversation records: appid={}, visitor={}, error={}",
                appid, visitorUserId, e.getMessage(), e);
        }
    }

    /**
     * 广播游客消息给所有在线客服（实现客服协同）
     *
     * @param appid 租户ID
     * @param visitorUserId 游客user_id
     * @param payload 消息内容
     */
    private void broadcastVisitorMessageToAllKefu(String appid, Integer visitorUserId, Map<String, Object> payload) {
        try {
            // 1. 查询同一个appid下的所有启用状态的客服
            QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("appid", appid);
            wrapper.eq("status", 1);  // 只查询启用状态的客服
            wrapper.isNotNull("user_id");  // 必须有user_id

            List<ChatServiceEntity> kefuList = chatServiceMapper.selectList(wrapper);

            log.info("📡 Broadcasting visitor message to {} customer service agents (appid={}, visitor={})",
                kefuList.size(), appid, visitorUserId);

            // 2. 推送给每个在线客服（为每个客服查询对应的 recored）
            int broadcastCount = 0;
            for (ChatServiceEntity kefu : kefuList) {
                Integer kefuUserId = kefu.getUserId();
                if (kefuUserId != null) {
                    boolean isOnline = webSocketPushService.isOnline(appid, kefuUserId);
                    if (isOnline) {
                        // ✅ 为每个客服查询对应的会话记录
                        QueryWrapper<ChatServiceRecordEntity> recordWrapper = new QueryWrapper<>();
                        recordWrapper.eq("appid", appid);
                        recordWrapper.eq("user_id", visitorUserId);  // 游客
                        recordWrapper.eq("to_user_id", kefuUserId);  // 当前客服
                        ChatServiceRecordEntity record = chatServiceRecordMapper.selectOne(recordWrapper);

                        // 构建该客服专属的 payload
                        Map<String, Object> kefuPayload = new LinkedHashMap<>(payload);
                        if (record != null) {
                            Map<String, Object> recored = new HashMap<>();
                            recored.put("id", record.getId());  // ✅ 添加 id 字段（前端用于判断是否同一用户）
                            recored.put("user_id", record.getUserId());
                            recored.put("to_user_id", record.getToUserId());
                            recored.put("nickname", record.getNickname());
                            recored.put("avatar", record.getAvatar());
                            recored.put("is_tourist", record.getIsTourist());
                            recored.put("online", record.getOnline());
                            recored.put("type", record.getType());
                            recored.put("num", record.getNum());
                            recored.put("message", record.getMsn());
                            recored.put("message_type", record.getMessageType());
                            recored.put("add_time", record.getAddTime());
                            recored.put("update_time", record.getUpdateTime());
                            kefuPayload.put("recored", recored);
                        }

                        // 推送消息给客服
                        webSocketPushService.sendReply(appid, kefuUserId, kefuPayload);
                        broadcastCount++;
                        log.debug("📨 Broadcasted visitor message to kefu: {} (user_id={})", kefu.getNickname(), kefuUserId);
                    } else {
                        log.debug("⚠️ Kefu is offline, skipping: {} (user_id={})", kefu.getNickname(), kefuUserId);
                    }
                }
            }

            log.info("✅ Successfully broadcasted visitor message to {}/{} online customer service agents",
                broadcastCount, kefuList.size());

        } catch (Exception e) {
            // 广播失败不影响主流程，只记录日志
            log.error("❌ Failed to broadcast visitor message to customer service agents: appid={}, visitor={}, error={}",
                appid, visitorUserId, e.getMessage(), e);
        }
    }
}
