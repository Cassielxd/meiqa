package io.renren.crmchat.formbuilder.components;

import java.util.HashMap;
import java.util.Map;

/**
 * Option选项组件（用于Select/Radio/Checkbox）
 * 对应PHP: FormBuilder\UI\Iview\Components\Option
 */
public class OptionComponent {

    private Object value;
    private String label;
    private Boolean disabled;

    public OptionComponent(Object value, String label, boolean disabled) {
        this.value = value;
        this.label = label;
        this.disabled = disabled;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("value", this.value);
        map.put("label", this.label);
        if (this.disabled != null && this.disabled) {
            map.put("disabled", this.disabled);
        }
        return map;
    }

    // Getters
    public Object getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public Boolean getDisabled() {
        return disabled;
    }
}
