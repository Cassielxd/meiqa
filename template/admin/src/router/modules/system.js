
import BasicLayout from '@/components/main'

const pre = 'system_'

export default {
  path: '/admin/system',
  name: 'system',
  header: 'system',
  redirect: {
    name: `${pre}configTab`
  },
  meta: {
    auth: ['admin-system']
  },
  component: BasicLayout,
  children: [
    {
      path: 'code',
      name: `${pre}code`,
      meta: {
        auth: ['admin-system-code'],
        title: '代码获取'
      },
      component: () => import('@/pages/system/code/index')
    },
    {
      path: 'config/system_config_tab/index',
      name: `${pre}configTab`,
      meta: {
        auth: ['system-config-system_config-tab'],
        title: '配置分类'
      },
      component: () => import('@/pages/system/configTab/index')
    },
    {
      path: 'config/system_config_tab/list/:id?',
      name: `${pre}configTabList`,
      meta: {
        auth: ['system-config-system_config_tab-list'],
        title: '配置列表'
      },
      component: () => import('@/pages/system/configTab/list')
    },
    {
      path: 'config/system_group/index',
      name: `${pre}group`,
      meta: {
        auth: ['system-config-system_config-group'],
        title: '组合数据'
      },
      component: () => import('@/pages/system/group/index')
    },
    {
      path: 'maintain/system_log/index',
      name: `${pre}systemLog`,
      meta: {
        auth: ['system-maintain-system-log'],
        title: '系统日志'
      },
      component: () => import('@/pages/system/maintain/systemLog/index')
    },
  ]
}
