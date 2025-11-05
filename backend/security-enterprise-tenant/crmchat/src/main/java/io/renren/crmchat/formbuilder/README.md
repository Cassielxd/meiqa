# Java FormBuilder 使用文档

## 概述

Java FormBuilder 是一个动态表单生成器，一比一还原 PHP FormBuilder (xaboy/form-builder) 的实现方式。用于生成前端 Iview 表单配置 JSON。

## 目录结构

```
formbuilder/
├── FormBuilder.java          # 主入口类
├── FormHelper.java            # 表单辅助类
├── factory/
│   └── IviewFactory.java      # Iview组件工厂
└── components/
    ├── BaseComponent.java         # 组件基类
    ├── InputComponent.java        # 输入框
    ├── SelectComponent.java       # 下拉选择
    ├── SwitchComponent.java       # 开关
    ├── TextareaComponent.java     # 文本域
    ├── FrameImageComponent.java   # 图片选择器
    ├── RadioComponent.java        # 单选框
    ├── CheckboxComponent.java     # 复选框
    ├── DatePickerComponent.java   # 日期选择器
    ├── InputNumberComponent.java  # 数字输入框
    ├── UploadComponent.java       # 上传组件
    ├── HiddenComponent.java       # 隐藏字段
    └── OptionComponent.java       # 选项组件
```

## 快速开始

### 1. 基础使用

```java
@Service
public class MyService {

    @Autowired
    private FormBuilder builder;

    public Map<String, Object> createForm() {
        List<BaseComponent> fields = new ArrayList<>();

        // 添加输入框
        fields.add(builder.input("name", "姓名", "")
            .col(24)
            .required());

        // 添加下拉选择
        fields.add(builder.select("type", "类型", 0)
            .addOption(0, "普通用户")
            .addOption(1, "VIP用户"));

        // 添加开关
        fields.add(builder.switches("status", "状态", 1)
            .trueValue(1)
            .falseValue(0)
            .openStr("启用")
            .closeStr("禁用"));

        // 创建完整表单配置
        return FormHelper.createForm(
            "用户表单",    // 表单标题
            fields,        // 表单字段
            "/api/user",   // 提交地址
            "POST"         // 提交方法
        );
    }
}
```

### 2. 对应 PHP 代码

**PHP 版本：**
```php
public function createServiceForm(array $formData = [])
{
    $field[] = $this->builder->input('nickname', '客服名称', $formData['nickname'] ?? '')
        ->col(24)
        ->required();

    $field[] = $this->builder->select('group_id', '请选择分组', $formData['group_id'] ?? 0)
        ->options($seervice->getOptions());

    $field[] = $this->builder->switches('status', '客服状态', (int)($formData['status'] ?? 0))
        ->falseValue(0)
        ->trueValue(1)
        ->openStr('打开')
        ->closeStr('关闭')
        ->size('large');

    return $field;
}

public function create()
{
    return create_form('添加客服', $this->createServiceForm(), $this->url('/chat/kefu'), 'POST');
}
```

**Java 版本（一比一还原）：**
```java
public List<BaseComponent> createServiceForm(Map<String, Object> formData) {
    List<BaseComponent> field = new ArrayList<>();

    field.add(builder.input("nickname", "客服名称",
            (String) formData.getOrDefault("nickname", ""))
        .col(24)
        .required());

    field.add(builder.select("group_id", "请选择分组",
            formData.getOrDefault("group_id", 0))
        .options(service.getOptions()));

    field.add(builder.switches("status", "客服状态",
            (int) formData.getOrDefault("status", 0))
        .falseValue(0)
        .trueValue(1)
        .openStr("打开")
        .closeStr("关闭")
        .size("large"));

    return field;
}

public Map<String, Object> create() {
    return FormHelper.createForm(
        "添加客服",
        createServiceForm(Map.of()),
        "/chat/kefu",
        "POST"
    );
}
```

## 组件详解

### 1. Input 输入框

```java
// 基础用法
builder.input("username", "用户名", "")
    .col(12)
    .required()
    .placeholder("请输入用户名")
    .maxlength(20);

// 密码输入框
builder.input("password", "密码")
    .type("password")
    .required();

// 带图标
builder.input("email", "邮箱")
    .icon("ios-mail")
    .clearable();
```

### 2. Select 下拉选择

```java
// 基础用法
builder.select("city", "城市", 0)
    .addOption(1, "北京")
    .addOption(2, "上海")
    .addOption(3, "广州")
    .filterable()
    .clearable();

// 使用选项列表
List<OptionComponent> options = Arrays.asList(
    new OptionComponent(1, "选项1", false),
    new OptionComponent(2, "选项2", false)
);
builder.select("type", "类型", 0)
    .options(options)
    .multiple();
```

### 3. Switch 开关

```java
builder.switches("status", "状态", 1)
    .trueValue(1)          // 打开时的值
    .falseValue(0)         // 关闭时的值
    .openStr("启用")       // 打开时的文字
    .closeStr("禁用")      // 关闭时的文字
    .size("large");        // 尺寸：small/default/large
```

### 4. Textarea 文本域

```java
builder.textarea("description", "描述", "")
    .rows(4)
    .maxlength(200)
    .autosize();
```

### 5. FrameImage 图片选择器

```java
builder.frameImage("avatar", "头像", "/admin/widget/images", "")
    .icon("ios-add")
    .width("950px")
    .height("420px")
    .maxLength(1);
```

### 6. Radio 单选框

```java
builder.radio("gender", "性别", 1)
    .addOption(1, "男")
    .addOption(2, "女");
```

### 7. Checkbox 复选框

```java
builder.checkbox("hobby", "爱好", Arrays.asList(1, 2))
    .addOption(1, "读书")
    .addOption(2, "运动")
    .addOption(3, "旅游");
```

### 8. DatePicker 日期选择器

```java
// 日期选择
builder.datePicker("birthday", "生日", "")
    .type("date")
    .format("yyyy-MM-dd")
    .clearable();

// 日期范围
builder.datePicker("dateRange", "日期范围", "")
    .type("daterange");

// 日期时间
builder.datePicker("datetime", "日期时间", "")
    .type("datetime")
    .format("yyyy-MM-dd HH:mm:ss");
```

### 9. InputNumber 数字输入框

```java
builder.inputNumber("age", "年龄", 0)
    .min(0)
    .max(150)
    .step(1);

// 小数
builder.inputNumber("price", "价格", 0.0)
    .min(0)
    .precision(2);
```

### 10. Upload 上传组件

```java
builder.upload("images", "图片上传", "/api/upload")
    .maxLength(5)
    .accept("image/*")
    .listType("picture-card")
    .multiple();
```

### 11. Hidden 隐藏字段

```java
builder.hidden("id", 123);
builder.hidden("appid", "app_12345");
```

## 通用方法

所有组件都继承自 `BaseComponent`，支持以下通用方法：

```java
// 栅格布局（1-24）
.col(12)

// 必填验证
.required()
.required(true)

// 占位符
.placeholder("请输入...")

// 禁用
.disabled(true)

// 自定义属性
.prop("size", "large")
.prop("clearable", true)

// 自定义验证规则
.addValidate("min", 6, "最少6个字符")
.addValidate("max", 20, "最多20个字符")
.addValidate("pattern", "^[a-zA-Z0-9]+$", "只能包含字母和数字")
```

## 完整示例

### Controller 层

```java
@RestController
@RequestMapping("/admin/chat")
public class ServiceController {

    @Autowired
    private ChatServiceService chatServiceService;

    /**
     * 添加客服表单
     */
    @GetMapping("/kefu/add")
    public Result add() {
        Map<String, Object> formConfig = chatServiceService.create();
        return Result.ok(formConfig);
    }

    /**
     * 编辑客服表单
     */
    @GetMapping("/kefu/edit/{id}")
    public Result edit(@PathVariable int id) {
        Map<String, Object> serviceInfo = chatServiceService.getById(id);
        Map<String, Object> formConfig = chatServiceService.edit(id, serviceInfo);
        return Result.ok(formConfig);
    }
}
```

### Service 层

```java
@Service
public class ChatServiceService {

    @Autowired
    private FormBuilder builder;

    /**
     * 创建客服表单字段
     */
    public List<BaseComponent> createServiceForm(Map<String, Object> formData) {
        List<BaseComponent> field = new ArrayList<>();

        // 分组选择
        field.add(builder.select("group_id", "请选择分组",
                formData.getOrDefault("group_id", 0))
            .options(getGroupOptions()));

        // 客服头像
        field.add(builder.frameImage("avatar", "客服头像",
                "/admin/widget/images/index?fodder=avatar",
                (String) formData.getOrDefault("avatar", ""))
            .icon("ios-add")
            .width("950px")
            .height("420px"));

        // 客服名称
        field.add(builder.input("nickname", "客服名称",
                (String) formData.getOrDefault("nickname", ""))
            .col(24)
            .required());

        // 手机号码
        field.add(builder.input("phone", "手机号码",
                (String) formData.getOrDefault("phone", ""))
            .col(24)
            .required()
            .addValidate("pattern", "^[+0-9\\s\\-()]{4,25}$", "请输入正确的手机号（支持国际号码格式）"));

        // 登录账号和密码
        if (!formData.isEmpty()) {
            // 编辑模式
            field.add(builder.input("account", "登录账号",
                    (String) formData.getOrDefault("account", ""))
                .col(24).required());
            field.add(builder.input("password", "登录密码")
                .type("password").col(24));
            field.add(builder.input("true_password", "确认密码")
                .type("password").col(24));
        } else {
            // 新增模式
            field.add(builder.input("account", "登录账号")
                .col(24).required()
                .addValidate("pattern", "^[a-zA-Z0-9]{4,30}$", "账号必须为数字或字母的组合4-30位"));
            field.add(builder.input("password", "登录密码")
                .type("password").col(24).required()
                .addValidate("pattern", "^[0-9a-z_$]{6,20}$", "密码必须为数字或字母的组合6-20位"));
            field.add(builder.input("true_password", "确认密码")
                .type("password").col(24).required());
        }

        // 欢迎语
        field.add(builder.textarea("welcome_words", "欢迎语",
                (String) formData.getOrDefault("welcome_words", "")));

        // 自动回复
        field.add(builder.switches("auto_reply", "自动回复",
                (int) formData.getOrDefault("auto_reply", 0))
            .falseValue(0).trueValue(1)
            .openStr("打开").closeStr("关闭")
            .size("large"));

        // 客服状态
        field.add(builder.switches("status", "客服状态",
                (int) formData.getOrDefault("status", 0))
            .falseValue(0).trueValue(1)
            .openStr("打开").closeStr("关闭")
            .size("large"));

        return field;
    }

    /**
     * 创建表单配置
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
     * 编辑表单配置
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
     * 获取分组选项
     */
    private List<OptionComponent> getGroupOptions() {
        // 从数据库查询分组
        // 这里简化为示例数据
        List<OptionComponent> options = new ArrayList<>();
        options.add(new OptionComponent(0, "默认分组", false));
        options.add(new OptionComponent(1, "售前客服", false));
        options.add(new OptionComponent(2, "售后客服", false));
        return options;
    }
}
```

## 返回的 JSON 格式

```json
{
  "title": "添加客服",
  "action": "/chat/kefu",
  "method": "POST",
  "rule": [
    {
      "type": "select",
      "field": "group_id",
      "title": "请选择分组",
      "value": 0,
      "options": [
        {"value": 0, "label": "默认分组"},
        {"value": 1, "label": "售前客服"}
      ]
    },
    {
      "type": "input",
      "field": "nickname",
      "title": "客服名称",
      "value": "",
      "col": {"span": 24},
      "validate": [
        {"required": true, "message": "客服名称不能为空", "trigger": "blur"}
      ]
    },
    {
      "type": "switch",
      "field": "status",
      "title": "客服状态",
      "value": 0,
      "props": {
        "activeValue": 1,
        "inactiveValue": 0,
        "activeText": "打开",
        "inactiveText": "关闭",
        "size": "large"
      }
    }
  ],
  "config": {
    "form": {
      "inline": false,
      "labelPosition": "right",
      "labelWidth": "125px",
      "size": "default"
    },
    "submitBtn": {
      "show": true,
      "innerText": "提交",
      "type": "primary",
      "size": "default",
      "col": {"span": 12}
    },
    "resetBtn": {
      "show": false,
      "innerText": "重置",
      "size": "default",
      "col": {"span": 12}
    }
  }
}
```

## 前端使用（Vue + Iview）

```vue
<template>
  <div>
    <Modal v-model="visible" :title="formConfig.title" width="800">
      <Form :model="formData" :label-width="125">
        <FormItem
          v-for="item in formConfig.rule"
          :key="item.field"
          :label="item.title"
          :prop="item.field"
          :rules="item.validate"
        >
          <!-- Input -->
          <Input
            v-if="item.type === 'input'"
            v-model="formData[item.field]"
            v-bind="item.props"
          />

          <!-- Select -->
          <Select
            v-if="item.type === 'select'"
            v-model="formData[item.field]"
            v-bind="item.props"
          >
            <Option
              v-for="opt in item.options"
              :key="opt.value"
              :value="opt.value"
              :label="opt.label"
            />
          </Select>

          <!-- Switch -->
          <i-switch
            v-if="item.type === 'switch'"
            v-model="formData[item.field]"
            v-bind="item.props"
          />

          <!-- 其他组件... -->
        </FormItem>
      </Form>

      <div slot="footer">
        <Button @click="visible = false">取消</Button>
        <Button type="primary" @click="handleSubmit">提交</Button>
      </div>
    </Modal>
  </div>
</template>

<script>
export default {
  data() {
    return {
      visible: false,
      formConfig: {},
      formData: {}
    }
  },
  methods {
    async loadForm() {
      const res = await this.$http.get('/admin/chat/kefu/add');
      this.formConfig = res.data;
      // 初始化表单数据
      this.formData = {};
      this.formConfig.rule.forEach(item => {
        this.formData[item.field] = item.value;
      });
      this.visible = true;
    },
    async handleSubmit() {
      await this.$http.post(this.formConfig.action, this.formData);
      this.$Message.success('提交成功');
      this.visible = false;
    }
  }
}
</script>
```

## 对比 PHP 和 Java

| 特性 | PHP | Java |
|------|-----|------|
| 链式调用 | `->col(24)->required()` | `.col(24).required()` |
| 数组操作 | `$field[] = ...` | `field.add(...)` |
| 默认值 | `$data['field'] ?? ''` | `formData.getOrDefault("field", "")` |
| 全局函数 | `create_form()` | `FormHelper.createForm()` |
| 依赖注入 | `app()->make(...)` | `@Autowired` |

## 注意事项

1. **类型转换**：Java 是强类型语言，需要显式类型转换
   ```java
   (String) formData.getOrDefault("nickname", "")
   (int) formData.getOrDefault("status", 0)
   ```

2. **Map 使用**：Java 的 Map 操作比 PHP 数组稍微复杂
   ```java
   // PHP: $data['field'] ?? ''
   // Java: formData.getOrDefault("field", "")
   ```

3. **依赖注入**：使用 Spring 的 `@Autowired` 而不是 PHP 的 `app()->make()`

4. **返回值**：统一返回 `Map<String, Object>` 以便 Jackson 序列化为 JSON

## 扩展开发

如需添加新组件，按以下步骤：

1. 在 `components` 包下创建新组件类，继承 `BaseComponent`
2. 在 `IviewFactory` 中添加工厂方法
3. 在 `FormBuilder` 中添加便捷方法
4. 更新文档

示例：添加 Slider 滑块组件

```java
// 1. 创建 SliderComponent.java
public class SliderComponent extends BaseComponent {
    public SliderComponent(String field, String title, Number value) {
        this.type = "slider";
        this.field = field;
        this.title = title;
        this.value = value;
    }

    public SliderComponent range(Number min, Number max) {
        this.props.put("min", min);
        this.props.put("max", max);
        return this;
    }
}

// 2. 在 IviewFactory 中添加
public SliderComponent slider(String field, String title, Number value) {
    return new SliderComponent(field, title, value);
}

// 3. 在 FormBuilder 中添加
public SliderComponent slider(String field, String title, Number value) {
    return factory.slider(field, title, value);
}

// 4. 使用
builder.slider("volume", "音量", 50)
    .range(0, 100);
```

## 总结

Java FormBuilder 完全还原了 PHP FormBuilder 的功能和使用方式，主要差异在于：

- 语法：从 PHP 的 `->` 改为 Java 的 `.`
- 类型：Java 需要显式类型转换
- 依赖注入：使用 Spring 的 `@Autowired`

使用体验和 PHP 版本基本一致，可以无缝迁移。
