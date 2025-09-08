<script>
	import {
		navigateTo,
		Modal
	} from 'pages/utils/uniApi.js';
	import http from 'pages/api/index';
	import api from 'pages/api/api.js';
	import config from './pages/config/app.js';
	import store from '@/store';

	export default {
		globalData: {
			http: store.state.http,
			kefuInfo: store.state.kefuInfo,
			openLogin: false
		},
		onLaunch: function() {
			console.log('App Launch');
			this.init();
			this.addEventListener();
			if (this.globalData.http && store.state.token) {
				this.scoket.startConnect();
				this.socketEvent();
			}
		},
		onShow: function() {
			if (this.globalData.http) {
				http(api.setKefubackstage, {
					value: 1
				}).then(res => {
					let kefuInfo = JSON.parse(JSON.stringify(this.globalData.kefuInfo));
					kefuInfo.is_backstage = 1;
					this.globalData.kefuInfo.is_backstage = 1;
					store.commit('setkefuInfo', {
						one: true,
						is_backstage: 1
					});
				});
			}

			console.log('App Show');

			//#ifdef APP-PLUS
			this.cheackSingleLogin();
			//#endif
		},
		onHide: function() {
			//#ifdef APP-PLUS
			if (this.globalData.http) {
				http(api.setKefubackstage, {
					value: 0
				}).then(res => {
					let kefuInfo = JSON.parse(JSON.stringify(this.globalData.kefuInfo));
					kefuInfo.is_backstage = 0;
					store.commit('setkefuInfo', kefuInfo);
					this.globalData.kefuInfo.is_backstage = 0;
				});
			}
			//#endif
			//#ifndef APP-PLUS
			if (this.globalData.http) {
				http(api.setKefubackstage, {
					value: 1
				}).then(res => {
					let kefuInfo = JSON.parse(JSON.stringify(this.globalData.kefuInfo));
					kefuInfo.is_backstage = 1;
					this.globalData.kefuInfo.is_backstage = 1;
					store.commit('setkefuInfo', kefuInfo);
				});
			}
			//#endif
			console.log('App Hide');
		},
		methods: {
			//单点登录检测
			cheackSingleLogin() {
				let that = this;
				if (store.state.token && this.globalData.http && !that.globalData.openLogin) {
					let info = plus.push.getClientInfo();
					let cid = info.clientid;
					this.$store.dispatch('getKeufuInfo').then(res => {
						if (res.client_id && res.client_id !== cid) {
							that.globalData.openLogin = true;
							//需要强制退出登录
							uni.showModal({
								title: '强制退出登录',
								content: '您的账号已在另一台设备上登录',
								showCancel: false,
								confirmText: '确认退出',
								success() {
									//关闭长连接
									that.scoket.clearPing();
									that.$store.commit('clearChat');
									that.$store.commit('logout');
									uni.reLaunch({
										url: '/pages/view/login/index'
									});
								},
								complete: () => {
									that.globalData.openLogin = false;
								}
							});
						} else if (!res.client_id) {
							this.$store.dispatch('saveUserClient', cid);
						}
					});
				}
			},
			addEventListener() {
				//#ifdef APP-PLUS
				/* 5+  push 消息推送 ps:使用:H5+的方式监听，实现推送*/
				var info = plus.push.getClientInfo();
				var cid = info.clientid;
				plus.push.addEventListener(
					'click',
					function(msg) {
						console.log('点击事件:', JSON.parse(JSON.parse(msg.payload)));
						navigateTo(1, JSON.parse(JSON.parse(msg.payload)).url);
					},
					false
				);

				plus.push.addEventListener(
					'receive',
					function(msg) {
						console.log('透传消息来了:', msg);
						if (msg.aps) {
							try {
								let options = {
									cover: false,
									sound: 'system',
									title: msg.payload.title
								};
								plus.push.createMessage(msg.payload.body, 'LocalMSG', options);
							} catch (e) {
								console.log(e);
							}
							console.log(msg);
						} else if (msg.payload === 'LocalMSG') {} else if (msg.payload.type === 'url') {
							navigateTo(1, msg.payload.url);
						} else {
							try {
								let options = {
									cover: false,
									sound: 'system',
									title: msg.payload.title
								};
								plus.push.createMessage(msg.payload.body, 'LocalMSG', options);
							} catch (e) {
								console.log(e);
							}
							console.log(msg);
						}
					},
					false
				);

				//#endif
				if (!store.state.token && this.$cache.get('pages_login')) {
					this.$cache.remove('pages_login');
				}
			},
			// 获取域名, 若没有域名，自动跳转到填写域名界面
			getDomainName() {
				let dominName = this.$cache.get('dominName');
				if (!dominName && !this.$cache.get('pages_dominName')) {
					navigateTo(2, '/pages/view/dominName/index');
				}
			},
			init() {
				if (!this.$cache.get('wsFile')) {
					this.$cache.set('wsFile', config.defaultWsType);
				}
				if (!this.$cache.get('dominName') && config.defaultDomainName) {
					this.$cache.set('dominName', config.defaultDomainName);
					this.globalData.http = config.defaultRequestType + '://' + config.defaultDomainName;
				}
				if (!this.$cache.get('requestFile')) {
					this.$cache.set('requestFile', config.defaultRequestType);
				}
			},
			socketEvent() {
				uni.$on('success', data => {
					console.log('success');
				});

				uni.$on('mssage_num', data => {
					if (data.num > 0) {
						this.$onlineAudio.play();
						uni.setTabBarBadge({
							index: 0,
							text: data.num ? String(data.num) : 0 // 设置为数值，右下角则不在显示
						});
					}
					this.scoket.setBadgeNumber(data.num);
				});
			}
		}
	};
</script>

<style lang="less">
	/*每个页面公共less */
	@import url('./static/globalLess/index.less');
	@import url('./static/globalLess/iconfont.css');
	// 表情包
	@import url('./static/emoji/emojione.css');
</style>
