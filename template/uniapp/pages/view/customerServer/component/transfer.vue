<template>
	<div class="transfer_container">
		<div class="transfer_container_title">
			<div class="transfer_container_title_value"><span>转接客服</span></div>
			<div class="transfer_container_title_icon" @click="close"><span class="iconfont">&#xe6b5;</span></div>
		</div>

		<div class="customerServer_list">
			<scroll-view scroll-y="true" class="max_height">
				<div class="customerServer_list_item" v-for="(item, index) in serverList" :key="index" @click="selectCustomerServer(item)">
					<div class="customerServer_list_item_radio">
						<div><div v-if="item.select"></div></div>
					</div>
					<div class="customerServer_list_item_avar"><image :src="item.avatar" mode=""></image></div>
					<div class="customerServer_list_item_nickName">
						<span>{{ item.nickname }}</span>
					</div>
				</div>
			</scroll-view>
		</div>

		<div class="handle_box" @click="handleSubmit"><div>确定</div></div>
	</div>
</template>

<script>
import { Toast } from 'pages/utils/uniApi.js';
import http from 'pages/api/index';
import api from 'pages/api/api.js';
export default {
	props:{
		userInfo:{
			type:Object,
			default:{}
		}
	},
	data() {
		return {
			serverList: []
		};
	},
	created() {
		this.initData();
	},
	methods: {
		// 查询其他客服
		initData() {
			http(api.serviceTransferList).then(res => {
				this.serverList = res.list;
				this.serverList.forEach(item => {
					this.$set(item, 'select', false);
				});
				console.log(this.serverList);
			});
		},
		// 选择客服
		selectCustomerServer(item) {
			if (item.select) {
				item.select = false;
			} else {
				this.serverList.forEach(item => {
					item.select = false;
				});
				item.select = true;
			}
		},
		// 确认转接
		handleSubmit() {
			http(api.serviceTransfer, {
				kefuToUserId: this.serverList.find(item => item.select).user_id, // 转接人id
				user_id: this.userInfo.id
			}).then(res => {
				Toast('转接成功');
				this.$emit('handleSubmit');
			});
		},
		close() {
			this.$emit('close');
		}
	}
};
</script>

<style lang="less" scoped>
.transfer_container {
	padding: 40rpx 0 68rpx 0;
	background: #fff;
	border-radius: 12rpx 12rpx 0px 0px;
	&_title {
		position: relative;
		display: flex;
		justify-content: flex-end;
		padding: 0 30rpx;
		&_value {
			position: absolute;
			top: 50%;
			left: 50%;
			transform: translate(-50%, -50%);
			color: #282828;
			font-weight: 600;
			font-size: 16px;
		}
		&_icon {
			.iconfont {
				font-size: 22px;
				color: #c8cad0;
			}
		}
	}

	.customerServer_list {
		&_item {
			display: flex;
			align-items: center;
			padding: 0 30px;
			height: 122rpx;
			border-bottom: 1px solid #f0f2f7;
			&_radio {
				margin-right: 38rpx;
				> div {
					width: 34rpx;
					height: 34rpx;
					border: 1px solid @primaryColor;
					border-radius: 50%;
					display: flex;
					align-items: center;
					justify-content: center;
					> div {
						width: 18rpx;
						height: 18rpx;
						background: @primaryColor;
						border-radius: 50%;
					}
				}
			}
			&_avar {
				width: 90rpx;
				height: 90rpx;
				border-radius: 6px;
				overflow: hidden;
				margin-right: 28rpx;
				image {
					width: 100%;
					height: 100%;
				}
			}
			&_nickName {
				color: #282828;
				font-size: 15px;
				font-weight: bold;
			}
		}
	}

	.handle_box {
		display: flex;
		justify-content: center;
		margin-top: 80rpx;
		> div {
			width: 690rpx;
			height: 86rpx;
			background: #3875ea;
			opacity: 1;
			border-radius: 43px;
			color: #ffffff;
			font-size: 15px;
			display: flex;
			align-items: center;
			justify-content: center;
		}
	}
}
.max_height {
	max-height: 772rpx;
}
</style>
