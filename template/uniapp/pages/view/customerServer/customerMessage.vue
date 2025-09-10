<template>
	<div class="container">
		<lay-out titleName="客户信息" headerColor="#fff">
			<!-- 		<div slot="hander_handle">
				<div @click="changeInputRadio"><span class="iconfont" :class="{ selected: inputRadio }">&#xe6b2;</span></div>
			</div> -->
			<div slot="content" class="content">
				<div class="content_header">
					<div class="content_header_avar"><image :src="userDetilsData.avatar" mode="widthFix"></image></div>
					<div class="content_header_userName">
						<span>{{ userDetilsData.nickname }}</span>
					</div>
					<div class="content_header_tag">
						<span>{{ formType[userDetilsData.type] }}</span>
					</div>
				</div>

				<div class="content_message">
					<div class="content_message_item">
						<div class="content_message_item_label"><span>nickname</span></div>
						<div class="content_message_item_value">
							<input type="text" v-model="userDetilsData.remark_nickname" placeholder="please input nickname" v-if="inputRadio" />
							<span v-else>{{ userDetilsData.remark_nickname ? userDetilsData.remark_nickname : 'no data' }}</span>
						</div>
					</div>

					<div class="content_message_item">
						<div class="content_message_item_label"><span>phone</span></div>
						<div class="content_message_item_value">
							<input type="text" v-model="userDetilsData.phone" placeholder="please input phone" v-if="inputRadio" />
							<span v-else>{{ userDetilsData.phone ? userDetilsData.phone : 'no data' }}</span>
						</div>
					</div>

					<div class="content_message_item">
						<div class="content_message_item_label"><span>gender</span></div>

						<div class="content_message_item_value" v-if="inputRadio">
							<picker mode="selector" :range="sexArr" range-key="label" @change="changeSex">
								<view>{{ sexType[userDetilsData.sex] }}</view>
							</picker>
						</div>

						<div class="content_message_item_value" v-else>
							<span>{{ sexType[userDetilsData.sex] }}</span>
						</div>
					</div>

					<div class="content_message_item">
						<div class="content_message_item_label"><span>remarks</span></div>
						<div class="content_message_item_value">
							<input type="text" v-model="userDetilsData.remarks" placeholder="please input remarks" v-if="inputRadio" />
							<span v-else>{{ userDetilsData.remarks ? userDetilsData.remarks : 'no data' }}</span>
						</div>
					</div>
					<div class="content_message_item" @click="openUserTag">
						<div class="content_message_item_label"><span>user label</span></div>
						<div class="content_message_item_value flex">
							<div class="tag_list">
								<span v-for="(item, index) in userDetilsData.label" :key="index">{{ item.label }}</span>
							</div>
							<div class="to_link" v-if="inputRadio"><span class="iconfont font">&#xe6c3;</span></div>
						</div>
					</div>
					<div class="content_message_item" @click="editGroup">
						<div class="content_message_item_label"><span>group</span></div>
						<div class="content_message_item_value">
							<span>{{ userGroupConcat[userDetilsData.group_id] }}</span>
						</div>
						<div class="to_link" v-if="inputRadio"><span class="iconfont">&#xe6c3;</span></div>
					</div>

					<div class="content_message_item">
						<div class="content_message_item_label"><span>user type</span></div>
						<div class="content_message_item_value">
							<span>{{ formType[userDetilsData.type] }}</span>
						</div>
					</div>
					<!-- <div class="content_message_item">
						<div class="content_message_item_label"><span>生日</span></div>
						<div class="content_message_item_value"><span>9月5号</span></div>
					</div> -->

					<div class="content_message_item marginTop16" @click="handelComplaint">
						<div class="content_message_item_label"><span>complaint</span></div>
						<div class="content_message_item_value">
							<span>{{ formType[userDetilsData.type] }}</span>
						</div>
					</div>

					<div class="content_message_item marginTop16" @click="handelblock">
						<div class="content_message_item_label"><span>block</span></div>
						<div class="content_message_item_value">
							<span>{{ formType[userDetilsData.type] }}</span>
						</div>
					</div>
				</div>
                 <!-- #ifdef H5 -->
                 <div class="userTag_container_handle" v-if="inputRadio" @click="handleEditUserMessage">
                 	<div class="userTag_container_handle_button"><span>confirm</span></div>
                 </div> 
                 

				<!-- 	<div class="userTag_container_handle"  @click="toMessage">
					<div class="userTag_container_handle_button"><span>发消息</span></div>
				</div> -->
			</div>
		</lay-out>

		<uni-popup ref="userTagModel" type="center" animation>
			<div class="userTag_container">
				<div class="userTag_container_title">
					<div class="userTag_container_title_message"><span>user label</span></div>
					<div class="closeModel" @click="closeUserTagModel"><span class="iconfont">&#xe6c6;</span></div>
				</div>

				<div class="userTag_container_list">
					<div class="userTag_container_list_item" :class="{ mg40: index == 0 }" v-for="(item, index) in userTagList" :key="index">
						<div class="userTag_container_list_item_label">
							<span>{{ item.name }}</span>
						</div>
						<div class="userTag_container_list_item_value">
							<span :class="{ select: val.disabled }" v-for="(val, i) in item.label" :key="i" @click="selectTags(val)">{{ val.label }}</span>
						</div>
					</div>
				</div>

				<div class="userTag_container_handle" @click="handleSetTags">
					<div class="userTag_container_handle_button" ><span>confirm</span></div>
				</div>
			</div>
		</uni-popup>

		<uni-popup ref="userGroupModel" type="center" animation>
			<div class="userTag_container">
				<div class="userTag_container_title">
					<div class="userTag_container_title_message"><span>user group</span></div>
					<div class="closeModel" @click="closeUserGroupModel"><span class="iconfont">&#xe6c6;</span></div>
				</div>
				<div class="userTag_container_list">
					<div class="userTag_container_list_item">
						<div class="userTag_container_list_item_value">
							<span :class="{ select: val.id == selectGroupId }" v-for="(val, i) in userGroup" :key="i" @click="selectGroup(val)">{{ val.group_name }}</span>
						</div>
					</div>
				</div>

				<div class="userTag_container_handle" @click="handleSetGroup">
					<div class="userTag_container_handle_button"><span>confirm</span></div>
				</div>
			</div>
		</uni-popup>
	</div>
</template>

<script>
import { Toast, navigateTo, Modal } from 'pages/utils/uniApi.js';
import http from 'pages/api/index';
import api from 'pages/api/api.js';
export default {
	data() {
		return {
			inputRadio: true,
			selectGroupId: '',
			userTagList: [],
			formType: {
				0: 'pc',
				1: 'wechat',
				2: 'mini program',
				3: 'H5'
			},
			sexType: {
				0: 'unknown',
				1: 'male',
				2: 'female'
			},
			sexArr: [
				{
					value: 0,
					label: 'unknown'
				},
				{
					value: 1,
					label: 'male'
				},
				{
					value: 2,
					label: 'female'
				}
			],
			userGroup: [], // 用户分组
			userGroupConcat: {},
			queryUserData: {}, // url上挂载的参数
			userDetilsData: {} // 用户详细信息
		};
	},
	onLoad(opt) {
		this.queryUserData = opt;
		this.initUserGroup();
	},
	onNavigationBarButtonTap() {
		this.handleEditUserMessage();
	},
	methods: {
		// 拉黑
		handelblock() {
			Modal('reminder', `"${this.userDetilsData.nickname}"will be blocked, are you sure you want to continue?`).then(() => {
				http(api.putUserStatus, { userId: this.userDetilsData.user_id }).then(res => {
					Toast('block success');
					this.$store.commit('setRefresh', true);
					navigateTo(4, '/pages/view/messageList/index');
				});
			});
		},
		// 点击投诉按钮，跳转投诉界面
		handelComplaint() {
			navigateTo(1, '/pages/view/customerServer/complaint?id='+this.userDetilsData.id);
		},
		// 获取用户详细信息
		initData() {
			console.log(this.queryUserData);
			http(api.userInfo, this.queryUserData).then(res => {
				this.userDetilsData = res;
				this.selectGroupId = this.userDetilsData.group_id;
			});
		},
		// inputRadio 为 true 时，可以修改客户信息
		changeInputRadio() {
			this.inputRadio = !this.inputRadio;
		},
		// 获取所有分组 ---> 获取详细信息
		initUserGroup() {
			http(api.UserGroup).then(res => {
				this.userGroup = res;
				this.userGroup.forEach(item => {
					this.userGroupConcat[item.id] = item.group_name;
				});
				this.initData();
			});
		},
		// 打开用户标签选择选项
		openUserTag() {
			if (!this.inputRadio) {
				Toast('please open the modify switch');
				return;
			}
			http(api.userLabelSelect, { id: this.queryUserData.user_id }).then(res => {
				this.userTagList = res;
				this.$refs.userTagModel.open('center');
			});
		},
		// 选择标签
		selectTags(item) {
			this.$set(item, 'disabled', !item.disabled);
		},
		// 选择性别
		changeSex(e) {
			this.userDetilsData.sex = e.detail.value;
		},
		// 选择分组弹框
		editGroup() {
			if (!this.inputRadio) {
				Toast('please open the modify switch');
				return;
			}
			this.$refs.userGroupModel.open();
		},
		// 关闭选择分组
		closeUserGroupModel() {
			this.$refs.userGroupModel.close();
		},
		// 选择分组
		selectGroup(item) {
			if (this.selectGroupId == item.id) {
				this.selectGroupId = '';
				return;
			}
			this.selectGroupId = item.id;
		},
		// 确认选择分组
		handleSetGroup() {
			this.userDetilsData.group_id = this.selectGroupId;
			http(api.setUserGroup, {
				id: this.userDetilsData.group_id,
				userId: this.userDetilsData.id
			}).then(res => {
				Toast('user group set success');
				this.$refs.userGroupModel.close();
			});
		},
		// 设置标签
		handleSetTags() {
			let postData = {
				label_ids: [],
				un_label_ids: [],
				userId: this.queryUserData.user_id
			},Label = [];
			
			this.userTagList.forEach(item => {
				item.label.forEach(val => {
					if (val.disabled) {
						postData.label_ids.push(val.id);
						Label.push(val)
					} else {
						postData.un_label_ids.push(val.id);
					}
				});
			});
			http(api.putUserLabel, postData).then(res => {
				this.$refs.userTagModel.close();
				// this.initData();
				this.selectGroupId = postData.label_ids
				this.userDetilsData.label = Label
			});
		},
		// 关闭用户标签选项
		closeUserTagModel() {
			this.$refs.userTagModel.close();
		},
		// 确认修改用户信息
		handleEditUserMessage() {
			http(api.userUpdateUser, { ...this.userDetilsData, userId: this.userDetilsData.id }).then(res => {
				// this.inputRadio = !this.inputRadio;
				this.initUserGroup();
				Toast('修改成功');
			});
		},
		toMessage() {
			navigateTo('2', '/pages/view/customerServer/index', this.queryUserData);
		}
	}
};
</script>

<style scoped lang="less">
@import url('./less/customerMessage.less');
</style>
