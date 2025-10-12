package io.renren.crmchat.formbuilder.components;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表单组件基类
 * 对应PHP: FormBuilder\Driver\FormComponent
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseComponent {

    /**
     * 组件类型
     */
    protected String type;

    /**
     * 字段名
     */
    protected String field;

    /**
     * 标题/标签
     */
    protected String title;

    /**
     * 字段值
     */
    protected Object value;

    /**
     * 组件属性
     */
    protected Map<String, Object> props = new HashMap<>();

    /**
     * 验证规则
     */
    protected List<Map<String, Object>> validate = new ArrayList<>();

    /**
     * 栅格布局列数(1-24)
     */
    protected Integer col;

    /**
     * 是否必填
     */
    protected Boolean required;

    /**
     * 占位符
     */
    protected String placeholder;

    /**
     * 是否禁用
     */
    protected Boolean disabled;

    /**
     * 设置栅格布局
     * 对应PHP: ->col(24)
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseComponent> T col(int col) {
        this.col = col;
        return (T) this;
    }

    /**
     * 设置为必填
     * 对应PHP: ->required()
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseComponent> T required() {
        return required(true);
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseComponent> T required(boolean required) {
        this.required = required;
        if (required) {
            Map<String, Object> rule = new HashMap<>();
            rule.put("required", true);
            rule.put("message", this.title + " is required");
            rule.put("trigger", "blur");
            this.validate.add(rule);
        }
        return (T) this;
    }

    /**
     * 设置占位符
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseComponent> T placeholder(String placeholder) {
        this.placeholder = placeholder;
        this.props.put("placeholder", placeholder);
        return (T) this;
    }

    /**
     * 设置禁用状态
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseComponent> T disabled(boolean disabled) {
        this.disabled = disabled;
        this.props.put("disabled", disabled);
        return (T) this;
    }

    /**
     * 设置自定义属性
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseComponent> T prop(String key, Object value) {
        this.props.put(key, value);
        return (T) this;
    }

    /**
     * 添加自定义验证规则
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseComponent> T addValidate(String type, Object value, String message) {
        Map<String, Object> rule = new HashMap<>();
        rule.put(type, value);
        rule.put("message", message);
        rule.put("trigger", "blur");
        this.validate.add(rule);
        return (T) this;
    }

    /**
     * 转换为Map格式（用于JSON序列化）
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", this.type);
        map.put("field", this.field);
        map.put("title", this.title);
        map.put("value", this.value);

        if (this.props != null && !this.props.isEmpty()) {
            map.put("props", this.props);
        }

        if (this.validate != null && !this.validate.isEmpty()) {
            map.put("validate", this.validate);
        }

        if (this.col != null) {
            Map<String, Integer> colMap = new HashMap<>();
            colMap.put("span", this.col);
            map.put("col", colMap);
        }

        return map;
    }
}
