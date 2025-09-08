// 聊天界面接口
const customerServer = {
	
	pingNetWork:{
		url:'kefu/service/ping',
		noAuth:true,
		method:'get',
		timeout:1000
	},
	kefuServiceList: {
		url: 'kefu/service/list',
		method: 'get'
	},
	getSendId: {
		url: 'kefu/service/get_send_id',
		method: 'get'
	},
	sendMessage:{
		url: 'kefu/service/send_message',
		method: 'post'
	},
	// 获取转接客服列表
	serviceTransferList: {
		url: 'kefu/service/transfer_list',
		method: 'get'
	},
	// 确认转接
	serviceTransfer: {
		url: 'kefu/service/transfer',
		method: 'post'
	},
	// 查询客服话术
	serviceSpeechcraft: {
		url: 'kefu/service/speechcraft',
		method: 'get'
	},
	// 获取话术分类
	kefuServiceCate: {
		url: 'kefu/service/cate',
		method: 'get'
	},
	// 添加话术
	postServiceSpeechcraft: {
		url: 'kefu/service/speechcraft',
		method: 'post'
	},
	// 修改话术
	putServiceSpeechcraft: {
		url: 'kefu/service/speechcraft',
		method: 'put',
		queryKey: ['id']
	},
	// 删除话术
	deleteServiceSpeechcraft: {
		url: 'kefu/service/speechcraft',
		method: 'delete',
		queryKey: ['id'],
		canNotInputQuery: true
	},
	// 修改分组
	putServiceCate: {
		url: 'kefu/service/cate',
		method: 'put',
		queryKey: ['id']
	},
	// 删除分组
	deleteServiceCate: {
		url: 'kefu/service/cate',
		method: 'delete',
		queryKey: ['id']
	}



}
const api = {
	// 获取客户列表
	userList: {
		url: 'kefu/user/list',
		method: 'get'
	},
	// 获取聊过天的用户
	userRecord: {
		url: 'kefu/user/record',
		method: 'get'
	},
	// 获取客服未读条数
	userCount: {
		url: 'kefu/user/count',
		method: 'get'
	},
	// 获取所有聊天消息id
	userRecordAll: {
		url: 'kefu/user/recordAll',
		method: 'get'
	},
	// 删除聊天列表用户
	deleteUserRecord: {
		url: 'kefu/user/record',
		method: 'delete',
		queryKey: ['id']
	},
	...customerServer
};

export default api;
