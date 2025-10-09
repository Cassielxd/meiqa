package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.*;
import io.renren.crmchat.entity.*;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.security.TenantGuard;
import io.renren.crmchat.security.TenantQueryHelper;
import io.renren.crmchat.websocket.WebSocketPushService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kefu Message Service - 客服消息发送服务
 * PHP Reference: /app/controller/kefu/Service.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getSendId(): 获取消息唯一ID（使用Snowflake算法，简化版使用UUID）
 * 2. sendMessage(): 发送消息（保存记录+WebSocket推送）
 * 3. setLoginCode(): 设置扫码登录code
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class KefuMessageService {

    private final ChatServiceDialogueRecordMapper chatServiceDialogueRecordMapper;
    private final ChatServiceRecordMapper chatServiceRecordMapper;
    private final ChatUserMapper chatUserMapper;
    private final ChatServiceMapper chatServiceMapper;
    private final KefuLoginCodeManager kefuLoginCodeManager;
    private final WebSocketPushService webSocketPushService;
    private final AutoBadgeService autoBadgeService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 获取消息发送ID
     * GET /api/kefu/message/send_id
     *
     * PHP Reference: Service.php::getSendId() -> ChatServiceDialogueRecordServices::getSendId()
     *
     * 业务逻辑:
     * 1. PHP使用Snowflake算法生成分布式唯一ID
     * 2. Java简化实现：使用UUID或时间戳+随机数
     * 3. 将ID存入Redis（TODO: 需要Redis支持）
     * 4. 返回send_id供客户端使用
     *
     * @return send_id
     */
    public Map<String, Object> getSendId() {
        // PHP中使用Snowflake算法生成32位或64位ID
        // Java简化版：使用UUID去掉横线，或使用时间戳+随机数
        String sendId = UUID.randomUUID().toString().replace("-", "");

        // TODO: 将sendId存入Redis
        // CacheService.redisHandler().set(sendId, 1);

        Map<String, Object> result = new HashMap<>();
        result.put("send_id", sendId);

        log.info("生成消息发送ID: sendId={}", sendId);
        return result;
    }

    /**
     * 发送消息
     * POST /api/kefu/message/send
     *
     * PHP Reference: Service.php::sendMessage() -> KefuServices::sendMessage()
     *
     * 业务逻辑:
     * 1. 验证必填字段：to_user_id, msn（消息内容）, guid（消息唯一ID）
     * 2. 验证不能和自己聊天
     * 3. 保存消息到ChatServiceDialogueRecord表
     * 4. 更新ChatServiceRecord表（最新消息、未读数）
     * 5. 通过WebSocket推送消息给目标用户并刷新徽章
     * 6. 返回消息记录（附带guid）
     *
     * @param data         消息数据
     * @param kefuUserId   当前客服的user_id
     * @return 发送结果（包含消息记录和guid）
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> sendMessage(Map<String, Object> data, Integer kefuUserId, String appid) {
        // 1. 验证必填字段
        if (!data.containsKey("to_user_id") || data.get("to_user_id") == null) {
            throw new CrmChatException("User does not exist");
        }
        if (!data.containsKey("msn") || data.get("msn") == null || data.get("msn").toString().trim().isEmpty()) {
            throw new CrmChatException("Message content cannot be empty");
        }
        if (!data.containsKey("guid") || data.get("guid") == null || data.get("guid").toString().trim().isEmpty()) {
            throw new CrmChatException("Message ID does not exist");
        }

        Integer toUserId = Integer.parseInt(data.get("to_user_id").toString());
        String msn = data.get("msn").toString();
        String guid = data.get("guid").toString();

        // 2. 验证不能和自己聊天
        if (toUserId.equals(kefuUserId)) {
            throw new CrmChatException("Cannot chat with yourself");
        }

        Integer msnType = 1;
        if (data.containsKey("msn_type") && data.get("msn_type") != null) {
            msnType = Integer.parseInt(data.get("msn_type").toString());
        }

        String other = "";
        if (data.containsKey("other") && data.get("other") != null) {
            other = data.get("other").toString();
        }

        Integer isTourist = null;
        if (data.containsKey("is_tourist") && data.get("is_tourist") != null) {
            isTourist = Integer.parseInt(data.get("is_tourist").toString());
        }

        ChatUserEntity toUser = chatUserMapper.selectById(toUserId);
        if (toUser == null) {
            throw new CrmChatException("User does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(toUser.getAppid(), "User does not exist");
        if (isTourist == null && toUser.getIsTourist() != null) {
            isTourist = toUser.getIsTourist();
        }
        if (isTourist == null) {
            isTourist = 0;
        }

        ChatServiceEntity kefu = findKefuByUserId(appid, kefuUserId);
        if (kefu == null) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        boolean recipientOnline = webSocketPushService.isOnline(appid, toUserId);

        ChatServiceDialogueRecordEntity record = new ChatServiceDialogueRecordEntity();
        record.setUserId(kefuUserId);
        record.setToUserId(toUserId);
        record.setMsn(msn);
        record.setMsnType(msnType);
        record.setOther(other);
        record.setGuid(guid);
        record.setIsTourist(isTourist);
        record.setType(recipientOnline ? 1 : 0);
        record.setAddTime((int) (System.currentTimeMillis() / 1000));
        record.setAppid(appid);

        int result = chatServiceDialogueRecordMapper.insert(record);
        if (result <= 0) {
            throw new CrmChatException("Failed to send");
        }

        String summaryMessage = summarizeMessage(msn, msnType);
        int userOnlineFlag = toUser.getOnline() == null ? 0 : toUser.getOnline();

        // 根据PHP实现: eb_chat_service_record表中 user_id=游客, to_user_id=客服
        // PRIMARY record: user_id=visitor, to_user_id=service (游客的会话记录，显示与哪个客服聊天)
        upsertRecord(appid, toUserId, kefuUserId, summaryMessage, msnType, 0,
                toUser.getIsTourist() == null ? 0 : toUser.getIsTourist(),
                userOnlineFlag, toUser.getNickname(), toUser.getAvatar());

        int pairUnread = countUnreadForPair(appid, toUserId, kefuUserId);
        // REVERSE record: user_id=service, to_user_id=visitor (客服的会话记录，显示与哪个游客聊天)
        upsertRecord(appid, kefuUserId, toUserId, summaryMessage, msnType, pairUnread,
                isTourist, recipientOnline ? 1 : 0, kefu.getNickname(), kefu.getAvatar());

        ChatServiceRecordEntity receiverRecord = fetchRecord(appid, toUserId, kefuUserId);
        Map<String, Object> recoredMap = receiverRecord != null ? buildRecordMap(receiverRecord) : Collections.emptyMap();

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", record.getId());
        payload.put("user_id", record.getUserId());
        payload.put("appid", appid);
        payload.put("to_user_id", record.getToUserId());
        payload.put("msn", record.getMsn());
        payload.put("msn_type", record.getMsnType());
        payload.put("type", record.getType());
        payload.put("other", record.getOther());
        payload.put("guid", guid);
        payload.put("add_time", record.getAddTime());
        payload.put("nickname", kefu.getNickname());
        payload.put("avatar", kefu.getAvatar());
        payload.put("recored", recoredMap);

        if (recipientOnline) {
            webSocketPushService.sendReply(appid, toUserId, payload);
        } else {
            int totalUnread = countTotalUnread(appid, toUserId);
            webSocketPushService.sendMessageNum(appid, toUserId, kefuUserId, pairUnread, totalUnread, recoredMap);
        }

        Map<String, Object> response = new HashMap<>(payload);
        autoBadgeService.dispatch(kefuUserId, toUserId, appid);

        log.info("发送消息成功: kefuUserId={}, toUserId={}, guid={}", kefuUserId, toUserId, guid);
        return response;
    }

    private ChatServiceRecordEntity upsertRecord(String appid,
                                                 int ownerUserId,
                                                 int peerUserId,
                                                 String message,
                                                 int messageType,
                                                 int unreadNum,
                                                 int isTourist,
                                                 int onlineFlag,
                                                 String nickname,
                                                 String avatar) {
        QueryWrapper<ChatServiceRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("user_id", ownerUserId);
        wrapper.eq("to_user_id", peerUserId);

        ChatServiceRecordEntity record = chatServiceRecordMapper.selectOne(wrapper);
        int now = (int) (System.currentTimeMillis() / 1000);

        if (record == null) {
            record = new ChatServiceRecordEntity();
            record.setAppid(appid);
            record.setUserId(ownerUserId);
            record.setToUserId(peerUserId);
            record.setAddTime(now);
        }

        if (nickname != null && !nickname.isBlank()) {
            record.setNickname(nickname);
        }
        if (avatar != null && !avatar.isBlank()) {
            record.setAvatar(avatar);
        }

        record.setMsn(message);
        record.setMessageType(messageType);
        record.setNum(unreadNum);
        record.setUpdateTime(now);
        record.setIsTourist(isTourist);
        record.setOnline(onlineFlag);
        record.setType(record.getType() == null ? 0 : record.getType());

        if (record.getId() == null) {
            chatServiceRecordMapper.insert(record);
        } else {
            chatServiceRecordMapper.updateById(record);
        }
        return record;
    }

    private ChatServiceRecordEntity fetchRecord(String appid, int ownerUserId, int peerUserId) {
        QueryWrapper<ChatServiceRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("user_id", ownerUserId);
        wrapper.eq("to_user_id", peerUserId);
        return chatServiceRecordMapper.selectOne(wrapper);
    }

    private ChatServiceEntity findKefuByUserId(String appid, Integer kefuUserId) {
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("user_id", kefuUserId);
        return chatServiceMapper.selectOne(wrapper);
    }

    private String summarizeMessage(String message, int messageType) {
        if (message == null) {
            return "";
        }
        return switch (messageType) {
            case 2 -> "[Emoji]";
            case 3 -> "[Image]";
            case 4 -> "[Voice]";
            case 5, 6 -> "[Rich Media]";
            default -> message;
        };
    }

    private int countUnreadForPair(String appid, int receiverUserId, int senderUserId) {
        QueryWrapper<ChatServiceDialogueRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("to_user_id", receiverUserId);
        wrapper.eq("user_id", senderUserId);
        wrapper.eq("type", 0);
        return Math.toIntExact(chatServiceDialogueRecordMapper.selectCount(wrapper));
    }

    private int countTotalUnread(String appid, int receiverUserId) {
        QueryWrapper<ChatServiceDialogueRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("to_user_id", receiverUserId);
        wrapper.eq("type", 0);
        return Math.toIntExact(chatServiceDialogueRecordMapper.selectCount(wrapper));
    }

    private Map<String, Object> buildRecordMap(ChatServiceRecordEntity record) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", record.getId());
        map.put("user_id", record.getUserId());
        map.put("to_user_id", record.getToUserId());
        map.put("message", record.getMsn());
        map.put("message_type", record.getMessageType());
        map.put("mssage_num", record.getNum());
        map.put("nickname", decorateNickname(record));
        map.put("avatar", record.getAvatar());
        map.put("is_tourist", record.getIsTourist());
        map.put("online", record.getOnline());
        map.put("update_time", record.getUpdateTime());
        map.put("_update_time", formatTimestamp(record.getUpdateTime()));
        return map;
    }

    private String decorateNickname(ChatServiceRecordEntity record) {
        ChatUserEntity user = chatUserMapper.selectById(record.getUserId());
        String nickname = record.getNickname();
        if (user != null && user.getVersion() != null && !user.getVersion().isBlank()) {
            return "[" + user.getVersion() + "]" + (nickname == null ? "" : nickname);
        }
        return nickname;
    }

    private String formatTimestamp(Integer epochSeconds) {
        if (epochSeconds == null || epochSeconds <= 0) {
            return "";
        }
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
        return TIME_FORMATTER.format(time);
    }

    /**
     * 设置扫码登录code
     * POST /api/kefu/login/code
     *
     * PHP Reference: Service.php::setLoginCode()
     *
     * 业务逻辑:
     * 1. 客户端扫码后，将code发送到服务端
     * 2. 验证code在Redis中存在且未过期
     * 3. 将客服的uniqid字段设置为code
     * 4. 更新Redis中的code状态为"0"（已使用）
     * 5. 前端轮询检测code状态，完成登录
     *
     * @param code  扫码获取的登录code
     * @param kefuId 当前客服ID
     * @return 登录结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> setLoginCode(String code, Integer kefuUserId, String currentAppid) {
        if (code == null || code.trim().isEmpty()) {
            throw new CrmChatException("Login code does not exist");
        }

        KefuLoginCodeManager.LoginCode loginCode = kefuLoginCodeManager.get(code);
        if (loginCode == null) {
            throw new CrmChatException("QR code has expired, please scan again");
        }

        // 获取客服信息
        QueryWrapper<ChatServiceEntity> kefuWrapper = new QueryWrapper<>();
        kefuWrapper.eq("user_id", kefuUserId);
        TenantQueryHelper.applyAppid(kefuWrapper, currentAppid);
        ChatServiceEntity kefu = chatServiceMapper.selectOne(kefuWrapper);
        if (kefu == null) {
            throw new CrmChatException("You are not a customer service agent and cannot login");
        }

        // 设置uniqid为code
        kefu.setUniqid(code);
        kefu.setUpdateTime((int) (System.currentTimeMillis() / 1000));

        int result = chatServiceMapper.updateById(kefu);
        if (result <= 0) {
            throw new CrmChatException("Login failed");
        }

        kefuLoginCodeManager.markScanned(code, kefu.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");

        log.info("设置登录code成功: kefuUserId={}, code={}", kefuUserId, code);

        return response;
    }
}
