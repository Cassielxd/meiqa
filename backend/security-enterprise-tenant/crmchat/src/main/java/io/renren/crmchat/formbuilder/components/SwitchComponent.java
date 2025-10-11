package io.renren.crmchat.formbuilder.components;

/**
 * Switch开关组件
 * 对应PHP: FormBuilder\UI\Iview\Components\Switches
 */
public class SwitchComponent extends BaseComponent {

    public SwitchComponent(String field, String title, int value) {
        this.type = "switch";
        this.field = field;
        this.title = title;
        this.value = value;
        // 默认值
        this.props.put("activeValue", 1);
        this.props.put("inactiveValue", 0);
    }

    /**
     * 设置打开时的值
     * 对应PHP: ->trueValue(1)
     */
    public SwitchComponent trueValue(Object value) {
        this.props.put("activeValue", value);
        return this;
    }

    /**
     * 设置关闭时的值
     * 对应PHP: ->falseValue(0)
     */
    public SwitchComponent falseValue(Object value) {
        this.props.put("inactiveValue", value);
        return this;
    }

    /**
     * 设置打开时的文字
     * 对应PHP: ->openStr('打开')
     */
    public SwitchComponent openStr(String text) {
        this.props.put("activeText", text);
        return this;
    }

    /**
     * 设置关闭时的文字
     * 对应PHP: ->closeStr('关闭')
     */
    public SwitchComponent closeStr(String text) {
        this.props.put("inactiveText", text);
        return this;
    }

    /**
     * 设置尺寸
     * 对应PHP: ->size('large')
     */
    public SwitchComponent size(String size) {
        this.props.put("size", size);
        return this;
    }
}
