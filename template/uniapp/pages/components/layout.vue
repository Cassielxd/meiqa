<template>
	<view class="layout" :class="{ noBottomHeight: noBottomHeight }">
		<view class="layout_header">
			<view v-if="isShowHeader" class="global_title" :style="{ 'background-color': headerColor, color: headerTextColor }">
				<view class="goBack" @click="goBack"><span class="iconfont">&#xe6c4;</span></view>
				<view class="title_message">{{ titleName ? titleName : getCurRoute.$holder.navigationBarTitleText }}</view>
				<view><slot name="hander_handle"></slot></view>
			</view>
			<slot v-else name="header"></slot>
		</view>
		<view class="layout_content">
			<!-- <slot name="content"></slot> -->
			<scroll-view
				:key="refresherTriggered"
				:scroll-with-animation="scrollWithAnimation"
				scroll-anchoring
				:scroll-y="true"
				:refresher-enabled="refresherEnabled"
				@refresherpulling="onPulling"
				@refresherrefresh="refresherrefresh"
				:refresher-triggered="refresherTriggered"
				:refresher-threshold="refresherThreshold"
				@refresherrestore="refresherrestore"
				@refresherabort="refresherabort"
				class="layout_content_scroll"
				:scroll-top="scrollTop"
				@scrolltolower="scrolltolower"
				@scrolltoupper="scrolltoupper"
			>
				<slot name="content" class="slot-content"></slot>
			</scroll-view>
		</view>
		<view class="layout_bottom">
			<view v-if="isShowTap" class="footer">
				<view class="footer_item" v-for="(item, index) in footerData" :key="index" @click="toRouter(item)">
					<view class="footer_item_image"><image :src="getCurRoute.route == item.path ? item.selectImage : item.unSelectImage" mode="widthFix"></image></view>
					<view class="footer_item_tip">
						<span>{{ item.title }}</span>
					</view>
					<view class="footer_message" v-if="item.id == 1 && unreadMessage">{{ unreadMessage }}</view>
				</view>
			</view>
			<slot v-else name="bottom"></slot>
		</view>
	</view>
</template>

<script>
import { navigateTo, navigateBack } from '../utils/uniApi.js';
export default {
	props: {
		// 是否减去底部高度
		noBottomHeight: {
			type: Boolean,
			default: false
		},
		scrollWithAnimation:{
			type: Boolean,
			default: true
		},
		// 标题名称
		titleName: {
			type: String,
			default: '设置'
		},
		// 下拉刷新阈值
		refresherThreshold: {
			type: Number,
			default: 90
		},
		// 是否开启自定义下拉
		refresherEnabled: {
			type: Boolean,
			default: false
		},
		// 下拉刷新状态
		refresherTriggered: {
			type: Boolean | String,
			default: false
		},
		unreadMessage: {
			type: Number | String,
			default: 0
		},
		headerTextColor: {
			type: String,
			default: ''
		},
		headerColor: {
			type: String,
			default: ''
		},
		isShowHeader: {
			type: Boolean,
			default: false
		},
		isShowTap: {
			type: Boolean,
			default: false
		},
		scrollTop: {
			type: Number,
			default: 0
		}
	},
	data() {
		return {
			selectId: 1,
			footerData: [
				{
					title: '聊天',
					selectImage: require('../../static/image/footer/chatSelect.png'),
					unSelectImage: require('../../static/image/footer/chatUnSelcect.png'),
					id: 1,
					path: 'pages/view/messageList/index'
				},
				{
					title: '客户',
					selectImage: require('../../static/image/footer/userselect.png'),
					unSelectImage: require('../../static/image/footer/userUnselect.png'),
					id: 2,
					path: 'pages/view/list/addbook'
				},
				{
					title: '统计',
					selectImage: require('../../static/image/footer/staticSelect.png'),
					unSelectImage: require('../../static/image/footer/staticUnSelect.png'),
					id: 3,
					path: 'pages/view/statistics/index'
				},
				{
					title: '我的',
					selectImage: require('../../static/image/footer/mySelect.png'),
					unSelectImage: require('../../static/image/footer/myUnSelect.png'),
					id: 4,
					path: 'pages/view/user/index'
				}
			]
		};
	},
	created() {},
	computed: {
		getCurRoute() {
			let routes = getCurrentPages(); // 获取当前打开过的页面路由数组
			let curRoute = routes[routes.length - 1]; // 获取当前路由
			return curRoute;
		}
	},
	methods: {
		// 滑动到底部
		scrolltolower() {
			this.$emit('goBottom');
		},
		// 滑动到顶部
		scrolltoupper() {
			this.$emit('goTop');
		},
		// 自定义下拉控件被下拉
		onPulling(e) {
			// console.log(e);
		},

		// 自定义下拉被触发
		refresherrefresh() {
			this.$emit('refresherrefresh');
		},
		// 自定义下拉复位
		refresherrestore() {
			this.$emit('refresherrestore');
		},
		// 自定义下拉被终止
		refresherabort() {
			this.$emit('refresherabort');
		},
		toRouter(item) {
			navigateTo(3, '/' + item.path);
		},
		// 返回上一页
		goBack() {
			navigateBack(1);
		}
	}
};
</script>

<style lang="less" scoped>
.layout {
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.noBottomHeight {
	// height: calc(100% - 100rpx);
	position: fixed;
	top: 0;
	left: 0;
}
.layout_header {
	// padding-top: 10rpx;
}
.layout_content {
	flex: 1;
	overflow: hidden;
	.layout_content_scroll {
		height: 100%;
	}
}

.global_title {
	height: 96rpx;
	display: flex;
	align-items: center;
	padding: 0 30rpx;
	position: relative;
	font-size: 16px;
	justify-content: space-between;
	/* #ifdef APP-PLUS */
	padding-top: 64rpx;
	// border-bottom: 1px solid rgba(0, 0, 0, 0.1);
	/* #endif */
	// font-weight: 600;
	.title_message {
		position: absolute;
		top: 50%;
		/* #ifdef APP-PLUS */
		top: 70%;
		/* #endif */
		left: 50%;
		transform: translate(-50%, -50%);
		font-weight: bold;
	}
	.goBack {
		.iconfont {
		}
	}
}

.footer {
	height: 98rpx;
	padding: 0 65rpx;
	/* #ifdef APP-PLUS */
	padding-bottom: 65rpx;

	/* #endif */
	display: flex;
	justify-content: space-between;
	align-items: center;
	box-shadow: 0px -3px 16px rgba(0, 33, 96, 0.04);
	background: #fff;
	position: relative;
	&_item {
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		&_image {
			width: 44rpx;
			image {
				width: 100%;
			}
		}
		&_tip {
			color: #707070;
			font-size: 22rpx;
		}
	}
	&_message {
		position: absolute;
		top: 0;
		left: 90rpx;
		height: 30rpx;
		padding: 4rpx 10rpx;
		background: #f74c31;
		opacity: 1;
		border-radius: 16rpx;
		color: #fff;
		font-size: 10px;
		display: flex;
		align-items: center;
		justify-content: center;
	}
}
</style>
