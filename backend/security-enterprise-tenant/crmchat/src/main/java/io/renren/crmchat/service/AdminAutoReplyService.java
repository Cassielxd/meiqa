package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatAutoReplyMapper;
import io.renren.crmchat.entity.ChatAutoReplyEntity;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.security.TenantContextUtils;
import io.renren.crmchat.security.TenantGuard;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin Auto Reply Service - 管理员自动回复管理
 * PHP Reference: AutoReply.php
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class AdminAutoReplyService {

    private final ChatAutoReplyMapper chatAutoReplyMapper;

    /**
     * 获取自动回复列表
     * PHP Reference: AutoReply.php::index()
     */
    public List<ChatAutoReplyEntity> getList(String appid, Integer userId) {
        QueryWrapper<ChatAutoReplyEntity> wrapper = new QueryWrapper<>();
        String resolvedAppid = TenantContextUtils.resolveAppid(appid);
        if (resolvedAppid != null && !resolvedAppid.trim().isEmpty()) {
            wrapper.eq("appid", resolvedAppid);
        }
        if (userId != null && userId > 0) {
            wrapper.eq("user_id", userId);
        }
        wrapper.orderByDesc("id");
        return chatAutoReplyMapper.selectList(wrapper);
    }

    /**
     * 获取自动回复表单
     * PHP Reference: AutoReply.php::create()
     */
    public Map<String, Object> getForm(Integer id, Integer userId, String appid) {
        Map<String, Object> result = new HashMap<>();

        if (id != null && id > 0) {
            // 编辑模式
            ChatAutoReplyEntity reply = chatAutoReplyMapper.selectById(id);
            if (reply == null) {
                throw new CrmChatException("Auto reply does not exist");
            }
            TenantGuard.ensureOwnedByCurrentTenant(reply.getAppid(), "Auto reply does not exist");
            result.put("reply", reply);
        } else {
            // 创建模式 - 返回空form_rules
            result.put("form_rules", new Object[0]);
            result.put("user_id", userId);
            result.put("appid", TenantContextUtils.resolveAppid(appid));
        }

        return result;
    }

    /**
     * 保存自动回复(创建或更新)
     * PHP Reference: AutoReply.php::save()
     */
    @Transactional(rollbackFor = Exception.class)
    public String saveAutoReply(Integer id, Map<String, Object> data, String appid) {
        String keyword = (String) data.get("keyword");
        String content = (String) data.get("content");
        Integer userId = (Integer) data.getOrDefault("user_id", 0);
        Integer sort = (Integer) data.getOrDefault("sort", 0);

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new CrmChatException("Please enter keyword");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new CrmChatException("Please enter reply content");
        }

        String targetAppid = TenantContextUtils.resolveAppid(appid);

        if (id != null && id > 0) {
            // 更新
            ChatAutoReplyEntity reply = chatAutoReplyMapper.selectById(id);
            if (reply == null) {
                throw new CrmChatException("Auto reply does not exist");
            }
            TenantGuard.ensureOwnedByCurrentTenant(reply.getAppid(), "Auto reply does not exist");
            reply.setKeyword(keyword);
            reply.setContent(content);
            reply.setUserId(userId);
            reply.setSort(sort);
            chatAutoReplyMapper.updateById(reply);
            return "Modified successfully";
        } else {
            // 创建
            ChatAutoReplyEntity reply = new ChatAutoReplyEntity();
            reply.setAppid(targetAppid);
            reply.setKeyword(keyword);
            reply.setContent(content);
            reply.setUserId(userId);
            reply.setSort(sort);
            reply.setAddTime((int) (System.currentTimeMillis() / 1000));
            chatAutoReplyMapper.insert(reply);
            return "Saved successfully";
        }
    }

    /**
     * 删除自动回复
     * PHP Reference: AutoReply.php::delete()
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAutoReply(Integer id, String appid) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        ChatAutoReplyEntity reply = chatAutoReplyMapper.selectById(id);
        if (reply == null) {
            throw new CrmChatException("Auto reply does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(reply.getAppid(), "Auto reply does not exist");

        return chatAutoReplyMapper.deleteById(id) > 0;
    }
}
