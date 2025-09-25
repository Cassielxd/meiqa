
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
        title: 'menu.roleManagement'
      },
      component: () => import('@/pages/setting/systemRole/index')
    },
    {
      path: 'system_admin/index',
      name: `${pre}systemAdmin`,
      meta: {
        title: 'menu.adminList'
      },
      component: () => import('@/pages/setting/systemAdmin/index')
    },
    {
      path: 'system_menus/index',
      name: `${pre}systemMenus`,
      meta: {
        title: 'menu.permissionRules'
      },
      component: () => import('@/pages/setting/systemMenus/index')
    },
    {
      path: 'system_config',
      name: `${pre}setSystem`,
      meta: {
        title: 'menu.systemSettings'
      },
      component: () => import('@/pages/setting/setSystem/index')
    },
    {
      path: 'store_service/index',
      name: `${pre}service`,
      meta: {
        title: 'menu.customerServiceManagement'
      },
      component: () => import('@/pages/setting/storeService/index')
    },
    {
      path: 'system_group_data',
      name: `${pre}systemGroupData`,
      meta: {
        title: 'menu.dataConfig'
      },
      component: () => import('@/pages/system/group/list')
    },
    {
      path: 'system_group_data/kf_adv',
      name: `${pre}kfAdv`,
      meta: {
        title: 'menu.serviceAd'
      },
      component: () => import('@/pages/system/group/kfAdv')
    },
    {
      path: 'system_group_data/privacy',
      name: `${pre}privacy`,
      meta: {
        title: 'menu.privacyPolicy'
      },
      component: () => import('@/pages/system/group/privacy')
    },
    {
      path: 'store_service/speechcraft',
      name: `${pre}speechcraft`,
      meta: {
        title: 'menu.serviceSpeechcraft'
      },
      component: () => import('@/pages/setting/storeService/speechcraft')
    },
    {
      path: 'store_service/feedback',
      name: `${pre}feedback`,
      meta: {
        title: 'menu.userFeedback'
      },
      component: () => import('@/pages/setting/storeService/feedback')
    },
    {
        path: 'system_group_data/kf_icon',
        name: `${pre}kfIcon`,
        meta: {
            title: 'menu.serviceIcon'
        },
        component: () => import('@/pages/system/group/kfIcon')
    },
    {
        path: 'app/version',
        name: `${pre}version`,
        meta: {
            title: 'menu.appUpgrade'
        },
        component: () => import('@/pages/setting/version/index')
    }
  ]
}
