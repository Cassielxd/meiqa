# FormBuilder 实际使用示例

## 完整的应用管理示例（一比一还原PHP）

### 1. Service层实现

**PHP代码：**
```php
// ApplicationServices.php
public function getFormRule(array $data = [])
{
    return [
        FormBuilder::frameImage('icon', '应用图标', $this->url('admin/widget.images/index', ['fodder' => 'icon'], true), $data['value'])
            ->icon('ios-image')->width('950px')->height('420px')->info($data['desc'])->col(13)->required(),
        FormBuilder::input('name', '应用名称', $data['name'] ?? '')->required(),
        FormBuilder::textarea('introduce', '应用简介', $data['introduce'] ?? ''),
    ];
}

public function getCreateForm()
{
    return create_form('添加应用', $this->getFormRule(), $this->url('admin/app'), 'post');
}

public function getUpdateForm(int $id)
{
    $appInfo = $this->dao->get($id);
    if (!$appInfo) {
        throw new AdminException('修改的应用不存在');
    }
    return create_form('修改应用', $this->getFormRule($appInfo->toArray()), $this->url('admin/app', ['id' => $id]), 'put');
}
```

**Java代码（已实现）：**
```java
// AdminApplicationService.java
@Service
@AllArgsConstructor
public class AdminApplicationService {

    private final ApplicationMapper applicationMapper;
    private final FormBuilder formBuilder;

    /**
     * 获取表单规则
     * 对应PHP: ApplicationServices.php::getFormRule()
     */
    public List<BaseComponent> getFormRule(Map<String, Object> data) {
        List<BaseComponent> rules = new ArrayList<>();

        // 1. 应用图标（FrameImage组件）
        rules.add(formBuilder.frameImage("icon", "应用图标",
                "/admin/widget/images/index?fodder=icon",
                (String) data.getOrDefault("icon", ""))
            .icon("ios-image")
            .width("950px")
            .height("420px")
            .col(13)
            .required());

        // 2. 应用名称
        rules.add(formBuilder.input("name", "应用名称",
                (String) data.getOrDefault("name", ""))
            .required());

        // 3. 应用简介
        rules.add(formBuilder.textarea("introduce", "应用简介",
                (String) data.getOrDefault("introduce", "")));

        return rules;
    }

    /**
     * 获取创建表单
     * 对应PHP: ApplicationServices.php::getCreateForm()
     */
    public Map<String, Object> getCreateForm() {
        return FormHelper.createForm(
            "添加应用",
            getFormRule(new HashMap<>()),
            "/admin/app",
            "POST"
        );
    }

    /**
     * 获取编辑表单
     * 对应PHP: ApplicationServices.php::getUpdateForm()
     */
    public Map<String, Object> getEditForm(Integer id) {
        if (id == null || id <= 0) {
            throw new CrmChatException("Missing required parameter");
        }

        ApplicationEntity app = applicationMapper.selectById(id);
        if (app == null || app.getIsDelete() == 1) {
            throw new CrmChatException("Application does not exist");
        }

        // 转换实体为Map（对应PHP的toArray()）
        Map<String, Object> appData = new HashMap<>();
        appData.put("icon", app.getIcon());
        appData.put("name", app.getName());
        appData.put("introduce", app.getIntroduce());

        return FormHelper.createForm(
            "修改应用",
            getFormRule(appData),
            "/admin/app/" + id,
            "PUT"
        );
    }
}
```

### 2. Controller层实现

**Java代码：**
```java
@RestController
@RequestMapping("/admin/app")
public class ApplicationController {

    @Autowired
    private AdminApplicationService applicationService;

    /**
     * 获取创建表单
     * 对应PHP: Application.php::create()
     */
    @GetMapping("/create")
    public Result getCreateForm() {
        Map<String, Object> formConfig = applicationService.getCreateForm();
        return Result.ok(formConfig);
    }

    /**
     * 获取编辑表单
     * 对应PHP: Application.php::edit()
     */
    @GetMapping("/edit/{id}")
    public Result getEditForm(@PathVariable Integer id) {
        Map<String, Object> formConfig = applicationService.getEditForm(id);
        return Result.ok(formConfig);
    }

    /**
     * 保存应用
     */
    @PostMapping
    public Result save(@RequestBody Map<String, Object> data) {
        String message = applicationService.saveApplication(data);
        return Result.ok(message);
    }

    /**
     * 更新应用
     */
    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        String message = applicationService.updateApplication(id, data);
        return Result.ok(message);
    }
}
```

### 3. 返回的JSON格式

**创建表单（/admin/app/create）：**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "title": "添加应用",
    "action": "/admin/app",
    "method": "POST",
    "rule": [
      {
        "type": "frameInputs",
        "field": "icon",
        "title": "应用图标",
        "value": "",
        "props": {
          "src": "/admin/widget/images/index?fodder=icon",
          "maxLength": 1,
          "type": "image",
          "icon": "ios-image",
          "modalWidth": "950px",
          "modalHeight": "420px"
        },
        "col": {
          "span": 13
        },
        "validate": [
          {
            "required": true,
            "message": "应用图标不能为空",
            "trigger": "blur"
          }
        ]
      },
      {
        "type": "input",
        "field": "name",
        "title": "应用名称",
        "value": "",
        "validate": [
          {
            "required": true,
            "message": "应用名称不能为空",
            "trigger": "blur"
          }
        ]
      },
      {
        "type": "input",
        "field": "introduce",
        "title": "应用简介",
        "value": "",
        "props": {
          "type": "textarea"
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
        "col": {
          "span": 12
        }
      },
      "resetBtn": {
        "show": false,
        "innerText": "重置",
        "size": "default",
        "col": {
          "span": 12
        }
      }
    }
  }
}
```

**编辑表单（/admin/app/edit/1）：**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "title": "修改应用",
    "action": "/admin/app/1",
    "method": "PUT",
    "rule": [
      {
        "type": "frameInputs",
        "field": "icon",
        "title": "应用图标",
        "value": "/uploads/icon.png",
        "props": {
          "src": "/admin/widget/images/index?fodder=icon",
          "icon": "ios-image",
          "modalWidth": "950px",
          "modalHeight": "420px"
        },
        "col": {
          "span": 13
        },
        "validate": [
          {
            "required": true,
            "message": "应用图标不能为空",
            "trigger": "blur"
          }
        ]
      },
      {
        "type": "input",
        "field": "name",
        "title": "应用名称",
        "value": "我的应用",
        "validate": [
          {
            "required": true,
            "message": "应用名称不能为空",
            "trigger": "blur"
          }
        ]
      },
      {
        "type": "input",
        "field": "introduce",
        "title": "应用简介",
        "value": "这是一个测试应用",
        "props": {
          "type": "textarea"
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
        "col": {
          "span": 12
        }
      },
      "resetBtn": {
        "show": false,
        "innerText": "重置",
        "size": "default",
        "col": {
          "span": 12
        }
      }
    }
  }
}
```

### 4. 前端Vue集成

```vue
<template>
  <div>
    <!-- 列表页 -->
    <Button @click="handleAdd">添加应用</Button>
    <Table :columns="columns" :data="list">
      <template slot-scope="{ row }" slot="action">
        <Button @click="handleEdit(row.id)">编辑</Button>
        <Button @click="handleDelete(row.id)">删除</Button>
      </template>
    </Table>

    <!-- 表单弹窗 -->
    <Modal v-model="formVisible" :title="formConfig.title" width="800">
      <DynamicForm :formConfig="formConfig" @submit="handleSubmit" />
    </Modal>
  </div>
</template>

<script>
export default {
  data() {
    return {
      list: [],
      formVisible: false,
      formConfig: {}
    }
  },
  methods: {
    // 添加
    async handleAdd() {
      const res = await this.$http.get('/admin/app/create');
      this.formConfig = res.data;
      this.formVisible = true;
    },

    // 编辑
    async handleEdit(id) {
      const res = await this.$http.get(`/admin/app/edit/${id}`);
      this.formConfig = res.data;
      this.formVisible = true;
    },

    // 提交
    async handleSubmit(formData) {
      const { action, method } = this.formConfig;

      if (method === 'POST') {
        await this.$http.post(action, formData);
      } else if (method === 'PUT') {
        await this.$http.put(action, formData);
      }

      this.$Message.success('保存成功');
      this.formVisible = false;
      this.loadList();
    }
  }
}
</script>
```

### 5. 动态表单组件（DynamicForm.vue）

```vue
<template>
  <Form :model="formData" :label-width="formConfig.config?.form?.labelWidth || 125">
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

      <!-- Textarea -->
      <Input
        v-if="item.type === 'input' && item.props?.type === 'textarea'"
        v-model="formData[item.field]"
        type="textarea"
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
      >
        <span slot="open">{{ item.props.activeText }}</span>
        <span slot="close">{{ item.props.inactiveText }}</span>
      </i-switch>

      <!-- FrameImage -->
      <FrameImageUpload
        v-if="item.type === 'frameInputs'"
        v-model="formData[item.field]"
        :src="item.props.src"
        :max-length="item.props.maxLength"
      />

      <!-- 其他组件... -->
    </FormItem>

    <FormItem>
      <Button type="primary" @click="handleSubmit">提交</Button>
      <Button @click="$emit('cancel')">取消</Button>
    </FormItem>
  </Form>
</template>

<script>
export default {
  props: {
    formConfig: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      formData: {}
    }
  },
  watch: {
    formConfig: {
      immediate: true,
      handler(config) {
        // 初始化表单数据
        this.formData = {};
        if (config.rule) {
          config.rule.forEach(item => {
            this.formData[item.field] = item.value;
          });
        }
      }
    }
  },
  methods: {
    handleSubmit() {
      this.$emit('submit', this.formData);
    }
  }
}
</script>
```

## 总结

### PHP vs Java 对比

| 特性 | PHP | Java |
|------|-----|------|
| 表单规则 | `getFormRule(array $data = [])` | `getFormRule(Map<String, Object> data)` |
| 创建表单 | `create_form('标题', $rules, $url, 'post')` | `FormHelper.createForm("标题", rules, url, "POST")` |
| 链式调用 | `->icon('ios-image')->width('950px')` | `.icon("ios-image").width("950px")` |
| 数组操作 | `$data['field'] ?? ''` | `data.getOrDefault("field", "")` |
| 实体转数组 | `$appInfo->toArray()` | 手动转换为Map |

### 使用流程

1. **Service层**：使用FormBuilder构建表单规则
2. **Controller层**：调用Service方法返回表单配置JSON
3. **前端**：动态渲染表单，提交数据到后端
4. **完全一致**：PHP和Java的使用方式完全相同

### 优势

- ✅ 一比一还原PHP实现
- ✅ 链式调用，使用简单
- ✅ 类型安全（Java强类型）
- ✅ 自动生成前端表单配置
- ✅ 支持所有Iview组件
- ✅ 可扩展性强
