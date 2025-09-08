<template>
	<div class="container">
		<lay-out @goBottom="goBottom" @goTop="goTop" @refresherrefresh="refresherrefresh"
			:refresherTriggered="refresherTriggered" @refresherrestore="refresherrestore"
			@refresherabort="refresherabort" :scroll-top="scrollTop">
			<div slot="header">
				<!-- 搜索框 -->
				<div class="content_search">
					<div class="content_search_box">
						<div class="icon" @click="searchUser"><span class="iconfont">&#xe6bf;</span></div>
						<div class="input">
							<input class="uni-input" @confirm="searchUser" @focus="searchFocus" @blur="searchBlur"
								v-model="nickname" type="text" placeholder="搜索用户名称" />
						</div>
					</div>
				</div>
			</div>

			<div slot="content" class="content">
				<!-- 聊天列表 -->
				<div class="content_userMessgae">
					<uni-swipe-action>
						<uni-swipe-action-item v-for="(item, index) in userList" :right-options="options"
							@click="deleteUserRecord($event, index)" :key="item.id">
							<div :id="`content_userMessgae_item${item.id}`" class="content_userMessgae_item"
								@click="connentServerForUser(item)">
								<div class="content_userMessgae_item_avar">
									<image :src="item.avatar" mode="widthFix"></image>

									<span class="online_status" :class="{ off: item.online }"></span>
								</div>

								<div class="content_userMessgae_item_value">
									<div class="content_userMessgae_item_value_user">
										<div class="content_userMessgae_item_value_user_nickName">{{ item.nickname }}
										</div>
										<div class="content_userMessgae_item_value_user_form" v-if="item.type"
											:class="formType[item.type].className">
											<span>{{ formType[item.type].label }}</span>
										</div>
									</div>
									<!-- 用户消息 -->
									<div class="content_userMessgae_item_value_message">
										<span>{{ item.message }}</span>
									</div>
									<!-- 用户消息结束 -->
								</div>

								<!-- 聊天时间 -->
								<div class="content_userMessgae_item_createTime">
									<div class="content_userMessgae_item_createTime_value">
										<span>{{ item.update_time | toDay }}</span>
									</div>
									<div class="content_userMessgae_item_createTime_count" v-if="item.mssage_num">
										<span>{{ item.mssage_num }}</span>
									</div>
								</div>
								<!-- 聊天时间结束 -->
								<div class="content_userMessgae_item_del">删除</div>
							</div>
						</uni-swipe-action-item>
					</uni-swipe-action>
				</div>
				<!-- 	<div v-if="!userList.length" class="no_message">
						<span>暂无客户!</span>
					</div> -->
				<!-- 聊天列表结束 -->
			</div>
		</lay-out>
		<div class="fixed_model" v-if="showSearchModel"></div>
		<download-progress v-show='download' @clearAbort='clearAbort' :percent='percent'></download-progress>
	</div>
</template>

<script>
	import {
		navigateTo,
		Modal,
		formatDate,
		Toast,
		parseToObject
	} from 'pages/utils/uniApi.js';
	import http from 'pages/api/index';
	import api from 'pages/api/api.js';
	import {
		mapState
	} from 'vuex';
	import DownloadProgress from '../../../components/DownloadProgress.vue';
	export default {
		filters: {
			toDay(value) {
				if (!value) return '';
				return formatDate(value * 1000, 'M月d日 hh:mm');
			}
		},
		components: {
			DownloadProgress
		},
		data() {
			return {
				selectOnlineStatus: 1,
				onlineStatus: [{
						label: '在线',
						value: 1
					},
					{
						label: '下线',
						value: 0
					}
				],
				customerServerHandleModel: false, // 客服操作开关
				customerServerData: {},
				pageData: {
					page: 1,
					limit: 15
				},
				formType: {
					0: {
						className: 'pc',
						label: 'pc端'
					},
					1: {
						className: 'gozhonghao',
						label: '公众号'
					},
					2: {
						className: 'wchat',
						label: '小程序'
					},
					3: {
						className: 'h5',
						label: 'H5'
					}
				},
				refresherTriggered: false, // 下拉刷新状态
				nickname: '',
				is_tourist: '',
				userList: [], // 用户列表
				audioFun: '', // 音频对象
				showSearchModel: false,
				lastTapTime: 0,
				scrollTop: 0,
				count: 0,
				userListTemp: {},
				options: [{
					text: '取消',
					style: {
						backgroundColor: '#007aff'
					}
				}, {
					text: '删除',
					style: {
						backgroundColor: '#dd524d'
					}
				}],
				download: false,
				percent: 0
			};
		},
		watch: {
			chatInfo(data) {
				if (data.id) {
					let ids = [];
					this.userList.map(item => {
						ids.push(item.id);
					});
					if (ids.indexOf(data.id) === -1) {
						this.userList.unshift(data);
					}
					this.$store.commit('setChatInfo', {});
				}
			}
		},
		onLoad(opt) {
			this.initSocket();
			this.userList = this.$store.getters.userRecord.userList;
			this.appVersionLevel();
		},
		onShow(opt) {
			this.pageData.page = 1;
			// this.initData(this.pageData.page > 1 ? 'concat' : '');
			this.initData();
			this.getKefuInfo();
			this.updataUserCount();
		},
		onUnload() {
			uni.$off('mssage_num', this.mssageNumEvent);
			uni.$off('rm_transfer', this.rmTransferEvent);
			uni.$off('transfer', this.transferEvent)
		},
		onTabItemTap(e) {

		},
		onNavigationBarButtonTap(e) {
			this.handleScanCode(); // 吊起相机，扫描二维码
		},
		computed: {
			...mapState(['chatInfo', 'kefuInfo', 'config', 'userRecord', 'userLists'])
		},
		methods: {
			appVersionLevel() {
				// #ifdef APP-PLUS
				let that = this;
				plus.runtime.getProperty(plus.runtime.appid, ({
					version,
					name
				}) => {
					//升级过一次就不要在提醒
					if (this.$cache.get('update_' + version)) {
						return;
					}
					http(api.appVersion, {
						version,
						name
					}).then(data => {
						if (data.update === false) {
							return;
						}
						uni.showModal({
							title: '检测到新版本',
							content: data.info,
							confirmText: '立即升级',
							success: (res) => {
								if (res.confirm && data.update && data.url) {
									that.downloadFile(data.url);
								}
							},
							complete: () => {
								this.$cache.set('update_' + version, 1);
							}
						})

					}).catch(res => {})
				});
				// #endif
			},
			downloadFile(url) {
				this.downloadTask = uni.downloadFile({
					url: url,
					success: (res) => {
						if (res.statusCode === 200) {
							plus.runtime.install(
								res.tempFilePath, {
									force: false
								}, () => {
									plus.runtime.restart();
								}, err => {
									console.error(err);
								});
						}
					},
					complete: () => {
						this.download = false
						this.percent = 0;
					}
				});
				this.download = true;
				this.downloadTask.onProgressUpdate(res => {
					this.percent = res.progress
				});
			},
			clearAbort() {
				if (this.downloadTask) {
					this.downloadTask.abort();
					this.downloadTask = null;
				}
			},
			// 删除聊天列表用户
			deleteUserRecord(e, index) {
				if (e.index == 1) {
					this.$store.dispatch('deleteUserRecord', {
						id: this.userList[index].id
					}).then(res => {
						this.userList.splice(index, 1);
					});
				}
			},
			mssageNumEvent(data) {
				console.log('mssage_num', data);
				this.getMssageNum(data);
			},
			//被转走事件
			rmTransferEvent(data) {
				let rmIndex = -1;
				this.userList.map((item, index) => {
					if (item.id === data.recored.id) {
						rmIndex = index;
					}
				});
				if (rmIndex !== -1) {
					this.userList.splice(rmIndex, 1);
					this.updataUserCount();
				}
			},
			//转接到当前客服事件
			transferEvent(data) {
				let pushIndex = -1;
				this.userList.map((item, index) => {
					if (item.id === data.recored.id) {
						pushIndex = index;
					}
				});
				if (pushIndex === -1) {
					this.userList.unshift(data.recored);
					this.updataUserCount();
				}
			},
			initSocket() {
				uni.$on('mssage_num', this.mssageNumEvent);
				uni.$on('rm_transfer', this.rmTransferEvent);
				uni.$on('transfer', this.transferEvent);
			},
			getKefuInfo() {
				this.$store.dispatch('getKeufuInfo').then(res => {
					uni.setNavigationBarTitle({
						title: res.site_title
					});
				});
			},
			// 搜索框获得焦点
			searchFocus() {
				this.showSearchModel = true;
			},
			// 搜索框失去焦点
			searchBlur() {
				this.searchUser();
				this.showSearchModel = false;
			},
			// 搜索用户
			searchUser() {
				this.pageData.page = 1;
				this.initData();
			},
			// 获取客户列表, 客服信息
			initData(type) {
				this.pageData.limit = 10;
				this.$store.dispatch('getUserRecord', {
					nickname: this.nickname,
					pageData: this.pageData,
					is_tourist: this.is_tourist
				}).then(res => {
					if (res.length > 0) {
						this.pageData.page++;
					}
					if (type == 'concat') {
						this.userList = this.userList.concat(res);
					} else {
						this.userList = res;
						// this.$set(this.userListTemp, this.pageData.page, res);
					}
					if (this.refresherTriggered) {
						this.refresherTriggered = false;
					}
				});
			},

			// 下拉刷新被触发
			refresherrefresh() {
				if (this.refresherTriggered) {
					this.refresherTriggered = false;
					return;
				}
				if (!this.refresherTriggered) {
					this.refresherTriggered = true;
				}
				// this.refresherTriggered = true;
				this.pageData.page = 1;
				this.initData();
			},

			// 下拉刷新复位
			refresherrestore() {
				this.refresherTriggered = false;
			},

			//
			refresherabort() {
				this.refresherTriggered = false;
			},
			// 滑动到页面底部
			goBottom() {
				this.initData('concat');
			},
			goTop() {
				this.pageData.page = 1;
				this.initData();
			},
			updataUserCount() {
				this.$store.dispatch('updataUserCount').then(res => {
					this.scoket.setBadgeNumber(res.count);
				});
			},
			// 通过sockect连接收取新的消息
			getMssageNum(data) {
				if (data.num < 1) {
					return;
				}
				this.userList.forEach((item, index) => {
					// 如果发送方的user_id 与 列表中的 to_uid 一致
					if (item.to_user_id == data.user_id) {
						this.userList.splice(index, 1);
					}
				});
				this.userList.unshift(data.recored);
				this.updataUserCount();
			},
			// 扫描二维码
			handleScanCode() {
				// let str = "http://192.168.31.192:8081/pages/users/scan_login/index?key=463aca66113f65396dc28e3f0041a2f2";
				uni.scanCode({
					onlyFromCamera: true, // 是否只允许相机扫码，不允许相机选择图片
					success(res) {
						console.log(res);
						navigateTo(1, '/pages/view/authorizedLogin/index', {
							key: res.result.split('=')[1]
						});
					},
					fail: () => {}
				});
			},
			connentServerForUser(item) {
				item.mssage_num = 0;
				navigateTo(1, '/pages/view/customerServer/index', {
					to_user_id: item.to_user_id
				});
			}
		}
	};
</script>

<style lang="less" scoped>
	@import './less/messageList.less';
</style>
