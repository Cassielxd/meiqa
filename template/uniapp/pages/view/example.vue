<template>
	<div class="container">
		<lay-out>
			<div slot="content" class="content">
					<div v-for="(item, index) in 100" :key="index">{{index}}</div>
			</div>
			<div slot="bottom">asdad</div>
		</lay-out>
	</div>
</template>

<script>
import { navigateTo, formatDate, serialize, showWeekFirstDay, showMonthFirstDay, getQuarterStartDate } from 'pages/utils/uniApi.js';
export default {
	components: {
	},
	data() {
		return {
	
		}
	},
	onLoad(opt) {
	
	},
	onShow(opt) {
	
	},
	methods: {		
	}
};
</script>

<style lang="less" scoped>

</style>
