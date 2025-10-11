package io.renren.crmchat.formbuilder.components;

/**
 * InputNumber数字输入框组件
 */
public class InputNumberComponent extends BaseComponent {

    public InputNumberComponent(String field, String title, Number value) {
        this.type = "inputNumber";
        this.field = field;
        this.title = title;
        this.value = value;
    }

    /**
     * 设置最小值
     */
    public InputNumberComponent min(Number min) {
        this.props.put("min", min);
        return this;
    }

    /**
     * 设置最大值
     */
    public InputNumberComponent max(Number max) {
        this.props.put("max", max);
        return this;
    }

    /**
     * 设置步长
     */
    public InputNumberComponent step(Number step) {
        this.props.put("step", step);
        return this;
    }

    /**
     * 设置精度
     */
    public InputNumberComponent precision(int precision) {
        this.props.put("precision", precision);
        return this;
    }
}
