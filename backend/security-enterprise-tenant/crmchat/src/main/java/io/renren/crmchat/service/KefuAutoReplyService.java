package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatAutoReplyMapper;
import io.renren.crmchat.dao.ChatServiceMapper;
import io.renren.crmchat.entity.ChatAutoReplyEntity;
import io.renren.crmchat.entity.ChatServiceEntity;
import io.renren.crmchat.exception.CrmChatException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Kefu 自动回复服务
 * PHP Reference: /app/controller/kefu/AutoReply.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getMyAutoReplyList(): 获取个人自动回复列表
 *    - user_id = 当前客服的user_id（个人自动回复，不是系统级别）
 * 2. getAutoReplyDetail(): 获取自动回复详情
 * 3. createAutoReply(): 创建个人自动回复
 *    - 验证keyword和content非空
 *    - user_id = 当前客服的user_id
 * 4. updateAutoReply(): 更新个人自动回复
 * 5. deleteAutoReply(): 删除个人自动回复
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class KefuAutoReplyService {

    private final ChatAutoReplyMapper chatAutoReplyMapper;
    private final ChatServiceMapper chatServiceMapper;

    /**
     * 获取个人自动回复列表
     * GET /api/kefu/autoreply
     *
     * PHP Reference: AutoReply.php::index() (filtered by current kefu user_id)
     *
     * 业务逻辑:
     * 1. user_id = 当前客服的user_id（个人自动回复）
     * 2. 不同于Tenant的系统级别（user_id=0）
     *
     * @param kefuId       当前客服ID
     * @param currentAppid 当前租户appid
     * @return 自动回复列表
     */
    public List<ChatAutoReplyEntity> getMyAutoReplyList(Integer kefuId, String currentAppid) {
        // 1. 获取客服的user_id
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null || !kefu.getAppid().equals(currentAppid)) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        Integer myUserId = kefu.getUserId();
        if (myUserId == null) {
            throw new CrmChatException("Customer service user ID does not exist");
        }

        // 2. 查询个人自动回复
        QueryWrapper<ChatAutoReplyEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", currentAppid);
        wrapper.eq("user_id", myUserId); // 关键：个人自动回复，不是系统级别（user_id=0）

        wrapper.orderByAsc("sort");
        wrapper.orderByDesc("id");

        return chatAutoReplyMapper.selectList(wrapper);
    }

    /**
     * 获取自动回复详情
     * GET /api/kefu/autoreply/:id
     *
     * PHP Reference: AutoReply.php::create()
     *
     * @param id           自动回复ID
     * @param kefuId       当前客服ID
     * @param currentAppid 当前租户appid
     * @return 自动回复详情
     */
    public ChatAutoReplyEntity getAutoReplyDetail(Integer id, Integer kefuId, String currentAppid) {
        // 1. 获取客服的user_id
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null || !kefu.getAppid().equals(currentAppid)) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        Integer myUserId = kefu.getUserId();
        if (myUserId == null) {
            throw new CrmChatException("Customer service user ID does not exist");
        }

        // 2. 查询自动回复
        ChatAutoReplyEntity autoReply = chatAutoReplyMapper.selectById(id);
        if (autoReply == null || !autoReply.getAppid().equals(currentAppid) || !autoReply.getUserId().equals(myUserId)) {
            throw new CrmChatException("Failed to retrieve");
        }

        return autoReply;
    }

    /**
     * 创建个人自动回复
     * POST /api/kefu/autoreply
     *
     * PHP Reference: AutoReply.php::save() (with current kefu user_id)
     *
     * 业务逻辑:
     * 1. 验证keyword非空
     * 2. 验证content非空
     * 3. user_id = 当前客服的user_id
     *
     * @param data         自动回复数据
     * @param kefuId       当前客服ID
     * @param currentAppid 当前租户appid
     * @return 新创建的自动回复ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createAutoReply(Map<String, Object> data, Integer kefuId, String currentAppid) {
        // 1. 获取客服的user_id
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null || !kefu.getAppid().equals(currentAppid)) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        Integer myUserId = kefu.getUserId();
        if (myUserId == null) {
            throw new CrmChatException("Customer service user ID does not exist");
        }

        // 2. 验证必填字段
        if (!data.containsKey("keyword") || data.get("keyword") == null || data.get("keyword").toString().trim().isEmpty()) {
            throw new CrmChatException("Please enter keyword");
        }

        if (!data.containsKey("content") || data.get("content") == null || data.get("content").toString().trim().isEmpty()) {
            throw new CrmChatException("Please enter reply content");
        }

        String keyword = data.get("keyword").toString();
        String content = data.get("content").toString();

        // 3. 创建个人自动回复
        ChatAutoReplyEntity autoReply = new ChatAutoReplyEntity();
        autoReply.setKeyword(keyword);
        autoReply.setContent(content);
        autoReply.setUserId(myUserId); // 关键：设置为当前客服的user_id
        autoReply.setAppid(currentAppid);
        autoReply.setAddTime((int) (System.currentTimeMillis() / 1000));

        if (data.containsKey("sort") && data.get("sort") != null) {
            autoReply.setSort(Integer.parseInt(data.get("sort").toString()));
        } else {
            autoReply.setSort(0);
        }

        int result = chatAutoReplyMapper.insert(autoReply);
        if (result <= 0) {
            throw new CrmChatException("Failed to save");
        }

        return autoReply.getId();
    }

    /**
     * 更新个人自动回复
     * PUT /api/kefu/autoreply/:id
     *
     * PHP Reference: AutoReply.php::save() (with id)
     *
     * @param id           自动回复ID
     * @param data         自动回复数据
     * @param kefuId       当前客服ID
     * @param currentAppid 当前租户appid
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAutoReply(Integer id, Map<String, Object> data, Integer kefuId, String currentAppid) {
        // 1. 获取客服的user_id
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null || !kefu.getAppid().equals(currentAppid)) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        Integer myUserId = kefu.getUserId();
        if (myUserId == null) {
            throw new CrmChatException("Customer service user ID does not exist");
        }

        // 2. 验证必填字段
        if (!data.containsKey("keyword") || data.get("keyword") == null || data.get("keyword").toString().trim().isEmpty()) {
            throw new CrmChatException("Please enter keyword");
        }

        if (!data.containsKey("content") || data.get("content") == null || data.get("content").toString().trim().isEmpty()) {
            throw new CrmChatException("Please enter reply content");
        }

        // 3. 验证自动回复存在且属于当前客服
        ChatAutoReplyEntity autoReply = chatAutoReplyMapper.selectById(id);
        if (autoReply == null || !autoReply.getAppid().equals(currentAppid) || !autoReply.getUserId().equals(myUserId)) {
            throw new CrmChatException("Auto reply does not exist");
        }

        // 4. 更新自动回复
        autoReply.setKeyword(data.get("keyword").toString());
        autoReply.setContent(data.get("content").toString());

        if (data.containsKey("sort") && data.get("sort") != null) {
            autoReply.setSort(Integer.parseInt(data.get("sort").toString()));
        }

        int result = chatAutoReplyMapper.updateById(autoReply);
        if (result <= 0) {
            throw new CrmChatException("Failed to modify");
        }
    }

    /**
     * 删除个人自动回复
     * DELETE /api/kefu/autoreply/:id
     *
     * PHP Reference: AutoReply.php::delete()
     *
     * @param id           自动回复ID
     * @param kefuId       当前客服ID
     * @param currentAppid 当前租户appid
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAutoReply(Integer id, Integer kefuId, String currentAppid) {
        // 1. 获取客服的user_id
        ChatServiceEntity kefu = chatServiceMapper.selectById(kefuId);
        if (kefu == null || !kefu.getAppid().equals(currentAppid)) {
            throw new CrmChatException("Customer service agent does not exist");
        }

        Integer myUserId = kefu.getUserId();
        if (myUserId == null) {
            throw new CrmChatException("Customer service user ID does not exist");
        }

        // 2. 验证自动回复存在且属于当前客服
        ChatAutoReplyEntity autoReply = chatAutoReplyMapper.selectById(id);
        if (autoReply == null || !autoReply.getAppid().equals(currentAppid) || !autoReply.getUserId().equals(myUserId)) {
            throw new CrmChatException("Auto reply to delete does not exist");
        }

        // 3. 删除自动回复
        int result = chatAutoReplyMapper.deleteById(id);
        if (result <= 0) {
            throw new CrmChatException("Failed to delete");
        }
    }
}
