package io.renren.crmchat.formbuilder;

import io.renren.crmchat.formbuilder.components.*;
import io.renren.crmchat.formbuilder.factory.IviewFactory;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java版FormBuilder - 动态表单生成器
 * 对应PHP: FormBuilder\Factory\Iview
 *
 * 用于生成前端Iview表单配置JSON
 *
 * @author Claude
 */
@Component
public class FormBuilder {

    private final IviewFactory factory;

    public FormBuilder() {
        this.factory = new IviewFactory();
    }

    /**
     * 创建Input输入框
     * 对应PHP: $this->builder->input('nickname', '客服名称', $formData['nickname'] ?? '')
     */
    public InputComponent input(String field, String title) {
        return factory.input(field, title, "");
    }

    public InputComponent input(String field, String title, String value) {
        return factory.input(field, title, value);
    }

    /**
     * 创建Select下拉选择
     * 对应PHP: $this->builder->select('group_id', '请选择分组', $formData['group_id'] ?? 0)
     */
    public SelectComponent select(String field, String title, Object value) {
        return factory.select(field, title, value);
    }

    /**
     * 创建Switch开关
     * 对应PHP: $this->builder->switches('status', '客服状态', (int)($formData['status'] ?? 0))
     */
    public SwitchComponent switches(String field, String title, int value) {
        return factory.switches(field, title, value);
    }

    /**
     * 创建Textarea文本域
     * 对应PHP: $this->builder->textarea('welcome_words', '欢迎语', $formData['welcome_words'] ?? '')
     */
    public TextareaComponent textarea(String field, String title, String value) {
        return factory.textarea(field, title, value);
    }

    /**
     * 创建FrameImage图片选择器
     * 对应PHP: $this->builder->frameImage('avatar', '客服头像', $this->url('admin/widget.images/index'))
     */
    public FrameImageComponent frameImage(String field, String title, String src) {
        return factory.frameImage(field, title, src, "");
    }

    public FrameImageComponent frameImage(String field, String title, String src, String value) {
        return factory.frameImage(field, title, src, value);
    }

    /**
     * 创建Radio单选框
     */
    public RadioComponent radio(String field, String title, Object value) {
        return factory.radio(field, title, value);
    }

    /**
     * 创建Checkbox复选框
     */
    public CheckboxComponent checkbox(String field, String title, Object value) {
        return factory.checkbox(field, title, value);
    }

    /**
     * 创建DatePicker日期选择器
     */
    public DatePickerComponent datePicker(String field, String title, String value) {
        return factory.datePicker(field, title, value);
    }

    /**
     * 创建InputNumber数字输入框
     */
    public InputNumberComponent inputNumber(String field, String title, Number value) {
        return factory.inputNumber(field, title, value);
    }

    /**
     * 创建Number数字输入框 (别名方法,与PHP保持一致)
     * 对应PHP: Form::number('sort', '排序', 0)
     */
    public InputNumberComponent number(String field, String title, String value) {
        try {
            return factory.inputNumber(field, title, Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return factory.inputNumber(field, title, 0);
        }
    }

    /**
     * 创建Upload上传组件
     */
    public UploadComponent upload(String field, String title, String action) {
        return factory.upload(field, title, action);
    }

    /**
     * 创建Hidden隐藏字段
     */
    public HiddenComponent hidden(String field, Object value) {
        return factory.hidden(field, value);
    }

    /**
     * 创建Option选项（用于Select/Radio/Checkbox）
     */
    public OptionComponent option(Object value, String label) {
        return factory.option(value, label, false);
    }

    public OptionComponent option(Object value, String label, boolean disabled) {
        return factory.option(value, label, disabled);
    }
}
