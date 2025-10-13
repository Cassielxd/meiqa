package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.renren.crmchat.dao.ChatServiceSpeechcraftMapper;
import io.renren.crmchat.entity.ChatServiceSpeechcraftEntity;
import io.renren.crmchat.security.TenantGuard;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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
    private final io.renren.crmchat.dao.ChatServiceSpeechcraftCateMapper chatServiceSpeechcraftCateMapper;

    /**
     * 获取快捷回复列表
     * GET /api/tenant/chat/speechcraft
     *
     * PHP Reference: ServiceSpeechcraft.php::index()
     * PHP Returns: compact('list', 'count') = {list: [...], count: X}
     *
     * 业务逻辑:
     * 1. kefu_id=0（系统话术）
     * 2. 支持title、message、cate_id筛选
     * 3. appid隔离
     * 4. 分页支持
     *
     * @param filters      过滤条件
     * @param page         页码（默认1）
     * @param limit        每页数量（默认20）
     * @return {list: [...], count: X}
     */
    public Map<String, Object> getSpeechcraftList(Map<String, Object> filters, Integer page, Integer limit) {
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

        // PHP: [$page, $limit] = $this->getPageValue();
        // PHP: $list = $this->dao->getSpeechcraftList($where, $page, $limit);
        // PHP: $count = $this->dao->count($where);
        // PHP: return compact('list', 'count');

        IPage<ChatServiceSpeechcraftEntity> pageObj = new Page<>(page, limit);
        IPage<ChatServiceSpeechcraftEntity> result = chatServiceSpeechcraftMapper.selectPage(pageObj, wrapper);

        Map<String, Object> response = new HashMap<>();
        response.put("list", result.getRecords());
        response.put("count", result.getTotal());

        return response;
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
        // PHP Validation Rules: 'title' => 'max:50', 'message' => 'require|max:500'

        // Validation: title and message are required
        if (!data.containsKey("title") || data.get("title") == null || data.get("title").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Title is required");
        }
        if (!data.containsKey("message") || data.get("message") == null || data.get("message").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Quick reply content is required");
        }

        String title = data.get("title").toString();
        String message = data.get("message").toString();

        // 检测前端发送的是否为事件对象而非实际值 (BUG: 前端form-create可能发送 {isTrusted=true})
        if (title.contains("{isTrusted=") || title.equals("{}") || title.startsWith("{") && title.endsWith("}") && !title.contains(":")) {
            throw new io.renren.crmchat.exception.CrmChatException("标题格式错误，请检查前端表单数据收集逻辑");
        }
        if (message.contains("{isTrusted=") || message.equals("{}") || message.startsWith("{") && message.endsWith("}") && !message.contains(":")) {
            throw new io.renren.crmchat.exception.CrmChatException("内容格式错误，请检查前端表单数据收集逻辑");
        }

        // Length validation: PHP max:50 for title, max:500 for message
        if (title.length() > 50) {
            throw new io.renren.crmchat.exception.CrmChatException("标题长度不能超过50个字符");
        }
        if (message.length() > 500) {
            throw new io.renren.crmchat.exception.CrmChatException("话术内容长度不能超过500个字符");
        }

        // 2. PHP: if ($this->services->count(['message' => $data['message']])) return $this->fail('话术不能重复添加');
        // IMPORTANT: 必须添加租户隔离和kefu_id条件，否则会检查整个表
        String appid = io.renren.crmchat.security.TenantSecurityUtils.requireAppid();
        QueryWrapper<ChatServiceSpeechcraftEntity> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("message", message);
        checkWrapper.eq("kefu_id", 0);
        checkWrapper.eq("appid", appid);
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

        // cate_id: 处理空字符串的情况
        if (data.containsKey("cate_id") && data.get("cate_id") != null && !data.get("cate_id").toString().trim().isEmpty()) {
            speechcraft.setCateId(Integer.parseInt(data.get("cate_id").toString()));
        } else {
            speechcraft.setCateId(0);
        }

        // sort: 处理空字符串的情况
        if (data.containsKey("sort") && data.get("sort") != null && !data.get("sort").toString().trim().isEmpty()) {
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
        // PHP Validation Rules: 'title' => 'max:50', 'message' => 'require|max:500'

        if (!data.containsKey("title") || data.get("title") == null || data.get("title").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Title is required");
        }
        if (!data.containsKey("message") || data.get("message") == null || data.get("message").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Quick reply content is required");
        }

        String title = data.get("title").toString();
        String message = data.get("message").toString();

        // 检测前端发送的是否为事件对象而非实际值 (BUG: 前端form-create可能发送 {isTrusted=true})
        if (title.contains("{isTrusted=") || title.equals("{}") || title.startsWith("{") && title.endsWith("}") && !title.contains(":")) {
            throw new io.renren.crmchat.exception.CrmChatException("标题格式错误，请检查前端表单数据收集逻辑");
        }
        if (message.contains("{isTrusted=") || message.equals("{}") || message.startsWith("{") && message.endsWith("}") && !message.contains(":")) {
            throw new io.renren.crmchat.exception.CrmChatException("内容格式错误，请检查前端表单数据收集逻辑");
        }

        // Length validation: PHP max:50 for title, max:500 for message
        if (title.length() > 50) {
            throw new io.renren.crmchat.exception.CrmChatException("标题长度不能超过50个字符");
        }
        if (message.length() > 500) {
            throw new io.renren.crmchat.exception.CrmChatException("话术内容长度不能超过500个字符");
        }

        // 2. 验证快捷回复存在
        ChatServiceSpeechcraftEntity speechcraft = chatServiceSpeechcraftMapper.selectById(id);
        if (speechcraft == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Quick reply does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(speechcraft.getAppid(), "Quick reply does not exist");

        // 3. PHP: $message = $this->services->get(['message' => $data['message']]);
        // PHP: if ($message && $message['id'] != $id) return $this->fail('话术不能重复添加');
        // IMPORTANT: 必须添加租户隔离和kefu_id条件，否则会检查整个表
        String appid = io.renren.crmchat.security.TenantSecurityUtils.requireAppid();
        QueryWrapper<ChatServiceSpeechcraftEntity> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("message", message);
        checkWrapper.eq("kefu_id", 0);
        checkWrapper.eq("appid", appid);
        ChatServiceSpeechcraftEntity existing = chatServiceSpeechcraftMapper.selectOne(checkWrapper);
        if (existing != null && !existing.getId().equals(id)) {
            throw new io.renren.crmchat.exception.CrmChatException("Quick reply cannot be added repeatedly");
        }

        // 4. 更新快捷回复
        speechcraft.setTitle(title);
        speechcraft.setMessage(message);

        // cate_id: 处理空字符串的情况
        if (data.containsKey("cate_id") && data.get("cate_id") != null && !data.get("cate_id").toString().trim().isEmpty()) {
            speechcraft.setCateId(Integer.parseInt(data.get("cate_id").toString()));
        }

        // sort: 处理空字符串的情况
        if (data.containsKey("sort") && data.get("sort") != null && !data.get("sort").toString().trim().isEmpty()) {
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

    /**
     * 获取创建快捷回复表单配置
     * GET /api/tenant/chat/speechcraft/create
     *
     * PHP Reference: ServiceSpeechcraft.php::create()
     */
    public Map<String, Object> getCreateForm() {
        String appid = io.renren.crmchat.security.TenantSecurityUtils.requireAppid();

        // 获取分类列表用于下拉选择
        QueryWrapper<io.renren.crmchat.entity.ChatServiceSpeechcraftCateEntity> cateWrapper = new QueryWrapper<>();
        cateWrapper.eq("owner_id", 0);
        cateWrapper.eq("type", 1);
        cateWrapper.eq("appid", appid);
        cateWrapper.orderByAsc("sort");
        cateWrapper.orderByDesc("id");
        List<io.renren.crmchat.entity.ChatServiceSpeechcraftCateEntity> categories = chatServiceSpeechcraftCateMapper.selectList(cateWrapper);

        // 构建分类选项
        List<Map<String, Object>> cateOptions = new java.util.ArrayList<>();
        for (io.renren.crmchat.entity.ChatServiceSpeechcraftCateEntity cate : categories) {
            Map<String, Object> option = new java.util.HashMap<>();
            option.put("value", cate.getId());
            option.put("label", cate.getName());
            cateOptions.add(option);
        }

        // 构建表单字段
        List<Map<String, Object>> rules = new java.util.ArrayList<>();

        // 标题字段 - PHP: FormBuilder::textarea('title', '话术标题', ...) 无required
        // IMPORTANT: form-create expects type="input" with props.type="textarea", not type="textarea" directly
        Map<String, Object> titleRule = new java.util.HashMap<>();
        titleRule.put("type", "input");  // Must be "input", not "textarea"
        titleRule.put("field", "title");
        titleRule.put("title", "标题");
        titleRule.put("value", "");
        Map<String, Object> titleProps = new java.util.HashMap<>();
        titleProps.put("type", "textarea");
        titleProps.put("rows", 3);
        titleProps.put("maxlength", 50);
        titleRule.put("props", titleProps);

        // 详情字段 - PHP: FormBuilder::textarea('message', '话术内容', ...)->required()
        // 注意：PHP的required()在后端验证，前端不显示必填标记
        // IMPORTANT: form-create expects type="input" with props.type="textarea", not type="textarea" directly
        Map<String, Object> messageRule = new java.util.HashMap<>();
        messageRule.put("type", "input");  // Must be "input", not "textarea"
        messageRule.put("field", "message");
        messageRule.put("title", "详情");
        messageRule.put("value", "");
        Map<String, Object> messageProps = new java.util.HashMap<>();
        messageProps.put("type", "textarea");
        messageProps.put("rows", 5);
        messageProps.put("maxlength", 500);
        messageRule.put("props", messageProps);

        // 分类字段
        Map<String, Object> cateRule = new java.util.HashMap<>();
        cateRule.put("type", "select");
        cateRule.put("field", "cate_id");
        cateRule.put("title", "分类");
        cateRule.put("value", "");
        cateRule.put("options", cateOptions);

        // 排序字段
        Map<String, Object> sortRule = new java.util.HashMap<>();
        sortRule.put("type", "inputNumber");
        sortRule.put("field", "sort");
        sortRule.put("title", "排序");
        sortRule.put("value", 0);

        // PHP field order: cate_id → title → message → sort
        rules.add(cateRule);
        rules.add(titleRule);
        rules.add(messageRule);
        rules.add(sortRule);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("rules", rules);
        result.put("title", "添加话术");
        result.put("action", "/chat/speechcraft");
        result.put("method", "POST");
        result.put("info", "");
        result.put("status", true);

        return result;
    }

    /**
     * 获取编辑快捷回复表单配置
     * GET /api/tenant/chat/speechcraft/:id/edit
     *
     * PHP Reference: ServiceSpeechcraft.php::edit()
     */
    public Map<String, Object> getEditForm(Integer id) {
        String appid = io.renren.crmchat.security.TenantSecurityUtils.requireAppid();

        // 获取快捷回复数据
        ChatServiceSpeechcraftEntity speechcraft = chatServiceSpeechcraftMapper.selectById(id);
        if (speechcraft == null) {
            throw new io.renren.crmchat.exception.CrmChatException("快捷回复没有查询到");
        }
        TenantGuard.ensureOwnedByCurrentTenant(speechcraft.getAppid(), "快捷回复没有查询到");

        // 获取分类列表
        QueryWrapper<io.renren.crmchat.entity.ChatServiceSpeechcraftCateEntity> cateWrapper = new QueryWrapper<>();
        cateWrapper.eq("owner_id", 0);
        cateWrapper.eq("type", 1);
        cateWrapper.eq("appid", appid);
        cateWrapper.orderByAsc("sort");
        cateWrapper.orderByDesc("id");
        List<io.renren.crmchat.entity.ChatServiceSpeechcraftCateEntity> categories = chatServiceSpeechcraftCateMapper.selectList(cateWrapper);

        // 构建分类选项
        List<Map<String, Object>> cateOptions = new java.util.ArrayList<>();
        for (io.renren.crmchat.entity.ChatServiceSpeechcraftCateEntity cate : categories) {
            Map<String, Object> option = new java.util.HashMap<>();
            option.put("value", cate.getId());
            option.put("label", cate.getName());
            cateOptions.add(option);
        }

        // 构建表单字段（填充现有数据）
        List<Map<String, Object>> rules = new java.util.ArrayList<>();

        // 标题字段 - PHP: FormBuilder::textarea('title', '话术标题', ...) 无required
        // IMPORTANT: form-create expects type="input" with props.type="textarea", not type="textarea" directly
        Map<String, Object> titleRule = new java.util.HashMap<>();
        titleRule.put("type", "input");  // Must be "input", not "textarea"
        titleRule.put("field", "title");
        titleRule.put("title", "标题");
        titleRule.put("value", speechcraft.getTitle() != null ? speechcraft.getTitle() : "");
        Map<String, Object> titleProps = new java.util.HashMap<>();
        titleProps.put("type", "textarea");
        titleProps.put("rows", 3);
        titleProps.put("maxlength", 50);
        titleRule.put("props", titleProps);

        // 详情字段 - PHP: FormBuilder::textarea('message', '话术内容', ...)->required()
        // 注意：PHP的required()在后端验证，前端不显示必填标记
        // IMPORTANT: form-create expects type="input" with props.type="textarea", not type="textarea" directly
        Map<String, Object> messageRule = new java.util.HashMap<>();
        messageRule.put("type", "input");  // Must be "input", not "textarea"
        messageRule.put("field", "message");
        messageRule.put("title", "详情");
        messageRule.put("value", speechcraft.getMessage() != null ? speechcraft.getMessage() : "");
        Map<String, Object> messageProps = new java.util.HashMap<>();
        messageProps.put("type", "textarea");
        messageProps.put("rows", 5);
        messageProps.put("maxlength", 500);
        messageRule.put("props", messageProps);

        // 分类字段
        Map<String, Object> cateRule = new java.util.HashMap<>();
        cateRule.put("type", "select");
        cateRule.put("field", "cate_id");
        cateRule.put("title", "分类");
        cateRule.put("value", speechcraft.getCateId() != null ? speechcraft.getCateId() : "");
        cateRule.put("options", cateOptions);

        // 排序字段
        Map<String, Object> sortRule = new java.util.HashMap<>();
        sortRule.put("type", "inputNumber");
        sortRule.put("field", "sort");
        sortRule.put("title", "排序");
        sortRule.put("value", speechcraft.getSort() != null ? speechcraft.getSort() : 0);

        // PHP field order: cate_id → title → message → sort
        rules.add(cateRule);
        rules.add(titleRule);
        rules.add(messageRule);
        rules.add(sortRule);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("rules", rules);
        result.put("title", "修改话术");
        result.put("action", "/chat/speechcraft/" + id);
        result.put("method", "PUT");
        result.put("info", "");
        result.put("status", true);

        return result;
    }
}
