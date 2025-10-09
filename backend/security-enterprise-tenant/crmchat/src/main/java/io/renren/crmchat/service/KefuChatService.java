package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.crmchat.dao.ChatServiceDialogueRecordMapper;
import io.renren.crmchat.dao.ChatServiceMapper;
import io.renren.crmchat.dao.ChatUserMapper;
import io.renren.crmchat.entity.ChatServiceDialogueRecordEntity;
import io.renren.crmchat.entity.ChatServiceEntity;
import io.renren.crmchat.entity.ChatUserEntity;
import io.renren.crmchat.exception.CrmChatException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        result.put("count", pageResult.getTotal());

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
        result.put("count", pageResult.getTotal());

        return result;
    }
}
