<template>
	<div class="container">
		<lay-out>
			<div slot="content" class="content">
				<!-- 	<div class="handle_title" @click="createdTag">
					<span class="iconfont">&#xe6c1;</span>
					<span class="handle_title_message">新建标签</span>
				</div> -->

				<!-- 				<uni-swipe-action>
					<uni-swipe-action-item>
						<div class="tag_container">
							<div class="tag_container_name">
								<span class="name">意向客户</span>
								<span class="num">(9)</span>
							</div>
							<div class="tag_container_user">
								<div class="userList">
									阿萨大大啊水水水水水水水水水水水水水水水水水水水啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊反反复复反反复复反反复复方法方法分分分3
								</div>
							</div>
						</div>

						<template v-slot:right>
							<div class="delete_tags">
								<text>删除</text>
							</div>
						</template>
					</uni-swipe-action-item>
				</uni-swipe-action> -->

				<div class="tag_container" v-for="(item, index) in tagList" :key="index" @click="toTagDetils(item)">
					<div class="tag_container_name">
						<span class="name">{{item.label}}</span>
						<span class="num" v-if="item.user.length">({{ item.user.length }})</span>
					</div>
					<div class="tag_container_user">
						<div class="userList" v-if="item.user.length">
							{{item.user.map(item=> item.nickname).join('，')}}
						</div>
					</div>
				</div>
				
			</div>
		</lay-out>
	</div>
</template>

<script>
import { navigateTo} from 'pages/utils/uniApi.js';
import http from 'pages/api/index';
import api from 'pages/api/api.js';
export default {
	data() {
		return {
			userData: {},
			tagList: []
		};
	},
	onLoad(opt) {
		this.userData = opt;
	},
	onShow() {
		this.initData();
	},
	methods: {
		initData() {
			http(api.userLabe, { id: this.userData.user_id }).then(res => {
				console.log(res);
				this.tagList = res;
			})
		},
		toTagDetils(item) {
			this.$cache.get('userTagData', item);
			navigateTo(1, '/pages/view/setTags/addTag');
			
		},
		createdTag() {
			navigateTo(1, '/pages/view/setTags/addTag');
		}
	}
};
</script>

<style lang="less" scoped>
@import url('./less/setTags.less');
</style>
