<template>
  <div>
    <Form ref="formValidate" :model="form" :rules="rules" :label-width="100">
      <Row :gutter="20">
        <!-- 基本信息 -->
        <Col span="24">
          <h3 style="margin-bottom: 16px; color: #515a6e;">基本信息</h3>
        </Col>

        <Col span="12">
          <FormItem label="租户账号" prop="account">
            <Input
              v-model="form.account"
              placeholder="请输入租户账号"
              :disabled="isEdit"
            />
            <div class="ivu-form-item-tip" v-if="!isEdit">
              租户账号创建后不可修改，支持邮箱格式
            </div>
          </FormItem>
        </Col>

        <Col span="12">
          <FormItem label="租户名称" prop="tenant_name">
            <Input v-model="form.tenant_name" placeholder="请输入租户名称" />
          </FormItem>
        </Col>

        <Col span="12">
          <FormItem label="邮箱地址" prop="contact_email">
            <Input v-model="form.contact_email" placeholder="请输入邮箱地址" />
          </FormItem>
        </Col>

        <Col span="12">
          <FormItem label="联系电话" prop="contact_phone">
            <Input v-model="form.contact_phone" placeholder="请输入联系电话" />
          </FormItem>
        </Col>

        <Col span="12" v-if="!isEdit">
          <FormItem label="初始密码" prop="pwd">
            <Input
              type="password"
              v-model="form.pwd"
              placeholder="请输入初始密码"
            />
            <div class="ivu-form-item-tip">
              密码长度6-20位，包含数字和字母
            </div>
          </FormItem>
        </Col>

        <Col span="12">
          <FormItem label="租户状态" prop="status">
            <Select v-model="form.status" placeholder="请选择状态">
              <Option :value="1">正常</Option>
              <Option :value="0">禁用</Option>
            </Select>
          </FormItem>
        </Col>

        <!-- 权限配置 -->
        <Col span="24">
          <h3 style="margin: 24px 0 16px 0; color: #515a6e;">权限配置</h3>
        </Col>

        <Col span="12">
          <FormItem label="用户数量限制" prop="user_limit">
            <InputNumber
              v-model="form.user_limit"
              :min="1"
              :max="10000"
              placeholder="用户数量限制"
              style="width: 100%"
            />
            <div class="ivu-form-item-tip">
              该租户下最多可创建的用户数量，0表示不限制
            </div>
          </FormItem>
        </Col>

        <Col span="12">
          <FormItem label="客服数量限制" prop="service_limit">
            <InputNumber
              v-model="form.service_limit"
              :min="1"
              :max="1000"
              placeholder="客服数量限制"
              style="width: 100%"
            />
            <div class="ivu-form-item-tip">
              该租户下最多可创建的客服数量，0表示不限制
            </div>
          </FormItem>
        </Col>

        <!-- 有效期设置 -->
        <Col span="24">
          <h3 style="margin: 24px 0 16px 0; color: #515a6e;">有效期设置</h3>
        </Col>

        <Col span="12">
          <FormItem label="到期时间" prop="expire_at">
            <DatePicker
              v-model="form.expire_at"
              type="date"
              placeholder="请选择到期时间"
              style="width: 100%"
              :options="dateOptions"
            />
            <div class="ivu-form-item-tip">
              不设置表示永久有效
            </div>
          </FormItem>
        </Col>

        <Col span="12">
          <FormItem label="自动续期" prop="auto_renew">
            <i-switch v-model="form.auto_renew" :true-value="1" :false-value="0">
              <span slot="open">开启</span>
              <span slot="close">关闭</span>
            </i-switch>
            <div class="ivu-form-item-tip">
              开启后系统将在到期前自动续期
            </div>
          </FormItem>
        </Col>

        <!-- 其他设置 -->
        <Col span="24">
          <h3 style="margin: 24px 0 16px 0; color: #515a6e;">其他设置</h3>
        </Col>

        <Col span="24">
          <FormItem label="备注信息" prop="remark">
            <Input
              v-model="form.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注信息"
              :maxlength="200"
              show-word-limit
            />
          </FormItem>
        </Col>
      </Row>
    </Form>
  </div>
</template>

<script>
import { validateGlobalPhone } from '@/utils/validate'

export default {
  name: 'TenantForm',
  props: {
    formData: {
      type: Object,
      default: () => ({})
    },
    isEdit: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      form: {
        account: '',
        tenant_name: '',
        contact_email: '',
        contact_phone: '',
        pwd: '',
        status: 1,
        user_limit: 100,
        service_limit: 10,
        expire_at: '',
        auto_renew: 0,
        remark: ''
      },
      rules: {
        account: [
          { required: true, message: '请输入租户账号', trigger: 'blur' },
          { min: 3, max: 50, message: '账号长度在 3 到 50 个字符', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9@._-]+$/, message: '账号只能包含字母、数字、@、.、_、-', trigger: 'blur' }
        ],
        tenant_name: [
          { required: true, message: '请输入租户名称', trigger: 'blur' },
          { min: 2, max: 20, message: '名称长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        contact_email: [
          { required: true, message: '请输入邮箱地址', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ],
        contact_phone: [
          { validator: validateGlobalPhone, trigger: 'blur', message: this.$t('kefu.phoneFormatError') }
        ],
        pwd: [
          { required: !this.isEdit, message: '请输入初始密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
          { pattern: /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{6,20}$/, message: '密码必须包含字母和数字', trigger: 'blur' }
        ],
        status: [
          { required: true, type: 'number', message: '请选择租户状态', trigger: 'change' }
        ],
        user_limit: [
          { required: true, type: 'number', message: '请输入用户数量限制', trigger: 'blur' }
        ],
        service_limit: [
          { required: true, type: 'number', message: '请输入客服数量限制', trigger: 'blur' }
        ]
      },
      dateOptions: {
        disabledDate(date) {
          // 禁止选择今天之前的日期
          return date && date.valueOf() < Date.now() - 86400000
        }
      }
    }
  },
  watch: {
    formData: {
      handler(newVal) {
        if (newVal && Object.keys(newVal).length > 0) {
          this.form = { ...this.form, ...newVal }
          // 处理日期格式
          if (this.form.expire_at) {
            this.form.expire_at = new Date(this.form.expire_at)
          }
        }
      },
      immediate: true,
      deep: true
    }
  },
  methods: {
    // 验证表单
    validate() {
      return new Promise((resolve) => {
        this.$refs.formValidate.validate((valid) => {
          resolve(valid)
        })
      })
    },

    // 获取表单数据
    getFormData() {
      const formData = { ...this.form }

      // 处理日期格式
      if (formData.expire_at) {
        const date = new Date(formData.expire_at)
        formData.expire_at = date.getFullYear() + '-' +
          String(date.getMonth() + 1).padStart(2, '0') + '-' +
          String(date.getDate()).padStart(2, '0')
      } else {
        formData.expire_at = null
      }

      formData.contact_phone = formData.contact_phone ? formData.contact_phone.trim() : ''

      // 编辑时不传递密码字段
      if (this.isEdit) {
        delete formData.pwd
      }

      return formData
    },

    // 重置表单
    resetForm() {
      this.$refs.formValidate.resetFields()
      this.form = {
        account: '',
        tenant_name: '',
        contact_email: '',
        contact_phone: '',
        pwd: '',
        status: 1,
        user_limit: 100,
        service_limit: 10,
        expire_at: '',
        auto_renew: 0,
        remark: ''
      }
    }
  }
}
</script>

<style scoped>
.ivu-form-item-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

h3 {
  font-weight: 600;
  border-left: 4px solid #2d8cf0;
  padding-left: 8px;
}
</style>