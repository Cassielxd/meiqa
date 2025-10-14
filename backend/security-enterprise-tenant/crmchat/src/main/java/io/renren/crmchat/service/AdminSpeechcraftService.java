package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.crmchat.dao.ChatServiceSpeechcraftMapper;
import io.renren.crmchat.dao.ChatUserLabelCateMapper;
import io.renren.crmchat.entity.ChatServiceSpeechcraftEntity;
import io.renren.crmchat.entity.ChatUserLabelCateEntity;
import io.renren.crmchat.exception.CrmChatException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Admin Speechcraft Service - 管理员话术管理
 * PHP Reference: ServiceSpeechcraft.php + ServiceSpeechcraftCate.php
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class AdminSpeechcraftService {

    private final ChatServiceSpeechcraftMapper chatServiceSpeechcraftMapper;
    private final ChatUserLabelCateMapper chatUserLabelCateMapper;

    // ==================== 话术管理 ====================

    /**
     * 获取话术列表
     * PHP Reference: ServiceSpeechcraft.php::index()
     */
    public Map<String, Object> getSpeechcraftList(Map<String, Object> where) {
        QueryWrapper<ChatServiceSpeechcraftEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("kefu_id", 0);  // 0=系统话术

        // title模糊查询
        String title = (String) where.get("title");
        if (title != null && !title.trim().isEmpty()) {
            wrapper.like("title", title);
        }

        // message模糊查询
        String message = (String) where.get("message");
        if (message != null && !message.trim().isEmpty()) {
            wrapper.like("message", message);
        }

        // cate_id精确查询
        Integer cateId = (Integer) where.get("cate_id");
        if (cateId != null && cateId > 0) {
            wrapper.eq("cate_id", cateId);
        }

        // 分页
        Integer page = (Integer) where.getOrDefault("page", 1);
        Integer limit = (Integer) where.getOrDefault("limit", 20);
        Page<ChatServiceSpeechcraftEntity> pageObj = new Page<>(page, limit);

        IPage<ChatServiceSpeechcraftEntity> result = chatServiceSpeechcraftMapper.selectPage(pageObj, wrapper);

        Map<String, Object> response = new HashMap<>();
        response.put("list", result.getRecords());
        response.put("count", (int) result.getTotal());
        return response;
    }

    /**
     * 获取创建话术表单
     * PHP Reference: ServiceSpeechcraft.php::create()
     */
    public Map<String, Object> createSpeechcraftForm() {
        Map<String, Object> result = new HashMap<>();
        result.put("form_rules", new ArrayList<>());
        return result;
    }

    /**
     * 保存话术
     * PHP Reference: ServiceSpeechcraft.php::save()
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveSpeechcraft(Map<String, Object> data) {
        String title = (String) data.get("title");
        String message = (String) data.get("message");
        Integer cateId = (Integer) data.getOrDefault("cate_id", 0);
        Integer sort = (Integer) data.getOrDefault("sort", 0);

        if (message == null || message.trim().isEmpty()) {
            throw new CrmChatException("Please enter quick reply content");
        }

        // 检查message是否重复
        long count = chatServiceSpeechcraftMapper.selectCount(
                new QueryWrapper<ChatServiceSpeechcraftEntity>().eq("message", message)
        );
        if (count > 0) {
            throw new CrmChatException("Quick reply cannot be added repeatedly");
        }

        ChatServiceSpeechcraftEntity speechcraft = new ChatServiceSpeechcraftEntity();
        speechcraft.setKefuId(0);  // 0=系统话术
        speechcraft.setTitle(title);
        speechcraft.setMessage(message);
        speechcraft.setCateId(cateId);
        speechcraft.setSort(sort);
        speechcraft.setAddTime((int) (System.currentTimeMillis() / 1000));

        return chatServiceSpeechcraftMapper.insert(speechcraft) > 0;
    }

    /**
     * 获取话术详情
     * PHP Reference: ServiceSpeechcraft.php::read()
     */
    public ChatServiceSpeechcraftEntity getSpeechcraft(Integer id) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Failed to retrieve");
        }

        ChatServiceSpeechcraftEntity speechcraft = chatServiceSpeechcraftMapper.selectById(id);
        if (speechcraft == null) {
            throw new CrmChatException("Failed to retrieve");
        }

        return speechcraft;
    }

    /**
     * 获取编辑话术表单
     * PHP Reference: ServiceSpeechcraft.php::edit()
     */
    public Map<String, Object> updateSpeechcraftForm(Integer id) {
        ChatServiceSpeechcraftEntity speechcraft = getSpeechcraft(id);

        Map<String, Object> result = new HashMap<>();
        result.put("speechcraft", speechcraft);
        result.put("form_rules", new ArrayList<>());
        return result;
    }

    /**
     * 更新话术
     * PHP Reference: ServiceSpeechcraft.php::update()
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSpeechcraft(Integer id, Map<String, Object> data) {
        ChatServiceSpeechcraftEntity speechcraft = chatServiceSpeechcraftMapper.selectById(id);
        if (speechcraft == null) {
            throw new CrmChatException("Quick reply does not exist");
        }

        String title = (String) data.get("title");
        String message = (String) data.get("message");
        Integer cateId = (Integer) data.getOrDefault("cate_id", 0);
        Integer sort = (Integer) data.getOrDefault("sort", 0);

        if (message == null || message.trim().isEmpty()) {
            throw new CrmChatException("Please enter quick reply content");
        }

        // 检查message是否重复（排除自己）
        ChatServiceSpeechcraftEntity existing = chatServiceSpeechcraftMapper.selectOne(
                new QueryWrapper<ChatServiceSpeechcraftEntity>().eq("message", message)
        );
        if (existing != null && !existing.getId().equals(id)) {
            throw new CrmChatException("Quick reply cannot be added repeatedly");
        }

        speechcraft.setTitle(title);
        speechcraft.setMessage(message);
        speechcraft.setCateId(cateId);
        speechcraft.setSort(sort);

        return chatServiceSpeechcraftMapper.updateById(speechcraft) > 0;
    }

    /**
     * 删除话术
     * PHP Reference: ServiceSpeechcraft.php::delete()
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSpeechcraft(Integer id) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Quick reply to delete does not exist");
        }

        ChatServiceSpeechcraftEntity speechcraft = chatServiceSpeechcraftMapper.selectById(id);
        if (speechcraft == null) {
            throw new CrmChatException("Quick reply to delete does not exist");
        }

        return chatServiceSpeechcraftMapper.deleteById(id) > 0;
    }

    // ==================== 话术分类管理 ====================

    /**
     * 获取话术分类列表
     * PHP Reference: ServiceSpeechcraftCate.php::index()
     */
    public List<ChatUserLabelCateEntity> getSpeechcraftCateList(Map<String, Object> where, String appid) {
        QueryWrapper<ChatUserLabelCateEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("appid", appid);
        wrapper.eq("type", 1);  // 1=话术分类
        wrapper.eq("owner_id", 0);  // 0=系统分类

        // name模糊查询
        String name = (String) where.get("name");
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like("name", name);
        }

        wrapper.orderByAsc("sort");
        return chatUserLabelCateMapper.selectList(wrapper);
    }

    /**
     * 获取创建话术分类表单
     * PHP Reference: ServiceSpeechcraftCate.php::create()
     */
    public Map<String, Object> createSpeechcraftCateForm() {
        Map<String, Object> result = new HashMap<>();
        result.put("form_rules", new ArrayList<>());
        return result;
    }

    /**
     * 保存话术分类
     * PHP Reference: ServiceSpeechcraftCate.php::save()
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean saveSpeechcraftCate(Map<String, Object> data, String appid) {
        String name = (String) data.get("name");
        Integer sort = (Integer) data.getOrDefault("sort", 0);

        if (name == null || name.trim().isEmpty()) {
            throw new CrmChatException("Please enter category name");
        }

        // 检查分类名称是否重复
        long count = chatUserLabelCateMapper.selectCount(
                new QueryWrapper<ChatUserLabelCateEntity>()
                        .eq("appid", appid)
                        .eq("name", name)
                        .eq("type", 1)
        );
        if (count > 0) {
            throw new CrmChatException("Category already exists");
        }

        ChatUserLabelCateEntity cate = new ChatUserLabelCateEntity();
        cate.setPid(0);
        cate.setOwnerId(0);
        cate.setName(name);
        cate.setSort(sort);
        cate.setType(1);  // 1=话术分类
        cate.setAddTime((int) (System.currentTimeMillis() / 1000));

        return chatUserLabelCateMapper.insert(cate) > 0;
    }

    /**
     * 获取话术分类详情
     * PHP Reference: ServiceSpeechcraftCate.php::read()
     */
    public ChatUserLabelCateEntity getSpeechcraftCate(Integer id, String appid) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Failed to retrieve");
        }

        ChatUserLabelCateEntity cate = chatUserLabelCateMapper.selectById(id);
        if (cate == null || cate.getType() != 1) {
            throw new CrmChatException("Failed to retrieve");
        }

        return cate;
    }

    /**
     * 获取编辑话术分类表单
     * PHP Reference: ServiceSpeechcraftCate.php::edit()
     */
    public Map<String, Object> editSpeechcraftCateForm(Integer id, String appid) {
        ChatUserLabelCateEntity cate = getSpeechcraftCate(id, appid);

        Map<String, Object> result = new HashMap<>();
        result.put("cate", cate);
        result.put("form_rules", new ArrayList<>());
        return result;
    }

    /**
     * 更新话术分类
     * PHP Reference: ServiceSpeechcraftCate.php::update()
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSpeechcraftCate(Integer id, Map<String, Object> data, String appid) {
        ChatUserLabelCateEntity cate = getSpeechcraftCate(id, appid);

        String name = (String) data.get("name");
        Integer sort = (Integer) data.getOrDefault("sort", 0);

        if (name == null || name.trim().isEmpty()) {
            throw new CrmChatException("Please enter category name");
        }

        cate.setName(name);
        cate.setSort(sort);

        return chatUserLabelCateMapper.updateById(cate) > 0;
    }

    /**
     * 删除话术分类
     * PHP Reference: ServiceSpeechcraftCate.php::delete()
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSpeechcraftCate(Integer id, String appid) {
        ChatUserLabelCateEntity cate = getSpeechcraftCate(id, appid);
        return chatUserLabelCateMapper.deleteById(id) > 0;
    }
}
