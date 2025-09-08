import BasicLayout from '@/components/main';

const pre = 'kefu_';

export default {
    path: '/admin/kefu',
    name: 'kefu',
    header: 'kefu',
    redirect: {
        name: `${pre}qrcode`
      },
    meta: {
        auth: ['setting-store-service']
    },
    component: BasicLayout,
    children: [
        {
            path: 'qrcode',
            name: `${pre}qrcode`,
            meta: {
                auth: ['admin-kefu-qrcode'],
                title: '客服二维码'
            },
            component: () => import('@/pages/kefu/qrcode')
        },
        {
            path: 'record',
            name: `${pre}record`,
            meta: {
                auth: ['admin-kefu-record'],
                title: '聊天记录'
            },
            component: () => import('@/pages/kefu/record')
        },
        {
            path: 'statistics',
            name: `${pre}statistics`,
            meta: {
                auth: ['admin-kefu-statistics'],
                title: '站点统计'
            },
            component: () => import('@/pages/kefu/statistics')
        }
    ]
}