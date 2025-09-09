<template>
	<div class="container">
		<lay-out  headerColor="#3A3A3A" headerTextColor="#fff">
			<div slot="content" class="content">
				<div  class="header_banner">
					<div class="header_banner_content">
						<div class="header_banner_content_title">
							<span>feedback</span>
						</div>
						<div class="header_banner_content_message">
							<span>Dear, you have other opinions about the application or you receive</span>
						</div>
					</div>
					<div class="header_banner_image">
						<div class="header_banner_image_imageBox">
							<image src="~static/image/feedback/bg.png" mode="widthFix"></image>
						</div>
					</div>
				</div>
	
				<div class="feedback">
					<div class="feedback_title">
						<span>i want to feedback</span>
					</div>
					<div class="feedback_input">
						<input v-model="feedBackData.rela_name" type="text" value="" placeholder="please input your name"/>
					</div>
					<div class="feedback_input">
						<input v-model="feedBackData.phone" type="text" value="" placeholder="please input your phone number" />
					</div>
					<div class="feedback_textarea">
						<textarea v-model="feedBackData.content" value="" placeholder="please input your feedback content" />
					</div>
					
					<div class="handle_button" @click="handleSubmit">
						<span>submit</span>
					</div>
				</div>
			
			</div>			
		</lay-out>
		
		<uni-popup ref="feedbackReslove" animation>
			<submit-response @handleSubmitRes="handleSubmitRes"></submit-response>
		</uni-popup>
		
		
	</div>
</template>

<script>
	import {navigateBack} from 'pages/utils/uniApi.js'
	import submitResponse from './component/submitResponse.vue';
	import http from 'pages/api/index';
	import api from 'pages/api/api.js';
	export default {
		components: {
			submitResponse
		},
		data() {
			return {
				feedBackData: {
					rela_name: '',
					phone: '',
					content: ''
				}
			}
		},
		onLoad() {
		},
		methods: {
			handleSubmit() {
				http(api.userFeedback, this.feedBackData).then(res => {
					this.$refs.feedbackReslove.open('center');
				})
			},
			handleSubmitRes() {
				navigateBack(1);
				this.$refs.feedbackReslove.close();
			}
		}
	}
</script>

<style lang="less" scoped>
	@import url('./less/feedback.less');
</style>
