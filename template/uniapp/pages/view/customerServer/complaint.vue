<template>
	<div class="container">
		<div class="header_banner">
			<div class="header_banner_content">
				<div class="header_banner_content_title"><span>提建议或举报不良内容</span></div>
				<div class="header_banner_content_message">
					<span>亲，若您收到不良信息，请及时反馈，我们非常重视您给我们提出宝贵的建议，帮助我们不断完善产品，谢谢！</span></div>
			</div>
			<div class="header_banner_image">
				<div class="header_banner_image_imageBox">
					<image src="~static/image/feedback/bg.png" mode="widthFix"></image>
				</div>
			</div>
		</div>
		<!-- 			<div class="organization_work">
					
					<div class="organization_work_item">
						<div class="organization_work_item_label">
							<span>09:00~22:00</span>
						</div>
						<div class="organization_work_item_value">
							<span>我们的工作时间</span>
						</div>
					</div>
					<div class="tip"></div>
					<div class="organization_work_item">
						<div class="organization_work_item_label">
							<span>400-8888-794</span>
						</div>
						<div class="organization_work_item_value">
							<span>售前客服电话</span>
						</div>
					</div>
					
				</div> -->

		<div class="feedback">
			<div class="feedback_title"><span>我要投诉</span></div>

			<div class="feedback_list">
				<div class="feedback_list_item" v-for="(item, index) in feedbackList" :key="index"
					@click="selectItem(item)">
					<div>
						<div :class="{ feedback_list_item_selected: item.selected }"></div>
					</div>
					<div>{{ item.name }}</div>
				</div>
			</div>

			<!-- <div class="feedback_input">
						<input v-model="feedBackData.rela_name" type="text" value="" placeholder="请输入您的姓名"/>
					</div>
					<div class="feedback_input">
						<input v-model="feedBackData.phone" type="text" value="" placeholder="请输入您的联系电话" />
					</div> -->

			<div class="feedback_textarea"><textarea v-model="feedBackData.content" value="" placeholder="请填写留言内容" />
			</div>

			<div class="handle_button" @click="handleSubmit"><span>提交</span></div>
		</div>

		<uni-popup ref="feedbackReslove" animation>
			<submit-response @handleSubmitRes="handleSubmitRes"></submit-response>
		</uni-popup>
	</div>
</template>

<script>
	import {
		navigateBack
	} from 'pages/utils/uniApi.js';
	import submitResponse from '../feedback/component/submitResponse.vue';
	import http from 'pages/api/index';
	import api from 'pages/api/api.js';
	export default {
		components: {
			submitResponse
		},
		data() {
			return {
				feedBackData: {
					content: '',
					user_id: '',
					cate_id: []
				},
				feedbackList: [],
				userId: 0,
			};
		},
		onLoad(option) {
			this.userId = option.id || 0;
		},
		onShow() {
			this.getUserComplain();
		},
		methods: {
			// 获取投诉内容列表
			getUserComplain() {
				http(api.userComplain).then(res => {
					this.feedbackList = res;
					this.feedbackList.forEach(item => {
						this.$set(item, 'selected', false);
					});
				});
			},
			// 选择投诉项目
			selectItem(item) {
				item.selected = !item.selected;
			},
			handleSubmit() {
				this.feedBackData.cate_id = this.feedbackList.filter(item => item.selected).map(val => val.id);
				this.feedBackData.user_id = this.userId;

				http(api.postUserComplain, this.feedBackData).then(res => {
					this.$refs.feedbackReslove.open('center');
				});
			},
			handleSubmitRes() {
				navigateBack(1);
				this.$refs.feedbackReslove.close();
			}
		}
	};
</script>

<style lang="less" scoped>
	@import url('../feedback/less/feedback.less');
</style>
