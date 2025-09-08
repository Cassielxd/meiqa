<template>
	<div class="login_container">
		<!-- 头部背景图 -->
		<div class="login_container_bg"><!-- <image class="login_container_bg_image" src="~static/dominName/bg.png" mode="widthFix"></image> --></div>
		<!-- 头部背景图结束 -->
		<div class="login_container_content">
			<div class="login_container_content_title"><span>设置请求域名</span></div>

			<div class="login_container_content_form">
				<div class="login_container_content_form_input">
					<picker mode="selector" :range="fileList" @change="chageFile" range-key="label">
						<view>
							<span>{{ fileListConcat[selectFile] }}</span>
							<span>://</span>
						</view>
					</picker>
					<input type="text" v-model="dominName" placeholder="请输入域名" />
					<!-- <span class="iconfont">&#xe6f6;</span> -->
				</div>

				<!-- 			<div class="login_container_content_form_input">
					<span>{{wsFileConcat[selectFile]}}</span>
					<span>://</span>
					<span>{{dominName}}</span>
				</div> -->
			</div>

			<div class="login_container_content_handle">
				<div class="login_button" @click="cancel" v-if="isShowCencel">取消</div>
				<div class="login_primary_button" @click="handleSetDominName">确定</div>
			</div>
		</div>
	</div>
</template>

<script>
import { navigateTo, Modal, navigateBack, Toast } from 'pages/utils/uniApi.js';
const app = getApp()
export default {
	components: {},
	data() {
		return {
			selectFile: 0,
			dominName: '',
			fileList: [
				{
					label: 'http',
					value: 0
				},
				{
					label: 'https',
					value: 1
				}
			],
			fileListConcat: {
				0: 'http',
				1: 'https'
			},
			wsFileConcat: {
				0: 'ws',
				1: 'wss'
			},
			isShowCencel: false
		};
	},
	onLoad() {
		this.$cache.set('pages_dominName', 1);
		this.$cache.remove('accountData');
		if (this.$cache.get('dominName')) {
			this.dominName = this.$cache.get('dominName');
			if (this.$cache.get('requestFile') == 'http') {
				this.selectFile = 0;
			} else {
				this.selectFile = 1;
			}
			this.isShowCencel = true;
		}
	},
	methods: {
		handleSetDominName() {
			if (!this.dominName) {
				Toast('请设置域名');
				return;
			}

			Modal('温馨提示', `您设置的域名是 "${this.dominName}", 请问是否继续?`).then(res => {
				this.$store.commit('setHttp', {
					wsFile: this.wsFileConcat[this.selectFile],
					dominName: this.dominName,
					requestFile: this.fileListConcat[this.selectFile]
				});
				app.globalData.http = this.$store.state.http
				this.scoket.clearPing();
				this.$store.commit('clearChat');
				this.$store.commit('logout');
				navigateTo(1, '/pages/view/login/index');
				this.$cache.remove('pages_dominName');
			});
		},
		cancel() {
			navigateBack(1);
		},
		chageFile(e) {
			console.log(e);
			this.selectFile = e.detail.value;
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
		padding: 54rpx 79rpx 67rpx 79rpx;
		// box-shadow: 0px 3px 20px rgba(0, 20, 41, 0.06);
		box-sizing: border-box;
		position: absolute;
		top: 209rpx;
		left: 0;
		right: 0;
		background: #fff;
		margin: auto;
		border-radius: 20rpx;
		&_title {
			text-align: center;
			font-size: 16px;
			font-weight: 600;
			margin-bottom: 54rpx;
		}
		&_form {
			&_input {
				display: flex;
				align-items: center;
				height: 96rpx;
				// background: #f5f5f5;
				padding: 0 28rpx;
				// border-radius: 12rpx;
				input {
					margin-left: 16rpx;
					flex: 1;
					font-size: 16px;
				}
			}
			> div:nth-child(1) {
				border-bottom: 1px solid #ddd;
			}
		}
		&_handle {
			margin-top: 57rpx;
			display: flex;
			.login_primary_button {
				font-size: 14px;
				width: 100%;
				height: 70rpx;
				background: @primaryBgColor;
				border-radius: 41px;
				display: flex;
				align-items: center;
				justify-content: center;
				color: #fff;
			}
			.login_button {
				font-size: 14px;
				width: 100%;
				height: 70rpx;
				border-radius: 41px;
				display: flex;
				align-items: center;
				justify-content: center;
				border: 1px solid #ccc;
				margin-right: 32rpx;
			}
		}
	}
}
</style>
