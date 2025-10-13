package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatServiceSpeechcraftCateMapper;
import io.renren.crmchat.entity.ChatServiceSpeechcraftCateEntity;
import io.renren.crmchat.security.TenantGuard;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Tenant 快捷回复分类服务
 * PHP Reference: /app/controller/tenant/chat/ServiceSpeechcraftCate.php
 *
 * 核心业务逻辑（严格参考PHP）:
 * 1. getCateList(): 获取分类列表
 *    - owner_id=0（系统分类）
 *    - type=1（快捷回复分类）
 *    - 支持name模糊查询
 * 2. getCateDetail(): 获取分类详情
 * 3. createCate(): 创建分类
 *    - 验证name非空
 *    - 验证name唯一性
 * 4. updateCate(): 更新分类
 *    - 验证name非空
 *    - 验证分类存在
 * 5. deleteCate(): 删除分类
 *    - 验证分类存在
 *
 * @author CRMChat Team
 */
@Slf4j
@Service
@AllArgsConstructor
public class TenantServiceSpeechcraftCateService {

    private final ChatServiceSpeechcraftCateMapper chatServiceSpeechcraftCateMapper;
    private final io.renren.crmchat.formbuilder.FormBuilder formBuilder;

    /**
     * 获取分类列表
     * GET /api/tenant/chat/speechcraft_cate
     *
     * PHP Reference: ServiceSpeechcraftCate.php::index()
     *
     * 业务逻辑:
     * 1. owner_id=0（系统分类）
     * 2. type=1（快捷回复分类）
     * 3. 支持name模糊查询
     * 4. appid隔离
     *
     * @param filters      过滤条件
     * @return 分类列表
     */
    public List<ChatServiceSpeechcraftCateEntity> getCateList(Map<String, Object> filters) {
        // PHP: $where['owner_id'] = 0;
        // PHP: $where['type'] = 1;
        // PHP: $where['appid'] = $appid;

        QueryWrapper<ChatServiceSpeechcraftCateEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("owner_id", 0);
        wrapper.eq("type", 1);

        // name模糊查询
        if (filters.containsKey("name") && filters.get("name") != null && !filters.get("name").toString().isEmpty()) {
            wrapper.like("name", filters.get("name"));
        }

        wrapper.orderByAsc("sort");
        wrapper.orderByDesc("id");

        return chatServiceSpeechcraftCateMapper.selectList(wrapper);
    }

    /**
     * 获取分类详情
     * GET /api/tenant/chat/speechcraft_cate/:id
     *
     * PHP Reference: ServiceSpeechcraftCate.php::read()
     *
     * @param id           分类ID
     * @return 分类详情
     */
    public ChatServiceSpeechcraftCateEntity getCateDetail(Integer id) {
        // PHP: $info = $this->services->get($id);
        // PHP: if (!$info) return $this->fail('获取失败');

        if (id == null || id <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Missing required parameter");
        }

        ChatServiceSpeechcraftCateEntity cate = chatServiceSpeechcraftCateMapper.selectById(id);
        if (cate == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to retrieve");
        }
        TenantGuard.ensureOwnedByCurrentTenant(cate.getAppid(), "Failed to retrieve");

        return cate;
    }

    /**
     * 创建分类
     * POST /api/tenant/chat/speechcraft_cate
     *
     * PHP Reference: ServiceSpeechcraftCate.php::save()
     *
     * 业务逻辑:
     * 1. 验证name非空
     * 2. 验证name唯一性
     * 3. 设置type=1, owner_id=0, add_time
     *
     * @param data         分类数据
     * @return 新创建的分类ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createCate(Map<String, Object> data) {
        // 1. PHP: if (!$data['name']) return $this->fail('请输入分类名称');
        if (!data.containsKey("name") || data.get("name") == null || data.get("name").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter category name");
        }

        String name = data.get("name").toString();

        // 2. PHP: if ($this->services->count(['name' => $data['name']])) return $this->fail('分类已存在');
        QueryWrapper<ChatServiceSpeechcraftCateEntity> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("name", name);
        checkWrapper.eq("type", 1);
        Long count = chatServiceSpeechcraftCateMapper.selectCount(checkWrapper);
        if (count > 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Category already exists");
        }

        // 3. 创建分类
        ChatServiceSpeechcraftCateEntity cate = new ChatServiceSpeechcraftCateEntity();
        cate.setName(name);
        cate.setType(1);
        cate.setOwnerId(0);
        cate.setAddTime((int) (System.currentTimeMillis() / 1000));

        if (data.containsKey("sort") && data.get("sort") != null) {
            cate.setSort(Integer.parseInt(data.get("sort").toString()));
        } else {
            cate.setSort(0);
        }

        int result = chatServiceSpeechcraftCateMapper.insert(cate);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to add");
        }

        return cate.getId();
    }

    /**
     * 更新分类
     * PUT /api/tenant/chat/speechcraft_cate/:id
     *
     * PHP Reference: ServiceSpeechcraftCate.php::update()
     *
     * 业务逻辑:
     * 1. 验证name非空
     * 2. 验证分类存在
     * 3. 更新name和sort
     *
     * @param id           分类ID
     * @param data         分类数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateCate(Integer id, Map<String, Object> data) {
        // 1. PHP: if (!$data['name']) return $this->fail('请输入分类名称');
        if (!data.containsKey("name") || data.get("name") == null || data.get("name").toString().trim().isEmpty()) {
            throw new io.renren.crmchat.exception.CrmChatException("Please enter category name");
        }

        // 2. PHP: $cateInfo = $this->services->get($id);
        // PHP: if (!$cateInfo) return $this->fail('修改的分类不存在');
        ChatServiceSpeechcraftCateEntity cate = chatServiceSpeechcraftCateMapper.selectById(id);
        if (cate == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Category to modify does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(cate.getAppid(), "Category to modify does not exist");

        // 3. 更新分类
        cate.setName(data.get("name").toString());
        if (data.containsKey("sort") && data.get("sort") != null) {
            cate.setSort(Integer.parseInt(data.get("sort").toString()));
        }

        int result = chatServiceSpeechcraftCateMapper.updateById(cate);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to modify");
        }
    }

    /**
     * 删除分类
     * DELETE /api/tenant/chat/speechcraft_cate/:id
     *
     * PHP Reference: ServiceSpeechcraftCate.php::delete()
     *
     * @param id           分类ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCate(Integer id) {
        // PHP: $cateInfo = $this->services->get($id);
        // PHP: if (!$cateInfo) return $this->fail('删除的分类不存在');
        ChatServiceSpeechcraftCateEntity cate = chatServiceSpeechcraftCateMapper.selectById(id);
        if (cate == null) {
            throw new io.renren.crmchat.exception.CrmChatException("Category to delete does not exist");
        }
        TenantGuard.ensureOwnedByCurrentTenant(cate.getAppid(), "Category to delete does not exist");

        // PHP: $cateInfo->delete();
        int result = chatServiceSpeechcraftCateMapper.deleteById(id);
        if (result <= 0) {
            throw new io.renren.crmchat.exception.CrmChatException("Failed to delete");
        }
    }

    /**
     * 获取创建分类表单配置
     * GET /api/tenant/chat/speechcraftcate/create
     *
     * PHP Reference: ChatServiceSpeechcraftCateServices.php::createForm()
     *
     * PHP代码：
     * public function serviceSpeechcraftCateForm(array $data = [])
     * {
     *     $f[] = FormBuilder::input('name', '分类名称', $data['name'] ?? '')->required();
     *     $f[] = FormBuilder::number('sort', '排序', (int)($data['sort'] ?? 0));
     *     return $f;
     * }
     *
     * public function createForm()
     * {
     *     return create_form('添加分类', $this->serviceSpeechcraftCateForm(), $this->url('chat/speechcraftcate'), 'POST');
     * }
     *
     * @return 表单配置
     */
    public Map<String, Object> getCreateForm() {
        // 构建表单字段
        List<Map<String, Object>> rules = new java.util.ArrayList<>();

        // PHP: FormBuilder::input('name', '分类名称', $data['name'] ?? '')->required();
        Map<String, Object> nameRule = new java.util.HashMap<>();
        nameRule.put("type", "input");
        nameRule.put("field", "name");
        nameRule.put("title", "分类名称");
        nameRule.put("value", "");
        Map<String, Object> nameValidate = new java.util.HashMap<>();
        nameValidate.put("required", true);
        nameValidate.put("message", "请输入分类名称");
        nameValidate.put("trigger", "blur");
        nameRule.put("validate", new Object[]{nameValidate});

        // PHP: FormBuilder::number('sort', '排序', (int)($data['sort'] ?? 0));
        Map<String, Object> sortRule = new java.util.HashMap<>();
        sortRule.put("type", "inputNumber");
        sortRule.put("field", "sort");
        sortRule.put("title", "排序");
        sortRule.put("value", 0);

        rules.add(nameRule);
        rules.add(sortRule);

        // PHP: return create_form('添加分类', $this->serviceSpeechcraftCateForm(), $this->url('chat/speechcraftcate'), 'POST');
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("rules", rules);
        result.put("title", "添加分类");
        result.put("action", "/chat/speechcraftcate");
        result.put("method", "POST");
        result.put("info", "");
        result.put("status", true);

        return result;
    }

    /**
     * 获取编辑分类表单配置
     * GET /api/tenant/chat/speechcraftcate/:id/edit
     *
     * PHP Reference: ChatServiceSpeechcraftCateServices.php::editForm()
     *
     * @param id 分类ID
     * @return 表单配置
     */
    public Map<String, Object> getEditForm(Integer id) {
        // 获取现有分类数据
        ChatServiceSpeechcraftCateEntity cate = chatServiceSpeechcraftCateMapper.selectById(id);
        if (cate == null) {
            throw new io.renren.crmchat.exception.CrmChatException("分类没有查询到");
        }
        TenantGuard.ensureOwnedByCurrentTenant(cate.getAppid(), "分类没有查询到");

        // 构建表单字段（使用现有数据填充）
        List<Map<String, Object>> rules = new java.util.ArrayList<>();

        // PHP: FormBuilder::input('name', '分类名称', $data['name'] ?? '')->required();
        Map<String, Object> nameRule = new java.util.HashMap<>();
        nameRule.put("type", "input");
        nameRule.put("field", "name");
        nameRule.put("title", "分类名称");
        nameRule.put("value", cate.getName() != null ? cate.getName() : "");
        Map<String, Object> nameValidate = new java.util.HashMap<>();
        nameValidate.put("required", true);
        nameValidate.put("message", "请输入分类名称");
        nameValidate.put("trigger", "blur");
        nameRule.put("validate", new Object[]{nameValidate});

        // PHP: FormBuilder::number('sort', '排序', (int)($data['sort'] ?? 0));
        Map<String, Object> sortRule = new java.util.HashMap<>();
        sortRule.put("type", "inputNumber");
        sortRule.put("field", "sort");
        sortRule.put("title", "排序");
        sortRule.put("value", cate.getSort() != null ? cate.getSort() : 0);

        rules.add(nameRule);
        rules.add(sortRule);

        // PHP: return create_form('修改分类', $this->serviceSpeechcraftCateForm($cateInfo->toArray()), $this->url('chat/speechcraftcate/' . $id), 'PUT');
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("rules", rules);
        result.put("title", "修改分类");
        result.put("action", "/chat/speechcraftcate/" + id);
        result.put("method", "PUT");
        result.put("info", "");
        result.put("status", true);

        return result;
    }
}
