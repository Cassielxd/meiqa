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
import io.renren.crmchat.service.common.AbstractUserLabelService;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TenantUserLabelService extends AbstractUserLabelService {

    @Getter
    private final ChatUserLabelCateMapper chatUserLabelCateMapper;
    private final FormBuilder formBuilder;

    public TenantUserLabelService(ChatUserLabelMapper chatUserLabelMapper,
                                  @Nullable ChatUserLabelAssistMapper chatUserLabelAssistMapper,
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
     * 获取标签列表（分页）
     * GET /api/tenant/user/label
     *
     * 返回结构：{ "list": [...], "count": 123 }
     */
    public Map<String, Object> getLabelListWithPagination(
            Map<String, Object> filters, String appid, Integer page, Integer limit) {

        List<ChatUserLabelEntity> allLabels = super.listLabels(filters, appid);

        // 计算分页
        int total = allLabels.size();
        int start = (page - 1) * limit;
        int end = Math.min(start + limit, total);

        List<ChatUserLabelEntity> paginatedList;
        if (start >= total) {
            paginatedList = new ArrayList<>();
        } else {
            paginatedList = allLabels.subList(start, end);
        }

        // 前端期望 res.data.list 和 res.data.count 结构
        Map<String, Object> result = new HashMap<>();
        result.put("list", paginatedList);
        result.put("count", total);
        return result;
    }

    /**
     * 获取创建标签表单
     * GET /api/tenant/user/label/create
     *
     * PHP Reference: ChatUserLabelServices.php::getFormCreate()
     */
    public Map<String, Object> getCreateForm(String appid) {
        // 获取标签分类选项(type=0表示用户标签分类)
        QueryWrapper<ChatUserLabelCateEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("type", 0);
        wrapper.orderByAsc("id");

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
        rules.add(formBuilder.select("cate_id", "标签分类", defaultCateId)
            .options(options)
            .required());

        // 2. 标签名称输入框
        rules.add(formBuilder.input("label", "标签名称", "")
            .required()
            .placeholder("请输入标签名称"));

        return FormHelper.createForm(
            "添加标签",
            rules,
            "/user/label",
            "POST"
        );
    }

    /**
     * 获取编辑标签表单
     * GET /api/tenant/user/label/:id/edit
     *
     * PHP Reference: ChatUserLabelServices.php::getFormEdit()
     */
    public Map<String, Object> getEditForm(Integer id, String appid) {
        ChatUserLabelEntity label = super.requireOwnedLabel(id, appid);

        // 获取标签分类选项(type=0表示用户标签分类)
        QueryWrapper<ChatUserLabelCateEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("type", 0);
        wrapper.orderByAsc("id");

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
        rules.add(formBuilder.select("cate_id", "标签分类", String.valueOf(label.getCateId()))
            .options(options)
            .required());

        // 2. 标签名称输入框（设置当前值）
        rules.add(formBuilder.input("label", "标签名称", label.getLabel())
            .required()
            .placeholder("请输入标签名称"));

        return FormHelper.createForm(
            "修改标签",
            rules,
            "/user/label/" + id,
            "PUT"
        );
    }

    public Integer createLabel(Map<String, Object> data, String appid) {
        return super.createLabel(data, appid);
    }

    public void updateLabel(Integer id, Map<String, Object> data, String appid) {
        super.updateLabel(id, data, appid);
    }

    public void deleteLabel(Integer id, String appid) {
        super.deleteLabel(id, appid);
    }

    public void moveLabel(List<Integer> ids, Integer page, String appid) {
        super.moveLabels(ids, page, appid);
    }
}
