
import BasicLayout from '@/components/main'

// export default {
//   path: '/',
//   name: 'home',
//   redirect: '/admin/home',
//   component: BasicLayout,
//   meta: {
//     hideInMenu: true,
//     notCache: true,
//     auth: true
//   },
//   children: [
//     {
//       path: 'admin/home',
//       name: 'home',
//       meta: {
//         title: '首页',
//         auth: ['admin-index-index']
//       },
//       component: () => import('@/pages/index/index')
//     }
//   ]
// }

const meta = {}

const pre = 'home_'

export default {
  path: '/',
  name: 'home',
  header: 'home',
  redirect: {
    name: `${pre}index`
  },
  meta,
  component: BasicLayout,
  children: [
    {
      path: 'tenant/home/',
      name: `${pre}index`,
      header: 'home',
      meta: {
        title: '主页'
      },
      component: () => import('@/pages/index/index')
    }
  ]
}
