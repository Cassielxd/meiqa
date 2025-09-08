<template>
	<div class="auth_login container">
		<div class="auth_login_content">
			<div class="userData">
				<div class="avater"><image :src="userData.avatar" mode=""></image></div>
				<div class="userName">
					<span>{{ userData.nickname }}</span>
				</div>
				<div class="tipMessage">点击授权登录您的客服工作台</div>
			</div>

			<div class="handle">
				<div class="handle_login" @click="handle_login">授权登录</div>
				<div class="handle_cancel" @click="handleCancel">取消</div>
			</div>
		</div>
		<div class="bottom"></div>
	</div>
</template>

<script>
import http from 'pages/api/index';
import api from 'pages/api/api.js';
import { navigateTo, Modal, navigateBack } from 'pages/utils/uniApi.js';
export default {
	data() {
		return {
			key: '',
			userData: {}
		};
	},
	onLoad(opt) {
		this.key = opt.key;
		this.userData = this.$store.state.kefuInfo;
	},

	methods: {
		handle_login() {
			http(api.serviceCode, { code: this.key }).then(res => {
				Modal('温馨提示', '登录成功', { showCancel: false }).then(() => {
					navigateBack(1);
				});
			});
		},
		handleCancel() {
			navigateBack(1);
		}
	}
};
</script>

<style lang="less" scoped>
.auth_login {
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	&_content {
		flex: 1;
		.userData {
			.avater {
				width: 178rpx;
				height: 178rpx;
				border-radius: 50%;
				overflow: hidden;
				margin: 112rpx auto 20rpx auto;
				image {
					width: 100%;
					height: 100%;
				}
			}
			
			.userName {
				text-align: center;
				font-weight: bold;
				margin-bottom: 10rpx;
			}
			
			.tipMessage {
				text-align: center;
				color: #9F9F9F;
				font-size: 14px;
			}
		}
		.handle {
			margin-top: 96rpx;
			> div {
				width: 594rpx;
				height: 86rpx;
				border-radius: 43rpx;
				font-size: 15px;
				display: flex;
				align-items: center;
				justify-content: center;
				margin: 0 auto;
			}
			&_login {
				background: linear-gradient(90deg, #3875ea 0%, #1890fc 100%);
				color: #fff;
			}
			&_cancel {
				margin-top: 28rpx !important;
				border: 1px solid #3875EA;
				color: #3875EA;
			}
		}
	}
}
</style>
