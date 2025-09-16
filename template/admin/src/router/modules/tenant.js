import BasicLayout from '@/components/main'

const pre = 'tenant_'

export default {
  path: '/admin/tenant',
  name: 'tenant',
  header: 'tenant',
  meta: {
    auth: ['admin-tenant'],
    title: '租户管理'
  },
  component: BasicLayout,
  children: [
    {
      path: 'list',
      name: `${pre}list`,
      meta: {
        auth: ['tenant-list'],
        title: '租户列表'
      },
      component: () => import('@/pages/tenant/index')
    }
  ]
}
