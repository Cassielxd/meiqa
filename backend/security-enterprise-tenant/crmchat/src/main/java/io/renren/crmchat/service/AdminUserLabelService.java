package io.renren.crmchat.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.crmchat.dao.ChatUserLabelAssistMapper;
import io.renren.crmchat.dao.ChatUserLabelCateMapper;
import io.renren.crmchat.dao.ChatUserLabelMapper;
import io.renren.crmchat.entity.ChatUserLabelCateEntity;
import io.renren.crmchat.entity.ChatUserLabelEntity;
import io.renren.crmchat.formbuilder.FormBuilder;
import io.renren.crmchat.formbuilder.FormHelper;
import io.renren.crmchat.formbuilder.components.BaseComponent;
import io.renren.crmchat.security.TenantContextUtils;
import io.renren.crmchat.service.common.AbstractUserLabelService;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminUserLabelService extends AbstractUserLabelService {

    @Getter
    private final ChatUserLabelCateMapper chatUserLabelCateMapper;
    private final FormBuilder formBuilder;

    public AdminUserLabelService(ChatUserLabelMapper chatUserLabelMapper,
                                 ChatUserLabelAssistMapper chatUserLabelAssistMapper,
                                 ChatUserLabelCateMapper chatUserLabelCateMapper,
                                 FormBuilder formBuilder) {
        super(chatUserLabelMapper, chatUserLabelAssistMapper);
        this.chatUserLabelCateMapper = chatUserLabelCateMapper;
        this.formBuilder = formBuilder;
    }

    public List<ChatUserLabelEntity> getLabelList(Map<String, Object> filters, String appid) {
        return super.listLabels(filters, appid);
    }

    /**
     * 获取创建标签表单
     * PHP Reference: ChatUserLabelServices.php::getFormCreate()
     *
     * PHP代码:
     * public function getFormCreate()
     * {
     *     return create_form('创建标签', $this->fromRule(), $this->url('user/label'));
     * }
     *
     * fromRule方法:
     * $options = $service->getColumn(['type' => 0], 'id as value,name as label');
     * return [
     *     Form::select('cate_id', '标签分类', $label['cate_id'] ?? 0)->options($options),
     *     Form::input('label', '标签名称', $label['label'] ?? ''),
     * ];
     */
    public Map<String, Object> getCreateForm(String appid) {
        // 获取标签分类选项(type=0表示用户标签分类)
        QueryWrapper<ChatUserLabelCateEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("type", 0);
        //wrapper.eq("appid", TenantContextUtils.resolveAppid(appid));
        wrapper.orderBy(true, true, "id");

        List<ChatUserLabelCateEntity> categories = chatUserLabelCateMapper.selectList(wrapper);

        // 转换为下拉选项格式 [{value: id, label: name}, ...]
        // 注意：value需要转为字符串，前端form-create库期望字符串格式
        List<Map<String, Object>> options = categories.stream().map(cat -> {
            Map<String, Object> option = new HashMap<>();
            option.put("value", String.valueOf(cat.getId())); // 转为字符串
            option.put("label", cat.getName());
            return option;
        }).collect(Collectors.toList());

        // 构建表单规则
        List<BaseComponent> rules = new ArrayList<>();

        // 1. 标签分类下拉框
        // 默认值：如果有分类则使用第一个分类的ID（字符串格式），否则为空字符串
        String defaultCateId = categories.isEmpty() ? "" : String.valueOf(categories.get(0).getId());
        rules.add(formBuilder.select("cate_id", "Label Category", defaultCateId)
            .options(options)
            .required());

        // 2. 标签名称输入框
        rules.add(formBuilder.input("label", "Label Name", "")
            .required()
            .placeholder("Enter label name"));

        return FormHelper.createForm(
            "Create Label",
            rules,
            "user/label",
            "POST"
        );
    }

    public Integer saveLabel(Map<String, Object> data, String appid) {
        return super.createLabel(data, appid);
    }

    public boolean moveSort(List<Integer> ids, Integer page, String appid) {
        super.moveLabels(ids, page, appid);
        return true;
    }

    /**
     * 获取修改标签表单
     * PHP Reference: ChatUserLabelServices.php::getFormEdit()
     *
     * PHP代码:
     * public function getFormEdit(int $id)
     * {
     *     $label = $this->dao->get($id);
     *     if (!$label) {
     *         throw new ValidateException('修改的标签不存在');
     *     }
     *     return create_form('修改标签', $this->fromRule($label->toArray()), $this->url('user/label/' . $id), 'put');
     * }
     */
    public Map<String, Object> getEditForm(Integer id, String appid) {
        ChatUserLabelEntity label = super.requireOwnedLabel(id, appid);

        // 获取标签分类选项(type=0表示用户标签分类)
        QueryWrapper<ChatUserLabelCateEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("type", 0);
        wrapper.eq("appid", TenantContextUtils.resolveAppid(appid));
        wrapper.orderBy(true, true, "id");

        List<ChatUserLabelCateEntity> categories = chatUserLabelCateMapper.selectList(wrapper);

        // 转换为下拉选项格式 [{value: id, label: name}, ...]
        // 注意：value需要转为字符串，前端form-create库期望字符串格式
        List<Map<String, Object>> options = categories.stream().map(cat -> {
            Map<String, Object> option = new HashMap<>();
            option.put("value", String.valueOf(cat.getId())); // 转为字符串
            option.put("label", cat.getName());
            return option;
        }).collect(Collectors.toList());

        // 构建表单规则（带默认值）
        List<BaseComponent> rules = new ArrayList<>();

        // 1. 标签分类下拉框（设置当前值，转为字符串格式）
        rules.add(formBuilder.select("cate_id", "Label Category", String.valueOf(label.getCateId()))
            .options(options)
            .required());

        // 2. 标签名称输入框（设置当前值）
        rules.add(formBuilder.input("label", "Label Name", label.getLabel())
            .required()
            .placeholder("Enter label name"));

        return FormHelper.createForm(
            "Edit Label",
            rules,
            "user/label/" + id,
            "PUT"
        );
    }

    public void updateLabel(Integer id, Map<String, Object> data, String appid) {
        super.updateLabel(id, data, appid);
    }

    public void deleteLabel(Integer id, String appid) {
        super.deleteLabel(id, appid);
    }
}
