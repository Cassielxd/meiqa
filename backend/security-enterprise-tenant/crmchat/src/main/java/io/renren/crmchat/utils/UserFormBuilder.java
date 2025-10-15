package io.renren.crmchat.utils;

import io.renren.crmchat.entity.ChatUserEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户表单构建工具类
 * 统一处理Admin和Tenant端的用户编辑表单构建逻辑
 *
 * PHP Reference: form-builder/FormBuilder.php
 *
 * @author CRMChat Team
 */
public class UserFormBuilder {

    /**
     * 构建用户编辑表单配置
     *
     * 生成form-create兼容的表单配置，包含：
     * - rules: 表单字段规则数组
     * - title: 表单标题
     * - action: 提交URL
     * - method: 提交方法(PUT)
     *
     * @param user          用户实体
     * @param groupOptions  用户分组下拉选项列表，格式: [{value: 1, label: "VIP"}]
     * @param actionUrl     表单提交URL，如: "user/123"
     * @param pathPrefix    路径前缀：admin 或 tenant
     * @return 表单配置Map
     */
    public static Map<String, Object> buildEditForm(
            ChatUserEntity user,
            List<Map<String, Object>> groupOptions,
            String actionUrl,
            String pathPrefix) {

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (actionUrl == null || actionUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Action URL cannot be empty");
        }
        if (pathPrefix == null || pathPrefix.trim().isEmpty()) {
            pathPrefix = "admin"; // 默认使用admin路径
        }

        // 构建form-create兼容的表单规则（参考PHP: Form::frameImage, Form::input等）
        List<Map<String, Object>> rules = new ArrayList<>();

        // 1. 头像字段 - frame类型（图片选择器）
        // PHP Reference:
        //   Admin: Form::frameImage('avatar', '用户头像', $this->url('admin/widget.images/index.html', ['fodder' => 'avatar', 'big' => 1]))
        //   Tenant: $this->url('tenant/widget.images/index', ['fodder' => 'avatar'], true)
        // Frontend Routes:
        //   Admin: /admin/widget.images/index.html -> @/components/uploadPictures/widgetImg
        //   Tenant: /tenant/widget.images/index.html -> @/components/uploadPictures/widgetImg
        Map<String, Object> avatarRule = new HashMap<>();
        avatarRule.put("type", "frame");
        avatarRule.put("field", "avatar");
        avatarRule.put("title", "用户头像");
        avatarRule.put("value", user.getAvatar() != null ? user.getAvatar() : "");

        Map<String, Object> avatarProps = new HashMap<>();
        avatarProps.put("type", "image");
        // 根据pathPrefix使用不同的图片选择器路径
        // Vue Router使用hash模式,需要在URL中包含 # 符号
        avatarProps.put("src", "/" + pathPrefix + "/#/" + pathPrefix + "/widget.images/index.html?fodder=avatar&big=1");
        avatarProps.put("icon", "ios-image");
        avatarProps.put("width", "950px");
        avatarProps.put("height", "420px");
        avatarRule.put("props", avatarProps);
        rules.add(avatarRule);

        // 2. 昵称字段
        Map<String, Object> nicknameRule = new HashMap<>();
        nicknameRule.put("type", "input");
        nicknameRule.put("field", "nickname");
        nicknameRule.put("title", "用户昵称");
        nicknameRule.put("value", user.getNickname() != null ? user.getNickname() : "");
        rules.add(nicknameRule);

        // 3. 备注昵称字段
        Map<String, Object> remarkNicknameRule = new HashMap<>();
        remarkNicknameRule.put("type", "input");
        remarkNicknameRule.put("field", "remark_nickname");
        remarkNicknameRule.put("title", "备注昵称");
        remarkNicknameRule.put("value", user.getRemarkNickname() != null ? user.getRemarkNickname() : "");
        rules.add(remarkNicknameRule);

        // 4. 手机号字段
        Map<String, Object> phoneRule = new HashMap<>();
        phoneRule.put("type", "input");
        phoneRule.put("field", "phone");
        phoneRule.put("title", "手机号");
        phoneRule.put("value", user.getPhone() != null ? user.getPhone() : "");
        rules.add(phoneRule);

        // 5. 用户分组下拉框
        // 参考PHP OptionsRule.php: options直接在根级别，不在props内
        Map<String, Object> groupRule = new HashMap<>();
        groupRule.put("type", "select");
        groupRule.put("field", "group_id");
        groupRule.put("title", "用户分组");
        groupRule.put("value", user.getGroupId() != null ? user.getGroupId() : 0);
        groupRule.put("options", groupOptions != null ? groupOptions : new ArrayList<>());
        rules.add(groupRule);

        // 6. 用户备注文本域
        Map<String, Object> remarksRule = new HashMap<>();
        remarksRule.put("type", "textarea");
        remarksRule.put("field", "remarks");
        remarksRule.put("title", "用户备注");
        remarksRule.put("value", user.getRemarks() != null ? user.getRemarks() : "");
        rules.add(remarksRule);

        // 返回form-create格式（参考PHP: create_form函数返回值）
        Map<String, Object> result = new HashMap<>();
        result.put("rules", rules);
        result.put("title", "修改用户");
        result.put("action", actionUrl);
        result.put("method", "PUT");
        result.put("info", "");
        result.put("status", true);

        return result;
    }

    /**
     * 构建分组选项列表
     *
     * @param id    分组ID
     * @param name  分组名称
     * @return 选项Map，格式: {value: id, label: name}
     */
    public static Map<String, Object> buildGroupOption(Integer id, String name) {
        Map<String, Object> option = new HashMap<>();
        option.put("value", id);
        option.put("label", name != null ? name : "");
        return option;
    }
}
