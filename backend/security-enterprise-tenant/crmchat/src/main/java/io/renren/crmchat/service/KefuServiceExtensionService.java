package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.crmchat.dao.ChatServiceDialogueRecordMapper;
import io.renren.crmchat.dao.ChatServiceMapper;
import io.renren.crmchat.dao.ChatServiceRecordMapper;
import io.renren.crmchat.dao.ChatUserMapper;
import io.renren.crmchat.entity.ChatServiceDialogueRecordEntity;
import io.renren.crmchat.entity.ChatServiceEntity;
import io.renren.crmchat.entity.ChatServiceRecordEntity;
import io.renren.crmchat.entity.ChatUserEntity;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.security.TenantContextUtils;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Kefu Service Extension Service - 客服扩展功能服务
 * PHP Reference: /app/services/kefu/KefuServices.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getServiceList(): 获取在线客服列表（用于转接）
 *    - status=1（启用）, online=1（在线）
 *    - 排除指定的客服ID列表
 * 2. getServiceInfo(): 获取当前客服详细信息
 * 3. transfer(): 客服转接功能
 * 4. setAutoReply(): 设置自动回复开关
 * 5. setBackstage(): 设置是否后台运行
 * 6. getChatHistory(): 获取聊天历史记录
 * 7. ping(): 心跳检测
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class KefuServiceExtensionService {

    private final ChatServiceMapper chatServiceMapper;
    private final ChatServiceRecordMapper chatServiceRecordMapper;
    private final ChatServiceDialogueRecordMapper chatServiceDialogueRecordMapper;
    private final ChatUserMapper chatUserMapper;
    private final WebSocketPushService webSocketPushService;
    private final AutoBadgeService autoBadgeService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 获取在线客服列表（用于转接）
     * GET /api/kefu/service/list
     *
     * PHP Reference: KefuServices.php::getServiceList()
     *
     * 业务逻辑:
     * 1. 只显示status=1（启用）且online=1（在线）的客服
     * 2. 排除当前客服和指定用户
     * 3. 支持nickname模糊查询
     * 4. 分页查询
     *
     * @param filters      过滤条件（nickname, page, limit）
     * @param excludeIds   排除的客服ID列表（包含当前客服和当前聊天用户）
     * @param currentAppid 当前租户appid
     * @return 客服列表
     */
    public Map<String, Object> getServiceList(Map<String, Object> filters, List<Integer> excludeIds, String currentAppid) {
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        TenantQueryHelper.applyAppid(wrapper, currentAppid);
        wrapper.eq("status", 1);   // 启用状态
        wrapper.eq("online", 1);   // 在线状态

        // 排除指定的客服ID
        if (excludeIds != null && !excludeIds.isEmpty()) {
            wrapper.notIn("id", excludeIds);
        }

        // 支持nickname模糊查询
        if (filters.containsKey("nickname") && filters.get("nickname") != null && !filters.get("nickname").toString().trim().isEmpty()) {
            wrapper.like("nickname", filters.get("nickname"));
        }

        wrapper.orderByDesc("online");
        wrapper.orderByDesc("update_time");

        // 分页
        Integer page = filters.containsKey("page") ? Integer.parseInt(filters.get("page").toString()) : 1;
        Integer limit = filters.containsKey("limit") ? Integer.parseInt(filters.get("limit").toString()) : 20;

        Page<ChatServiceEntity> pageObj = new Page<>(page, limit);
        Page<ChatServiceEntity> pageResult = chatServiceMapper.selectPage(pageObj, wrapper);

        List<Map<String, Object>> list = pageResult.getRecords().stream()
                .map(this::buildKefuListItem)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("count", (int) pageResult.getTotal());

        return result;
    }

    /**
     * 获取当前客服详细信息
     * GET /api/kefu/service/info
     *
     * PHP Reference: Service.php::getServiceInfo()
     *
     * 业务逻辑:
     * 1. 返回当前客服完整信息
     * 2. 附加: site_name（系统配置）
     * 3. 附加: config_export_open（系统配置）
     * 4. 附加: user_ids（同租户所有客服的user_id列表）
     *
     * @param kefuId       当前客服ID
     * @param currentAppid 当前租户appid
     * @return 客服详细信息
     */
    public Map<String, Object> getServiceInfo(Integer kefuId, String currentAppid) {
        // 1. 获取当前客服信息
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null) {
            throw new CrmChatException("Customer service agent does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(kefu.getAppid(), "Customer service agent does not exist");

        // 2. 转换为Map
        Map<String, Object> result = new HashMap<>();
        result.put("id", kefu.getId());
        result.put("user_id", kefu.getUserId());
        result.put("appid", kefu.getAppid());
        result.put("account", kefu.getAccount());
        result.put("nickname", kefu.getNickname());
        result.put("avatar", kefu.getAvatar());
        result.put("phone", kefu.getPhone());
        result.put("online", kefu.getOnline());
        result.put("status", kefu.getStatus());
        result.put("notify", kefu.getNotify());
        result.put("customer", kefu.getCustomer());
        result.put("is_app", kefu.getIsApp());
        result.put("is_backstage", kefu.getIsBackstage());
        result.put("auto_reply", kefu.getAutoReply());
        result.put("welcome_words", kefu.getWelcomeWords());
        result.put("group_id", kefu.getGroupId());
        result.put("add_time", kefu.getAddTime());
        result.put("update_time", kefu.getUpdateTime());

        // 3. 附加系统配置（TODO: 从系统配置表读取）
        result.put("site_name", "CRM Customer Service System");
        result.put("config_export_open", 1);

        // 4. 获取同租户所有客服的user_id列表
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        TenantQueryHelper.applyAppid(wrapper, currentAppid);
        wrapper.select("user_id");
        List<ChatServiceEntity> kefuList = chatServiceMapper.selectList(wrapper);
        List<Integer> userIds = new ArrayList<>();
        for (ChatServiceEntity k : kefuList) {
            if (k.getUserId() != null) {
                userIds.add(k.getUserId());
            }
        }
        result.put("user_ids", userIds);

        return result;
    }

    /**
     * 客服转接
     * POST /api/kefu/service/transfer
     *
     * PHP Reference: KefuServices.php::setTransfer()
     *
     * 业务逻辑:
     * 1. 验证不能转接给自己
     * 2. 验证目标客服存在且在线
     * 3. 转移ChatServiceRecord记录（删除原客服记录，创建新客服记录）
     * 4. 注: WebSocket通知功能暂未实现，留待后续
     *
     * @param fromKefuId   当前客服ID
     * @param toKefuUserId 目标客服的user_id
     * @param userId       用户ID
     * @param currentAppid 当前租户appid
     */
    @Transactional(rollbackFor = Exception.class)
    public void transfer(Integer fromKefuId, Integer toKefuUserId, Integer userId, String currentAppid) {
        // 1. 获取当前客服信息
        ChatServiceEntity fromKefu = chatServiceMapper.selectById(fromKefuId);
        if (fromKefu == null) {
            throw new CrmChatException("Current customer service representative does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(fromKefu.getAppid(), "Current customer service agent does not exist");

        Integer fromKefuUserId = fromKefu.getUserId();
        if (fromKefuUserId == null) {
            throw new CrmChatException("Current customer service user ID does not exist");
        }

        // 2. 验证不能转接给自己
        if (fromKefuUserId.equals(toKefuUserId)) {
            throw new CrmChatException("Cannot transfer to yourself");
        }

        // 3. 验证目标客服存在
        QueryWrapper<ChatServiceEntity> toKefuWrapper = new QueryWrapper<>();
        toKefuWrapper.eq("user_id", toKefuUserId);
        TenantQueryHelper.applyAppid(toKefuWrapper, currentAppid);
        toKefuWrapper.eq("status", 1); // 必须是启用状态
        ChatServiceEntity toKefu = chatServiceMapper.selectOne(toKefuWrapper);
        if (toKefu == null) {
            throw new CrmChatException("Target customer service agent does not exist or is not enabled");
        }

        // 4. 查找当前客服与用户的聊天记录
        QueryWrapper<ChatServiceRecordEntity> recordWrapper = new QueryWrapper<>();
        TenantQueryHelper.applyAppid(recordWrapper, currentAppid);
        recordWrapper.and(w -> w
            .and(w1 -> w1.eq("user_id", fromKefuUserId).eq("to_user_id", userId))
            .or(w2 -> w2.eq("user_id", userId).eq("to_user_id", fromKefuUserId))
        );
        recordWrapper.orderByDesc("update_time");
        recordWrapper.last("LIMIT 1");

        ChatServiceRecordEntity existingRecord = chatServiceRecordMapper.selectOne(recordWrapper);
        if (existingRecord == null) {
            throw new CrmChatException("No chat history found with this user");
        }

        Map<String, Object> existingRecordMap = toRecordMap(existingRecord);

        // 5. 创建新的聊天记录（转接给目标客服）
        ChatServiceRecordEntity newRecord = new ChatServiceRecordEntity();
        newRecord.setAppid(TenantContextUtils.resolveAppid(currentAppid));
        newRecord.setUserId(userId);
        newRecord.setToUserId(toKefuUserId);
        newRecord.setMsn(existingRecord.getMsn());
        newRecord.setType(existingRecord.getType());
        newRecord.setMessageType(existingRecord.getMessageType());
        newRecord.setIsTourist(existingRecord.getIsTourist());
        newRecord.setNickname(existingRecord.getNickname());
        newRecord.setAvatar(existingRecord.getAvatar());
        newRecord.setAddTime((int) (System.currentTimeMillis() / 1000));
        newRecord.setUpdateTime((int) (System.currentTimeMillis() / 1000));
        newRecord.setNum(0); // 转接后未读消息数重置为0

        int insertResult = chatServiceRecordMapper.insert(newRecord);
        if (insertResult <= 0) {
            throw new CrmChatException("Failed to create transfer record");
        }

        // 6. 删除原客服的聊天记录
        QueryWrapper<ChatServiceRecordEntity> deleteWrapper = new QueryWrapper<>();
        TenantQueryHelper.applyAppid(deleteWrapper, currentAppid);
        deleteWrapper.and(w -> w
            .and(w1 -> w1.eq("user_id", fromKefuUserId).eq("to_user_id", userId))
            .or(w2 -> w2.eq("user_id", userId).eq("to_user_id", fromKefuUserId))
        );

        int deleteResult = chatServiceRecordMapper.delete(deleteWrapper);

        log.info("Agent transfer completed: fromKefu={} -> toKefu={}, user={}, deleted={}",
                 fromKefuUserId, toKefuUserId, userId, deleteResult);

        ChatServiceRecordEntity persistedNewRecord = chatServiceRecordMapper.selectById(newRecord.getId());
        if (persistedNewRecord == null) {
            QueryWrapper<ChatServiceRecordEntity> selectWrapper = new QueryWrapper<>();
            TenantQueryHelper.applyAppid(selectWrapper, currentAppid);
            selectWrapper.eq("user_id", userId);
            selectWrapper.eq("to_user_id", toKefuUserId);
            selectWrapper.orderByDesc("update_time");
            selectWrapper.last("LIMIT 1");
            persistedNewRecord = chatServiceRecordMapper.selectOne(selectWrapper);
        }

        Map<String, Object> newRecordMap = toRecordMap(persistedNewRecord);
        Map<String, Object> fromKefuInfo = buildKefuInfo(fromKefu);
        Map<String, Object> toKefuInfo = buildKefuInfo(toKefu);

        if (!newRecordMap.isEmpty()) {
            Map<String, Object> transferPayload = new HashMap<>();
            transferPayload.put("recored", newRecordMap);
            transferPayload.put("kefuInfo", fromKefuInfo);
            webSocketPushService.sendEvent(currentAppid, toKefuUserId, "transfer", transferPayload);
        }

        if (!existingRecordMap.isEmpty()) {
            Map<String, Object> removalPayload = new HashMap<>();
            removalPayload.put("recored", existingRecordMap);
            webSocketPushService.sendEvent(currentAppid, fromKefuUserId, "rm_transfer", removalPayload);
        }

        if (!newRecordMap.isEmpty()) {
            Map<String, Object> userPayload = new HashMap<>();
            userPayload.put("recored", newRecordMap);
            userPayload.put("kefuInfo", toKefuInfo);
            webSocketPushService.sendEvent(currentAppid, userId, "to_transfer", userPayload);
        }

        autoBadgeService.dispatch(toKefuUserId, userId, currentAppid);
        autoBadgeService.dispatch(fromKefuUserId, userId, currentAppid);
    }

    /**
     * 设置自动回复开关
     * PUT /api/kefu/service/auto_reply/:value
     *
     * PHP Reference: Service.php::setAutoReply()
     *
     * @param kefuId       当前客服ID
     * @param value        自动回复开关值：0-关闭，1-开启
     * @param currentAppid 当前租户appid
     */
    @Transactional(rollbackFor = Exception.class)
    public void setAutoReply(Integer kefuId, Integer value, String currentAppid) {
        // 验证客服存在
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null) {
            throw new CrmChatException("Customer service agent does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(kefu.getAppid(), "Customer service agent does not exist");

        // 验证value值
        if (value != 0 && value != 1) {
            throw new CrmChatException("Parameter error: value must be 0 or 1");
        }

        // 更新auto_reply字段
        kefu.setAutoReply(value);
        int result = chatServiceMapper.updateById(kefu);
        if (result <= 0) {
            throw new CrmChatException("Failed to set");
        }

        log.info("Updated auto-reply setting: kefuId={}, value={}", kefuId, value);
    }

    /**
     * 设置是否后台运行
     * PUT /api/kefu/service/backstage/:value
     *
     * PHP Reference: Service.php::backstage()
     *
     * @param kefuId       当前客服ID
     * @param value        后台运行开关值：0-关闭，1-开启
     * @param currentAppid 当前租户appid
     */
    @Transactional(rollbackFor = Exception.class)
    public void setBackstage(Integer kefuId, Integer value, String currentAppid) {
        // 验证客服存在
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null || !kefu.getAppid().equals(currentAppid)) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        // 验证value值
        if (value != 0 && value != 1) {
            throw new CrmChatException("Parameter error: value must be 0 or 1");
        }

        // 更新is_backstage字段
        kefu.setIsBackstage(value);
        int result = chatServiceMapper.updateById(kefu);
        if (result <= 0) {
            throw new CrmChatException("Failed to set");
        }

        log.info("Updated background running flag: kefuId={}, value={}", kefuId, value);
    }

    /**
     * 获取聊天历史记录
     * GET /api/kefu/chat/history
     *
     * PHP Reference: KefuServices.php::getChatList()
     *
     * 业务逻辑:
     * 1. 获取当前客服与指定用户之间的聊天记录
     * 2. 支持分页和上拉加载（upperId）
     * 3. 倒序排列（最新消息在最后）
     *
     * @param filters      过滤条件（user_id, upperId, limit）
     * @param kefuId       当前客服ID
     * @param currentAppid 当前租户appid
     * @return 聊天记录列表（转换为下划线命名的Map）
     */
    public List<Map<String, Object>> getChatHistory(Map<String, Object> filters, Integer kefuId, String currentAppid) {
        // 1. 获取客服的user_id
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null || !kefu.getAppid().equals(currentAppid)) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        Integer kefuUserId = kefu.getUserId();
        if (kefuUserId == null) {
            throw new CrmChatException("Customer service user ID does not exist");
        }

        // 2. 获取目标用户ID
        Integer toUserId = filters.containsKey("user_id") && filters.get("user_id") != null
            ? Integer.parseInt(filters.get("user_id").toString()) : 0;
        if (toUserId == 0) {
            throw new CrmChatException("Missing user ID parameter");
        }

        // 3. 构建查询条件
        QueryWrapper<ChatServiceDialogueRecordEntity> wrapper = new QueryWrapper<>();
        TenantQueryHelper.applyAppid(wrapper, currentAppid);

        // 查询客服与用户之间的对话（双向）
        wrapper.and(w -> w
            .and(w1 -> w1.eq("user_id", kefuUserId).eq("to_user_id", toUserId))
            .or(w2 -> w2.eq("user_id", toUserId).eq("to_user_id", kefuUserId))
        );

        // 4. 支持上拉加载（upperId - 比这个ID更早的记录）
        if (filters.containsKey("upperId") && filters.get("upperId") != null) {
            Integer upperId = Integer.parseInt(filters.get("upperId").toString());
            if (upperId > 0) {
                wrapper.lt("id", upperId);
            }
        }

        // 5. 排序和分页
        wrapper.orderByDesc("add_time");
        Integer limit = filters.containsKey("limit") ? Integer.parseInt(filters.get("limit").toString()) : 20;
        wrapper.last("LIMIT " + limit);

        List<ChatServiceDialogueRecordEntity> list = chatServiceDialogueRecordMapper.selectList(wrapper);

        // 6. 倒序（PHP中使用array_reverse，最新消息在最后）
        Collections.reverse(list);

        autoBadgeService.dispatch(kefuUserId, toUserId, currentAppid);

        // 7. 转换为前端期望的下划线命名格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatServiceDialogueRecordEntity entity : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", entity.getId());
            map.put("user_id", entity.getUserId());
            map.put("to_user_id", entity.getToUserId());
            map.put("msn", entity.getMsn());
            map.put("type", entity.getType());
            map.put("other", entity.getOther());
            map.put("add_time", entity.getAddTime());
            map.put("appid", entity.getAppid());
            map.put("is_tourist", entity.getIsTourist());
            map.put("msn_type", entity.getMsnType());
            map.put("remind", entity.getRemind());
            map.put("guid", entity.getGuid());
            map.put("mer_id", entity.getMerId());
            result.add(map);
        }

        return result;
    }

    /**
     * 心跳检测
     * GET /api/kefu/service/ping
     *
     * PHP Reference: Service.php::ping()
     *
     * @return 当前时间戳
     */
    public Map<String, Object> ping() {
        Map<String, Object> result = new HashMap<>();
        result.put("time", System.currentTimeMillis() / 1000);
        return result;
    }

    private Map<String, Object> toRecordMap(ChatServiceRecordEntity record) {
        if (record == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", record.getId());
        map.put("user_id", record.getUserId());
        map.put("to_user_id", record.getToUserId());
        map.put("message", record.getMsn());
        map.put("message_type", record.getMessageType());
        map.put("mssage_num", record.getNum());
        map.put("avatar", record.getAvatar());
        map.put("nickname", decorateNickname(record));
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

    private Map<String, Object> buildKefuInfo(ChatServiceEntity kefu) {
        if (kefu == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> info = new HashMap<>();
        info.put("id", kefu.getId());
        info.put("user_id", kefu.getUserId());
        info.put("nickname", kefu.getNickname());
        info.put("avatar", kefu.getAvatar());
        info.put("account", kefu.getAccount());
        return info;
    }

    private Map<String, Object> buildKefuListItem(ChatServiceEntity kefu) {
        if (kefu == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> item = new HashMap<>();
        item.put("id", kefu.getId());
        item.put("user_id", kefu.getUserId());
        item.put("userId", kefu.getUserId());
        item.put("appid", kefu.getAppid());
        item.put("nickname", kefu.getNickname());
        item.put("avatar", kefu.getAvatar());
        item.put("online", kefu.getOnline());
        item.put("status", kefu.getStatus());
        item.put("account", kefu.getAccount());
        item.put("group_id", kefu.getGroupId());
        item.put("update_time", kefu.getUpdateTime());
        return item;
    }

    private String formatTimestamp(Integer epochSeconds) {
        if (epochSeconds == null || epochSeconds <= 0) {
            return "";
        }
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
        return TIME_FORMATTER.format(time);
    }
}
