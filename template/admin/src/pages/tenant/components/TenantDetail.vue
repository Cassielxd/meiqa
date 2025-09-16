<template>
  <Modal
    v-model="visible"
    :title="modalTitle"
    width="1000"
    :mask-closable="false"
    footer-hide
  >
    <div v-if="loading" class="text-center" style="padding: 40px;">
      <Spin size="large" />
    </div>

    <div v-else-if="tenantInfo" class="tenant-detail">
      <!-- 基本信息卡片 -->
      <Card :bordered="false" dis-hover class="ivu-mt">
        <div slot="title">
          <Icon type="md-person" style="margin-right: 8px;" />
          基本信息
        </div>
        <div slot="extra">
          <Button type="primary" @click="editTenant" icon="md-create">编辑租户</Button>
          <Button type="warning" @click="resetPassword" icon="md-key" class="ml10">重置密码</Button>
          <Dropdown @on-click="handleMoreAction" trigger="hover" class="ml10">
            <Button type="info">
              更多操作
              <Icon type="ios-arrow-down"></Icon>
            </Button>
            <DropdownMenu slot="list">
              <DropdownItem name="toggleStatus">
                {{ tenantInfo.status === 1 ? '禁用' : '启用' }}租户
              </DropdownItem>
              <DropdownItem name="delete" style="color: #ed4014">删除租户</DropdownItem>
            </DropdownMenu>
          </Dropdown>
        </div>

        <Row :gutter="20">
          <Col span="8">
            <div class="info-item">
              <div class="label">租户ID</div>
              <div class="value">{{ tenantInfo.id }}</div>
            </div>
          </Col>
          <Col span="8">
            <div class="info-item">
              <div class="label">租户账号</div>
              <div class="value">{{ tenantInfo.account }}</div>
            </div>
          </Col>
          <Col span="8">
            <div class="info-item">
              <div class="label">租户名称</div>
              <div class="value">{{ tenantInfo.tenant_name }}</div>
            </div>
          </Col>
          <Col span="8">
            <div class="info-item">
              <div class="label">邮箱地址</div>
              <div class="value">{{ tenantInfo.contact_email }}</div>
            </div>
          </Col>
          <Col span="8">
            <div class="info-item">
              <div class="label">联系电话</div>
              <div class="value">{{ tenantInfo.contact_phone || '-' }}</div>
            </div>
          </Col>
          <Col span="8">
            <div class="info-item">
              <div class="label">状态</div>
              <div class="value">
                <Tag :color="tenantInfo.status === 1 ? 'success' : 'error'">
                  {{ tenantInfo.status === 1 ? '正常' : '禁用' }}
                </Tag>
              </div>
            </div>
          </Col>
          <Col span="8">
            <div class="info-item">
              <div class="label">创建时间</div>
              <div class="value">{{ tenantInfo.created_at }}</div>
            </div>
          </Col>
          <Col span="8">
            <div class="info-item">
              <div class="label">最后登录</div>
              <div class="value">{{ tenantInfo.last_time || '从未登录' }}</div>
            </div>
          </Col>
          <Col span="8">
            <div class="info-item">
              <div class="label">登录IP</div>
              <div class="value">{{ tenantInfo.last_ip || '-' }}</div>
            </div>
          </Col>
        </Row>

        <Row :gutter="20" class="mt20" v-if="tenantInfo.remark">
          <Col span="24">
            <div class="info-item">
              <div class="label">备注信息</div>
              <div class="value">{{ tenantInfo.remark }}</div>
            </div>
          </Col>
        </Row>
      </Card>

      <!-- 权限配置卡片 -->
      <Card :bordered="false" dis-hover class="ivu-mt">
        <div slot="title">
          <Icon type="md-settings" style="margin-right: 8px;" />
          权限配置
        </div>

        <Row :gutter="20">
          <Col span="8">
            <div class="info-item">
              <div class="label">用户数量限制</div>
              <div class="value">
                {{ tenantInfo.user_limit || '不限制' }}
                <span v-if="tenantInfo.user_limit" class="unit">个</span>
              </div>
            </div>
          </Col>
          <Col span="8">
            <div class="info-item">
              <div class="label">客服数量限制</div>
              <div class="value">
                {{ tenantInfo.service_limit || '不限制' }}
                <span v-if="tenantInfo.service_limit" class="unit">个</span>
              </div>
            </div>
          </Col>
          <Col span="8">
            <div class="info-item">
              <div class="label">到期时间</div>
              <div class="value">
                <span v-if="tenantInfo.expire_at">
                  {{ tenantInfo.expire_at }}
                  <Tag color="warning" v-if="isExpiringSoon(tenantInfo.expire_at)">即将过期</Tag>
                  <Tag color="error" v-if="isExpired(tenantInfo.expire_at)">已过期</Tag>
                </span>
                <span v-else>永久有效</span>
              </div>
            </div>
          </Col>
          <Col span="8">
            <div class="info-item">
              <div class="label">自动续期</div>
              <div class="value">
                <Tag :color="tenantInfo.auto_renew === 1 ? 'success' : 'default'">
                  {{ tenantInfo.auto_renew === 1 ? '已开启' : '未开启' }}
                </Tag>
              </div>
            </div>
          </Col>
        </Row>
      </Card>

      <!-- 应用信息卡片 -->
      <Card :bordered="false" dis-hover class="ivu-mt" v-if="tenantInfo.appid">
        <div slot="title">
          <Icon type="md-code" style="margin-right: 8px;" />
          应用信息
        </div>

        <Row :gutter="20">
          <Col span="12">
            <div class="info-item">
              <div class="label">应用ID</div>
              <div class="value">{{ tenantInfo.appid || '-' }}</div>
            </div>
          </Col>
          <Col span="12">
            <div class="info-item">
              <div class="label">应用密钥</div>
              <div class="value" style="font-family: monospace;">{{ tenantInfo.app_secret ? '••••••••••••••••' : '-' }}</div>
            </div>
          </Col>
        </Row>
      </Card>
    </div>

    <!-- 编辑租户弹窗 -->
    <Modal
      v-model="editModalVisible"
      title="编辑租户"
      width="700"
      :loading="editModalLoading"
      @on-ok="handleEditOk"
      @on-cancel="handleEditCancel"
    >
      <tenant-form
        ref="tenantForm"
        :form-data="tenantInfo"
        :is-edit="true"
      />
    </Modal>
  </Modal>
</template>

<script>
import { tenantInfoApi, tenantUpdateApi, tenantDeleteApi, tenantUpdateStatusApi, tenantResetPasswordApi } from '@/api/tenant'
import TenantForm from './TenantForm'

export default {
  name: 'TenantDetail',
  components: {
    TenantForm
  },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    tenantId: {
      type: [String, Number],
      default: null
    }
  },
  data() {
    return {
      visible: this.value,
      tenantInfo: null,
      loading: false,
      stats: {},
      logs: [],

      // 编辑弹窗
      editModalVisible: false,
      editModalLoading: true
    }
  },
  computed: {
    modalTitle() {
      if (this.tenantInfo) {
        return `租户详情 - ${this.tenantInfo.tenant_name} (${this.tenantInfo.account})`
      }
      return '租户详情'
    }
  },
  watch: {
    value(val) {
      this.visible = val
      if (val && this.tenantId) {
        this.getTenantDetail()
      }
    },
    visible(val) {
      this.$emit('input', val)
      if (!val) {
        this.tenantInfo = null
        this.stats = {}
        this.logs = []
      }
    },
    tenantId(val) {
      if (val && this.visible) {
        this.getTenantDetail()
      }
    }
  },
  methods: {
    // 获取租户详情
    async getTenantDetail() {
      if (!this.tenantId) {
        this.$Message.error('租户ID不存在')
        this.visible = false
        return
      }

      this.loading = true
      try {
        const { data } = await tenantInfoApi(this.tenantId)
        this.tenantInfo = data
        this.stats = {}
        this.logs = []
      } catch (error) {
        this.$Message.error('获取租户详情失败')
        this.visible = false
      } finally {
        this.loading = false
      }
    },

    // 编辑租户
    editTenant() {
      this.editModalVisible = true
    },

    // 编辑确认
    async handleEditOk() {
      const valid = await this.$refs.tenantForm.validate()
      if (!valid) {
        this.editModalLoading = false
        this.$nextTick(() => {
          this.editModalLoading = true
        })
        return
      }

      try {
        const formData = this.$refs.tenantForm.getFormData()
        await tenantUpdateApi(this.tenantId, formData)
        this.$Message.success('租户更新成功')
        this.editModalVisible = false
        this.getTenantDetail()
        this.$emit('refresh')
      } catch (error) {
        this.$Message.error('租户更新失败')
        this.editModalLoading = false
        this.$nextTick(() => {
          this.editModalLoading = true
        })
      }
    },

    // 编辑取消
    handleEditCancel() {
      this.editModalVisible = false
    },

    // 重置密码
    resetPassword() {
      this.$Modal.confirm({
        title: '确认重置',
        content: `确定要重置租户 ${this.tenantInfo.account} 的密码吗？`,
        onOk: async () => {
          try {
            await tenantResetPasswordApi(this.tenantId, {})
            this.$Message.success('密码重置成功')
          } catch (error) {
            this.$Message.error('密码重置失败')
          }
        }
      })
    },

    // 更多操作
    handleMoreAction(name) {
      if (name === 'toggleStatus') {
        this.toggleStatus()
      } else if (name === 'delete') {
        this.deleteTenant()
      }
    },

    // 切换状态
    async toggleStatus() {
      const status = this.tenantInfo.status === 1 ? 0 : 1
      const action = status === 1 ? '启用' : '禁用'

      this.$Modal.confirm({
        title: `确认${action}`,
        content: `确定要${action}租户 ${this.tenantInfo.account} 吗？`,
        onOk: async () => {
          try {
            await tenantUpdateStatusApi(this.tenantId, status)
            this.$Message.success(`租户${action}成功`)
            this.tenantInfo.status = status
            this.$emit('refresh')
          } catch (error) {
            this.$Message.error(`租户${action}失败`)
          }
        }
      })
    },

    // 删除租户
    deleteTenant() {
      this.$Modal.confirm({
        title: '确认删除',
        content: `确定要删除租户 ${this.tenantInfo.account} 吗？此操作无法撤销。`,
        onOk: async () => {
          try {
            await tenantDeleteApi(this.tenantId)
            this.$Message.success('租户删除成功')
            this.visible = false
            this.$emit('refresh')
          } catch (error) {
            this.$Message.error('租户删除失败')
          }
        }
      })
    },


    // 判断是否即将过期
    isExpiringSoon(expireTime) {
      if (!expireTime) return false
      const now = new Date()
      const expire = new Date(expireTime)
      const diffDays = (expire - now) / (1000 * 60 * 60 * 24)
      return diffDays > 0 && diffDays <= 30
    },

    // 判断是否已过期
    isExpired(expireTime) {
      if (!expireTime) return false
      return new Date(expireTime) < new Date()
    },

  }
}
</script>

<style scoped>
.tenant-detail {
  max-width: 100%;
}

.info-item {
  margin-bottom: 16px;
}

.info-item .label {
  color: #999;
  font-size: 14px;
  margin-bottom: 4px;
}

.info-item .value {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.info-item .unit {
  font-size: 12px;
  color: #999;
  font-weight: normal;
  margin-left: 2px;
}

.stat-card {
  text-align: center;
  padding: 16px;
  background: #f8f8f9;
  border-radius: 6px;
  border: 1px solid #e8eaec;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #2d8cf0;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-progress {
  height: 6px;
}

.ml10 {
  margin-left: 10px;
}

.mt20 {
  margin-top: 20px;
}

.text-center {
  text-align: center;
}
</style>