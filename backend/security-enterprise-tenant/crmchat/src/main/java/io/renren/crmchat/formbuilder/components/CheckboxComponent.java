package io.renren.crmchat.formbuilder.components;

import java.util.ArrayList;
import java.util.List;

/**
 * Checkbox复选框组件
 */
public class CheckboxComponent extends BaseComponent {

    private List<OptionComponent> options = new ArrayList<>();

    public CheckboxComponent(String field, String title, Object value) {
        this.type = "checkbox";
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
    public CheckboxComponent options(List<?> options) {
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

    public CheckboxComponent addOption(Object value, String label) {
        this.options.add(new OptionComponent(value, label, false));
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
