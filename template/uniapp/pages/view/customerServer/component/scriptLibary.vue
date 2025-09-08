<!-- 话数库 -->
<template>
	<div class="scriptLibary_container" :style="{ 'max-height': maxHeight, height: height,'min-height': minWidth, width: width }">
		<div class="script_group">
			<scroll-view scroll-y="true" class="script_group_scroll">
				<div class="tag_list flex-center" @click="addGroup" v-if="speechArtType == 1">
					<span class="iconfont">&#xe6c1;</span>
					<span class="title_value">分组</span>
				</div>
				<div class="tag_list tag_list_group" v-for="(item, index) in tagList" :key="index" :class="{ selectEdTag: cateId == item.id }" @click="selectTags(item)">
					<div class="tag_list_tag" v-if="cateId == item.id"></div>
					<div class="tag_list_label">{{ item.name }}</div>
				</div>
			</scroll-view>
		</div>
		<div class="script_list">
			<scroll-view scroll-y="true" class="script_list_scroll">
				<div class="add_script" v-if="speechArtType == 1" @click="addSpeech">
					<div><span class="iconfont">&#xe6b9;</span></div>
					<div><span>添加话术</span></div>
				</div>
				<div class="script_list_item" v-for="(item, index) in scriptList" :key="index" @click="selectScript(item)">
					<div class="script_list_item_label">{{ item.title }}</div>
					<div class="script_list_item_value line2">{{ item.message }}</div>
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
					<div>添加话术</div>
					<div @click="closeAddGroup"><span class="iconfont">&#xe6b5;</span></div>
				</div>

				<div class="addGroup_form">
					<div class="input_box"><input class="inp_title" maxlength="20" v-model="speechData.title" type="text" placeholder="请输入标题名称" /></div>
					<div class="input_box"><textarea v-model="speechData.message" type="text" placeholder="请输入您的话术"></textarea></div>
					<div class="button_box" :class="{ canInput: speechData.title && speechData.message }" @click="handleSubmiteditAdd"><div>确定</div></div>
				</div>
			</div>
		</uni-popup>

		<uni-popup ref="addEdUserGroups" animation type="center">
			<div class="addGroup_container">
				<div class="addGroup_container_title">
					<div></div>
					<div>{{handleGroupTitle}}</div>
					<div @click="closeUserGroups"><span class="iconfont">&#xe6b5;</span></div>
				</div>

				<div class="addGroup_form">
					<div class="input_box"><input v-model.trim="userGroupData.name" maxlength="4" type="text" placeholder="请输入分组名称" /></div>
					<div class="input_box"><input v-model="userGroupData.sort" type="text" placeholder="请输入分组排序"></input></div>
					<div class="button_box" :class="{ canInput: userGroupData.name }" @click="handleSubmitUserGroup"><div>确定</div></div>
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
		},
		minWidth: {
			type: String,
			default: ''
		},
		width: {
			type: String,
			default: ''
		},
		

	},
	data() {
		return {
			// 话术分类
			tagList: {
				type: Array,
				default() {
					return [];
				}
			},
			scriptList: [],
			pageData: {
				page: 1,
				limit: '200'
			},
			cateId: '', // 选中的侧边栏ID
			speechData: {
				title: '',
				message: ''
			},
			userGroupData: {
				name: '',
				sort: ''
			},
			handleAdEdType: 1, // 1新增 2修改
			handleGroupTitle: '新增分组',
			groupType: 1, // 1新增 2修改
			
		};
	},
	watch: {
		speechArtType: {
			handler(val) {
				this.selectScriptLibary();
				// this.getSpeechArtList('init');
				console.log(this.tagList);
			}
		},
		title: {
			handler(val) {
				setTimeout(() => {
					this.getSpeechArtList();
				}, 500);
			}
		},
		tagList: {
			handler(val) {
				this.getSpeechArtList('init');
			}
		}
	},
	created() {
		this.selectScriptLibary();
	},
	methods: {
		// 查询话术类
		selectScriptLibary() {
			http(api.kefuServiceCate, {
				type: this.speechArtType
			}).then(res => {
				this.tagList = res.data;
				this.getSpeechArtList('init');
			});
		},
		// 获取话术列表
		getSpeechArtList(type = 'select') {
			if (this.tagList.length) {
				if (type == 'init') {
					this.cateId = this.tagList[0].id;
				}
				http(api.serviceSpeechcraft, {
					cate_id: this.cateId,
					type: this.speechArtType,
					...this.pageData,
					title: this.title
				}).then(res => {
					this.scriptList = res;
					this.speechData = {
						title: '',
						message: ''
					}
					console.log(this.scriptList);
				});
			} else {
				// setTimeout(() => {
				// 	this.getSpeechArtList(type);
				// }, 500);
			}
		},
		// 选择标签
		selectTags(item) {
			if(this.cateId == item.id) {
				console.log(item);
				this.userGroupData = serialize(item);
				ActionSheet(['编辑', '删除']).then(res => {
					console.log(res)
					switch(res) {
						case 0:
							this.groupType = 2;
							this.handleGroupTitle = '编辑分组';
							this.$refs.addEdUserGroups.open();
						break;
						case 1:
							Modal('温馨提示', `分组 "${item.name}"将被删除, 请问是否继续？`).then(() => {
								http(api.deleteServiceCate, item).then(res => {
									Toast('删除成功');
									this.selectScriptLibary('init');
								})
							})
						break;
						default:
						break;
					}
				})
			} else {
				this.cateId = item.id;
				this.getSpeechArtList();
			}
		},	
		// 选择话术
		selectScript(item) {
			this.$emit('sendSpeechMessage', item);
		},
		// 添加话术
		addSpeech() {
			this.speechData.title = '';
			this.speechData.message = '';
			this.handleAdEdType = 1;
			this.$refs.addEdGroup.open();
		},
		// 修改话术
		editSpeech(item) {
			this.handleAdEdType = 2;
			this.speechData = serialize(item);
			this.$refs.addEdGroup.open();
		},
		// 确认新增，修改话术
		handleSubmiteditAdd() {
			if (this.handleAdEdType == 1) {
				this.addSpeechClass();
				return;
			}
			if ((this.handleAdEdType = 2)) {
				this.editSpeechClass();
				return;
			}
		},
		// 修改话术事件
		editSpeechClass() {
			let canInput = true;
			console.log(this.speechData);

			Object.keys(this.speechData).forEach(item => {
				if (!this.speechData.title || !this.speechData.message) {
					canInput = false;
				}
			});

			if (!canInput) {
				Toast('请将内容填写完整');
				return;
			}

			http(api.putServiceSpeechcraft, this.speechData).then(res => {
				Toast('修改成功');
				this.getSpeechArtList();
				this.$refs.addEdGroup.close();
			});
		},
		// 新增话术事件
		addSpeechClass() {
			let canInput = true;
			
			Object.keys(this.speechData).forEach(item => {
				if (!this.speechData[item]) {
					canInput = false;
				}
			});
			if (!canInput) {
				Toast('请将内容填写完整');
				return;
			}
			http(api.postServiceSpeechcraft, {
				...this.speechData,
				cate_id: this.cateId
			}).then(res => {
				Toast('新增成功');
				this.getSpeechArtList();
				this.$refs.addEdGroup.close();
			});
		},
		// 删除话术
		handleDeleteServiceSpeechcraft(item) {
			Modal('温馨提示', `话术"${item.title}"将被删除，请问是否继续？`).then(res => {
				http(api.deleteServiceSpeechcraft, item).then(res => {
					Toast('删除成功');
					this.getSpeechArtList();
				});
			});
		},
		closeAddGroup() {
			this.$refs.addEdGroup.close();
		},
		// 新增分组
		addGroup() {
			this.groupType = 1;
			this.$refs.addEdUserGroups.open();
		},
		// 确定新增 或 修改 分组 1增 2改
		async handleSubmitUserGroup() {
			if(this.groupType == 1) {
				await http(api.serviceCate, {
					...this.userGroupData
				}).then(res => {
					Toast('添加成功');
				})
			}
			
			if(this.groupType == 2) {
				console.log(this.userGroupData);
				await http(api.putServiceCate, this.userGroupData).then(res => {
						Toast('修改成功');
						this.selectScriptLibary();
						this.$refs.addEdUserGroups.close();
					})
			}
			
			this.selectScriptLibary();
			this.$refs.addEdUserGroups.close();
			this.userGroupData = {
				name: '',
				sort: ''
			}
		},
		// 关闭分组弹框
		closeUserGroups() {
			this.$refs.addEdUserGroups.close();
		}
	}
};
</script>

<style lang="less" scoped>
.scriptLibary_container {
	display: flex;
	overflow: hidden;
	.script_group {
		// width: 176rpx;
		width: 20%;
		position: fixed;
		background: #f5f6f9;
		.script_group_scroll {
			width: 100%;
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
		// flex: 1;
		width: 80%;
		position: absolute;
		right: 0;
		.add_script {
			display: flex;
			align-items: center;
			justify-content: flex-end;
			padding: 0 30rpx;
			height: 109rpx;
		}
		.script_list_scroll {
			height: 100%;
			.script_list_item {
				padding: 25rpx 30rpx;
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

			display: flex;
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
.line2 {
	display: -webkit-box;
	overflow: hidden;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
}
.inp_title {
	width: 600rpx;
}
</style>
