package io.renren.crmchat.formbuilder;

import io.renren.crmchat.formbuilder.components.BaseComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表单辅助类
 * 对应PHP: create_form() 全局函数
 */
public class FormHelper {

    /**
     * 创建表单配置
     * 对应PHP: create_form('添加客服', $this->createServiceForm(), $this->url('/chat/kefu'), 'POST')
     *
     * @param title 表单标题
     * @param components 表单组件列表
     * @param action 提交地址
     * @param method 提交方法（GET/POST/PUT/DELETE）
     * @return 表单配置Map
     */
    public static Map<String, Object> createForm(String title, List<BaseComponent> components, String action, String method) {
        Map<String, Object> formConfig = new HashMap<>();

        // 表单基础配置
        formConfig.put("title", title);
        formConfig.put("action", action);
        formConfig.put("method", method);

        // 转换组件为Map列表
        List<Map<String, Object>> rulesList = new ArrayList<>();
        for (BaseComponent component : components) {
            rulesList.add(component.toMap());
        }
        // 前端form-create期望的字段名是 rules（复数）
        formConfig.put("rules", rulesList);

        // 表单配置项
        Map<String, Object> config = new HashMap<>();
        config.put("form", createFormConfig());
        config.put("submitBtn", createSubmitBtn());
        config.put("resetBtn", createResetBtn());

        formConfig.put("config", config);

        // 添加form-create需要的额外字段
        formConfig.put("info", "");
        formConfig.put("status", true);

        return formConfig;
    }

    /**
     * 创建表单配置（对应PHP中的form配置）
     */
    private static Map<String, Object> createFormConfig() {
        Map<String, Object> form = new HashMap<>();
        form.put("inline", false);
        form.put("labelPosition", "right");
        form.put("labelWidth", "125px");
        form.put("size", "default");
        return form;
    }

    /**
     * 创建提交按钮配置
     */
    private static Map<String, Object> createSubmitBtn() {
        Map<String, Object> btn = new HashMap<>();
        btn.put("show", true);
        btn.put("innerText", "Submit");
        btn.put("type", "primary");
        btn.put("size", "default");
        btn.put("col", createCol(12));
        return btn;
    }

    /**
     * 创建重置按钮配置
     */
    private static Map<String, Object> createResetBtn() {
        Map<String, Object> btn = new HashMap<>();
        btn.put("show", false);
        btn.put("innerText", "Reset");
        btn.put("size", "default");
        btn.put("col", createCol(12));
        return btn;
    }

    /**
     * 创建栅格配置
     */
    private static Map<String, Integer> createCol(int span) {
        Map<String, Integer> col = new HashMap<>();
        col.put("span", span);
        return col;
    }
}
