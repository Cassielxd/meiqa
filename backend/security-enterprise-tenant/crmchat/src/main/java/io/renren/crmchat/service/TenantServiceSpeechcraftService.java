package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatServiceSpeechcraftMapper;
import io.renren.crmchat.entity.ChatServiceSpeechcraftEntity;
import io.renren.crmchat.security.TenantGuard;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Tenant 快捷回复服务
 * PHP Reference: /app/controller/tenant/chat/ServiceSpeechcraft.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getSpeechcraftList(): 获取快捷回复列表
 *    - kefu_id=0（系统话术）
 *    - 支持title、message、cate_id筛选
 * 2. getSpeechcraftDetail(): 获取快捷回复详情
 * 3. createSpeechcraft(): 创建快捷回复
 *    - 验证title和message非空（通过SpeechcraftValidate）
 *    - 验证message唯一性
 * 4. updateSpeechcraft(): 更新快捷回复
 *    - 验证title和message非空
 *    - 验证message唯一性（排除自己）
 * 5. deleteSpeechcraft(): 删除快捷回复
 *    - 验证快捷回复存在
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantServiceSpeechcraftService {

    private final ChatServiceSpeechcraftMapper chatServiceSpeechcraftMapper;

    /**
     * 获取快捷回复列表
     * GET /api/tenant/chat/speechcraft
     *
     * PHP Reference: ServiceSpeechcraft.php::index()
     *
     * 业务逻辑:
     * 1. kefu_id=0（系统话术）
     * 2. 支持title、message、cate_id筛选
     * 3. appid隔离
     *
     * @param filters      过滤条件
     * @return 快捷回复列表
     */
    public List<ChatServiceSpeechcraftEntity> getSpeechcraftList(Map<String, Object> filters) {
        // PHP: $where['kefu_id'] = 0;
        // PHP: $where["appid"] = $appid;

        QueryWrapper<ChatServiceSpeechcraftEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("kefu_id", 0);

        // title模糊查询
        if (filters.containsKey("title") && filters.get("title") != null && !filters.get("title").toString().isEmpty()) {
            wrapper.like("title", filters.get("title"));
        }

        // message模糊查询
        if (filters.containsKey("message") && filters.get("message") != null && !filters.get("message").toString().isEmpty()) {
            wrapper.like("message", filters.get("message"));
        }

        // cate_id精确查询
        if (filters.containsKey("cate_id") && filters.get("cate_id") != null && !filters.get("cate_id").toString().isEmpty()) {
            wrapper.eq("cate_id", filters.get("cate_id"));
        }

        wrapper.orderByAsc("sort");
        wrapper.orderByDesc("id");

        return chatServiceSpeechcraftMapper.selectList(wrapper);
    }

    /**
     * 获取快捷回复详情
     * GET /api/tenant/chat/speechcraft/:id
     *
     * PHP Reference: ServiceSpeechcraft.php::read()
     *
     * @param id           快捷回复ID
     * @return 快捷回复详情
     */
    public ChatServiceSpeechcraftEntity getSpeechcraftDetail(Integer id) {
        // PHP: $info = $this->services->get($id);
        // PHP: if (!$info) return $this->fail('获取失败');

        if (id == null || id <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Missing required parameter");
        }

        ChatServiceSpeechcraftEntity speechcraft = chatServiceSpeechcraftMapper.selectById(id);
        if (speechcraft == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to retrieve");
        }
        TenantGuard.ensureOwnedByCurrentTenant(speechcraft.getAppid(), "Failed to retrieve");

        return speechcraft;
    }

    /**
     * 创建快捷回复
     * POST /api/tenant/chat/speechcraft
     *
     * PHP Reference: ServiceSpeechcraft.php::save()
     *
     * 业务逻辑:
     * 1. 验证title和message非空（SpeechcraftValidate）
     * 2. 验证message唯一性
     * 3. 设置kefu_id=0, add_time
     *
     * @param data         快捷回复数据
     * @return 新创建的快捷回复ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createSpeechcraft(Map<String, Object> data) {
        // 1. PHP: validate(SpeechcraftValidate::class)->check($data);
        // Validation: title and message are required
        if (!data.containsKey("title") || data.get("title") == null || data.get("title").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Title is required");
        }
        if (!data.containsKey("message") || data.get("message") == null || data.get("message").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Quick reply content is required");
        }

        String title = data.get("title").toString();
        String message = data.get("message").toString();

        // 2. PHP: if ($this->services->count(['message' => $data['message']])) return $this->fail('话术不能重复添加');
        QueryWrapper<ChatServiceSpeechcraftEntity> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("message", message);
        Long count = chatServiceSpeechcraftMapper.selectCount(checkWrapper);
        if (count > 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Quick reply cannot be added repeatedly");
        }

        // 3. 创建快捷回复
        ChatServiceSpeechcraftEntity speechcraft = new ChatServiceSpeechcraftEntity();
        speechcraft.setTitle(title);
        speechcraft.setMessage(message);
        speechcraft.setKefuId(0);
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

        int result = chatServiceSpeechcraftMapper.insert(speechcraft);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to create quick reply");
        }

        return speechcraft.getId();
    }

    /**
     * 更新快捷回复
     * PUT /api/tenant/chat/speechcraft/:id
     *
     * PHP Reference: ServiceSpeechcraft.php::update()
     *
     * 业务逻辑:
     * 1. 验证title和message非空
     * 2. 验证message唯一性（排除自己）
     * 3. 更新数据
     *
     * @param id           快捷回复ID
     * @param data         快捷回复数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSpeechcraft(Integer id, Map<String, Object> data) {
        // 1. 验证数据
        if (!data.containsKey("title") || data.get("title") == null || data.get("title").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Title is required");
        }
        if (!data.containsKey("message") || data.get("message") == null || data.get("message").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Quick reply content is required");
        }

        String title = data.get("title").toString();
        String message = data.get("message").toString();

        // 2. 验证快捷回复存在
        ChatServiceSpeechcraftEntity speechcraft = chatServiceSpeechcraftMapper.selectById(id);
        if (speechcraft == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Quick reply does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(speechcraft.getAppid(), "Quick reply does not exist");

        // 3. PHP: $message = $this->services->get(['message' => $data['message']]);
        // PHP: if ($message && $message['id'] != $id) return $this->fail('话术不能重复添加');
        QueryWrapper<ChatServiceSpeechcraftEntity> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("message", message);
        ChatServiceSpeechcraftEntity existing = chatServiceSpeechcraftMapper.selectOne(checkWrapper);
        if (existing != null && !existing.getId().equals(id)) {
            throw new io.renren.crmchat.exception.CrmChatException("Quick reply cannot be added repeatedly");
        }

        // 4. 更新快捷回复
        speechcraft.setTitle(title);
        speechcraft.setMessage(message);

        if (data.containsKey("cate_id") && data.get("cate_id") != null) {
            speechcraft.setCateId(Integer.parseInt(data.get("cate_id").toString()));
        }

        if (data.containsKey("sort") && data.get("sort") != null) {
            speechcraft.setSort(Integer.parseInt(data.get("sort").toString()));
        }

        int result = chatServiceSpeechcraftMapper.updateById(speechcraft);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to modify");
        }
    }

    /**
     * 删除快捷回复
     * DELETE /api/tenant/chat/speechcraft/:id
     *
     * PHP Reference: ServiceSpeechcraft.php::delete()
     *
     * @param id           快捷回复ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpeechcraft(Integer id) {
        // PHP: if (!$id || !($info = $this->services->get($id))) return $this->fail('删除的话术不存在！');
        if (id == null || id <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Missing required parameter");
        }

        ChatServiceSpeechcraftEntity speechcraft = chatServiceSpeechcraftMapper.selectById(id);
        if (speechcraft == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Quick reply to delete does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(speechcraft.getAppid(), "Quick reply to delete does not exist");

        // PHP: if ($info->delete()) return $this->success('删除成功');
        int result = chatServiceSpeechcraftMapper.deleteById(id);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to delete");
        }
    }
}
