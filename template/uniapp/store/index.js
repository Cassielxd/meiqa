import Vue from 'vue';
import Vuex from 'vuex';
import {
	cache
} from 'pages/utils/uniApi.js';
import http from 'pages/api/index';
import api from 'pages/api/api.js';
import Socket from 'pages/api/socket.js';

Vue.use(Vuex)

let kefuInfo = cache.get('kefuInfo')
const store = new Vuex.Store({
	state: {
		//请求地址
		http: cache.get('requestFile') && cache.get('dominName') ?
			`${cache.get('requestFile')}://${cache.get('dominName')}` : '',
		//登录token
		token: cache.get('token') || '',
		//聊天事件信息
		chatList: {},
		//聊天事件人物
		chatInfo: {},
		//聊天记录
		chatLog: cache.get('chatLog-' + kefuInfo.id) || {},
		//聊天分页
		chatLogPage: cache.get('chatLogPage-' + kefuInfo.id) || {},
		//客服信息
		kefuInfo: kefuInfo || {},
		//在线事件返回信息
		chatOnline: {},
		//账号记录
		accountData: cache.get('accountData') || {},
		//网站配置
		config: cache.get('config') || {
			site_title: '',
		},
		//用户列表分页
		userRecordPage: cache.get('userPage-' + kefuInfo.id) || 1,
		//用户列表
		userRecord: cache.get('userList-' + kefuInfo.id) || {},
		//客户列表
		bookList: cache.get('bookList') || {},
		//用户信息所有
		userAllInfo: cache.get('userAllInfo') || {},
		// 用户列表
		userLists: [],
		//app升级
		percent: 0,
		download: false,
		downloadTask: null,
	},
	mutations: {
		setPercent(state, data) {
			state.percent = data;
		},
		setDownload(state, data) {
			state.download = data;
		},
		setDownloadTask(state, data) {
			state.downloadTask = data;
			state.download = true;
		},
		stopDownloadTask(state) {
			if (state.downloadTask) {
				state.downloadTask.abort();
				state.downloadTask = null
				state.download = false;
				state.percent = 0;
			}
		},
		setHttp(state, data) {
			state.http = data.requestFile + '://' + data.dominName
			cache.set('dominName', data.dominName);
			cache.set('requestFile', data.requestFile);
			cache.set('wsFile', data.wsFile);
		},
		setRefresh(state, data) {
			state.refresh = data
		},
		setChatList(state, data) {
			if (state.chatList[data.id] == undefined) {
				state.chatList[data.id] = []
			}
			state.chatList[data.id].push(data.list);
		},
		delChatList(state, data) {
			state.chatList[data.id] && state.chatList[data.id].map((item, index) => {
				if (item.guid === data.guid) {
					state.chatList[data.id].splice(index, 1)
				}
			})
			if (state.chatList[data.id].length) {
				delete state.chatList[data.id]
			}
		},
		clearChat(state, data) {
			if (data === undefined) {
				state.chatList = {};
			} else if (data.id) {
				if (state.chatList[data.id] !== undefined) {
					state.chatList[data.id] = [];
				}
			}
		},
		setChatInfo(state, data) {
			state.chatInfo = data
		},
		setchatLog(state, data) {
			if (state.chatLog[data.user_id] == undefined) {
				state.chatLog[data.user_id] = {};
			}
			if (state.chatLog[data.user_id][data.upperId] == undefined) {
				state.chatLog[data.user_id][data.upperId] = [];
			}
			state.chatLog[data.user_id][data.upperId] = data.list
			cache.set('chatLog-' + state.kefuInfo.id, state.chatLog);
			if (state.chatLogPage[data.user_id] === undefined) {
				state.chatLogPage[data.user_id] = 0
			}
			if (data.list.length) {
				data.upperId = data.list[0].id;
				state.chatLogPage[data.user_id] = data.upperId
			}
			cache.set('chatLogPage-' + state.kefuInfo.id, state.chatLogPage);
		},
		setkefuInfo(state, data) {
			if (data.one === true) {
				state.kefuInfo.is_backstage = data.is_backstage;
			} else {
				state.kefuInfo = data;
			}
			cache.set('kefuInfo', data)
		},
		setChatOnline(state, data) {
			state.chatOnline = data;
		},
		setToken(state, data) {
			state.token = data;
			cache.set('token', data);
		},
		//退出登录
		logout(state) {
			state.token = '';
			state.kefuInfo = {};
			cache.remove('userData');
			cache.remove('accountData');
			cache.remove('token');
		},
		setConfig(state, data) {
			state.config = data;
			cache.set('config', data);
		},
		setUserRecord(state, data) {
			if (state.userRecord[data.page] === undefined) {
				state.userRecord[data.page] = [];
			}
			state.userRecord[data.page] = data.list;
			state.userRecordPage = data.page
			cache.set('userList-' + state.kefuInfo.id, state.userRecord);
			if (data.list.length > 0) {
				data.page++;
			}
			cache.set('userPage-' + state.kefuInfo.id, data.page);
		},
		setBookList(state, data) {
			state.bookList = data;
			cache.set('bookList', data)
		},
		setUserInfo(state, data) {
			state.userAllInfo[data.userId] = data.userInfo;
			cache.set('userAllInfo', state.userAllInfo);
		},
		setUserList(state, data) {
			state.userLists = data;
		}
	},
	actions: {
		//获取客服信息
		getKeufuInfo({
			commit
		}) {
			return new Promise((resolve, reject) => {
				http(api.kefuInfoApi).then(data => {
					commit('setkefuInfo', data)
					commit('setConfig', {
						site_title: data.site_title
					})
					resolve(data);
				}).catch(res => {
					reject(res)
				});
			})
		},
		//获取聊天用户列表
		getUserRecord({
			commit
		}, data) {
			return new Promise((resolve, reject) => {
				http(api.userRecord, {
					nickname: data.nickname,
					...data.pageData,
					is_tourist: data.is_tourist
				}).then(res => {
					commit('setUserRecord', {
						page: data.pageData.page,
						list: res
					});
					resolve(res);
				}).catch(err => {
					console.log(err)
					reject(err)
				})
			})
		},
		// 获取所有聊天消息id
		getUserRecordAll() {
			return new Promise((resolve, reject) => {
				http(api.userRecordAll).then(res => {
					resolve(res);
				}).catch(err => {
					reject(err);
				});
			});
		},
		// 删除聊天列表用户
		deleteUserRecord({
			commit
		}, data) {
			return new Promise((resolve, reject) => {
				http(api.deleteUserRecord, {
					id: data.id
				}).then(res => {
					resolve(res);
				}).catch(err => {
					reject(err);
				});
			});
		},
		//获取聊天记录
		getChatLogList({
			commit
		}, data) {
			return new Promise((resolve, reject) => {
				http(api.kefuServiceList, {
					user_id: data.user_id,
					...data.pageData
				}).then(res => {
					commit('setchatLog', {
						upperId: data.pageData.upperId,
						list: res,
						user_id: data.user_id
					})
					resolve(res)
				}).catch(err => {
					reject(err)
				})
			})
		},
		//获取客服列表
		getBookList({
			commit
		}, data) {
			return new Promise((resolve, reject) => {
				http(api.userList, data).then(res => {
					this.bookList = res;
					commit('setBookList', res)
					resolve(res)
				}).catch(res => {
					reject(res)
				});
			})
		},
		//设置Client id
		saveUserClient({
			commit
		}, data) {
			return new Promise((resolve, reject) => {
				http(api.userClient, {
					client_id: data
				}).then(res => {
					cache.set('cid', cid);
					resolve(res);
				}).catch(res => reject(res));
			})
		},
		updataUserCount({
			commit
		}) {
			return new Promise((resolve, reject) => {
				http(api.userCount).then(res => {
					if (res.count != 0) {
						uni.setTabBarBadge({
							index: 0,
							text: String(res.count) // 设置为数值，右下角则不在显示
						});
					} else {
						uni.removeTabBarBadge({
							index: 0
						});
					}
					resolve(res)
				}).catch(err => reject(err));
			});
		},
		login({
			commit
		}, data) {

		},
		getUserInfo({
			commit
		}, userId) {
			return new Promise((resolve, reject) => {
				http(api.userInfo, {
					user_id: userId
				}).then(res => {
					commit('setUserInfo', {
						userId: userId,
						userInfo: res
					});
					resolve(res);
				}).catch(err => reject(err));
			})
		}
	},
	getters: {
		chatList: state => state.chatList,
		chatInfo: state => state.chatInfo,
		chatLog: state => state.chatLog,
		token: state => state.token,
		http: state => state.http,
		userRecord: state => {
			let userList = [];
			Object.keys(state.userRecord).map(item => {
				state.userRecord[item].map(value => {
					userList.push(value)
				})
			})
			return {
				userList,
				page: state.userRecordPage,
			}
		},
		chatLog: (state) => (user_id) => {
			let chatLog = [];
			if (state.chatLog[user_id] === undefined) {
				return [];
			}
			Object.keys(state.chatLog[user_id]).map(item => {
				state.chatLog[user_id][item].map(value => {
					chatLog.push(value)
				})
			})
			return chatLog;
		},
		config: state => state.config,
		kefuInfo: state => state.kefuInfo,
		bookList: state => state.bookList,
		chatLogPage: (state) => (user_id) => state.chatLogPage[user_id],
		userInfo: (state) => (user_id) => state.userAllInfo[user_id] || {},
	}
});
export default store
