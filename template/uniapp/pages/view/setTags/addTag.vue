<template>
	<div class="container">
		<lay-out>
	

			<div slot="content">
				<div class="content">
					<div class="content_tag"><span>标签名称</span></div>
					<!-- <div class="content_input"><input type="text" placeholder="请输入标签名称" /></div> -->
					<div class="content_input">
						<span>{{ tagsData.label }}</span>
					</div>
					<div class="content_tag"><span>标签成员</span></div>
					<div class="content_group">
						<!-- 	<div class="content_group_handle">
							<span class="iconfont">&#xe6c1;</span>
							<span>添加成员</span>
						</div> -->
						<view class="user-content">
							<view class="user-list" v-for="item in canEachUserList" :key="item.key">
								<div class="content_tag">
									<span>{{ item.key }}</span>
								</div>
								<uni-swipe-action v-for="items in item.list" :key="items.id">
									<uni-swipe-action-item>
										<view class="user" @click="toUserMessage(items)">
											<view class="user-list-left"><image :src="items.avatar" mode=""></image></view>
											<view class="user-list-right">{{ items.nickname }}</view>
										</view>

										<template v-slot:right>
											<div class="delete_tags" @click="deleteUser(items)"><text>删除</text></div>
										</template>
									</uni-swipe-action-item>
								</uni-swipe-action>
							</view>
						</view>
					</div>
				</div>
			</div>
		</lay-out>
	</div>
</template>

<script>
import { navigateTo, navigateBack, Modal, Toast } from 'pages/utils/uniApi.js';
import { makePy } from 'pages/utils/utils';
import http from 'pages/api/index';
import api from 'pages/api/api.js';
export default {
	data() {
		return {
			userList: [],
			canEachUserList: [],
			tagsData: {}
		};
	},
	onLoad(opt) {
		this.tagsData = this.$cache.get('userTagData');
		this.canEachUserList = this.letterSrot(this.tagsData.user);
	},
	methods: {
		// 前往用户详情界面
		toUserMessage(item) {
			navigateTo(1, '/pages/view/customerServer/customerMessage', { ...item, user_id: item.id });
		},
		// 删除用户
		deleteUser(item) {
			Modal('温馨提示', `"${item.nickname}"将被删除，请问是否继续?`).then(() => {
				http(api.deleteUserLabel, {
					userId: item.id,
					labelId: this.tagsData.id
				}).then(res => {
					this.canEachUserList.forEach(key => {
						key.list.forEach((val, index, arr) => {
							if (item.id == val.id) {
								arr.splice(index, 1);
							}
						});
					});
					Toast('删除成功');
				});
			});
		},
		cencel() {
			navigateBack(1);
		},
		// 对通讯录按字母顺序做排序
		letterSrot(userList) {
			const sortList = [];
			const list = userList;
			const letter = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];
			letter.forEach(item => {
				const obj = {};
				obj.key = item;
				obj.list = [];
				for (let i = 0; i < list.length; i++) {
					if (item == makePy(list[i].nickname).substr(0, 1)) {
						obj.list.push(list[i]);
						list.splice(i, 1);
						i--;
					}
				}
				if (obj.list.length) sortList.push(obj);
			});
			return sortList;
		}
	}
};
</script>

<style scoped lang="less">
@import url('./less/addTag.less');
.header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	height: 96rpx;
	padding: 0 30rpx;
	font-size: 16px;
	&_title {
		font-weight: 600;
	}
	.finish {
		width: 92rpx;
		height: 56rpx;
		background: #dddddd;
		opacity: 1;
		border-radius: 5px;
		display: flex;
		align-items: center;
		justify-content: center;
		color: #aaaaaa;
		opacity: 0;
	}
}
</style>
