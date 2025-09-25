
const pre = 'kefu_';

export default [
	// 登录
	{
		path: '/admin/login',
		name: 'login',
		meta: {
			title: '登录',
			hideInMenu: true
		},
		component: () => import('@/pages/account/login')
	},
	// 客服
	{
		path: '/kefu',
		name: `${pre}index`,
		meta: {
			auth: true,
			title: 'kefu.management',
			kefu: true
		},
		component: () => import('@/pages/kefu/index')
	},


	{
		path: '/kefu/pc_list',
		name: `${pre}pc_list`,
		meta: {
			auth: true,
			title: 'kefu.customerService',
			kefu: true
		},
		component: () => import('@/pages/kefu/pc/index')
	},
	{
		path: '/kefu/appChat',
		name: `${pre}app-chat`,
		meta: {
			auth: true,
			title: 'kefu.customerService',
			kefu: true
		},
		component: () => import('@/pages/kefu/appChat/index')
	},
	{
		path: '/kefu/mobile_user_chat',
		name: `${pre}app-mobile_user_chat`,
		meta: {
			auth: true,
			title: 'kefu.userCustomerService',
			kefu: true
		},
		component: () => import('@/pages/kefu/appChat/mobile/index')
	},
	{
		path: '/kefu/mobile_feedback',
		name: `${pre}app-mobile_feedback`,
		meta: {
			auth: true,
			title: 'kefu.userFeedback',
			kefu: true
		},
		component: () => import('@/pages/kefu/appChat/mobile/feedback')
	},
	// 外部连接，跳转联系客服模块
	{
		path: '/chat/index',
		name: 'customerServerRedirect',
		meta: {
			title: 'chat.title'
		},
		component: () => import('@/pages/kefu/externalConnection/index')
	},
	{
		path: '/chat/pc',
		name: 'customerServerPc',
		meta: {
			title: 'chat.pcTitle'
		},
		component: () => import('@/pages/kefu/externalConnection/pcCustomerServer')
	},
	{
		path: '/chat/mobile',
		name: 'customerServerMobile',
		meta: {
			title: 'chat.mobileTitle'
		},
		component: () => import('@/pages/kefu/externalConnection/mobileCustomerServer')
	},
	{
		// 客服不在线。提交反馈
		path: '/chat/customerOutLine',
		name: 'customerOutLine',
		meta: {
			title: 'chat.submitFeedback'
		},
		component: () => import('@/pages/kefu/externalConnection/customerOutLine')
	},
	{
		// 完成提交反馈
		path: '/chat/finishSubmitOutLine',
		name: 'finishSubmitOutLine',
		meta: {
			title: 'chat.submitSuccess'
		},
		component: () => import('@/pages/kefu/externalConnection/finishSubmitOutLine')
	}
	// 外部连接，跳转联系客服模块结束
]
