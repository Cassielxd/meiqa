import apiSetting from './apiSetting.js';
import {
	cache
} from 'pages/utils/uniApi.js';
import store from '@/store';
import {
	onNetworkStatusChange,
	onNetworkStatusChangeV2
} from './network.js'

class Socket {
	constructor() {
		this.socketTask = null; //socket 任务
		this.timer = null; //心跳定时器
		this.connectStatus = false; //连接状态
		this.wsUrl = ''; //ws地址
		this.reconnectTimer = 2000; //重连
		this.handClse = false; //手动关闭
		this.reconnetime = null; //重连 定时器
		this.networkStatus = true;
		this.connectLing = false; //连接是否进行中
		this.defaultEvenv(); //执行默认事件
	}
	//网络状态变化监听
	networkEvent() {
		onNetworkStatusChange(() => {
			console.log('有网了')
			this.networkStatus = true;
			uni.$on('timeout', this.timeoutEvent.bind(this))
		}, () => {
			console.log('断网了')
			this.networkStatus = false;
			this.connectStatus = false;
			clearInterval(this.timer);
			this.timer = null;
			this.socketTask && this.socketTask.close();
		});
	}
	//开始连接
	startConnect() {
		console.log('开始链接')
		this.networkEvent();
		this.handClse = false;
		if (!this.connectStatus) {
			this.init();
			this.connect();
		}
	}
	//默认事件
	defaultEvenv() {

		uni.$off('success', this.successEvent);
		uni.$off('timeout', this.timeoutEvent)
		uni.$off('recored', this.recoredEvent);
		uni.$off('online', this.onlineEvent);

		uni.$on('success', this.successEvent.bind(this));
		uni.$on('timeout', this.timeoutEvent.bind(this))
		uni.$on('recored', this.recoredEvent.bind(this));
		uni.$on('online', this.onlineEvent.bind(this));
	}
	messageListEvent() {

	}

	timeoutEvent() {
		this.reconne();
	}
	recoredEvent(data) {
		store.commit('setChatInfo', data);
	}
	successEvent() {
		console.log('success默认事件');
		this.changOnline();
		this.openLaterEvent()
	}
	onlineEvent(data) {
		store.commit('setChatOnline', data);
	}
	//发送用户状态
	changOnline() {
		let online = cache.get('kefu_online')
		if (online !== undefined && online !== '') {
			this.send({
				data: {
					online: online
				},
				type: 'online'
			});
		}
	}

	//打开连接后的事件
	openLaterEvent() {
		if (store.state.chatList) {
			Object.keys(store.state.chatList).map(item => {
				store.state.chatList[item].map(value => {
					this.send({
						type: 'chat',
						data: {
							msn: value.msn,
							type: value.msn_type,
							to_user_id: value.to_user_id,
							guid: value.guid
						}
					}).then(() => {
						store.commit('delChatList', {
							id: item,
							guid: value.guid
						})
					})
				})
			})
		}
	}

	//连接websocket
	connect() {
		this.connectLing = true;
		this.socketTask = uni.connectSocket({
			url: this.wsUrl,
			complete: () => {}
		});

		this.socketTask.onOpen(this.onOpen.bind(this))
		this.socketTask.onError(this.onError.bind(this));
		this.socketTask.onMessage(this.onMessage.bind(this))
		this.socketTask.onClose(this.onClose.bind(this));

	}

	init() {
		let wsUrl = `${cache.get('wsFile')}://${cache.get('dominName')}/ws`

		wsUrl += `?type=kefu&app=1`

		this.wsUrl = `${wsUrl}&token=${store.state.token}&form=app&client_id=${cache.get('cid')}`
	}

	onOpen() {
		clearInterval(this.reconnetime);
		this.reconnetime = null;
		this.connectLing = false;
		this.connectStatus = true;
		this.ping();
	}

	onError(error) {
		console.log('连接发生错误', error)
		this.connectStatus = false;
		this.connectLing = false;
		this.reconne();
	}

	//断线重连
	reconne() {
		if (this.reconnetime || this.connectStatus) {
			return;
		}
		this.reconnetime = setInterval(() => {
			if (this.connectStatus) {
				return;
			}
			this.connectLing || this.connect();
		}, this.reconnectTimer);

	}

	onClose() {
		console.log('关闭连接')
		uni.$emit('close');
		//手动关闭不用重新连接
		if (this.handClse) {
			return;
		}
		clearInterval(this.timer);
		this.timer = null;
		this.connectStatus = false;
		this.connectLing = false;
		this.reconne();
	}

	ping() {
		this.timer = setInterval(() => {
			this.send({
				type: 'ping'
			})
		}, 10000)
	}

	onMessage(response) {
		let {
			type,
			data
		} = JSON.parse(response.data);
		// console.log(type, data);
		uni.$emit(type, data);
	}


	send(data) {
		let that = this;
		//没有网络,或者没有连接
		if (!this.connectStatus || !this.networkStatus) {
			this.reconne();
		}
		return new Promise((reslove, reject) => {
			this.socketTask.send({
				data: JSON.stringify(data),
				success() {
					reslove();
				},
				fail(res) {
					console.log(res)
					if (res.errMsg ==
						'sendSocketMessage:fail WebSocket is not connected' ||
						res.errMsg ==
						'sendSocketMessage:fail Error: SocketTask.readyState is not OPEN'
					) {
						that.reconne();
					}
					reject(res);
				},
				complete(res) {
					console.log(res)
				}
			})
		});
	}

	guid() {
		return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
			var r = Math.random() * 16 | 0,
				v = c == 'x' ? r : (r & 0x3 | 0x8);
			return v.toString(16);
		});
	}

	clearPing() {
		clearInterval(this.timer);
		this.timer = null;
		if (this.connectStatus) {
			this.socketTask.close();
		}
		this.handClse = true;
		this.connectStatus = false;
		this.connectLing = false;
	}

	setBadgeNumber(count) {
		//#ifdef APP-PLUS
		plus.runtime.setBadgeNumber(Number(count));
		//#endif
	}

}


export default Socket;
