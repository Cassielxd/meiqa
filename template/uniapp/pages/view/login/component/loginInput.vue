<template>
	<div class="login_input">
		<div class="title_icon">
			<slot name="icon"></slot>
		</div>
		<div class="input">
			<input  class="input_model" :placeholder="placeholder" :type="type" v-model="value"  @blur="$emit('input', $event.target.value)" />
		</div>
		<div class="clear" v-if="value" @click="clear">
			<span class="iconfont">&#xe6b5;</span>
		</div>
	</div>
</template>

<script>
export default {
	model: {
		prop: 'value',
		event: 'input'
	},
	props: {
		placeholder: {
			type: String,
			default:''
		},
		type: {
			type: String,
			default: 'text'
		}
	},
	data() {
		return {
			value: ''
		};
	},
	methods:{
		clear() {
			this.value = '';
			this.$emit('input', '');
		}
	}
};
</script>

<style scoped lang="less"> 
.login_input {
	display: flex;
	align-items: center;
	padding: 22rpx 0;
	border-bottom: 1px solid #EDEDED;
	font-size: 14px;
	.title_icon {
		margin-right: 12rpx;
		color: @primaryColor;
	}
	.input {
		flex: 1;
		&_model {
			border: none !important;
			outline: none !important;
		}
	
	}
	.clear {
		opacity: .2;
	}
}
</style>
