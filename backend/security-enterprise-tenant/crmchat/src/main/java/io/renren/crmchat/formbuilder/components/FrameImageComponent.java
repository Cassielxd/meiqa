package io.renren.crmchat.formbuilder.components;

/**
 * FrameImage图片选择器组件
 * 对应PHP: FormBuilder\UI\Iview\Components\Frame
 */
public class FrameImageComponent extends BaseComponent {

    public FrameImageComponent(String field, String title, String src, String value) {
        this.type = "frameInputs";
        this.field = field;
        this.title = title;
        this.value = value;
        this.props.put("src", src);
        this.props.put("maxLength", 1);
        this.props.put("type", "image");
    }

    /**
     * 设置图标
     * 对应PHP: ->icon('ios-add')
     */
    public FrameImageComponent icon(String icon) {
        this.props.put("icon", icon);
        return this;
    }

    /**
     * 设置弹窗宽度
     * 对应PHP: ->width('950px')
     */
    public FrameImageComponent width(String width) {
        this.props.put("modalWidth", width);
        return this;
    }

    /**
     * 设置弹窗高度
     * 对应PHP: ->height('420px')
     */
    public FrameImageComponent height(String height) {
        this.props.put("modalHeight", height);
        return this;
    }

    /**
     * 设置最大选择数量
     */
    public FrameImageComponent maxLength(int maxLength) {
        this.props.put("maxLength", maxLength);
        return this;
    }
}
