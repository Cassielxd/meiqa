<template>
	<div class="container">
		<lay-out>
			<div slot="content">
				<div class="header"></div>
				<div class="box">
					<div class="img" @click="setting">
						<image :src="customerServerData.avatar" mode=""></image>
					</div>
					<div>
						<div class="box_font1">
							<span>{{ customerServerData.nickname }}</span>
							<span class="isOnline" :class="{ online: customerServerData.online == '1' }"
								@click="handleChangeOnline">
								<span></span>
								<span>{{ customerServerData.online == '1' ? '在线' : '离线' }}</span>

								<!-- 切换上线下线弹框开始 -->
								<span class="onLineModel" v-if="onlineModel">
									<span class="sanjiao"></span>
									<span class="model_text" @click="online(1)">
										<span class="dian online"></span>
										<span>在线</span>
										<span class="iconfont"
											:class="{ opacity0: !(customerServerData.online == '1') }">&#xe6d1;</span>
									</span>
									<span class="xian"></span>
									<span class="model_text" @click="online(0)">
										<span class="dian"></span>
										<span>离线</span>
										<span class="iconfont"
											:class="{ opacity0: !(!customerServerData.online || customerServerData.online == '0') }">&#xe6d1;</span>
									</span>
								</span>

								<!-- 切换上线下线弹框结束 -->
							</span>
						</div>
						<div class="box_font2">手机号：{{ customerServerData.phone }}</div>
					</div>
				</div>

				<div class="content">
					<div class="content_box" @click="setting">
						<div class="icon"><span class="iconfont iconPrimaryColor">&#xe6bc;</span></div>
						<div class="text">个人信息</div>
						<div>
							<image src="~static/images/right.png" alt=""></image>
						</div>
					</div>
					<div class="line"></div>
					<div class="content_box" @click="authReply">
						<div class="icon"><span class="iconfont iconPrimaryColor">&#xe6d9;</span></div>
						<div class="text">自动回复</div>
						<div>
							<image src="~static/images/right.png" alt=""></image>
						</div>
					</div>
					<div class="line"></div>
					<div class="content_box" @click="feedback">
						<div class="icon"><span class="iconfont iconPrimaryColor">&#xe6d9;</span></div>
						<div class="text">意见反馈</div>
						<div>
							<image src="~static/images/right.png" alt=""></image>
						</div>
					</div>
					<div class="line" v-if='isDomainName'></div>
					<div class="content_box" v-if='isDomainName' @click="editDominName">
						<div class="icon"><span class="iconfont iconPrimaryColor">&#xe6f9;</span></div>
						<div class="text">域名设置</div>
						<div class="message">
							<span>{{ dominName }}</span>
							<image src="~static/images/right.png" alt=""></image>
						</div>
					</div>
				</div>

				<div class="content">
					<div class="content_box" @click="loginOut">
						<div class="icon"><span class="iconfont iconPrimaryColor">&#xe6f8;</span></div>
						<div class="text">退出登录</div>
						<div>
							<image src="~static/images/right.png" alt=""></image>
						</div>
					</div>
				</div>

				<div class="copyright"><span class="iconfont">&#xe6f7;</span></div>
			</div>
		</lay-out>
	</div>
</template>

<script>
	import {
		navigateTo,
		Modal
	} from 'pages/utils/uniApi.js';
	import http from 'pages/api/index';
	import api from 'pages/api/api.js';
	import config from 'pages/config/app.js';
	export default {
		components: {},
		data() {
			return {
				customerServerData: {},
				dominName: '',
				use: false,
				onlineModel: false,
				isDomainName: config.isDomainName
			};
		},
		onLoad(opt) {
			this.dominName = this.$cache.get('dominName');
			this.customerServerData = this.$store.getters.kefuInfo;
		},
		onShow() {
			this.initData();
		},
		onHide() {
			this.onlineModel = false;
		},
		onUnload() {
			this.onlineModel = false;
		},
		methods: {
			// 获取当前登录客服信息
			initData() {
				this.$store.dispatch('getKeufuInfo').then(res => {
					this.customerServerData = res;
				});
			},
			// 意见反馈
			feedback() {
				navigateTo(1, '/pages/view/feedback/index');
			},
			authReply() {
				navigateTo(1, '/pages/view/authReply/index');
			},
			// 设置
			setting() {
				navigateTo(1, '/pages/view/user/setting/setting', this.customerServerData);
			},
			// 修改域名
			editDominName() {
				navigateTo(1, '/pages/view/dominName/index');
			},
			// 退出登录
			loginOut() {
				Modal('温馨提示', '您确定要退出登录吗?').then(() => {
					http(api.userLogout)
						.then(res => {
							//关闭长连接
							this.scoket.clearPing();
							this.$store.commit('clearChat');
							this.$store.commit('logout');
							navigateTo(3, '/pages/view/login/index');
						})
						.catch(res => {
							this.scoket.clearPing();
							this.$store.commit('clearChat');
							this.$store.commit('logout');
							navigateTo(3, '/pages/view/login/index');
						});
				});
			},
			// 更改在线状态
			handleChangeOnline() {
				this.onlineModel = !this.onlineModel;
			},
			online(val) {
				this.scoket.send({
					data: {
						online: val
					},
					type: 'online'
				});
				this.customerServerData.online = val;
			}
		}
	};
</script>

<style lang="less" scoped>
	.header {
		background: #1890fc;
		padding: 43rpx 30rpx;
		// height: 83rpx;
		position: absolute;
		top: 0;
		left: 0;
		width: 100%;
		box-sizing: border-box;
	}

	.box {
		width: 690rpx;
		height: 180rpx;
		background: #ffffff;
		border-radius: 12px;
		margin: 0 auto;
		margin-bottom: 24rpx;
		display: flex;
		justify-content: left;
		align-items: center;
		position: relative;
		z-index: 10;

		image {
			width: 100rpx;
			height: 100rpx;
			margin-left: 34rpx;
			margin-right: 20rpx;
			border-radius: 50%;
		}

		.box_font1 {
			font-size: 30rpx;
			font-weight: 600;
			line-height: 42rpx;
			color: #282828;
			margin-bottom: 8rpx;
			display: flex;
			align-items: center;
		}

		.box_font2 {
			font-size: 26rpx;
			font-weight: 400;
			line-height: 37rpx;
			color: #999999;
		}
	}

	.content {
		width: 690rpx;
		margin: 0 auto;
		margin-top: 40rpx;
		background: #fff;
		border-radius: 10rpx;

		.line {
			width: 90%;
			height: 1px;
			background: #eeeeee;
			position: relative;
			left: 10%;
		}

		.content_box {
			padding: 28rpx 30rpx;
			// height: 88rpx;
			background: #ffffff;
			border-radius: 12rpx;
			// margin-bottom: 24rpx;
			display: flex;
			justify-content: space-between;
			align-items: center;

			.icon {
				margin-right: 20rpx;
			}

			.text {
				flex: 1;
				// width: 84%;
			}

			image {
				width: 33rpx;
				height: 33rpx;
			}

			.message {
				flex: 1;
				display: flex;
				justify-content: flex-end;
				align-items: center;
			}
		}
	}

	.copyright {
		display: flex;
		justify-content: center;
		margin-top: 300rpx;
	
	.iconfont {
			font-size: 250rpx;
			color: #cccccc;
			text-align: center;
		}
	}

	.iconPrimaryColor {
		// color: @primaryColor;
	}

	.isOnline {
		width: 100rpx;
		height: 40rpx;
		border-radius: 18rpx;
		background: #999999;
		display: flex;
		margin-left: 9rpx;
		color: #fff;
		align-items: center;
		justify-content: center;
		text-align: center;
		font-size: 20rpx;
		position: relative;
	}

	.isOnline>span:nth-child(1) {}

	.dian {
		display: inline-block;
		width: 8rpx;
		height: 8rpx;
		background: #ffffff;
		border-radius: 50%;
		margin-right: 6rpx;
	}

	.online {
		background: #20c93b;
	}

	.onLineModel {
		position: absolute;
		top: 50rpx;
		left: 43rpx;
		z-index: 999 !important;
		background: #434343;
		border-radius: 6px;
		width: 180rpx;
		height: 150rpx;
		display: flex;
		flex-direction: column;
		justify-content: space-between;

		.sanjiao {
			background: #434343;
			position: absolute;
			top: -15rpx;
			left: 30rpx;
			width: 30rpx;
			height: 30rpx;
			transform: rotate(45deg);
		}

		.model_text {
			flex: 1;
			display: flex;
			align-items: center;
			justify-content: space-between;
			padding: 0 30rpx;
			white-space: nowrap;
			font-size: 24rpx;

			.dian {
				margin-right: 8rpx;
			}
		}

		.xian {
			display: inline-block;
			width: 100%;
			height: 1px;
			background: #f0f1f2;
		}

		.opacity0 {
			opacity: 0;
		}
	}

	.onlineFlag {
		position: fixed;
		width: 100%;
		height: 100%;
		top: 0;
		left: 0;
		z-index: 0;
		background: rgba(0, 0, 0, 0.3);
	}
</style>
