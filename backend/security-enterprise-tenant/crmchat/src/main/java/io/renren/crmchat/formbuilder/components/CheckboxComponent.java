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

    public CheckboxComponent options(List<OptionComponent> options) {
        this.options = options;
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
