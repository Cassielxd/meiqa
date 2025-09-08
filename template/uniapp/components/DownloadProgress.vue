<template>
	<view class="download-popup">
		<view class="mask"></view>
		<view class="content">
			<view class="title">正在下载升级包中</view>
			<progress :percent="percent" show-info stroke-width="3" />
			<view class="button-wrap" @click='stop'>
				<button type="primary" size="mini">取消</button>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name:"DownloadProgress",
		props: {
			percent: {
				type: Number,
				default: 0
			}
		},
		data() {
			return {
				
			};
		},
		methods: {
			stop(){
				this.$emit('clearAbort')
			},
		}
	}
</script>

<style scoped>
	.mask {
		position: fixed;
		top: 0;
		right: 0;
		bottom: 0;
		left: 0;
		z-index: 999;
		background-color: rgba(0, 0, 0, .3);
	}
	
	.content {
		position: fixed;
		top: 50%;
		left: 50%;
		z-index: 1000;
		width: 600rpx;
		padding: 60rpx 60rpx 30rpx;
		border-radius: 20rpx;
		box-sizing: border-box;
		background-color: #FFFFFF;
		transform: translate(-50%, -50%);
	}
	
	.button-wrap {
		margin-top: 30rpx;
		font-size: 0;
		text-align: center;
	}
</style>
