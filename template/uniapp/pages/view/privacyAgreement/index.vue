<template>
	<view class="agreement">
		<view class="content" v-html="agreement"></view>
	</view>
</template>

<script>
import api from 'pages/api/api.js';
import http from 'pages/api/index';
export default {
	data() {
		return {
			agreement: ''
		};
	},
	mounted() {
		this.getAgreement();
	},
	methods: {
		getAgreement() {
			http(api.userAgreement, {}).then(res => {
				console.log(res);
				this.agreement = res.content;
			});
		}
	}
};
</script>
<style>
.agreement .content {
	margin: 10rpx;
}
</style>
