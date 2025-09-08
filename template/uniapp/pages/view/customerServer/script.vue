<template>
	<div class="container">
		<lay-out>
			<div slot="header" class="header">
				<div class="header_title">{{speechArtTypeConcat[speechArtType]}}</div>
				<div @click="finish">完成</div>
			</div>
			<div slot="content" class="content">
				<div class="scriptLibary_search">
					<div>
						<span class="iconfont">&#xe6d8;</span>
						<input v-model="propstitle" type="text" placeholder="搜索快捷回复" />
					</div>
				</div>
				<div class="scriptLibary_content">
					<scripts-libary
						:speechArtType="speechArtType"
						:maxHeight="maxHeight" 
						icCanEdit
						:tagList="speechTagList"
						:title="propstitle"
					></scripts-libary>
				</div>
			</div>
		</lay-out>
	</div>
</template>

<script>
import scriptsLibary from './component/scriptLibary.vue';
import { navigateTo, navigateBack } from 'pages/utils/uniApi.js';
import http from 'pages/api/index';
import api from 'pages/api/api.js';
export default {
	components: {
		scriptsLibary
	},
	data() {
		return {
			maxHeight: '',
			speechArtType: '',
			speechArtTypeConcat: {
				1: '个人库',
				0: '公共库'
			},
			speechTagList: [], // 话术分类
			propstitle: ''
		};
	},
	onLoad(opt) {
		this.speechArtType = opt.speechArtType;
		this.selectScriptLibary();
		console.log(this.speechArtType);
		this.$nextTick(() => {
			let info = uni.createSelectorQuery().select('.content');
			info
				.boundingClientRect((data) => {
					this.maxHeight = (Math.ceil(data.height) * 1.85) + 'rpx'
				})
				.exec();
		})

	},
	methods: {
		// 查询话术类
		selectScriptLibary() {
			http(api.kefuServiceCate, {
				type: this.speechArtType
			}).then(res => {
				this.speechTagList = res.data;
			});
		},
		finish() {
			navigateBack(1);
		}
	}
};
</script>

<style lang="less" scoped>
.header {
	height: 96rpx;
	display: flex;
	align-items: center;
	justify-content: flex-end;
	padding: 0 30rpx;
	position: relative;
	background: #fff;
	&_title {
		position: absolute;
		top: 70%;
		left: 50%;
		transform: translate(-50%, -50%);
		font-size: 16px;
		font-weight: bold;
	}
	> div:nth-child(2) {
		font-size: 14px;
		color: @primaryColor;
	}
}
.scriptLibary_search {
	background: #fff;
	padding-bottom: 44rpx;
	border-bottom: 1px solid #f0f3fa;
	> div {
		display: flex;
		background: #f5f6f9;
		width: 690rpx;
		height: 68rpx;
		margin: auto;
		display: flex;
		align-items: center;
		padding: 0 20rpx;
		box-sizing: border-box;
		border-radius: 39px;
		> input {
			flex: 1;
			font-size: 14px;
			margin-left: 8rpx;
		}
		.iconfont {
			font-size: 22px;
			color: #c4c4c4;
		}
	}
}
.content {
	overflow: hidden;
	height: 100%;
	.scriptLibary_content {
	}
}
</style>
