const api = {
	// 客户列表
	userList: {
		url: 'kefu/user/list',
		method: 'get'
	},
	// 客户全部标签
	userLabe: {
		url: 'kefu/user/label/all',
		method: 'get'
	},
	// 客户已有标签，带有选中标识
	userLabelSelect: {
		url: 'kefu/user/label',
		method: 'get'
	},
	// 设置用户标签
	putUserLabel: {
		url: 'kefu/user/label',
		method: 'put',
		queryKey: ['userId']
	},
	// 客户详细信息
	userInfo: {
		url: 'kefu/user/info',
		method: 'get',
		queryKey: ['user_id'],
		canInputQuery: true // 有此选项时，并且值为true，代表可以在url上不可以携带参数
	},
	// 用户分组
	UserGroup: {
		url: 'kefu/user/group',
		method: 'get'
	},
	// 设置用户分组
	setUserGroup: {
		url: 'kefu/user/group',
		method: 'put',
		queryKey: ['userId', 'id'],
		canInputQuery: true
	},
	// 修改用户信息
	userUpdateUser: {
		url: 'kefu/user/updateUser',
		method: 'put',
		queryKey: ['userId']
	},
	// 删除单个用户标签
	deleteUserLabel: {
		url: 'kefu/user/label',
		method: 'delete',
		queryKey: ['userId', 'labelId'],
		canInputQuery: true
	},
	// 添加分组
	serviceCate: {
		url: 'kefu/service/cate',
		method: 'post'
	},
	userAgreement: {
		url: 'kefu/agreement',
		method: 'get',
		noAuth: true
	},
	getAuthReply: {
		url: 'kefu/service/auth_reply',
		method: 'get'
	},
	saveAuthReply: {
		url: 'kefu/service/auth_reply',
		method: 'post',
		queryKey: ['id'],
	},
	deleteAuthReply: {
		url: 'kefu/service/auth_reply',
		method: 'delete',
		queryKey: ['id'],
		canInputQuery: true
	},

	putAuthReply: {
		url: 'kefu/service/auth_reply',
		method: 'put',
		queryKey: ['value'],
	},
	// 获取当前登录客服信息
	kefuInfoApi: {
		url: 'kefu/user/userInfo',
		method: 'get',
	},

	savelog: {
		url: 'kefu/user/savelog',
		method: 'post'
	},

	setKefubackstage: {
		url: 'kefu/service/backstage',
		method: 'put',
		queryKey: ['value'],
	}


}
export default api;
