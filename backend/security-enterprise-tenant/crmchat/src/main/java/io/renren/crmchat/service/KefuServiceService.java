package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatServiceSpeechcraftCateMapper;
import io.renren.crmchat.dao.ChatServiceSpeechcraftMapper;
import io.renren.crmchat.entity.ChatServiceSpeechcraftCateEntity;
import io.renren.crmchat.entity.ChatServiceSpeechcraftEntity;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.security.TenantGuard;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Kefu 快捷话术服务，对应 PHP /app/controller/kefu/Service.php。
 */
@Slf4j
@Service
@AllArgsConstructor
public class KefuServiceService {

    private final ChatServiceSpeechcraftMapper chatServiceSpeechcraftMapper;
    private final ChatServiceSpeechcraftCateMapper chatServiceSpeechcraftCateMapper;

    /**
     * 获取个人快捷话术列表。
     */
    public List<ChatServiceSpeechcraftEntity> getSpeechcraftList(Map<String, Object> filters, Integer kefuId) {
        QueryWrapper<ChatServiceSpeechcraftEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("kefu_id", kefuId);

        if (filters.containsKey("title") && filters.get("title") != null && !filters.get("title").toString().isEmpty()) {
            wrapper.like("title", filters.get("title"));
        }
        if (filters.containsKey("message") && filters.get("message") != null && !filters.get("message").toString().isEmpty()) {
            wrapper.like("message", filters.get("message"));
        }
        if (filters.containsKey("cate_id") && filters.get("cate_id") != null && !filters.get("cate_id").toString().isEmpty()) {
            wrapper.eq("cate_id", filters.get("cate_id"));
        }

        wrapper.orderByAsc("sort");
        wrapper.orderByDesc("id");
        return chatServiceSpeechcraftMapper.selectList(wrapper);
    }

    /**
     * 获取个人快捷话术详情。
     */
    public ChatServiceSpeechcraftEntity getSpeechcraftDetail(Integer id, Integer kefuId) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }
        ChatServiceSpeechcraftEntity speechcraft = chatServiceSpeechcraftMapper.selectById(id);
        if (speechcraft == null || !speechcraft.getKefuId().equals(kefuId)) {
            throw new CrmChatException("Failed to retrieve");
        }
        TenantGuard.ensureOwnedByCurrentTenant(speechcraft.getAppid(), "Failed to retrieve");
        return speechcraft;
    }

    /**
     * 创建个人快捷话术。
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createSpeechcraft(Map<String, Object> data, Integer kefuId) {
        if (!data.containsKey("title") || data.get("title") == null || data.get("title").toString().trim().isEmpty()) {
            throw new CrmChatException("Title is required");
        }
        if (!data.containsKey("message") || data.get("message") == null || data.get("message").toString().trim().isEmpty()) {
            throw new CrmChatException("Quick reply content is required");
        }

        String message = data.get("message").toString();
        QueryWrapper<ChatServiceSpeechcraftEntity> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("message", message);
        checkWrapper.eq("kefu_id", kefuId);
        if (chatServiceSpeechcraftMapper.selectCount(checkWrapper) > 0) {
            throw new CrmChatException("Quick reply cannot be added repeatedly");
        }

        ChatServiceSpeechcraftEntity speechcraft = new ChatServiceSpeechcraftEntity();
        speechcraft.setTitle(data.get("title").toString());
        speechcraft.setMessage(message);
        speechcraft.setKefuId(kefuId);
        speechcraft.setAddTime((int) (System.currentTimeMillis() / 1000));

        if (data.containsKey("cate_id") && data.get("cate_id") != null) {
            speechcraft.setCateId(Integer.parseInt(data.get("cate_id").toString()));
        } else {
            speechcraft.setCateId(0);
        }
        if (data.containsKey("sort") && data.get("sort") != null) {
            speechcraft.setSort(Integer.parseInt(data.get("sort").toString()));
        } else {
            speechcraft.setSort(0);
        }

        if (chatServiceSpeechcraftMapper.insert(speechcraft) <= 0) {
            throw new CrmChatException("Failed to create quick reply");
        }
        return speechcraft.getId();
    }

    /**
     * 更新个人快捷话术。
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSpeechcraft(Integer id, Map<String, Object> data, Integer kefuId) {
        if (!data.containsKey("title") || data.get("title") == null || data.get("title").toString().trim().isEmpty()) {
            throw new CrmChatException("Title is required");
        }
        if (!data.containsKey("message") || data.get("message") == null || data.get("message").toString().trim().isEmpty()) {
            throw new CrmChatException("Quick reply content is required");
        }

        ChatServiceSpeechcraftEntity speechcraft = chatServiceSpeechcraftMapper.selectById(id);
        if (speechcraft == null || !speechcraft.getKefuId().equals(kefuId)) {
            throw new CrmChatException("Quick reply does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(speechcraft.getAppid(), "Quick reply does not exist");

        String message = data.get("message").toString();
        QueryWrapper<ChatServiceSpeechcraftEntity> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("message", message);
        checkWrapper.eq("kefu_id", kefuId);
        ChatServiceSpeechcraftEntity existing = chatServiceSpeechcraftMapper.selectOne(checkWrapper);
        if (existing != null && !existing.getId().equals(id)) {
            throw new CrmChatException("Quick reply cannot be added repeatedly");
        }

        speechcraft.setTitle(data.get("title").toString());
        speechcraft.setMessage(message);
        if (data.containsKey("cate_id") && data.get("cate_id") != null) {
            speechcraft.setCateId(Integer.parseInt(data.get("cate_id").toString()));
        }
        if (data.containsKey("sort") && data.get("sort") != null) {
            speechcraft.setSort(Integer.parseInt(data.get("sort").toString()));
        }

        if (chatServiceSpeechcraftMapper.updateById(speechcraft) <= 0) {
            throw new CrmChatException("Failed to modify");
        }
    }

    /**
     * 删除个人快捷话术。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpeechcraft(Integer id, Integer kefuId) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }
        ChatServiceSpeechcraftEntity speechcraft = chatServiceSpeechcraftMapper.selectById(id);
        if (speechcraft == null || !speechcraft.getKefuId().equals(kefuId)) {
            throw new CrmChatException("Quick reply to delete does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(speechcraft.getAppid(), "Quick reply to delete does not exist");
        if (chatServiceSpeechcraftMapper.deleteById(id) <= 0) {
            throw new CrmChatException("Failed to delete");
        }
    }

    /**
     * 获取个人快捷话术分类列表。
     */
    public List<ChatServiceSpeechcraftCateEntity> getCateList(Map<String, Object> filters, Integer kefuId) {
        QueryWrapper<ChatServiceSpeechcraftCateEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("owner_id", kefuId);
        wrapper.eq("type", 1);
        if (filters.containsKey("name") && filters.get("name") != null && !filters.get("name").toString().isEmpty()) {
            wrapper.like("name", filters.get("name"));
        }
        wrapper.orderByAsc("sort");
        wrapper.orderByDesc("id");
        return chatServiceSpeechcraftCateMapper.selectList(wrapper);
    }

    /**
     * 创建分类。
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createCate(Map<String, Object> data, Integer kefuId) {
        if (!data.containsKey("name") || data.get("name") == null || data.get("name").toString().trim().isEmpty()) {
            throw new CrmChatException("Category name is required");
        }
        String name = data.get("name").toString();

        QueryWrapper<ChatServiceSpeechcraftCateEntity> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("name", name);
        checkWrapper.eq("owner_id", kefuId);
        if (chatServiceSpeechcraftCateMapper.selectCount(checkWrapper) > 0) {
            throw new CrmChatException("Category name already exists");
        }

        ChatServiceSpeechcraftCateEntity cate = new ChatServiceSpeechcraftCateEntity();
        cate.setName(name);
        cate.setOwnerId(kefuId);
        cate.setType(1);
        cate.setAddTime((int) (System.currentTimeMillis() / 1000));
        if (data.containsKey("sort") && data.get("sort") != null) {
            cate.setSort(Integer.parseInt(data.get("sort").toString()));
        } else {
            cate.setSort(0);
        }

        if (chatServiceSpeechcraftCateMapper.insert(cate) <= 0) {
            throw new CrmChatException("Failed to add");
        }
        return cate.getId();
    }

    /**
     * 更新分类。
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCate(Integer id, Map<String, Object> data, Integer kefuId) {
        if (!data.containsKey("name") || data.get("name") == null || data.get("name").toString().trim().isEmpty()) {
            throw new CrmChatException("Category name is required");
        }
        ChatServiceSpeechcraftCateEntity cate = chatServiceSpeechcraftCateMapper.selectById(id);
        if (cate == null || !cate.getOwnerId().equals(kefuId)) {
            throw new CrmChatException("Category does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(cate.getAppid(), "Category does not exist");

        cate.setName(data.get("name").toString());
        if (data.containsKey("sort") && data.get("sort") != null) {
            cate.setSort(Integer.parseInt(data.get("sort").toString()));
        }

        if (chatServiceSpeechcraftCateMapper.updateById(cate) <= 0) {
            throw new CrmChatException("Failed to modify");
        }
    }

    /**
     * 删除分类。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCate(Integer id, Integer kefuId) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }
        ChatServiceSpeechcraftCateEntity cate = chatServiceSpeechcraftCateMapper.selectById(id);
        if (cate == null || !cate.getOwnerId().equals(kefuId)) {
            throw new CrmChatException("Category to delete does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(cate.getAppid(), "Category to delete does not exist");

        if (chatServiceSpeechcraftCateMapper.deleteById(id) <= 0) {
            throw new CrmChatException("Failed to delete");
        }
    }
}
