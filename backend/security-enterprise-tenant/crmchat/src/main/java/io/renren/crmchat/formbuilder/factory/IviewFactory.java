package io.renren.crmchat.formbuilder.factory;

import io.renren.crmchat.formbuilder.components.*;

/**
 * Iview表单组件工厂
 * 对应PHP: FormBuilder\Factory\Iview
 */
public class IviewFactory {

    public InputComponent input(String field, String title, String value) {
        return new InputComponent(field, title, value);
    }

    public SelectComponent select(String field, String title, Object value) {
        return new SelectComponent(field, title, value);
    }

    public SwitchComponent switches(String field, String title, int value) {
        return new SwitchComponent(field, title, value);
    }

    public TextareaComponent textarea(String field, String title, String value) {
        return new TextareaComponent(field, title, value);
    }

    public FrameImageComponent frameImage(String field, String title, String src, String value) {
        return new FrameImageComponent(field, title, src, value);
    }

    public RadioComponent radio(String field, String title, Object value) {
        return new RadioComponent(field, title, value);
    }

    public CheckboxComponent checkbox(String field, String title, Object value) {
        return new CheckboxComponent(field, title, value);
    }

    public DatePickerComponent datePicker(String field, String title, String value) {
        return new DatePickerComponent(field, title, value);
    }

    public InputNumberComponent inputNumber(String field, String title, Number value) {
        return new InputNumberComponent(field, title, value);
    }

    public UploadComponent upload(String field, String title, String action) {
        return new UploadComponent(field, title, action);
    }

    public HiddenComponent hidden(String field, Object value) {
        return new HiddenComponent(field, value);
    }

    public OptionComponent option(Object value, String label, boolean disabled) {
        return new OptionComponent(value, label, disabled);
    }
}
