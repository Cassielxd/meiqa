const api = {
	// 客服登录
	login: {
		url: 'kefu/login',
		method: 'post',
		loading: true,
		noAuth: true
	},
	// 退出登录
	userLogout: {
		url: 'kefu/user/logout',
		method: 'post'
	},
	// 发送CID
	userClient: {
		url: 'kefu/user/client',
		method: 'put'
	},
	// 授权登录
	serviceCode: {
		url: 'kefu/service/code',
		method: 'post'
	}


}
export default api;
