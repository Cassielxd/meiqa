<template>
	<div class="container">
		<lay-out>
			<!-- 			<div slot="header" class="header">
				<div class="title_name">统计</div>

			</div> -->
			<div slot="content">
				<div class="statistics"></div>
				<div class="statistics_list">
					<div class="statistics_list_item">
						<div class="statistics_list_item_value">{{ statisticsAll.all }}</div>
						<div class="statistics_list_item_label">全部客户</div>
					</div>
					<div class="statistics_list_item">
						<div class="statistics_list_item_value">{{ statisticsAll.toDayKefu }}</div>
						<div class="statistics_list_item_label">今日客户</div>
					</div>
					<div class="statistics_list_item">
						<div class="statistics_list_item_value">{{ statisticsAll.month }}</div>
						<div class="statistics_list_item_label">本月客户</div>
					</div>
					<div class="statistics_list_item">
						<div class="statistics_list_item_value">{{ statisticsAll.toDayTourist }}</div>
						<div class="statistics_list_item_label">今日游客</div>
					</div>
				</div>

				<div class="content">
					<div class="content_charts">
						<div class="content_charts_title">
							<div class="content_charts_title_select">
								<scroll-view scroll-x class="content_charts_title_select_scroll">
									<view class="scroll_content">
										<view class="scroll_content_item" v-for="(item, index) in monthList" :key="index" @click="handleSelectMonth(item)">
											<div class="scroll_content_item_label" :class="{ selectEd: selectMonth == item.value }">{{ item.label }}</div>
											<div class="tip" v-if="selectMonth == item.value"></div>
										</view>
									</view>
								</scroll-view>
							</div>

							<div class="content_charts_title_date">
								<biaofun-datetime-picker
									class="positionPicker"
									:class="{ primaryColor: searchChartsData.value }"
									placeholder="请选择年月"
									:defaultValue="searchChartsData.value"
									start="2000"
									end="2100"
									fields="month"
									@change="changeDatetimePicker"
									:key="searchChartsData.value"
								></biaofun-datetime-picker>
								<!-- <span>{{searchChartsData.value}}</span> -->
								<span class="iconfont">&#xe6c5;</span>
							</div>
						</div>

						<div class="content_charts_value">
							<view class="charts-box">
								<div class="title">新增(人)</div>
								<qiun-data-charts type="area" :inScrollView="true" canvasId="scrolllineid" :opts="opts" :chartData="chartData" :ontouch="true" :canvas2d="true" />
							</view>
						</div>
						
					</div>
				</div>
			</div>
		</lay-out>
	</div>
</template>

<script>
import biaofunDatetimePicker from '@/components/biaofun-datetime-picker/biaofun-datetime-picker.vue';
import { formatDate } from 'pages/utils/uniApi.js';
import http from 'pages/api/index';
import api from 'pages/api/api.js';
export default {
	components: {
		biaofunDatetimePicker
	},
	data() {
		return {
			selectMonth: 'yesterday',
			chartData: {
				categories: [],
				series: [
					{
						name: '客户',
						data: [],
						color: '#3875EA',
						legendShape: 'square' // 长方形
					},
					{
						name: '游客',
						data: [],
						color: '#3FC7A9',
						legendShape: 'square'
					}
				]
			},
			opts: {
				enableScroll: true,
				extra: {
					area: { type: 'curve', addLine: true, gradient: true }
				},
				xAxis: {
					type: 'grid',
					gridType: 'solid',
					itemCount: 5, //x轴单屏显示数据的数量，默认为5个
					scrollShow: false, //新增是否显示滚动条，默认false
					scrollAlign: 'left', //滚动条初始位置
					scrollBackgroundColor: '#F7F7FF', //默认为 #EFEBEF
					scrollColor: '#DEE7F7' //默认为 #A6A6A6
				},
				legend: {
					show: true,
					position: 'top',
					float: 'right',
					padding: 2,
					margin: 20,
					backgroundColor: 'rgba(0,0,0,0)',
					borderColor: 'rgba(0,0,0,0)',
					borderWidth: 0,
					fontSize: 13,
					fontColor: '#666666',
					lineHeight: 11,
					hiddenColor: '#CECECE',
					itemGap: 10
				}
			},
			statisticsAll: {}, // 客户总统计数据
			searchChartsData: {
				year: String(new Date().getFullYear() + '-' + new Date().getMonth() + 1),
				month: new Date().getMonth() + 1,
				value: ''
			}
		};
	},
	computed: {
		monthList() {
			let arr = [
				{
					label: '昨天',
					value: 'yesterday'
				},
				{
					label: '今天',
					value: 'today'
				},
				{
					label: '7天',
					value: 'lately7'
				},
				{
					label: '30天',
					value: 'lately30'
				},
				{
					label: '半年',
					value: 'lately180'
				}
			];
			// for (let i = 1; i < new Date().getMonth(); i++) {
			// 	arr.push({
			// 		month: i,
			// 		label: i + '月'
			// 	});
			// }
			// let monthArr = [
			// 	{
			// 		label: '当月',
			// 		month: new Date().getMonth() + 1
			// 	},
			// 	{
			// 		label: '上月',
			// 		month: new Date().getMonth()
			// 	}
			// ];
			// arr.push(...monthArr);
			// arr.reverse();

			return arr;
		}
	},
	watch: {
		// searchChartsData: {
		// 	handler() {
		// 		this.getChartsData();
		// 	},
		// 	deep: true,
		// 	immediate: true
		// }
	},
	onShow() {
		// this.searchChartsData.value = formatDate(new Date().getTime(), 'yyyy-MM');
		// this.selectMonth = this.searchChartsData.month; // 自动选择本月
		this.initData();
		this.getChartsData();
	},
	methods: {
		// 获取数据总统计
		initData() {
			http(api.statisticsAll, {}).then(res => {
				this.statisticsAll = res;
			});
		},
		// 获取图标统计数据
		getChartsData() {
			let categories = [];
			http(api.statisticsIndex, { time: this.selectMonth }).then(res => {
				// 构建 X 轴数据
				// 将两条年份数据拼合在一起, 给对应的条目赋值
				// if (res.list.length) {
					res.list.forEach(item => {
						categories.push(item.time);
					});
					this.chartData.series[0].data = res.list.map(item => item.number);
				// }

				// if (res.tourist.length) {
					res.tourist.forEach(item => {
						categories.push(item.time);
					});
					this.chartData.series[1].data = res.tourist.map(item => item.number);
				// }
				// 数组去重
				categories = Array.from(new Set(categories));
				// 数组从小到大排序
				categories.sort((val1, val2) => {
					return Number(val1.replace(/-/g, '')) - Number(val2.replace(/-/g, ''));
				});

				this.chartData.categories = categories;
			});
		},
		// 选择年份
		changeDatetimePicker(e) {
			this.searchChartsData.value = formatDate(e.dt, 'yyyy/MM');
			this.selectMonth = this.searchChartsData.value;
			this.getChartsData();
		},
		// 选择月份
		handleSelectMonth(item) {
			this.selectMonth = item.value;
			this.searchChartsData.value = '';
			this.getChartsData();
		}
	}
};
</script>

<style lang="less" scoped>
@import url('./less/statistics.less');
</style>
