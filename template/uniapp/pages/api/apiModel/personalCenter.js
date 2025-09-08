const api = {
	// 获取当前登录客服信息
	userUserInfo: {
		url: 'kefu/user/userInfo',
		method: 'get'
	},
	// 上传头像
	kefuUploadAvatar: {
		url: 'kefu/upload',
		method: 'post',
		form: true
	},
	// 确认信息修改
	putKefuUserUserInfo: {
		url: 'kefu/user/userInfo',
		method: 'put'
	},
	// 客服信息反馈
	userFeedback: {
		url: 'kefu/user/feedback',
		method: 'post'
	},
	// 获取投诉列表
	userComplain: {
		url: 'kefu/user/complain',
		method: 'get'
	},
	// 保存客户投诉
	postUserComplain: {
		url: 'kefu/user/complain',
		method: 'post'
	},
	// 拉黑用户
	putUserStatus: {
		url: 'kefu/user/status',
		method: 'put',
		queryKey: ['userId']
	},
	appVersion: {
		url: 'kefu/version',
		method:'post'
	}
};
export default api;