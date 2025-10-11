package io.renren.crmchat.formbuilder.components;

/**
 * Textarea文本域组件
 */
public class TextareaComponent extends BaseComponent {

    public TextareaComponent(String field, String title, String value) {
        this.type = "input";
        this.field = field;
        this.title = title;
        this.value = value;
        this.props.put("type", "textarea");
    }

    /**
     * 设置行数
     */
    public TextareaComponent rows(int rows) {
        this.props.put("rows", rows);
        return this;
    }

    /**
     * 设置自动调整高度
     */
    public TextareaComponent autosize() {
        this.props.put("autosize", true);
        return this;
    }

    /**
     * 设置最大长度
     */
    public TextareaComponent maxlength(int maxlength) {
        this.props.put("maxlength", maxlength);
        return this;
    }
}
