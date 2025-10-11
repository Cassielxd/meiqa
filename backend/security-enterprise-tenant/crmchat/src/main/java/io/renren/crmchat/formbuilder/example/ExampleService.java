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

        // 1. 分组下拉选择
        List<OptionComponent> groupOptions = getGroupOptions();
        field.add(builder.select("group_id", "请选择分组",
                formData.getOrDefault("group_id", 0))
                .options(groupOptions));

        // 2. 客服头像（FrameImage组件）
        field.add(builder.frameImage("avatar", "客服头像",
                "/admin/widget/images/index?fodder=avatar",
                (String) formData.getOrDefault("avatar", ""))
                .icon("ios-add")
                .width("950px")
                .height("420px"));

        // 3. 客服名称
        field.add(builder.input("nickname", "客服名称",
                (String) formData.getOrDefault("nickname", ""))
                .col(24)
                .required());

        // 4. 手机号码
        field.add(builder.input("phone", "手机号码",
                (String) formData.getOrDefault("phone", ""))
                .col(24)
                .required());

        // 5. 登录账号和密码（根据是否编辑决定必填）
        if (!formData.isEmpty()) {
            // 编辑模式
            field.add(builder.input("account", "登录账号",
                    (String) formData.getOrDefault("account", ""))
                    .col(24)
                    .required());
            field.add(builder.input("password", "登录密码")
                    .type("password")
                    .col(24));
            field.add(builder.input("true_password", "确认密码")
                    .type("password")
                    .col(24));
        } else {
            // 新增模式
            field.add(builder.input("account", "登录账号")
                    .col(24)
                    .required());
            field.add(builder.input("password", "登录密码")
                    .type("password")
                    .col(24)
                    .required());
            field.add(builder.input("true_password", "确认密码")
                    .type("password")
                    .col(24)
                    .required());
        }

        // 6. 欢迎语
        field.add(builder.textarea("welcome_words", "欢迎语",
                (String) formData.getOrDefault("welcome_words", "")));

        // 7. 自动回复开关
        field.add(builder.switches("auto_reply", "自动回复",
                (int) formData.getOrDefault("auto_reply", 0))
                .falseValue(0)
                .trueValue(1)
                .openStr("打开")
                .closeStr("关闭")
                .size("large"));

        // 8. 客服状态开关
        field.add(builder.switches("status", "客服状态",
                (int) formData.getOrDefault("status", 0))
                .falseValue(0)
                .trueValue(1)
                .openStr("打开")
                .closeStr("关闭")
                .size("large"));

        return field;
    }

    /**
     * 创建完整表单配置（对应PHP的create()方法）
     */
    public Map<String, Object> create() {
        return FormHelper.createForm(
                "添加客服",
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
                "编辑客服",
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
        options.add(new OptionComponent(0, "默认分组", false));
        options.add(new OptionComponent(1, "售前客服", false));
        options.add(new OptionComponent(2, "售后客服", false));
        return options;
    }
}
