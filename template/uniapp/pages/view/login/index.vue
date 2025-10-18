<template>
	<div class="login-page">
		<div class="login-card">
			<div class="login-title">{{ $t('login.title') }}</div>

			<div class="login-form">
				<div class="form-item">
					<login-input v-model="loginData.account" type="text" :placeholder="$t('login.accountPlaceholder')">
						<span slot="icon" class="iconfont">&#xe6bc;</span>
					</login-input>
				</div>

				<div class="form-item">
					<login-input v-model="loginData.password" type="password" :placeholder="$t('login.passwordPlaceholder')">
						<span slot="icon" class="iconfont">&#xe6bd;</span>
					</login-input>
				</div>

				<div class="form-item privacy-agreement">
					<checkbox-group @change="checkoutPrivaey">
						<checkbox class="privacy-checkbox" :value="isCheckEd" />
						<span class="privacy-text">{{ $t('login.privacyPrefix') }} <text @click="showPrivaey" class="privacy-link">{{ $t('login.privacyLink') }}</text></span>
					</checkbox-group>
				</div>
			</div>

			<div class="login-actions">
				<div class="login-button" @click="handleLogin">{{ $t('login.loginButton') }}</div>
			</div>
			<div v-if="isDomainName" class="domain-link">
				<navigator hover-class="none" url="/pages/view/dominName/index">{{ $t('login.domainButton') }}</navigator>
			</div>
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
				Toast(this.$t('login.privacyToast'));
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
.login-page {
	display: flex;
	justify-content: center;
	align-items: center;
	height: 100vh;
	width: 100vw;
	background-image: url('~static/image/login/bg.png');
	background-size: cover;
	background-position: center;
}

.login-card {
	width: 90%;
	max-width: 400px;
	padding: 30px;
	background: rgba(255, 255, 255, 0.85);
	border-radius: 16px;
	box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.2);
	backdrop-filter: blur(10px);
	-webkit-backdrop-filter: blur(10px);
	border: 1px solid rgba(255, 255, 255, 0.18);
	text-align: center;
}

.login-logo {
	margin-bottom: 15px;
	.logo-image {
		width: 120px;
		height: 40px;
	}
}

.login-title {
	font-size: 24px;
	font-weight: 600;
	color: #333;
	margin-bottom: 30px;
}

.form-item {
	margin-bottom: 20px;
}

.login-actions {
	margin-top: 30px;
}

.login-button {
	font-size: 16px;
	width: 100%;
	padding: 12px 0;
	background: linear-gradient(90deg, #4b79ff, #3d6ae6);
	border-radius: 25px;
	color: #fff;
	border: none;
	cursor: pointer;
	transition: background 0.3s ease;
	&:hover {
		background: linear-gradient(90deg, #3d6ae6, #4b79ff);
	}
}

.privacy-agreement {
	display: flex;
	align-items: center;
	justify-content: center;
	margin-top: 15px;
	font-size: 13px;
	color: #666;

	.privacy-checkbox {
		transform: scale(0.8);
	}
	
	.privacy-text {
		display: flex;
		align-items: center;
	}

	.privacy-link {
		color: #4b79ff;
		margin-left: 5px;
		cursor: pointer;
	}
}

.domain-link {
	margin-top: 20px;
	font-size: 13px;
	color: #888;
	a {
		color: #888;
		text-decoration: none;
		&:hover {
			text-decoration: underline;
		}
	}
}
</style>
