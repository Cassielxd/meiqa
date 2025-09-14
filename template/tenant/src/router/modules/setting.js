
import BasicLayout from '@/components/main'

const meta = {}

const pre = 'setting_'

export default {
  path: '/tenant/setting',
  name: 'setting',
  header: 'setting',
  redirect: {
    name: `${pre}systemRole`
  },
  component: BasicLayout,
  children: [
    {
      path: 'system_role/index',
      name: `${pre}systemRole`,
      meta: {
        title: '身份管理'
      },
      component: () => import('@/pages/setting/systemRole/index')
    },
    {
      path: 'system_admin/index',
      name: `${pre}systemAdmin`,
      meta: {
        title: '管理员列表'
      },
      component: () => import('@/pages/setting/systemAdmin/index')
    },
    {
      path: 'system_menus/index',
      name: `${pre}systemMenus`,
      meta: {
        title: '权限规则'
      },
      component: () => import('@/pages/setting/systemMenus/index')
    },
    {
      path: 'system_config',
      name: `${pre}setSystem`,
      meta: {
        title: '系统设置'
      },
      component: () => import('@/pages/setting/setSystem/index')
    },
    {
      path: 'store_service/index',
      name: `${pre}service`,
      meta: {
        title: '客服管理'
      },
      component: () => import('@/pages/setting/storeService/index')
    },
    {
      path: 'system_group_data',
      name: `${pre}systemGroupData`,
      meta: {
        title: '数据配置'
      },
      component: () => import('@/pages/system/group/list')
    },
    {
      path: 'system_group_data/kf_adv',
      name: `${pre}kfAdv`,
      meta: {
        title: '客服页面广告'
      },
      component: () => import('@/pages/system/group/kfAdv')
    },
    {
      path: 'system_group_data/privacy',
      name: `${pre}privacy`,
      meta: {
        title: '隐私协议'
      },
      component: () => import('@/pages/system/group/privacy')
    },
    {
      path: 'store_service/speechcraft',
      name: `${pre}speechcraft`,
      meta: {
        title: '客服话术'
      },
      component: () => import('@/pages/setting/storeService/speechcraft')
    },
    {
      path: 'store_service/feedback',
      name: `${pre}feedback`,
      meta: {
        title: '用户留言'
      },
      component: () => import('@/pages/setting/storeService/feedback')
    },
    {
        path: 'system_group_data/kf_icon',
        name: `${pre}kfIcon`,
        meta: {
            title: '客服图标'
        },
        component: () => import('@/pages/system/group/kfIcon')
    },
    {
        path: 'app/version',
        name: `${pre}version`,
        meta: {
            title: 'APP在线升级'
        },
        component: () => import('@/pages/setting/version/index')
    }
  ]
}
