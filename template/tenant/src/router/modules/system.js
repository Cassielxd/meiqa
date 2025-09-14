
import BasicLayout from '@/components/main'

const pre = 'system_'

export default {
  path: '/tenant/system',
  name: 'system',
  header: 'system',
  redirect: {
    name: `${pre}configTab`
  },
  meta: {},
  component: BasicLayout,
  children: [
    {
      path: 'code',
      name: `${pre}code`,
      meta: {
        title: '代码获取'
      },
      component: () => import('@/pages/system/code/index')
    },
    {
      path: 'config/system_config_tab/index',
      name: `${pre}configTab`,
      meta: {
        title: '配置分类'
      },
      component: () => import('@/pages/system/configTab/index')
    },
    {
      path: 'config/system_config_tab/list/:id?',
      name: `${pre}configTabList`,
      meta: {
        title: '配置列表'
      },
      component: () => import('@/pages/system/configTab/list')
    },
    {
      path: 'config/system_group/index',
      name: `${pre}group`,
      meta: {
        title: '组合数据'
      },
      component: () => import('@/pages/system/group/index')
    },
    {
      path: 'maintain/system_log/index',
      name: `${pre}systemLog`,
      meta: {
        title: '系统日志'
      },
      component: () => import('@/pages/system/maintain/systemLog/index')
    },
  ]
}
