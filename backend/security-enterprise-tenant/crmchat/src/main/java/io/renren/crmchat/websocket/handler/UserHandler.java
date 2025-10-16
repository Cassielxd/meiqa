package io.renren.crmchat.websocket.handler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import io.renren.common.utils.SpringContextUtils;
import io.renren.crmchat.dao.ChatServiceRecordMapper;
import io.renren.crmchat.dao.ChatUserMapper;
import io.renren.crmchat.entity.ChatServiceRecordEntity;
import io.renren.crmchat.entity.ChatUserEntity;
import io.renren.crmchat.service.AdminApplicationService;
import io.renren.crmchat.service.ChatCacheService;
import io.renren.crmchat.service.common.TokenService;
import io.renren.crmchat.websocket.BaseHandler;
import io.renren.crmchat.websocket.ChatWebSocketServer;
import io.renren.crmchat.websocket.WebSocketPushService;
import io.renren.crmchat.websocket.WebSocketSessionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.websocket.Session;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class UserHandler implements BaseHandler {

    @Override
    public ChatWebSocketServer.SessionIdentity authenticate(Map<String, String> params) {
        String token = params.get("token");
        String appid = params.getOrDefault("appid", params.getOrDefault("app", ""));
        int formType = parseInt(params.get("form_type"), 0);
        Integer initialTarget = parseIntNullable(params.get("to_user_id"));

        // 验证token
        AdminApplicationService tokenService = SpringContextUtils.getBean(AdminApplicationService.class);
        Map result =  tokenService.parseToken(token,null);
        if (tokenService == null || result==null) {
            log.warn("WebSocket user handshake rejected: invalid token. params={}", params);
            return null;
        }

        Map appInfo = (Map) result.get("appInfo");
        Map userInfo = (Map) result.get("user");

        // 验证appInfo
        if (appInfo == null || appInfo.get("appid") == null) {
            log.warn("WebSocket user handshake rejected: appInfo missing. params={}", params);
            return null;
        }

        appid = appInfo.get("appid").toString();

        if (appid == null || appid.isBlank()) {
            log.warn("WebSocket user handshake rejected: token missing appid. params={}", params);
            return null;
        }

        // 对应PHP: !isset($application['user']) - token中没有用户信息
        if (userInfo == null || userInfo.get("id") == null) {
            log.info("Token does not contain user info, allowing connection. appid={}", appid);
            // 允许连接，使用userId=0占位，等待user消息更新为真实ID
            // 对应PHP: $this->nowRoom->add($fd, $data['data']['appid'] ?? '', 0);
            return new ChatWebSocketServer.SessionIdentity(
                    appid,
                    0,  // 使用0作为占位符，而不是随机临时ID
                    "user",
                    formType,
                    true,  // 游客
                    initialTarget,
                    null,
                    0  // 离线状态，对应PHP login返回中没有uid字段
            );
        }

        Long tokenUserId = Long.parseLong(userInfo.get("id").toString());

        // 查找用户信息（对应PHP: isset($application['user'])）
        ChatUserMapper userMapper = SpringContextUtils.getBean(ChatUserMapper.class);
        if (userMapper == null) {
            log.error("ChatUserMapper bean not available");
            return null;
        }

        ChatUserEntity chatUser = userMapper.selectOne(new QueryWrapper<ChatUserEntity>()
                .eq("id", tokenUserId.intValue())
                .eq("appid", appid));

        // PHP逻辑：用户不存在时也允许连接，等待后续user消息创建用户
        if (chatUser == null) {
            log.info("New user connecting, will create user on 'user' message: tokenUserId={}, appid={}", tokenUserId, appid);
            // 允许连接，使用userId=0占位，等待user消息创建并更新为真实ID
            return new ChatWebSocketServer.SessionIdentity(
                    appid,
                    0,  // 使用0作为占位符
                    "user",
                    formType,
                    true,  // 临时标记为游客
                    initialTarget,
                    null,
                    0  // 离线状态
            );
        }

        String userAppid = Optional.ofNullable(chatUser.getAppid()).orElse("");
        if (!userAppid.isEmpty() && !appid.equals(userAppid)) {
            log.warn("WebSocket user handshake rejected: appid mismatch. expected={}, actual={}", userAppid, appid);
            return null;
        }

        // 缓存用户信息
        ChatCacheService cacheService = SpringContextUtils.getBean(ChatCacheService.class);
        if (cacheService != null) {
            cacheService.cacheUser(chatUser);
        }

        boolean tourist = chatUser.getIsTourist() != null && chatUser.getIsTourist() == 1;
        int inferredFormType = formType != 0 ? formType : (chatUser.getType() != null ? chatUser.getType() : 0);
        int online = chatUser.getOnline() != null ? chatUser.getOnline() : 0;

        return new ChatWebSocketServer.SessionIdentity(
                appid,
                chatUser.getId(),
                "user",
                inferredFormType,
                tourist,
                initialTarget,
                null,
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

    /**
     * 处理WebSocket "user"消息
     * 对应PHP: UserHandler::user()
     *
     * 业务逻辑:
     * 1. 创建/更新用户信息
     * 2. 建立与客服的聊天关系
     * 3. 更新在线状态
     * 4. 清空未读消息计数
     * 5. 广播用户上线消息给所有客服
     */
    public void handleUserMessage(Session session, JsonNode dataNode) {
        WebSocketSessionRegistry.SessionHolder holder = WebSocketSessionRegistry.get(session);
        if (holder == null) {
            log.warn("SessionHolder not found for user message");
            return;
        }

        String appId = holder.getAppid();
        Integer currentUserId = holder.getUserId();

        log.info("Processing user message: appid={}, userId={}", appId, currentUserId);

        // 1. 解析前端发送的用户信息
        Integer toUserId = parseIntNullable(dataNode.path("to_user_id").asText(null));
        Integer uid = parseIntNullable(dataNode.path("uid").asText(null));
        String nickname = dataNode.path("nickname").asText("");
        String avatar = dataNode.path("avatar").asText("");
        String phone = dataNode.path("phone").asText("");
        String openid = dataNode.path("openid").asText("");
        Integer type = parseIntNullable(dataNode.path("type").asText("0"));

        try {
            ChatUserMapper userMapper = SpringContextUtils.getBean(ChatUserMapper.class);
            ChatServiceRecordMapper recordMapper = SpringContextUtils.getBean(ChatServiceRecordMapper.class);
            WebSocketPushService pushService = SpringContextUtils.getBean(WebSocketPushService.class);
            ChatCacheService cacheService = SpringContextUtils.getBean(ChatCacheService.class);

            if (userMapper == null) {
                log.error("ChatUserMapper not available");
                return;
            }
            QueryWrapper<ChatUserEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("uid", uid);
            wrapper.eq("appid", appId);
            // 2. 创建或更新用户信息 (对应PHP第96-102行: createUser)
            ChatUserEntity chatUser = userMapper.selectOne(wrapper);
            boolean isNewUser = false;

            if (chatUser == null) {
                // 用户不存在，创建新用户
                chatUser = new ChatUserEntity();
                // 注意: 不设置id，让数据库自动生成
                chatUser.setAppid(appId);
                chatUser.setUid(uid != null ? uid : currentUserId);
                chatUser.setNickname(nickname != null && !nickname.isEmpty() ? nickname : "Guest" + uid);
                chatUser.setAvatar(avatar != null && !avatar.isEmpty() ? avatar : "");
                chatUser.setPhone(phone != null ? phone : "");
                chatUser.setOpenid(openid != null ? openid : "");
                chatUser.setType(type != null ? type : 0);
                chatUser.setIsTourist(uid == null || uid <= 0 ? 1 : 0);
                chatUser.setGroupId(0);
                chatUser.setIsDelete(0);
                chatUser.setIsKefu(0);
                chatUser.setOnline(1);
                chatUser.setVersion("0");
                chatUser.setRemarkNickname("");
                chatUser.setRemarks("");
                chatUser.setCreateTime(LocalDateTime.now());
                chatUser.setUpdateTime(LocalDateTime.now());

                userMapper.insert(chatUser);
                isNewUser = true;
                log.info("Created new user: userId={}, uid={}", chatUser.getId(), uid);
            } else {
                // 用户已存在，更新用户信息
                boolean needUpdate = false;
                if (nickname != null && !nickname.isEmpty() && !nickname.equals(chatUser.getNickname())) {
                    chatUser.setNickname(nickname);
                    needUpdate = true;
                }
                if (avatar != null && !avatar.isEmpty() && !avatar.equals(chatUser.getAvatar())) {
                    chatUser.setAvatar(avatar);
                    needUpdate = true;
                }
                if (phone != null && !phone.isEmpty() && !phone.equals(chatUser.getPhone())) {
                    chatUser.setPhone(phone);
                    needUpdate = true;
                }
                if (openid != null && !openid.isEmpty() && !openid.equals(chatUser.getOpenid())) {
                    chatUser.setOpenid(openid);
                    needUpdate = true;
                }
                if (type != null && !type.equals(chatUser.getType())) {
                    chatUser.setType(type);
                    needUpdate = true;
                }
                if (uid != null && !uid.equals(chatUser.getUid())) {
                    chatUser.setUid(uid);
                    needUpdate = true;
                }

                if (needUpdate) {
                    chatUser.setUpdateTime(LocalDateTime.now());
                    userMapper.updateById(chatUser);
                    log.info("Updated user info: userId={}", chatUser.getId());
                }
            }

            // 关键修复: 更新session的userId为真实的数据库ID
            // 对应PHP: $this->room->update($this->fd, 'user_id', $userInfo['id'])
            int realUserId = chatUser.getId();
            if (currentUserId != realUserId) {
                log.info("Updating session userId from temporary {} to real database ID {}", currentUserId, realUserId);
                WebSocketSessionRegistry.updateUserId(session, realUserId);
                currentUserId = realUserId;  // 更新局部变量，用于后续逻辑
            }

            // 3. 更新在线状态 (对应PHP第106-111行)
            if (recordMapper != null) {
                int now = (int) (System.currentTimeMillis() / 1000);

                // 更新chat_service_record表中的在线状态
                UpdateWrapper<ChatServiceRecordEntity> wrapper1 = new UpdateWrapper<>();
                wrapper1.eq("appid", appId)
                        .eq("user_id", currentUserId);
                ChatServiceRecordEntity update = new ChatServiceRecordEntity();
                update.setOnline(1);
                update.setUpdateTime(now);
                recordMapper.update(update, wrapper1);
            }

            // 4. 建立聊天关系并清空未读消息 (对应PHP第113-127行)
            if (toUserId != null && toUserId > 0 && recordMapper != null) {
                int now = (int) (System.currentTimeMillis() / 1000);

                // 查找或创建与客服的聊天记录
                QueryWrapper<ChatServiceRecordEntity> query = new QueryWrapper<>();
                query.eq("appid", appId)
                        .eq("user_id", currentUserId)
                        .eq("to_user_id", toUserId);

                ChatServiceRecordEntity record = recordMapper.selectOne(query);
                if (record != null) {
                    // 清空未读消息计数
                    record.setNum(0);
                    record.setUpdateTime(now);
                    recordMapper.updateById(record);
                    log.info("Cleared unread messages for user-kefu pair: userId={}, toUserId={}", currentUserId, toUserId);
                }

                // 更新当前聊天对象
                WebSocketSessionRegistry.updateCurrentTarget(session, toUserId);
            }

            // 5. 缓存用户信息
            if (cacheService != null && chatUser != null) {
                cacheService.invalidateUserProfile(appId, currentUserId);
                cacheService.cacheUser(chatUser);
            }

            // 6. 广播用户上线消息给所有客服 (对应PHP第130-133行)
            // 获取所有客服的session (对应PHP: $this->room->getKefuRoomAll())
            Set<WebSocketSessionRegistry.SessionHolder> kefuSessions =
                    WebSocketSessionRegistry.getSessionsByType(appId, "kefu");

            if (!kefuSessions.isEmpty()) {
                Map<String, Object> onlineData = new HashMap<>();
                onlineData.put("user_id", currentUserId);
                onlineData.put("online", 1);
                onlineData.put("nickname", nickname != null ? nickname : "");
                onlineData.put("avatar", avatar != null ? avatar : "");

                // 向每个客服发送user_online消息
                for (WebSocketSessionRegistry.SessionHolder kefuHolder : kefuSessions) {
                    try {
                        ChatWebSocketServer.sendEnvelope(kefuHolder.getSession(), "user_online", onlineData);
                    } catch (Exception e) {
                        log.warn("Failed to send user_online to kefu session: {}", kefuHolder.getUserId(), e);
                    }
                }
                log.info("Broadcasted user online status to {} kefu sessions: userId={}", kefuSessions.size(), currentUserId);
            } else {
                log.warn("No kefu sessions found for broadcasting user online: appId={}", appId);
            }

            log.info("User message processed successfully: userId={}, toUserId={}", currentUserId, toUserId);

        } catch (Exception ex) {
            log.error("Failed to handle user message", ex);
        }
    }
}
