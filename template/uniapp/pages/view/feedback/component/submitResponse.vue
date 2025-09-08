<template>
	<div class="submit_res_container">
		<div class="image_box"><image src="~static/image/feedback/submitSuccess.png" mode="widthFix"></image></div>
		<div class="title">
			<span>提交成功</span>
		</div>
		<div class="message">
			<div>您的信息提交成功</div>
			<div>我们会尽快与您取得联系!</div>
		</div>
		<div class="handle" @click="handleSubmitRes">
			<span>好的</span>
		</div>
	</div>
</template>

<script>
	export default {
		methods:{
			handleSubmitRes() {
				this.$emit('handleSubmitRes')
			}
		}
	}
</script>

<style lang="less" scoped>
.submit_res_container {
	padding: 66rpx 111rpx;
	background: #fff;
	border-radius: 12px;
	.image_box {
		width: 344rpx;
		image {
			width: 100%;
		}
	}
	.title {
		color: #434343;
		font-weight: 600;
		text-align: center;
		margin: 30rpx 0 12rpx 0;
	}
	.message {
		text-align: center;
		font-size: 14px;
		color: #9F9F9F;
	}
	.handle {
		width: 426rpx;
		height: 76rpx;
		background: @primaryBgColor;
		opacity: 1;
		border-radius: 38px;
		display: flex;
		align-items: center;
		justify-content: center;
		color: #fff;
		margin-top: 54rpx;
	}
}
</style>
