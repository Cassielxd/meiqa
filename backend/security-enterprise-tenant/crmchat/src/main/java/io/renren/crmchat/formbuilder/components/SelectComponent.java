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
     * 设置选项列表
     * 对应PHP: ->options($seervice->getOptions())
     */
    public SelectComponent options(List<OptionComponent> options) {
        this.options = options;
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
