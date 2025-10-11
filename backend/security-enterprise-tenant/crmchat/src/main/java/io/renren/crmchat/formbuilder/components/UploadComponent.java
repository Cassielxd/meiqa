package io.renren.crmchat.formbuilder.components;

/**
 * Upload上传组件
 */
public class UploadComponent extends BaseComponent {

    public UploadComponent(String field, String title, String action) {
        this.type = "upload";
        this.field = field;
        this.title = title;
        this.value = "";
        this.props.put("action", action);
        this.props.put("maxLength", 1);
    }

    /**
     * 设置最大上传数量
     */
    public UploadComponent maxLength(int maxLength) {
        this.props.put("maxLength", maxLength);
        return this;
    }

    /**
     * 设置接受的文件类型
     */
    public UploadComponent accept(String accept) {
        this.props.put("accept", accept);
        return this;
    }

    /**
     * 设置上传列表样式（text/picture/picture-card）
     */
    public UploadComponent listType(String listType) {
        this.props.put("listType", listType);
        return this;
    }

    /**
     * 设置为多文件上传
     */
    public UploadComponent multiple() {
        this.props.put("multiple", true);
        return this;
    }
}
