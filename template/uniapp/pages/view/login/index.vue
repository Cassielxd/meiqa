<template>
	<div class="login_container">
		<!-- 头部背景图 -->
		<div class="login_container_bg"><image class="login_container_bg_image" src="~static/image/login/bg.png" mode="widthFix"></image></div>
		<!-- 头部背景图结束 -->
		<div class="login_container_content">
			<div class="login_container_content_title"><span>欢迎登录</span></div>

			<div class="login_container_content_form">
				<div class="login_container_content_form_input">
					<login-input v-model="loginData.account" type="text" placeholder="请输入账号"><span slot="icon" class="iconfont">&#xe6bc;</span></login-input>
				</div>

				<div class="login_container_content_form_input">
					<login-input v-model="loginData.password" type="password" placeholder="请输入密码"><span slot="icon" class="iconfont">&#xe6bd;</span></login-input>
				</div>

				<div class="login_container_content_form_input privacy_agreement">
					<div class="privacy_agreement_content">
						<checkbox-group @change="checkoutPrivaey">
							<checkbox class="checkbox" :value="isCheckEd" />
							<span>请阅读并勾选</span>
							<text @click="showPrivaey">隐私协议</text>
						</checkbox-group>
					</div>
				</div>
			</div>

			<div class="login_container_content_handle"><div class="login_primary_button" @click="handleLogin">登录</div></div>
			<div v-if='isDomainName' class="login-to-set-domin-name"><navigator hover-class="none" url="/pages/view/dominName/index">修改域名</navigator></div>
		</div>
	</div>
</template>

<script>
import loginInput from './component/loginInput.vue';
import { navigateTo, Modal, Toast } from 'pages/utils/uniApi.js';
import http from 'pages/api/index';
import api from 'pages/api/api.js';
import config from 'pages/config/app.js';

export default {
	components: {
		loginInput
	},
	data() {
		return {
			loginData: {
				account: '',
				password: ''
			},
			cid: '',
			isCheckEd: '1', // 是否选中隐私协议
			isCheckEdArr: [],
			isDomainName: config.isDomainName
		};
	},
	onLoad() {
		this.$cache.remove('accountData');
		this.$cache.set('pages_login', 1);
		this.scoket.clearPing()
	},
	onUnload() {
		this.$cache.remove('pages_login');
	},
	methods: {
		async handleLogin() {
			if (!this.isCheckEdArr.length) {
				Toast('请勾选隐私协议');
				return;
			}
			//#ifdef APP-PLUS
			let info = plus.push.getClientInfo();
			this.cid = info.clientid;
			this.$cache.set('cid', this.cid);
			//#endif
			let loginRes = await http(api.login, { ...this.loginData, client_id: this.cid, is_app: 1 });
			this.$cache.set('userData', loginRes);
			this.$store.commit('setToken', loginRes.token);
			this.$cache.set('accountData', this.loginData);
			//连接长连接
			this.scoket.startConnect();
			this.$cache.remove('pages_login');
			navigateTo(4, '/pages/view/messageList/index');
		},
		// 勾选隐私协议
		checkoutPrivaey(e) {
			this.isCheckEdArr = e.detail.value;
		},
		// 跳转隐私协议
		showPrivaey() {
			navigateTo(1, '/pages/view/privacyAgreement/index');
		}
	}
};
</script>

<style scoped lang="less">
.login_container {
	position: relative;
	&_bg {
		&_image {
			width: 100%;
		}
	}

	&_content {
		width: 642rpx;
		padding: 54rpx 79rpx 120rpx 79rpx;
		box-shadow: 0px 3px 20px rgba(0, 20, 41, 0.06);
		box-sizing: border-box;
		position: absolute;
		top: 349rpx;
		left: 0;
		right: 0;
		background: #fff;
		margin: auto;
		border-radius: 20rpx;
		&_title {
			text-align: center;
			font-size: 21px;
			font-weight: 600;
			margin-bottom: 54rpx;
		}
		&_handle {
			margin-top: 115rpx;

			.login_primary_button {
				font-size: 15px;
				width: 100%;
				height: 82rpx;
				background: @primaryBgColor;
				border-radius: 41px;
				display: flex;
				align-items: center;
				justify-content: center;
				color: #fff;
			}
		}
	}
}

.privacy_agreement {
	margin-top: 30rpx;
	&_content {
		font-size: 28rpx;
		.checkbox {
			transform: scale(0.7);
		}
		text {
			color: @primaryColor;
		}
	}
}

.login-to-set-domin-name {
	font-size: 20rpx;
	padding-top: 40rpx;
	text-align: center;
}
</style>
