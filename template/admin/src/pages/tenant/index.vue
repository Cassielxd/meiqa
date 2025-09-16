<template>
  <div>
    <div class="i-layout-page-header">
      <div class="i-layout-page-header">
        <span class="ivu-page-header-title">{{ $route.meta.title }}</span>
      </div>
    </div>
    <Card :bordered="false" dis-hover class="ivu-mt">
      <!-- 搜索和筛选 -->
      <Form ref="formValidate" :model="searchForm" :label-width="labelWidth" :label-position="labelPosition" @submit.native.prevent>
        <Row type="flex" :gutter="24">
          <Col v-bind="grid">
            <FormItem label="状态：">
              <Select v-model="searchForm.status" placeholder="请选择状态" @on-change="getList" clearable>
                <Option value="">全部</Option>
                <Option value="1">正常</Option>
                <Option value="0">禁用</Option>
              </Select>
            </FormItem>
          </Col>
          <Col v-bind="grid">
            <FormItem label="搜索：">
              <Input
                search
                enter-button
                placeholder="请输入租户账号、邮箱或名称"
                v-model="searchForm.keyword"
                @on-search="getList"
              />
            </FormItem>
          </Col>
        </Row>
        <Row type="flex">
          <Col v-bind="grid">
            <Button type="primary" @click="showCreateModal" icon="md-add">添加租户</Button>
            <Button type="warning" @click="showBatchModal" :disabled="selectedIds.length === 0" class="ml10">
              批量操作
            </Button>
            <Button type="info" @click="getExpiringTenants" icon="md-time" class="ml10">
              即将过期
            </Button>
          </Col>
        </Row>
      </Form>

      <!-- 租户表格 -->
      <Table
        :columns="columns"
        :data="list"
        class="mt25"
        no-data-text="暂无租户数据"
        no-filtered-data-text="暂无筛选结果"
        :loading="loading"
        highlight-row
        @on-selection-change="onSelectionChange"
      >
        <!-- 状态列 -->
        <template slot-scope="{ row }" slot="status">
          <i-switch
            v-model="row.status"
            :value="row.status"
            :true-value="1"
            :false-value="0"
            @on-change="toggleStatus(row)"
            size="large"
          >
            <span slot="open">正常</span>
            <span slot="close">禁用</span>
          </i-switch>
        </template>

        <!-- 到期时间列 -->
        <template slot-scope="{ row }" slot="expire_time">
          <span v-if="row.expire_at">
            {{ row.expire_at }}
            <Tag color="warning" v-if="isExpiringSoon(row.expire_at)">即将过期</Tag>
            <Tag color="error" v-if="isExpired(row.expire_at)">已过期</Tag>
          </span>
          <span v-else>-</span>
        </template>

        <!-- 操作列 -->
        <template slot-scope="{ row, index }" slot="action">
          <Button type="primary" size="small" @click="viewDetail(row)" class="mr5">详情</Button>
          <Button type="warning" size="small" @click="showEditModal(row)" class="mr5">编辑</Button>
          <Dropdown @on-click="handleDropdownClick($event, row)" trigger="hover" class="mr5">
            <Button type="info" size="small">
              更多
              <Icon type="ios-arrow-down"></Icon>
            </Button>
            <DropdownMenu slot="list">
              <DropdownItem name="resetPassword">重置密码</DropdownItem>
              <DropdownItem name="delete" style="color: #ed4014">删除租户</DropdownItem>
            </DropdownMenu>
          </Dropdown>
        </template>
      </Table>

      <!-- 分页 -->
      <div class="mt20" style="text-align: right">
        <Page
          :total="total"
          :page-size="pageSize"
          :current="currentPage"
          @on-change="changePage"
          @on-page-size-change="changePageSize"
          show-sizer
          show-elevator
          show-total
        />
      </div>
    </Card>

    <!-- 创建/编辑租户弹窗 -->
    <Modal
      v-model="modalVisible"
      :title="modalTitle"
      width="600"
      :loading="modalLoading"
      @on-ok="handleModalOk"
      @on-cancel="handleModalCancel"
    >
      <tenant-form
        ref="tenantForm"
        :form-data="currentTenant"
        :is-edit="isEdit"
      />
    </Modal>

    <!-- 批量操作弹窗 -->
    <Modal
      v-model="batchModalVisible"
      title="批量操作"
      @on-ok="handleBatchOk"
    >
      <p>已选择 {{ selectedIds.length }} 个租户</p>
      <Form>
        <FormItem label="操作类型：">
          <Select v-model="batchOperation" placeholder="请选择操作">
            <Option value="enable">启用</Option>
            <Option value="disable">禁用</Option>
          </Select>
        </FormItem>
      </Form>
    </Modal>

    <!-- 即将过期租户弹窗 -->
    <Modal
      v-model="expiringModalVisible"
      title="即将过期的租户"
      width="800"
      footer-hide
    >
      <Table :columns="expiringColumns" :data="expiringList" />
    </Modal>

    <!-- 租户详情弹窗 -->
    <tenant-detail
      v-model="detailModalVisible"
      :tenant-id="currentTenantId"
      @refresh="getList"
    />
  </div>
</template>

<script>
import {
  tenantListApi,
  tenantCreateApi,
  tenantUpdateApi,
  tenantDeleteApi,
  tenantUpdateStatusApi,
  tenantBatchUpdateStatusApi,
  tenantExpiringApi,
  tenantResetPasswordApi
} from '@/api/tenant'
import TenantForm from './components/TenantForm'
import TenantDetail from './components/TenantDetail'

export default {
  name: 'TenantList',
  components: {
    TenantForm,
    TenantDetail
  },
  data() {
    return {
      // 表格相关
      columns: [
        {
          type: 'selection',
          width: 60,
          align: 'center'
        },
        {
          title: 'ID',
          key: 'id',
          width: 80
        },
        {
          title: '租户账号',
          key: 'account',
          minWidth: 120
        },
        {
          title: '租户名称',
          key: 'tenant_name',
          minWidth: 120
        },
        {
          title: '邮箱',
          key: 'contact_email',
          minWidth: 180
        },
        {
          title: '联系电话',
          key: 'contact_phone',
          minWidth: 120
        },
        {
          title: '状态',
          key: 'status',
          slot: 'status',
          width: 100
        },
        {
          title: '到期时间',
          key: 'expire_at',
          slot: 'expire_time',
          minWidth: 120
        },
        {
          title: '创建时间',
          key: 'created_at',
          minWidth: 120
        },
        {
          title: '操作',
          slot: 'action',
          fixed: 'right',
          width: 200
        }
      ],
      list: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      loading: false,

      // 搜索表单
      searchForm: {
        status: '',
        keyword: ''
      },

      // 表单相关
      labelWidth: 80,
      labelPosition: 'right',
      grid: { xs: 24, sm: 12, md: 8, lg: 6, xl: 6 },

      // 弹窗相关
      modalVisible: false,
      modalTitle: '添加租户',
      modalLoading: true,
      isEdit: false,
      currentTenant: {},

      // 批量操作
      selectedIds: [],
      batchModalVisible: false,
      batchOperation: '',

      // 即将过期
      expiringModalVisible: false,
      expiringList: [],
      expiringColumns: [
        { title: '租户账号', key: 'account' },
        { title: '租户名称', key: 'tenant_name' },
        { title: '到期时间', key: 'expire_at' },
        { title: '剩余天数', key: 'remaining_days' }
      ],

      // 详情弹窗
      detailModalVisible: false,
      currentTenantId: null
    }
  },
  mounted() {
    this.getList()
  },
  methods: {
    // 获取租户列表
    async getList() {
      this.loading = true
      try {
        const params = {
          page: this.currentPage,
          limit: this.pageSize,
          ...this.searchForm
        }
        const { data } = await tenantListApi(params)
        this.list = data.list || []
        this.total = data.count || 0
      } catch (error) {
        this.$Message.error('获取租户列表失败')
      } finally {
        this.loading = false
      }
    },

    // 分页相关
    changePage(page) {
      this.currentPage = page
      this.getList()
    },
    changePageSize(size) {
      this.pageSize = size
      this.currentPage = 1
      this.getList()
    },

    // 状态切换
    async toggleStatus(row) {
      try {
        await tenantUpdateStatusApi(row.id, row.status)
        this.$Message.success('状态更新成功')
        this.getList()
      } catch (error) {
        this.$Message.error('状态更新失败')
        row.status = row.status === 1 ? 0 : 1 // 回滚状态
      }
    },

    // 选择变化
    onSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.id)
    },

    // 显示创建弹窗
    showCreateModal() {
      this.modalTitle = '添加租户'
      this.isEdit = false
      this.currentTenant = {}
      this.modalVisible = true
    },

    // 显示编辑弹窗
    showEditModal(row) {
      this.modalTitle = '编辑租户'
      this.isEdit = true
      this.currentTenant = { ...row }
      this.modalVisible = true
    },

    // 弹窗确认
    async handleModalOk() {
      const valid = await this.$refs.tenantForm.validate()
      if (!valid) {
        this.modalLoading = false
        this.$nextTick(() => {
          this.modalLoading = true
        })
        return
      }

      try {
        const formData = this.$refs.tenantForm.getFormData()
        if (this.isEdit) {
          await tenantUpdateApi(this.currentTenant.id, formData)
          this.$Message.success('租户更新成功')
        } else {
          await tenantCreateApi(formData)
          this.$Message.success('租户创建成功')
        }
        this.modalVisible = false
        this.getList()
      } catch (error) {
        this.$Message.error(this.isEdit ? '租户更新失败' : '租户创建失败')
        this.modalLoading = false
        this.$nextTick(() => {
          this.modalLoading = true
        })
      }
    },

    // 弹窗取消
    handleModalCancel() {
      this.modalVisible = false
    },

    // 下拉菜单点击
    handleDropdownClick(name, row) {
      if (name === 'resetPassword') {
        this.resetPassword(row)
      } else if (name === 'delete') {
        this.deleteTenant(row)
      }
    },

    // 重置密码
    async resetPassword(row) {
      this.$Modal.confirm({
        title: '确认重置',
        content: `确定要重置租户 ${row.account} 的密码吗？`,
        onOk: async () => {
          try {
            await tenantResetPasswordApi(row.id, {})
            this.$Message.success('密码重置成功')
          } catch (error) {
            this.$Message.error('密码重置失败')
          }
        }
      })
    },

    // 删除租户
    deleteTenant(row) {
      this.$Modal.confirm({
        title: '确认删除',
        content: `确定要删除租户 ${row.account} 吗？此操作无法撤销。`,
        onOk: async () => {
          try {
            await tenantDeleteApi(row.id)
            this.$Message.success('租户删除成功')
            this.getList()
          } catch (error) {
            this.$Message.error('租户删除失败')
          }
        }
      })
    },

    // 查看详情
    viewDetail(row) {
      this.currentTenantId = row.id
      this.detailModalVisible = true
    },

    // 显示批量操作弹窗
    showBatchModal() {
      this.batchModalVisible = true
    },

    // 批量操作确认
    async handleBatchOk() {
      if (!this.batchOperation) {
        this.$Message.error('请选择操作类型')
        return
      }

      try {
        const status = this.batchOperation === 'enable' ? 1 : 0
        await tenantBatchUpdateStatusApi(this.selectedIds, status)
        this.$Message.success('批量操作成功')
        this.batchModalVisible = false
        this.selectedIds = []
        this.getList()
      } catch (error) {
        this.$Message.error('批量操作失败')
      }
    },

    // 获取即将过期的租户
    async getExpiringTenants() {
      try {
        const { data } = await tenantExpiringApi({ days: 30 })
        this.expiringList = data || []
        this.expiringModalVisible = true
      } catch (error) {
        this.$Message.error('获取即将过期租户失败')
      }
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
    }
  }
}
</script>

<style scoped>
.ml10 {
  margin-left: 10px;
}
.mr5 {
  margin-right: 5px;
}
.mt20 {
  margin-top: 20px;
}
.mt25 {
  margin-top: 25px;
}
</style>