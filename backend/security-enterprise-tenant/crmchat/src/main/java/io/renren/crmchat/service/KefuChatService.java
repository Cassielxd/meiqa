package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.crmchat.dao.AuxiliaryMapper;
import io.renren.crmchat.dao.ChatServiceDialogueRecordMapper;
import io.renren.crmchat.dao.ChatServiceMapper;
import io.renren.crmchat.dao.ChatServiceRecordMapper;
import io.renren.crmchat.dao.ChatUserMapper;
import io.renren.crmchat.entity.AuxiliaryEntity;
import io.renren.crmchat.entity.ChatServiceDialogueRecordEntity;
import io.renren.crmchat.entity.ChatServiceEntity;
import io.renren.crmchat.entity.ChatServiceRecordEntity;
import io.renren.crmchat.entity.ChatUserEntity;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.websocket.WebSocketPushService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Kefu 聊天管理服务
 * PHP Reference: /app/controller/kefu/Service.php (chat related methods)
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getMyDialogueRecordList(): 获取当前客服的聊天记录
 *    - user_id或to_user_id为当前客服的user_id
 *    - 支持msn、user_id筛选
 *    - 分页查询
 * 2. getMyChatUserList(): 获取当前客服的聊天用户
 *    - 获取与当前客服聊过天的所有用户
 * 3. getChatMessageList(): 查看与特定用户的对话
 *    - 获取当前客服与指定用户之间的聊天记录
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class KefuChatService {

    private final ChatServiceDialogueRecordMapper chatServiceDialogueRecordMapper;
    private final ChatServiceMapper chatServiceMapper;
    private final ChatUserMapper chatUserMapper;
    private final ChatServiceRecordMapper chatServiceRecordMapper;
    private final AuxiliaryMapper auxiliaryMapper;
    private final WebSocketPushService webSocketPushService;

    /**
     * 获取当前客服的聊天记录
     * GET /api/kefu/chat/record
     *
     * PHP Reference: ServiceDialogueRecord.php::index() (filtered by kefu_id)
     *
     * 业务逻辑:
     * 1. 只显示当前客服的聊天记录
     * 2. user_id或to_user_id为当前客服的user_id
     * 3. 支持msn模糊查询
     * 4. 支持user_id查询（对方用户ID）
     * 5. 分页查询
     *
     * @param filters      过滤条件
     * @param kefuId       当前客服ID
     * @param currentAppid 当前租户appid
     * @return 对话记录列表
     */
    public Map<String, Object> getMyDialogueRecordList(Map<String, Object> filters, Integer kefuId, String currentAppid) {
        // 1. 获取客服的user_id
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null || !kefu.getAppid().equals(currentAppid)) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        Integer myUserId = kefu.getUserId();
        if (myUserId == null) {
            throw new CrmChatException("Customer service user ID does not exist");
        }

        QueryWrapper<ChatServiceDialogueRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", currentAppid);

        // 2. 只显示当前客服的聊天记录
        wrapper.and(w -> w.eq("user_id", myUserId).or().eq("to_user_id", myUserId));

        // 3. msn模糊查询
        if (filters.containsKey("msn") && filters.get("msn") != null && !filters.get("msn").toString().trim().isEmpty()) {
            wrapper.like("msn", filters.get("msn"));
        }

        // 4. 对方用户ID查询
        if (filters.containsKey("user_id") && filters.get("user_id") != null) {
            Integer userId = Integer.parseInt(filters.get("user_id").toString());
            wrapper.and(w -> w
                .and(w1 -> w1.eq("user_id", userId).or().eq("to_user_id", userId))
            );
        }

        wrapper.orderByDesc("add_time");

        // 5. 分页
        Integer page = filters.containsKey("page") ? Integer.parseInt(filters.get("page").toString()) : 1;
        Integer limit = filters.containsKey("limit") ? Integer.parseInt(filters.get("limit").toString()) : 20;

        Page<ChatServiceDialogueRecordEntity> pageObj = new Page<>(page, limit);
        Page<ChatServiceDialogueRecordEntity> pageResult = chatServiceDialogueRecordMapper.selectPage(pageObj, wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("count", (int) pageResult.getTotal());

        return result;
    }

    /**
     * 获取当前客服的聊天用户
     * GET /api/kefu/chat/user
     *
     * PHP Reference: Service.php::chat_user()
     *
     * 业务逻辑:
     * 1. 获取当前客服聊天过的所有用户
     * 2. 返回用户列表（id, nickname, avatar）
     *
     * @param kefuId       当前客服ID
     * @param currentAppid 当前租户appid
     * @return 聊天用户列表
     */
    public List<Map<String, Object>> getMyChatUserList(Integer kefuId, String currentAppid) {
        // 1. 获取客服的user_id
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null || !kefu.getAppid().equals(currentAppid)) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        Integer myUserId = kefu.getUserId();
        if (myUserId == null) {
            throw new CrmChatException("Customer service user ID does not exist");
        }

        // 2. 查询该客服聊天过的所有对话记录
        QueryWrapper<ChatServiceDialogueRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", currentAppid);
        wrapper.and(w -> w.eq("user_id", myUserId).or().eq("to_user_id", myUserId));
        wrapper.groupBy("user_id", "to_user_id");
        wrapper.select("user_id", "to_user_id");

        List<ChatServiceDialogueRecordEntity> records = chatServiceDialogueRecordMapper.selectList(wrapper);

        // 3. 提取唯一的对方用户ID
        Set<Integer> userIds = new HashSet<>();
        for (ChatServiceDialogueRecordEntity record : records) {
            if (!record.getUserId().equals(myUserId)) {
                userIds.add(record.getUserId());
            }
            if (!record.getToUserId().equals(myUserId)) {
                userIds.add(record.getToUserId());
            }
        }

        // 4. 查询用户信息
        List<Map<String, Object>> result = new ArrayList<>();
        if (!userIds.isEmpty()) {
            List<ChatUserEntity> users = chatUserMapper.selectBatchIds(userIds);
            for (ChatUserEntity user : users) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("nickname", user.getNickname());
                userMap.put("avatar", user.getAvatar());
                result.add(userMap);
            }
        }

        return result;
    }

    /**
     * 查看与特定用户的对话
     * GET /api/kefu/chat/message
     *
     * PHP Reference: Service.php::chat_list()
     *
     * 业务逻辑:
     * 1. 获取当前客服与指定用户之间的聊天记录
     * 2. 分页查询
     *
     * @param filters      过滤条件
     * @param kefuId       当前客服ID
     * @param currentAppid 当前租户appid
     * @return 对话消息列表
     */
    public Map<String, Object> getChatMessageList(Map<String, Object> filters, Integer kefuId, String currentAppid) {
        // 1. 获取客服的user_id
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null || !kefu.getAppid().equals(currentAppid)) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        Integer myUserId = kefu.getUserId();
        if (myUserId == null) {
            throw new CrmChatException("Customer service user ID does not exist");
        }

        // 2. 获取对方用户ID
        Integer toUserId = filters.containsKey("to_user_id") && filters.get("to_user_id") != null
            ? Integer.parseInt(filters.get("to_user_id").toString()) : 0;

        QueryWrapper<ChatServiceDialogueRecordEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", currentAppid);

        if (toUserId > 0) {
            // 3. 查询当前客服与指定用户之间的对话
            wrapper.and(w -> w
                .and(w1 -> w1.eq("user_id", myUserId).eq("to_user_id", toUserId))
                .or(w2 -> w2.eq("user_id", toUserId).eq("to_user_id", myUserId))
            );
        } else {
            // 如果没有指定对方用户ID，返回所有聊天记录
            wrapper.and(w -> w.eq("user_id", myUserId).or().eq("to_user_id", myUserId));
        }

        wrapper.orderByDesc("add_time");

        // 4. 分页
        Integer page = filters.containsKey("page") ? Integer.parseInt(filters.get("page").toString()) : 1;
        Integer limit = filters.containsKey("limit") ? Integer.parseInt(filters.get("limit").toString()) : 20;

        Page<ChatServiceDialogueRecordEntity> pageObj = new Page<>(page, limit);
        Page<ChatServiceDialogueRecordEntity> pageResult = chatServiceDialogueRecordMapper.selectPage(pageObj, wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("count", (int) pageResult.getTotal());

        return result;
    }

    /**
     * 客服转接
     * POST /api/kefu/chat/transfer
     *
     * PHP Reference: KefuServices.php::setTransfer() + Service.php::transfer()
     *
     * 业务逻辑（严格参考PHP实现）:
     * 1. 验证参数：kefuToUserId（目标客服user_id）, chatUserId（被转接的chat_user.id）
     * 2. 验证不能转接给自己
     * 3. 事务操作：
     *    a. 查询原客服与用户的会话记录信息
     *    b. 为新客服创建会话记录（eb_chat_service_record）
     *    c. 删除原客服的会话记录
     *    d. 保存转接关系到辅助表（eb_auxiliary）
     * 4. 发送WebSocket通知给用户
     *
     * @param kefuToUserId 目标客服user_id
     * @param chatUserId   被转接的chat_user.id
     * @param kefuId       当前客服ID
     * @param currentAppid 当前租户appid
     */
    @Transactional(rollbackFor = Exception.class)
    public void transferService(Integer kefuToUserId, Integer chatUserId, Integer kefuId, String currentAppid) {
        // 1. 获取当前客服信息
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null || !kefu.getAppid().equals(currentAppid)) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        Integer kefuUserId = kefu.getUserId();
        if (kefuUserId == null) {
            throw new CrmChatException("Customer service user ID does not exist");
        }

        // 2. 验证不能转接给自己
        if (kefuUserId.equals(kefuToUserId)) {
            throw new CrmChatException("Cannot transfer to yourself");
        }

        // 3. 验证目标客服是否存在且在线
        QueryWrapper<ChatServiceEntity> toKefuWrapper = new QueryWrapper<>();
        toKefuWrapper.eq("appid", currentAppid);
        toKefuWrapper.eq("user_id", kefuToUserId);
        toKefuWrapper.eq("status", 1);
        ChatServiceEntity toKefu = chatServiceMapper.selectOne(toKefuWrapper);

        if (toKefu == null) {
            throw new CrmChatException("Target customer service agent does not exist or is offline");
        }

        // 4. 查询原客服与用户的会话记录
        QueryWrapper<ChatServiceRecordEntity> recordWrapper = new QueryWrapper<>();
        recordWrapper.eq("appid", currentAppid);
        recordWrapper.eq("user_id", kefuUserId);
        recordWrapper.eq("to_user_id", chatUserId);
        ChatServiceRecordEntity originalRecord = chatServiceRecordMapper.selectOne(recordWrapper);

        if (originalRecord == null) {
            throw new CrmChatException("Conversation record does not exist");
        }

        // 5. 为新客服创建会话记录
        ChatServiceRecordEntity newRecord = new ChatServiceRecordEntity();
        newRecord.setAppid(currentAppid);
        newRecord.setUserId(kefuToUserId); // 新客服的user_id
        newRecord.setToUserId(chatUserId);  // 用户的id
        newRecord.setType(originalRecord.getType());
        newRecord.setMessageType(originalRecord.getMessageType());
        newRecord.setNum(0); // 新会话未读数为0
        newRecord.setIsTourist(originalRecord.getIsTourist());
        newRecord.setNickname(originalRecord.getNickname());
        newRecord.setAvatar(originalRecord.getAvatar());
        newRecord.setAddTime((int) (System.currentTimeMillis() / 1000));
        newRecord.setUpdateTime((int) (System.currentTimeMillis() / 1000));

        int insertResult = chatServiceRecordMapper.insert(newRecord);
        if (insertResult <= 0) {
            throw new CrmChatException("Failed to create new conversation record");
        }

        // 6. 删除原客服的会话记录（双向）
        QueryWrapper<ChatServiceRecordEntity> deleteWrapper1 = new QueryWrapper<>();
        deleteWrapper1.eq("appid", currentAppid);
        deleteWrapper1.eq("user_id", kefuUserId);
        deleteWrapper1.eq("to_user_id", chatUserId);
        chatServiceRecordMapper.delete(deleteWrapper1);

        QueryWrapper<ChatServiceRecordEntity> deleteWrapper2 = new QueryWrapper<>();
        deleteWrapper2.eq("appid", currentAppid);
        deleteWrapper2.eq("user_id", chatUserId);
        deleteWrapper2.eq("to_user_id", kefuUserId);
        chatServiceRecordMapper.delete(deleteWrapper2);

        // 7. 保存转接关系到辅助表
        QueryWrapper<AuxiliaryEntity> auxWrapper = new QueryWrapper<>();
        auxWrapper.eq("type", 0);
        auxWrapper.eq("appid", currentAppid);
        auxWrapper.eq("binding_id", chatUserId);
        AuxiliaryEntity existingAux = auxiliaryMapper.selectOne(auxWrapper);

        int now = (int) (System.currentTimeMillis() / 1000);
        if (existingAux != null) {
            // 更新现有记录
            existingAux.setRelationId(kefuToUserId);
            existingAux.setUpdateTime(now);
            auxiliaryMapper.updateById(existingAux);
        } else {
            // 创建新记录
            AuxiliaryEntity newAux = new AuxiliaryEntity();
            newAux.setType(0); // 0=客服转接辅助
            newAux.setAppid(currentAppid);
            newAux.setBindingId(chatUserId);
            newAux.setRelationId(kefuToUserId);
            newAux.setStatus(1);
            newAux.setAddTime(now);
            newAux.setUpdateTime(now);
            auxiliaryMapper.insert(newAux);
        }

        // 8. 发送WebSocket通知给用户
        try {
            // 获取新客服信息
            Map<String, Object> transferData = new HashMap<>();
            transferData.put("type", "transfer");
            transferData.put("message", "Your conversation has been transferred to another agent");
            transferData.put("to_user_id", kefuToUserId);
            transferData.put("to_user_nickname", toKefu.getNickname());
            transferData.put("to_user_avatar", toKefu.getAvatar());
            transferData.put("add_time", now);

            // 发送通知给用户
            webSocketPushService.sendChat(currentAppid, chatUserId, transferData);

            log.info("客服转接成功: fromKefu={}, toKefu={}, chatUserId={}", kefuUserId, kefuToUserId, chatUserId);
        } catch (Exception e) {
            // WebSocket发送失败不影响转接成功
            log.error("转接WebSocket通知发送失败", e);
        }
    }

    /**
     * 获取可转接的客服列表
     * GET /api/kefu/chat/transfer/list
     *
     * PHP Reference: Service.php::getServiceList()
     *
     * 业务逻辑:
     * 1. 查询当前租户下的所有在线客服
     * 2. 排除当前客服和指定用户
     *
     * @param kefuId       当前客服ID
     * @param chatUserId   被转接的用户ID（可选，用于排除）
     * @param currentAppid 当前租户appid
     * @return 可转接的客服列表
     */
    public List<Map<String, Object>> getTransferableKefuList(Integer kefuId, Integer chatUserId, String currentAppid) {
        // 1. 获取当前客服信息
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null || !kefu.getAppid().equals(currentAppid)) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        Integer kefuUserId = kefu.getUserId();
        if (kefuUserId == null) {
            throw new CrmChatException("Customer service user ID does not exist");
        }

        // 2. 查询所有在线客服（排除当前客服）
        QueryWrapper<ChatServiceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", currentAppid);
        wrapper.eq("status", 1); // 状态正常
        wrapper.eq("online", 1); // 在线
        wrapper.ne("user_id", kefuUserId); // 排除当前客服
        wrapper.orderByDesc("id");

        List<ChatServiceEntity> kefuList = chatServiceMapper.selectList(wrapper);

        // 3. 格式化返回结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatServiceEntity k : kefuList) {
            Map<String, Object> kefuMap = new HashMap<>();
            kefuMap.put("id", k.getId());
            kefuMap.put("user_id", k.getUserId());
            kefuMap.put("nickname", k.getNickname());
            kefuMap.put("avatar", k.getAvatar());
            kefuMap.put("online", k.getOnline());
            result.add(kefuMap);
        }

        return result;
    }
}
