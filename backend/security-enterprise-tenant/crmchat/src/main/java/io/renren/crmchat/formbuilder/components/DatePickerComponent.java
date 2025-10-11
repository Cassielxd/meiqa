package io.renren.crmchat.formbuilder.components;

/**
 * DatePicker日期选择器组件
 */
public class DatePickerComponent extends BaseComponent {

    public DatePickerComponent(String field, String title, String value) {
        this.type = "datePicker";
        this.field = field;
        this.title = title;
        this.value = value;
        this.props.put("type", "date");
        this.props.put("format", "yyyy-MM-dd");
    }

    /**
     * 设置类型（date/daterange/datetime/datetimerange）
     */
    public DatePickerComponent type(String type) {
        this.props.put("type", type);
        return this;
    }

    /**
     * 设置日期格式
     */
    public DatePickerComponent format(String format) {
        this.props.put("format", format);
        return this;
    }

    /**
     * 设置为可清空
     */
    public DatePickerComponent clearable() {
        this.props.put("clearable", true);
        return this;
    }
}
