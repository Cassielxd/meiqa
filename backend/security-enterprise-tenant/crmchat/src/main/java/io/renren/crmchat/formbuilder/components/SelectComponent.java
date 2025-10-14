package io.renren.crmchat.formbuilder.components;

import java.util.ArrayList;
import java.util.List;

/**
 * Select下拉选择组件
 * 对应PHP: FormBuilder\UI\Iview\Components\Select
 */
public class SelectComponent extends BaseComponent {

    private List<OptionComponent> options = new ArrayList<>();

    public SelectComponent(String field, String title, Object value) {
        this.type = "select";
        this.field = field;
        this.title = title;
        this.value = value;
    }

    /**
     * 设置选项列表（支持OptionComponent或Map格式）
     * 对应PHP: ->options($service->getOptions())
     *
     * 支持两种格式:
     * 1. List<OptionComponent>
     * 2. List<Map<String, Object>> - [{value: 1, label: "选项1"}, ...]
     */
    public SelectComponent options(List<?> options) {
        this.options.clear();

        if (options == null || options.isEmpty()) {
            return this;
        }

        // 检查第一个元素的类型
        Object first = options.get(0);

        if (first instanceof OptionComponent) {
            // 如果是OptionComponent列表
            for (Object item : options) {
                this.options.add((OptionComponent) item);
            }
        } else if (first instanceof java.util.Map) {
            // 如果是Map列表
            for (Object item : options) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) item;
                Object value = map.get("value");
                String label = (String) map.get("label");
                Boolean disabled = (Boolean) map.getOrDefault("disabled", false);
                this.options.add(new OptionComponent(value, label, disabled));
            }
        }

        return this;
    }

    /**
     * 添加单个选项
     */
    public SelectComponent addOption(Object value, String label) {
        this.options.add(new OptionComponent(value, label, false));
        return this;
    }

    /**
     * 设置为可搜索
     */
    public SelectComponent filterable() {
        this.props.put("filterable", true);
        return this;
    }

    /**
     * 设置为多选
     */
    public SelectComponent multiple() {
        this.props.put("multiple", true);
        return this;
    }

    /**
     * 设置为可清空
     */
    public SelectComponent clearable() {
        this.props.put("clearable", true);
        return this;
    }

    /**
     * 重写 required() 方法以支持多选时的数组验证
     * 对应PHP: Select::createValidate()
     *
     * PHP逻辑:
     * - multiple=true 时返回 Iview::validateArr() (type: 'array')
     * - multiple=false 时返回 Iview::validateStr() (type: 'string')
     */
    @Override
    public SelectComponent required() {
        return required(true);
    }

    @Override
    public SelectComponent required(boolean required) {
        this.required = required;
        if (required) {
            java.util.Map<String, Object> rule = new java.util.HashMap<>();
            rule.put("required", true);
            rule.put("message", this.title + " is required");
            rule.put("trigger", "change");  // PHP使用change而不是blur

            // 关键：根据multiple属性设置正确的type
            // PHP: if ($this->props['multiple'] == true) return Iview::validateArr();
            Object multipleValue = this.props.get("multiple");
            if (multipleValue != null && (Boolean) multipleValue) {
                rule.put("type", "array");  // 多选时必须使用array类型
            } else {
                rule.put("type", "string");  // 单选时使用string类型
            }

            this.validate.add(rule);
        }
        return this;
    }

    @Override
    public java.util.Map<String, Object> toMap() {
        java.util.Map<String, Object> map = super.toMap();
        if (!this.options.isEmpty()) {
            List<java.util.Map<String, Object>> optionsList = new ArrayList<>();
            for (OptionComponent option : this.options) {
                optionsList.add(option.toMap());
            }
            map.put("options", optionsList);
        }
        return map;
    }
}
