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
                title: '客服二维码'
            },
            component: () => import('@/pages/kefu/qrcode')
        },
        {
            path: 'record',
            name: `${pre}record`,
            meta: {
                title: '聊天记录'
            },
            component: () => import('@/pages/kefu/record')
        },
        {
            path: 'statistics',
            name: `${pre}statistics`,
            meta: {
                title: '站点统计'
            },
            component: () => import('@/pages/kefu/statistics')
        }
    ]
}