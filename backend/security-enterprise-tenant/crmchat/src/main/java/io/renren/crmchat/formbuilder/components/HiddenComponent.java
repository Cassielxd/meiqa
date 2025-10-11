package io.renren.crmchat.formbuilder.components;

/**
 * Hidden隐藏字段组件
 */
public class HiddenComponent extends BaseComponent {

    public HiddenComponent(String field, Object value) {
        this.type = "hidden";
        this.field = field;
        this.title = "";
        this.value = value;
    }
}
