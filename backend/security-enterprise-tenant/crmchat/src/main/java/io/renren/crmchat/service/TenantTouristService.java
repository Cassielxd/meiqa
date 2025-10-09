package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatServiceMapper;
import io.renren.crmchat.dao.ChatUserMapper;
import io.renren.crmchat.entity.ChatServiceEntity;
import io.renren.crmchat.entity.ChatUserEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Tenant 游客访问服务
 * PHP Reference: /app/services/chat/ChatServiceServices.php::getRecord()
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getOnlineServiceForTourist(): 游客获取在线客服
 *    - 查找/创建游客用户
 *    - 分配在线客服（优先级：指定客服 > 随机客服 > 上次聊天客服 > 随机在线客服）
 *    - 返回客服信息和聊天记录
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantTouristService {

    private final ChatUserMapper chatUserMapper;
    private final ChatServiceMapper chatServiceMapper;

    /**
     * 游客获取在线客服
     *
     * PHP Reference: ChatServiceServices.php::getRecord() (lines 190-292)
     *
     * 业务逻辑:
     * 1. 查找用户，没有自动生成游客
     * 2. 分配客服（优先级：toUserId > kefuId > kefuRand > 转接客服 > 上次客服 > 随机在线客服）
     * 3. 返回客服信息和聊天记录
     *
     * @param appid      租户appid
     * @param uid        用户UID
     * @param nickname   用户昵称
     * @param avatar     用户头像
     * @param phone      用户手机号
     * @param sex        用户性别
     * @param openid     用户openid
     * @param type       用户类型
     * @param idTo       翻页ID
     * @param limit      每页条数
     * @param toUserId   指定客服ID
     * @param cookieUid  Cookie中的UID
     * @param kefuId     客服ID
     * @param kefuRand   随机客服ID
     * @return 客服信息和聊天记录
     */
    public Map<String, Object> getOnlineServiceForTourist(
            String appid, Integer uid, String nickname, String avatar, String phone,
            Integer sex, String openid, Integer type, Integer idTo, Integer limit,
            Integer toUserId, Integer cookieUid, Integer kefuId, Integer kefuRand) {

        // 1. 查找用户，没有自动生成游客 (PHP lines 196-218)
        ChatUserEntity userInfo = getOrCreateTouristUser(appid, uid, nickname, avatar, phone, sex, openid, type);

        // 2. 获取当前分配客服 (PHP lines 220-270)
        Integer assignedServiceUserId = assignCustomerService(appid, toUserId, kefuId, kefuRand, userInfo.getId());

        if (assignedServiceUserId == null || assignedServiceUserId <= 0) {
            throw new RuntimeException("No customer service agents are currently online, please try again later");
        }

        // 3. 组合数据 (PHP lines 272-292)
        ChatServiceEntity toUserInfo = getServiceInfo(assignedServiceUserId);

        Map<String, Object> result = new HashMap<>();
        result.put("serviceList", new ArrayList<>());  // 临时返回空聊天记录
        result.put("to_user_id", assignedServiceUserId);
        result.put("is_tourist", userInfo.getIsTourist());
        result.put("uid", userInfo.getUid());
        result.put("user_id", userInfo.getId());
        result.put("nickname", userInfo.getNickname());
        result.put("avatar", userInfo.getAvatar());
        result.put("to_user_nickname", toUserInfo.getNickname());
        result.put("to_user_avatar", toUserInfo.getAvatar());
        result.put("welcome", false);  // 临时返回false，后续实现欢迎语逻辑

        return result;
    }

    /**
     * 获取或创建游客用户
     * PHP Reference: ChatServiceServices.php::getRecord() lines 196-218
     */
    private ChatUserEntity getOrCreateTouristUser(
            String appid, Integer uid, String nickname, String avatar,
            String phone, Integer sex, String openid, Integer type) {

        ChatUserEntity userInfo = null;

        // 查找用户
        if (uid != null && uid > 0) {
            QueryWrapper<ChatUserEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("uid", uid);
            wrapper.eq("appid", appid);
            userInfo = chatUserMapper.selectOne(wrapper);
        }

        // 没有找到，自动生成游客
        if (userInfo == null) {
            userInfo = new ChatUserEntity();
            userInfo.setUid(uid != null && uid > 0 ? uid : generateRandomUid());
            userInfo.setAppid(appid);
            userInfo.setNickname(nickname != null && !nickname.isEmpty() ? nickname : "游客" + System.currentTimeMillis());
            userInfo.setAvatar(avatar != null && !avatar.isEmpty() ? avatar : "");
            userInfo.setPhone(phone != null && !phone.isEmpty() ? phone : "");
            userInfo.setSex(sex != null ? sex : 0);
            userInfo.setOpenid(openid != null && !openid.isEmpty() ? openid : "");
            userInfo.setType(type != null ? type : 0);
            userInfo.setIsTourist(1);  // 标记为游客
            userInfo.setGroupId(0);
            userInfo.setIsDelete(0);

            LocalDateTime now = LocalDateTime.now();
            userInfo.setCreateTime(now);
            userInfo.setUpdateTime(now);

            chatUserMapper.insert(userInfo);
            log.info("Created tourist user: uid={}, id={}, appid={}", userInfo.getUid(), userInfo.getId(), appid);
        } else {
            // 更新用户信息（如果有变化）
            boolean needUpdate = false;
            if (nickname != null && !nickname.isEmpty() && !nickname.equals(userInfo.getNickname())) {
                userInfo.setNickname(nickname);
                needUpdate = true;
            }
            if (avatar != null && !avatar.isEmpty() && !avatar.equals(userInfo.getAvatar())) {
                userInfo.setAvatar(avatar);
                needUpdate = true;
            }
            if (needUpdate) {
                userInfo.setUpdateTime(LocalDateTime.now());
                chatUserMapper.updateById(userInfo);
            }
        }

        return userInfo;
    }

    /**
     * 分配客服
     * PHP Reference: ChatServiceServices.php::getRecord() lines 220-270
     *
     * 优先级:
     * 1. toUserId（指定客服ID）
     * 2. kefuId（客服ID）
     * 3. kefuRand（随机客服ID - 暂不实现）
     * 4. 转接客服（暂不实现）
     * 5. 上次聊天客服（暂不实现）
     * 6. 随机在线客服
     */
    private Integer assignCustomerService(String appid, Integer toUserId, Integer kefuId, Integer kefuRand, Integer userId) {

        // Priority 1: Check if toUserId is valid (PHP lines 220-221)
        if (toUserId != null && toUserId > 0) {
            QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("appid", appid);
            wrapper.eq("status", 1);
            wrapper.eq("user_id", toUserId);
            Long count = chatServiceMapper.selectCount(wrapper);
            if (count > 0) {
                return toUserId;
            }
        }

        // Priority 2: Check if kefuId is valid (PHP lines 221-223)
        if (kefuId != null && kefuId > 0) {
            QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("appid", appid);
            wrapper.eq("status", 1);
            wrapper.eq("id", kefuId);
            ChatServiceEntity service = chatServiceMapper.selectOne(wrapper);
            if (service != null) {
                return service.getUserId();
            }
        }

        // Priority 3: kefuRand - 暂不实现 (PHP lines 224-232)
        // TODO: Implement QR code random service selection

        // Priority 4: 转接客服 - 暂不实现 (PHP lines 234-243)
        // TODO: Implement service transfer logic

        // Priority 5 & 6: Get all online services and random select (PHP lines 245-269)
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("status", 1);
        wrapper.eq("online", 1);
        List<ChatServiceEntity> onlineServices = chatServiceMapper.selectList(wrapper);

        if (onlineServices.isEmpty()) {
            return null;  // No online services
        }

        // TODO: Check previous chat history (PHP lines 257-262)
        // For now, just random select

        // Random selection (PHP lines 264-265)
        Random random = new Random();
        ChatServiceEntity selected = onlineServices.get(random.nextInt(onlineServices.size()));
        return selected.getUserId();
    }

    /**
     * 获取客服信息
     * PHP Reference: ChatServiceServices.php::getRecord() line 272
     */
    private ChatServiceEntity getServiceInfo(Integer userId) {
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.select("nickname", "avatar");
        ChatServiceEntity service = chatServiceMapper.selectOne(wrapper);

        if (service == null) {
            throw new RuntimeException("Customer service agent does not exist");
        }

        return service;
    }

    /**
     * 生成随机UID（用于游客）
     */
    private Integer generateRandomUid() {
        // 生成一个随机UID，范围在 1000000 到 9999999 之间
        return 1000000 + new Random().nextInt(9000000);
    }
}
