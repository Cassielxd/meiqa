package io.renren.crmchat.formbuilder.example;

import io.renren.crmchat.formbuilder.FormBuilder;
import io.renren.crmchat.formbuilder.FormHelper;
import io.renren.crmchat.formbuilder.components.BaseComponent;
import io.renren.crmchat.formbuilder.components.OptionComponent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FormBuilder使用示例
 * 一比一还原PHP的实现方式
 *
 * 对应PHP代码：
 * public function createServiceForm(array $formData = [])
 * {
 *     $field[] = $this->builder->select('group_id', '请选择分组', $formData['group_id'] ?? 0)->options($seervice->getOptions());
 *     $field[] = $this->builder->frameImage('avatar', '客服头像', $this->url('admin/widget.images/index'), $formData['avatar'] ?? '')->icon('ios-add')->width('950px')->height('420px');
 *     $field[] = $this->builder->input('nickname', '客服名称', $formData['nickname'] ?? '')->col(24)->required();
 *     ...
 *     return $field;
 * }
 */
@Service
public class ExampleService {

    private final FormBuilder builder;

    public ExampleService(FormBuilder builder) {
        this.builder = builder;
    }

    /**
     * 创建客服表单（完全对应PHP实现）
     */
    public List<BaseComponent> createServiceForm(Map<String, Object> formData) {
        List<BaseComponent> field = new ArrayList<>();

        // 1. Group selector
        List<OptionComponent> groupOptions = getGroupOptions();
        field.add(builder.select("group_id", "Select Group",
                formData.getOrDefault("group_id", 0))
                .options(groupOptions));

        // 2. Agent avatar (FrameImage component)
        field.add(builder.frameImage("avatar", "Agent Avatar",
                "/#/admin/widget.images/index.html?fodder=avatar",
                (String) formData.getOrDefault("avatar", ""))
                .icon("ios-add")
                .width("950px")
                .height("420px"));

        // 3. Agent name
        field.add(builder.input("nickname", "Agent Name",
                (String) formData.getOrDefault("nickname", ""))
                .col(24)
                .required());

        // 4. Phone number
        field.add(builder.input("phone", "Phone Number",
                (String) formData.getOrDefault("phone", ""))
                .col(24)
                .required());

        // 5. Login account and password (required depending on edit mode)
        if (!formData.isEmpty()) {
            // edit mode
            field.add(builder.input("account", "Login Account",
                    (String) formData.getOrDefault("account", ""))
                    .col(24)
                    .required());
            field.add(builder.input("password", "Login Password")
                    .type("password")
                    .col(24));
            field.add(builder.input("true_password", "Confirm Password")
                    .type("password")
                    .col(24));
        } else {
            // create mode
            field.add(builder.input("account", "Login Account")
                    .col(24)
                    .required());
            field.add(builder.input("password", "Login Password")
                    .type("password")
                    .col(24)
                    .required());
            field.add(builder.input("true_password", "Confirm Password")
                    .type("password")
                    .col(24)
                    .required());
        }

        // 6. Welcome message
        field.add(builder.textarea("welcome_words", "Welcome Message",
                (String) formData.getOrDefault("welcome_words", "")));

        // 7. Auto-reply toggle
        field.add(builder.switches("auto_reply", "Auto Reply",
                (int) formData.getOrDefault("auto_reply", 0))
                .falseValue(0)
                .trueValue(1)
                .openStr("Enabled")
                .closeStr("Disabled")
                .size("large"));

        // 8. Agent status toggle
        field.add(builder.switches("status", "Agent Status",
                (int) formData.getOrDefault("status", 0))
                .falseValue(0)
                .trueValue(1)
                .openStr("Enabled")
                .closeStr("Disabled")
                .size("large"));

        return field;
    }

    /**
     * 创建完整表单配置（对应PHP的create()方法）
     */
    public Map<String, Object> create() {
        return FormHelper.createForm(
                "Add Customer Service Agent",
                createServiceForm(Map.of()),
                "/chat/kefu",
                "POST"
        );
    }

    /**
     * 编辑表单配置（对应PHP的edit()方法）
     */
    public Map<String, Object> edit(int id, Map<String, Object> serviceInfo) {
        return FormHelper.createForm(
                "Edit Customer Service Agent",
                createServiceForm(serviceInfo),
                "/chat/kefu/" + id,
                "PUT"
        );
    }

    /**
     * 获取分组选项（模拟数据）
     */
    private List<OptionComponent> getGroupOptions() {
        List<OptionComponent> options = new ArrayList<>();
        options.add(new OptionComponent(0, "Default Group", false));
        options.add(new OptionComponent(1, "Pre-sales Support", false));
        options.add(new OptionComponent(2, "After-sales Support", false));
        return options;
    }
}
