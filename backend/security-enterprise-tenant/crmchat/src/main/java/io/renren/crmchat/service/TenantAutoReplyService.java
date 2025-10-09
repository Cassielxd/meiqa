package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatAutoReplyMapper;
import io.renren.crmchat.entity.ChatAutoReplyEntity;
import io.renren.crmchat.security.TenantGuard;
import io.renren.crmchat.security.TenantSecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Tenant 自动回复服务
 * PHP Reference: /app/controller/tenant/chat/AutoReply.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getAutoReplyList(): 获取自动回复列表
 *    - 根据appid和user_id查询
 *    - user_id默认为0（系统级别）
 * 2. getAutoReplyDetail(): 获取自动回复详情
 * 3. createAutoReply(): 创建自动回复
 *    - 验证keyword和content非空
 *    - 设置add_time
 * 4. updateAutoReply(): 更新自动回复
 *    - 验证keyword和content非空
 *    - 更新数据
 * 5. deleteAutoReply(): 删除自动回复
 *    - 验证id存在
 *    - 删除记录
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantAutoReplyService {

    private final ChatAutoReplyMapper chatAutoReplyMapper;

    /**
     * 获取自动回复列表
     * GET /api/tenant/chat/auto_reply
     *
     * PHP Reference: AutoReply.php::index()
     *
     * 业务逻辑:
     * 1. 根据appid和user_id查询
     * 2. user_id默认为0（系统级别）
     *
     * @param userId 用户ID，0表示系统级别
     * @return 自动回复列表
     */
    public List<ChatAutoReplyEntity> getAutoReplyList(Integer userId) {
        // PHP: return $this->success($this->services->getList($appId, (int)$userId));

        String appid = TenantSecurityUtils.requireAppid();
        QueryWrapper<ChatAutoReplyEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);

        if (userId != null) {
            wrapper.eq("user_id", userId);
        } else {
            wrapper.eq("user_id", 0); // 默认系统级别
        }

        wrapper.orderByAsc("sort");
        wrapper.orderByDesc("id");

        return chatAutoReplyMapper.selectList(wrapper);
    }

    /**
     * 获取自动回复详情
     * GET /api/tenant/chat/auto_reply/:id
     *
     * PHP Reference: AutoReply.php::create() (用于获取表单数据)
     *
     * @param id           自动回复ID
     * @return 自动回复详情
     */
    public ChatAutoReplyEntity getAutoReplyDetail(Integer id) {
        if (id == null || id <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Missing required parameter");
        }

        ChatAutoReplyEntity autoReply = chatAutoReplyMapper.selectById(id);
        if (autoReply == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to retrieve");
        }
        TenantGuard.ensureOwnedByCurrentTenant(autoReply.getAppid(), "Failed to retrieve");

        return autoReply;
    }

    /**
     * 创建自动回复
     * POST /api/tenant/chat/auto_reply
     *
     * PHP Reference: AutoReply.php::save() (当id为空时)
     *
     * 业务逻辑:
     * 1. 验证keyword非空
     * 2. 验证content非空
     * 3. 设置add_time
     * 4. 保存
     *
     * @param data         自动回复数据
     * @return 新创建的自动回复ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createAutoReply(Map<String, Object> data) {
        // 1. PHP: if (!$data['keyword']) return $this->fail('请输入关键字');
        if (!data.containsKey("keyword") || data.get("keyword") == null || data.get("keyword").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter keyword");
        }

        // 2. PHP: if (!$data['content']) return $this->fail('请输入回复内容');
        if (!data.containsKey("content") || data.get("content") == null || data.get("content").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter reply content");
        }

        String keyword = data.get("keyword").toString();
        String content = data.get("content").toString();

        // 3. 创建自动回复
        String appid = TenantSecurityUtils.requireAppid();
        ChatAutoReplyEntity autoReply = new ChatAutoReplyEntity();
        autoReply.setKeyword(keyword);
        autoReply.setContent(content);
        autoReply.setAddTime((int) (System.currentTimeMillis() / 1000));
        autoReply.setAppid(appid);

        // user_id
        if (data.containsKey("user_id") && data.get("user_id") != null) {
            autoReply.setUserId(Integer.parseInt(data.get("user_id").toString()));
        } else {
            autoReply.setUserId(0); // 默认系统级别
        }

        // sort
        if (data.containsKey("sort") && data.get("sort") != null) {
            autoReply.setSort(Integer.parseInt(data.get("sort").toString()));
        } else {
            autoReply.setSort(0);
        }

        int result = chatAutoReplyMapper.insert(autoReply);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to save");
        }

        return autoReply.getId();
    }

    /**
     * 更新自动回复
     * PUT /api/tenant/chat/auto_reply/:id
     *
     * PHP Reference: AutoReply.php::save() (当id存在时)
     *
     * 业务逻辑:
     * 1. 验证keyword非空
     * 2. 验证content非空
     * 3. 更新数据
     *
     * @param id           自动回复ID
     * @param data         自动回复数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAutoReply(Integer id, Map<String, Object> data) {
        // 1. 验证数据
        if (!data.containsKey("keyword") || data.get("keyword") == null || data.get("keyword").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter keyword");
        }
        if (!data.containsKey("content") || data.get("content") == null || data.get("content").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter reply content");
        }

        // 2. 验证自动回复存在
        ChatAutoReplyEntity autoReply = chatAutoReplyMapper.selectById(id);
        if (autoReply == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Auto reply does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(autoReply.getAppid(), "Auto reply does not exist");

        // 3. 更新数据
        autoReply.setKeyword(data.get("keyword").toString());
        autoReply.setContent(data.get("content").toString());

        if (data.containsKey("user_id") && data.get("user_id") != null) {
            autoReply.setUserId(Integer.parseInt(data.get("user_id").toString()));
        }

        if (data.containsKey("sort") && data.get("sort") != null) {
            autoReply.setSort(Integer.parseInt(data.get("sort").toString()));
        }

        int result = chatAutoReplyMapper.updateById(autoReply);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to modify");
        }
    }

    /**
     * 删除自动回复
     * DELETE /api/tenant/chat/auto_reply/:id
     *
     * PHP Reference: AutoReply.php::delete()
     *
     * @param id           自动回复ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAutoReply(Integer id) {
        // PHP: if (!$id) return $this->fail('缺少参数');
        if (id == null || id <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Missing required parameter");
        }

        ChatAutoReplyEntity autoReply = chatAutoReplyMapper.selectById(id);
        if (autoReply == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Auto reply does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(autoReply.getAppid(), "Auto reply does not exist");

        // PHP: $this->services->delete($id);
        int result = chatAutoReplyMapper.deleteById(id);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to delete");
        }
    }
}
