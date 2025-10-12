package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatUserLabelCateMapper;
import io.renren.crmchat.dao.ChatUserLabelMapper;
import io.renren.crmchat.entity.ChatUserLabelCateEntity;
import io.renren.crmchat.entity.ChatUserLabelEntity;
import io.renren.crmchat.exception.CrmChatException;
import io.renren.crmchat.formbuilder.FormBuilder;
import io.renren.crmchat.formbuilder.FormHelper;
import io.renren.crmchat.formbuilder.components.BaseComponent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Admin User Label Cate Service - 管理员用户标签分类管理
 * PHP Reference: LabelCate.php
 *
 * 业务逻辑说明:
 * - 标签分类通过appid进行多租户隔离
 * - type=0表示用户标签分类
 * - 支持批量排序（move方法）
 * - 删除前需检查分类下是否有标签
 *
 * @author CRMChat Team
 */
@Service
@AllArgsConstructor
public class AdminUserLabelCateService {

    private final ChatUserLabelCateMapper chatUserLabelCateMapper;
    private final ChatUserLabelMapper chatUserLabelMapper;
    private final FormBuilder formBuilder;

    /**
     * 获取标签分类列表
     * PHP Reference: LabelCate.php::index()
     *
     * @param appid 租户ID
     * @return 分类列表
     */
    public List<ChatUserLabelCateEntity> getCateList(String appid) {
        // PHP: $this->services->getCateList(['type' => 0])
        // Category是平台级设置，不需要appid隔离
        QueryWrapper<ChatUserLabelCateEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("type", 0);
        wrapper.orderByAsc("sort", "id");

        return chatUserLabelCateMapper.selectList(wrapper);
    }

    /**
     * 获取创建表单数据
     * PHP Reference: LabelCate.php::create() -> ChatUserLabelCateServices::getCreateForm()
     *
     * PHP代码:
     * public function getCreateForm()
     * {
     *     return create_form('添加标签分类', $this->formRule(), $this->url('user/label/cate'));
     * }
     *
     * formRule方法:
     * return [
     *     Form::input('name', '分类名称', $cateInfo['name'] ?? ''),
     * ];
     *
     * @return 表单配置数据
     */
    public Map<String, Object> getCreateForm() {
        List<BaseComponent> rules = new ArrayList<>();

        // 分类名称输入框
        rules.add(formBuilder.input("name", "Category Name", "")
            .required()
            .placeholder("Enter category name"));

        return FormHelper.createForm(
            "Add Label Category",
            rules,
            "user/label/cate",
            "POST"
        );
    }

    /**
     * 保存标签分类
     * PHP Reference: LabelCate.php::save()
     *
     * @param data 分类数据
     * @param appid 租户ID
     * @return 新创建的分类ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveCate(Map<String, Object> data, String appid) {
        // PHP: if (!$data['name']) return $this->fail('请输入分类名称');
        String name = (String) data.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new CrmChatException("Please enter category name");
        }

        // PHP: if ($this->services->count(['name' => $data['name'], 'type' => 0]))
        // Category是平台级设置，检查名称重复时不需要appid过滤
        long count = chatUserLabelCateMapper.selectCount(
            new QueryWrapper<ChatUserLabelCateEntity>()
                .eq("name", name)
                .eq("type", 0)
        );
        if (count > 0) {
            throw new CrmChatException("Category name already exists");
        }

        ChatUserLabelCateEntity cate = new ChatUserLabelCateEntity();
        cate.setPid(0);
        cate.setOwnerId(0);
        cate.setName(name);
        cate.setSort((Integer) data.getOrDefault("sort", 0));
        cate.setType(0);  // PHP: $data['type'] = 0;
        cate.setAddTime((int) (System.currentTimeMillis() / 1000));  // PHP: $data['add_time'] = time();

        chatUserLabelCateMapper.insert(cate);

        // PHP: $this->services->update($res->id, ['sort' => $res->id]);
        Integer newId = cate.getId();
        cate.setSort(newId);
        chatUserLabelCateMapper.updateById(cate);

        return newId;
    }

    /**
     * 获取编辑表单数据
     * PHP Reference: LabelCate.php::edit() -> ChatUserLabelCateServices::getEditForm()
     *
     * PHP代码:
     * public function getEditForm(int $id)
     * {
     *     $cateInfo = $this->dao->get($id);
     *     if (!$cateInfo) {
     *         throw new ValidateException('获取分类失败');
     *     }
     *     return create_form('添加标签分类', $this->formRule($cateInfo->toArray()), $this->url('user/label/cate/' . $id), 'put');
     * }
     *
     * @param id 分类ID
     * @param appid 租户ID
     * @return 表单配置数据
     */
    public Map<String, Object> getEditForm(Integer id, String appid) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        ChatUserLabelCateEntity cate = chatUserLabelCateMapper.selectById(id);
        if (cate == null) {
            throw new CrmChatException("Category does not exist");
        }

        List<BaseComponent> rules = new ArrayList<>();

        // 分类名称输入框（带默认值）
        rules.add(formBuilder.input("name", "Category Name", cate.getName())
            .required()
            .placeholder("Enter category name"));

        return FormHelper.createForm(
            "Edit Label Category",
            rules,
            "user/label/cate/" + id,
            "PUT"
        );
    }

    /**
     * 更新标签分类
     * PHP Reference: LabelCate.php::update()
     *
     * @param id 分类ID
     * @param data 更新数据
     * @param appid 租户ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCate(Integer id, Map<String, Object> data, String appid) {
        ChatUserLabelCateEntity cate = chatUserLabelCateMapper.selectById(id);
        if (cate == null) {
            throw new CrmChatException("Category does not exist");
        }

        // PHP: if (!$data['name']) return $this->fail('请输入分类名称');
        String name = (String) data.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new CrmChatException("Please enter category name");
        }

        cate.setName(name);
        cate.setSort((Integer) data.getOrDefault("sort", cate.getSort()));
        cate.setType(0);  // PHP: $data['type'] = 0;

        return chatUserLabelCateMapper.updateById(cate) > 0;
    }

    /**
     * 批量移动排序
     * PHP Reference: LabelCate.php::move()
     *
     * @param ids 分类ID列表
     * @param appid 租户ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean labelMove(List<Integer> ids, String appid) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }

        // PHP: $this->services->labelMove($ids);
        for (int i = 0; i < ids.size(); i++) {
            ChatUserLabelCateEntity cate = chatUserLabelCateMapper.selectById(ids.get(i));
            if (cate != null) {
                cate.setSort(i + 1);
                chatUserLabelCateMapper.updateById(cate);
            }
        }

        return true;
    }

    /**
     * 删除标签分类
     * PHP Reference: LabelCate.php::delete()
     *
     * @param id 分类ID
     * @param appid 租户ID
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCate(Integer id, String appid) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        // PHP: if ($services->count(['cate_id' => $id])) return $this->fail('请先删除分类下的标签');
        // 检查分类下的标签时，需要查询所有租户的标签（因为Category是平台级共享的）
        long count = chatUserLabelMapper.selectCount(
            new QueryWrapper<ChatUserLabelEntity>().eq("cate_id", id)
        );
        if (count > 0) {
            throw new CrmChatException("Please delete tags under this category first");
        }

        ChatUserLabelCateEntity cate = chatUserLabelCateMapper.selectById(id);
        if (cate == null) {
            throw new CrmChatException("Deletion failed, please try again later");
        }

        return chatUserLabelCateMapper.deleteById(id) > 0;
    }
}
