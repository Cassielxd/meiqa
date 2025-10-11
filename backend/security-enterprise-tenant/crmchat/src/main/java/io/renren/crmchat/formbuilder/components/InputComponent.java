package io.renren.crmchat.formbuilder.components;

/**
 * Input输入框组件
 * 对应PHP: FormBuilder\UI\Iview\Components\Input
 */
public class InputComponent extends BaseComponent {

    public InputComponent(String field, String title, String value) {
        this.type = "input";
        this.field = field;
        this.title = title;
        this.value = value;
    }

    /**
     * 设置输入类型（text/password/textarea/url/email/date）
     * 对应PHP: ->type('password')
     */
    public InputComponent type(String type) {
        this.props.put("type", type);
        return this;
    }

    /**
     * 设置最大长度
     */
    public InputComponent maxlength(int maxlength) {
        this.props.put("maxlength", maxlength);
        return this;
    }

    /**
     * 设置为可清空
     */
    public InputComponent clearable() {
        this.props.put("clearable", true);
        return this;
    }

    /**
     * 设置前缀图标
     */
    public InputComponent icon(String icon) {
        this.props.put("icon", icon);
        return this;
    }
}
