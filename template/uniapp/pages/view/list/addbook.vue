<template>
	<view class="container">
		<lay-out noBottomHeight>
			<div slot="header">
				<view class="search-content padding">
					<view class="uni-input-wrapper"><input class="uni-input" @confirm="initData" v-model="searchData.nickname" placeholder="搜索用户名称" /></view>
				</view>
			</div>

			<div slot="content">
				<!-- 通讯录导航 -->
				<address-book :bookList="bookList" :letter="letter">
					<template v-slot:addressBookList>
						<view class="user-content">
							<view class="user-list" v-for="(item, index) in Object.keys(bookList)" :key="index">
								<view class="number padding">{{ item }}</view>
								<view class="user" v-for="(val, i) in bookList[item]" :key="i" @click="selectUser(val)">
									<view class="user-list-left"><image :src="val.avatar" mode=""></image></view>
									<view class="user-list-right">{{ val.nickname }}</view>
								</view>
							</view>
						</view>
					</template>
				</address-book>
			</div>
		</lay-out>

		<uni-popup ref="userTagModel" type="bottom" animation>
			<div class="userTag_container">
				<div class="userTag_container_title">
					<div :class="{ on: tabOn == 1 }" class="userTag_container_title_message" @click="tabOn = 1"><span>用户标签</span></div>
					<div :class="{ on: tabOn == 2 }" class="userTag_container_title_message" @click="tabOn = 2"><span>分组标签</span></div>
					<div class="closeModel" @click="closeUserTagModel"><span class="iconfont">&#xe6b5;</span></div>
				</div>

				<scroll-view scroll-y="true" class="userTag_container_list">
					<div v-if="tabOn == 1" class="userTag_container_list_item" :class="{ mg40: index == 0 }" v-for="(item, index) in userTagList" :key="index">
						<div class="userTag_container_list_item_label">
							<span>{{ item.name }}</span>
						</div>
						<div class="userTag_container_list_item_value">
							<span :class="{ select: val.disabled }" v-for="(val, i) in item.label" :key="i" @click="selectTags(val)">{{ val.label }}</span>
						</div>
					</div>
					<div v-if="tabOn == 2" class="userTag_container_list_item mg40">
						<div class="userTag_container_list_item_value">
							<span :class="{ select: val.disabled }" v-for="(val, i) in groupList" :key="val" @click="selectGroups(val)">{{ val.group_name }}</span>
						</div>
					</div>
				</scroll-view>
				<div class="userTag_container_handle" @click="handleSetTags">
					<div class="userTag_container_handle_button"><span>确定</span></div>
				</div>
			</div>
		</uni-popup>
	</view>
</template>

<script>
import AddressBook from './addressBookNavigation/addressBookNavigation';
import { makePy } from '../../utils/utils';
import { navigateTo } from 'pages/utils/uniApi.js';
import http from 'pages/api/index';
import api from 'pages/api/api.js';
export default {
	components: { AddressBook },
	data() {
		return {
			letter: ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'],
			list: [],
			search: '',
			bookList: {},
			customerServerData: {}, // 客服数据
			searchData: {
				nickname: '',
				label_id: '',
				group_id: ''
			},
			userTagList: [],
			groupList: [],
			tabOn: 1,
		};
	},
	onLoad() {
		this.customerServerData = this.$store.state.kefuInfo;
		this.bookList = this.$store.state.bookList;
	},
	onShow() {
		this.searchData.label_id = '';
		this.searchData.group_id = '';
		this.initData();
		// { id: this.queryUserData.user_id }
		http(api.userLabelSelect).then(res => {
			this.userTagList = res;
		});
		http(api.UserGroup).then(res => {
			this.groupList = res;
		});
	},
	onNavigationBarButtonTap() {
		// this.toTagList();
		this.$refs.userTagModel.open();
	},
	methods: {
		// 关闭弹窗
		closeUserTagModel() {
			this.$refs.userTagModel.close();
		},
		// 选择标签
		selectTags(item) {
			this.$set(item, 'disabled', !item.disabled);
		},
		selectGroups(item) {
			this.$set(item, 'disabled', !item.disabled);
		},
		// 设置标签
		handleSetTags() {
			let postData = {
				label_ids: [],
				group_ids: []
			};
			console.log(111);
			this.userTagList.forEach(item => {
				item.label.forEach(val => {
					if (val.disabled) {
						postData.label_ids.push(val.id);
					}
				});
			});
			this.groupList.forEach(item => {
				if (item.disabled) {
					postData.group_ids.push(item.id);
				}
			});
			
			console.log(postData);
			this.searchData.label_id = postData.label_ids.join(',');
			this.searchData.group_id = postData.group_ids.join();
			this.closeUserTagModel();
			this.initData(this.searchData);
		},
		// 查询当前客服的客户列表
		initData() {
			this.$store.dispatch('getBookList', this.searchData).then(res => {
				this.bookList = res;
			});
		},

		toTagList() {
			navigateTo(1, '/pages/view/setTags/index');
		},
		selectUser(item) {
			navigateTo(1, '/pages/view/customerServer/customerMessage', { user_id: item.to_user_id });
		},
		// 对通讯录按字母顺序做排序
		letterSrot() {
			const sortList = [];
			const list = this.list;
			this.letter.forEach(item => {
				const obj = {};
				obj.key = item;
				obj.list = [];
				for (let i = 0; i < list.length; i++) {
					if (item == makePy(list[i].username).substr(0, 1)) {
						obj.list.push(list[i]);
						list.splice(i, 1);
						i--;
					}
				}
				if (obj.list.length) sortList.push(obj);
			});
			this.bookList = sortList;
		}
	}
};
</script>

<style lang="less" scoped>
@padding-l-r: 30rpx;
@padding-t-p: 20rpx;
@bgc-f: #ffffff;
@fontSize: 28rpx;
@imgSize: 64rpx;
.padding {
	padding: @padding-t-p @padding-l-r;
}
.search-content {
	position: relative;
	background-color: @bgc-f;
	input {
		border: none;
		background-color: #f5f6f9;
		color: #c4c4c4;
		font-size: @fontSize;
		border-radius: 39px;
		padding: @padding-t-p 0 @padding-t-p @padding-l-r;
		text-align: center;
	}
}
.user-content {
	background-color: #eeeeee;
	.user-list {
		.number {
			color: #666666;
			font-size: @fontSize - 2rpx;
		}
		.user {
			background-color: @bgc-f;
			display: flex;
			align-items: center;
			padding: @padding-t-p @padding-l-r;
			border-bottom: 1rpx solid #f0f2f7;
			.user-list-left {
				width: @imgSize;
				height: @imgSize;
				padding-right: 20rpx;
				image {
					width: @imgSize;
					height: @imgSize;
					border-radius: 50%;
				}
			}
			.user-list-right {
				color: #282828;
			}
		}
	}
}
.header {
	&_content {
		display: flex;
		justify-content: flex-end;
		align-items: center;
		position: relative;
		height: 116rpx;
		background: #1890fc;
		padding: 0 30rpx;

		/* #ifdef APP-PLUS */
		padding-top: 64rpx;
		/* #endif */
		color: #fff;
		> div {
			flex: 1;
		}
		> div:nth-child(2) {
			text-align: center;
			font-weight: bold;
		}
		> div:nth-child(3) {
			font-size: 14px;
			text-align: right;
		}
	}
}
.userTag_container {
	padding: 30rpx 40rpx;
	// padding-bottom: 140rpx;
	background: #fff;
	width: 100%;
	box-sizing: border-box;
	&_title {
		display: flex;
		// justify-content: flex-end;
		position: relative;
		&_message {
			flex: 1;
			text-align: center;
			// position: absolute;
			// top: 50%;
			// left: 50%;
			// transform: translate(-50%, -50%);
			&.on {
				font-weight: bold;
				color: #3875EA;
			}
		}
		.closeModel {
			position: absolute;
			top: 0;
			right: 0;
			color: #c8cad0;
			.iconfont {
				font-size: 36rpx;
			}
		}
	}

	&_list {
		height: 600rpx;
		&_item {
			margin-top: 78rpx;
			&_label {
				color: #282828;
				font-weight: bold;
				margin-bottom: 26rpx;
			}

			&_value {
				display: grid;
				grid-template-columns: repeat(3, 178rpx);
				grid-row-gap: 26rpx;
				grid-column-gap: 26rpx;
				span {
					font-size: 14px;
					display: inline-block;
					background: #f0f0f0;
					text-align: center;
					padding: 12rpx 0;
					border-radius: 12rpx;
				}
				.select {
					background: @primaryColor;
					color: #fff;
				}
			}
		}
		.mg0 {
			margin-top: 40rpx;
		}
	}

	&_handle {
		width: 610rpx;
		height: 76rpx;
		background: @primaryColor;
		opacity: 1;
		border-radius: 43px;
		margin: auto;
		margin-top: 44rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		color: #fff;
		font-weight: 600;
		font-size: 14px;
	}
}
</style>
