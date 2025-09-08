<!-- 话数库 -->
<template>
	<div class="scriptLibary_container" :style="{ 'max-height': maxHeight, height: height }">
		<div class="script_list">
			<scroll-view scroll-y="true" class="script_list_scroll">
				<div class="add_script" v-if="speechArtType == 1" @click="addSpeech">
					<div><span class="iconfont">&#xe6b9;</span></div>
					<div><span>添加自动回复</span></div>
				</div>
				<div class="script_list_item" v-for="(item, index) in authReply" :key="index" @click="selectScript(item)">
					<div class="script_list_item_label">{{ item.keyword }}</div>
					<div class="script_list_item_value">{{ item.content }}</div>
					<div class="edit_box" v-if="speechArtType == 1">
						<div @click.stop="editSpeech(item)"><span class="iconfont">&#xe6b2;</span></div>
						<div @click.stop="handleDeleteServiceSpeechcraft(item)"><span class="iconfont">&#xe6b3;</span></div>
					</div>
				</div>
			</scroll-view>
		</div>

		<uni-popup ref="addEdGroup" animation type="center">
			<div class="addGroup_container">
				<div class="addGroup_container_title">
					<div></div>
					<div v-if="speechData.id">编辑自动回复</div>
					<div v-else>添加自动回复</div>
					<div @click="closeAddGroup"><span class="iconfont">&#xe6b5;</span></div>
				</div>

				<div class="addGroup_form">
					<div class="input_box"><input v-model="speechData.keyword" type="text" placeholder="请输入关键词,多个关键字请用逗号隔开" /></div>
					<div class="input_box"><textarea v-model="speechData.content" type="text" placeholder="请输入自动回复内容"></textarea></div>
					<div class="input_box" style='width: 40%;'><input v-model="speechData.sort" type="number" placeholder='排序' /></div>
					<div class="button_box" :class="{ canInput: speechData.keyword && speechData.content }" @click="handleSubmiteditAdd"><div>确定</div></div>
				</div>
			</div>
		</uni-popup>
	</div>
</template>

<script>
import { Toast, throttle, serialize, Modal, ActionSheet } from 'pages/utils/uniApi.js';
import http from 'pages/api/index';
import api from 'pages/api/api.js';
export default {
	props: {
		// 话术库分类
		speechArtType: {
			type: Number | String,
			default: '1'
		},
		// 话术检索
		title: {
			type: String,
			default: ''
		},
		// 是否展示添加话术
		icCanEdit: {
			type: Boolean,
			default: false
		},
		maxHeight: {
			type: String,
			default: ''
		},
		height: {
			type: String,
			default: ''
		}
	},
	data() {
		return {
			authReply: [],
			where: {
				page: 1,
				limit: 5
			},
			scriptList: [],
			pageData: {
				page: 1,
				limit: '200'
			},
			cateId: '', // 选中的侧边栏ID
			speechData: {
				keyword: '',
				content: '',
				sort:0
			},
			userGroupData: {
				name: '',
				sort: ''
			},
			handleAdEdType: 1, // 1新增 2修改
			handleGroupTitle: '新增分组',
			groupType: 1 // 1新增 2修改
		};
	},
	watch: {
		authReply: {
			handler(val) {
				console.log(val);
			},
			deep: true
		},
		title: {
			handler(val) {}
		}
	},
	created() {
		this.getAuthReplyList();
	},
	methods: {
		// 查询话术类
		getAuthReplyList() {
			http(api.getAuthReply, this.where).then(res => {
				this.authReply = res;
			});
		},
		// 添加话术
		addSpeech() {
			this.handleAdEdType = 1;
			this.speechData = {};
			this.$refs.addEdGroup.open();
		},
		// 修改自动回复
		editSpeech(item) {
			this.handleAdEdType = 2;
			this.speechData = serialize(item);
			this.$refs.addEdGroup.open();
		},
		// 确认新增，修改自动回复
		handleSubmiteditAdd() {
			this.speechData.id = this.speechData.id ? this.speechData.id : 0;
			http(api.saveAuthReply, this.speechData).then(res => {
				Toast(this.speechData.id ? '修改成功' : '添加成功');
				this.getAuthReplyList();
				this.closeAddGroup();
			});
		},
		// 删除自动回复
		handleDeleteServiceSpeechcraft(item) {
			Modal('温馨提示', `自动回复"${item.content}"将被删除，请问是否继续？`).then(res => {
				http(api.deleteAuthReply,item).then(res => {
					Toast('删除成功');
					this.getAuthReplyList();
				});
			});
		},
		closeAddGroup() {
			this.$refs.addEdGroup.close();
		}
	}
};
</script>

<style lang="less" scoped>
.scriptLibary_container {
	display: flex;
	overflow: hidden;
	.script_group {
		width: 176rpx;
		background: #f5f6f9;
		.script_group_scroll {
			height: 100%;
			.tag_list {
				width: 100%;
				height: 109rpx;
				.iconfont {
					color: #9f9f9f;
					font-size: 11px;
					margin-right: 4rpx;
				}
				.title_value {
					color: #9f9f9f;
					font-size: 13px;
					position: relative;
					top: 2rpx;
				}
				.tag_list_tag {
					width: 6rpx;
					height: 46rpx;
					background: @primaryColor;
					opacity: 1;
					border-radius: 0px 4px 4px 0px;
				}
			}

			.flex-center {
				display: flex;
				justify-content: center;
				align-items: center;
			}

			.tag_list_group {
				display: flex;
				align-items: center;
				justify-content: flex-start;
				position: relative;

				.tag_list_label {
					position: absolute;
					top: 50%;
					left: 50%;
					transform: translate(-50%, -50%);
					white-space: nowrap;
					color: #282828;
					font-size: 13px;
					font-weight: bold;
				}
			}
		}
	}

	.script_list {
		flex: 1;
		.add_script {
			display: flex;
			align-items: center;
			justify-content: flex-end;
			padding: 0 30rpx;
			height: 109rpx;
			background-color: #ffffff;
		}
		.script_list_scroll {
			height: 100%;
			.script_list_item {
				padding: 25rpx 30rpx;
				background-color: #ffffff;
				&_label {
					color: #282828;
					font-size: 14px;
					font-weight: bold;
				}
				&_value {
					font-size: 14px;
					color: #9f9f9f;
				}
			}
			.edit_box {
				display: flex;
				justify-content: flex-end;
				> div {
					margin-left: 48rpx;
				}
				.iconfont {
					font-size: 16px;
				}
			}
		}
	}
}
.selectEdTag {
	background: #fff !important;
	.tag_list_label {
		color: @primaryColor !important;
	}
}

.addGroup_container {
	width: 690rpx;
	background: #fff;
	border-radius: 12px;
	&_title {
		display: flex;
		justify-content: space-between;
		align-items: center;
		position: relative;
		padding: 30rpx 30rpx 50rpx 0;
		.iconfont {
			font-size: 22px;
			color: #c8cad0;
		}
		> div {
			flex: 1;
			text-align: center;
		}
		> div:nth-child(3) {
			text-align: right;
		}
	}

	.addGroup_form {
		padding: 0 30rpx;
		padding-bottom: 69rpx;
		.input_box {
			margin-bottom: 32rpx;
			background: #f5f5f5;
			border-radius: 14rpx;
			align-items: center;
			padding: 0 20rpx;
			input {
				font-size: 14px;
				height: 68rpx;
			}
			textarea {
				padding: 8px 0;
				font-size: 14px;
				height: 192rpx;
			}
		}

		.button_box {
			margin-top: 60rpx;
			> div {
				height: 86rpx;
				background: #c8cad0;
				opacity: 1;
				border-radius: 43px;
				display: flex;
				align-items: center;
				justify-content: center;
				color: #ffffff;
			}
		}
		.canInput {
			> div {
				height: 86rpx;
				background: @primaryColor;
				opacity: 1;
				border-radius: 43px;
				display: flex;
				align-items: center;
				justify-content: center;
				color: #ffffff;
			}
		}
	}
}
</style>
