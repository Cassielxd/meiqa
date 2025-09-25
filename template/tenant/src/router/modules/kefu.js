import BasicLayout from '@/components/main';

const pre = 'kefu_';

export default {
    path: '/tenant/kefu',
    name: 'kefu',
    header: 'kefu',
    redirect: {
        name: `${pre}qrcode`
      },
    meta: {},
    component: BasicLayout,
    children: [
        {
            path: 'qrcode',
            name: `${pre}qrcode`,
            meta: {
                title: 'menu.qrcode'
            },
            component: () => import('@/pages/kefu/qrcode')
        },
        {
            path: 'record',
            name: `${pre}record`,
            meta: {
                title: 'menu.record'
            },
            component: () => import('@/pages/kefu/record')
        },
        {
            path: 'statistics',
            name: `${pre}statistics`,
            meta: {
                title: 'menu.statistics'
            },
            component: () => import('@/pages/kefu/statistics')
        }
    ]
}