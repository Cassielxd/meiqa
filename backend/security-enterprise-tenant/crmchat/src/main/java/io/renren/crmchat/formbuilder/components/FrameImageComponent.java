package io.renren.crmchat.formbuilder.components;

/**
 * FrameImage图片选择器组件
 * 对应PHP: FormBuilder\UI\Iview\Components\Frame
 */
public class FrameImageComponent extends BaseComponent {

    public FrameImageComponent(String field, String title, String src, String value) {
        // PHP: lcfirst(basename('FormBuilder\\UI\\Iview\\Components\\Frame')) = "frame"
        this.type = "frame";
        this.field = field;
        this.title = title;
        this.value = value;

        // PHP Frame 组件的 props 格式
        this.props.put("type", "image");
        this.props.put("maxLength", 1);
        this.props.put("title", "请选择" + title);  // PHP: props 中也有 title 字段
        this.props.put("src", src);
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
     *
     * 注意：PHP 使用 width，不是 modalWidth
     */
    public FrameImageComponent width(String width) {
        this.props.put("width", width);  // 修改为 width
        return this;
    }

    /**
     * 设置弹窗高度
     * 对应PHP: ->height('420px')
     *
     * 注意：PHP 使用 height，不是 modalHeight
     */
    public FrameImageComponent height(String height) {
        this.props.put("height", height);  // 修改为 height
        return this;
    }

    /**
     * 设置最大选择数量
     */
    public FrameImageComponent maxLength(int maxLength) {
        this.props.put("maxLength", maxLength);
        return this;
    }

    /**
     * 设置 props 中的 title（弹窗标题）
     * PHP: ->title('请选择图片')
     */
    public FrameImageComponent propsTitle(String propsTitle) {
        this.props.put("title", propsTitle);
        return this;
    }
}
