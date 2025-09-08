<template>
	<view>
		<!-- 右边字母选择导航条 -->
		<view class="searchLetter touchClass" :style="{ height: itemH * searchLetter.length + 'px', paddingRight: paddingRight + 'upx', width: searchLetterWidth + 'upx' }">
			<view
				v-for="(item, index) in searchLetter"
				:style="{ height: itemH + 'px' }"
				:key="index"
				:data-letter="item.name"
				@touchstart.stop.prevent="searchStart"
				@touchmove.stop.prevent="searchMove"
				@touchend.stop.prevent="searchEnd"
			>
				{{ item.name }}
			</view>
		</view>

		<!-- 字母选择提示框 -->
		<block v-if="isShowLetter">
			<view class="showSlectedLetter">{{ showLetter }}</view>
		</block>

		<!-- 通讯录列表 -->
		<scroll-view scroll-y :scrollTop="scrollTop"><slot name="addressBookList"></slot></scroll-view>
	</view>
</template>

<script>
export default {
	name: 'addressBookNavigation',
	props: {
		//通讯录名字集合
		bookList: {
			type: Object,
			default() {
				return {};
			},
			required: true
		},
		//索引集合
		letter: {
			type: Array,
			default() {
				return ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];
			}
		},
		//通讯录索引字母显示高度 upx
		bookKeyHeight: {
			type: Number,
			default() {
				return 100;
			}
		},
		//通讯录名字显示高度 upx
		bookItemHeight: {
			type: Number,
			default() {
				return 40;
			}
		},
		//右边索引条右边距 upx
		paddingRight: {
			type: Number,
			default() {
				return 20;
			}
		},
		//若有搜索框则是搜索框高度 upx
		searchHeight: {
			type: Number,
			default() {
				return 20;
			}
		},
		//右边索引条距离搜索框位置 upx
		searchLetterTop: {
			type: Number,
			default() {
				return 60;
			}
		},
		//右边索引条宽度 upx
		searchLetterWidth: {
			type: Number,
			default() {
				return 40;
			}
		},
		//减去的右边导航条高度 upx
		subtractiveSearchLetterHeight: {
			type: Number,
			default() {
				return 80;
			}
		},
		//底部tab高度  使用原生导航栏固定为50px  自定义tab传入自定义tab高度  单位为px
		tabHeight: {
			type: Number,
			default() {
				return 100;
			}
		},
		//导航栏高度
		navHeight: {
			type: Number,
			default() {
				return 44;
			}
		}
	},
	data() {
		return {
			searchLetter: [],
			//右边索引条单个字母元素高度
			itemH: 0,
			//可滑动区域高度
			usableHeight: 0,
			screenWidth: 0,
			isShowLetter: false,
			showLetter: '',
			scrollTop: 0,
			badPageY: uni.upx2px(this.searchHeight + this.searchLetterTop),
			letterPositionTop: 0,
			scrollViewPositionTop: 0
		};
	},
	mounted() {
		//#ifdef H5
		this.letterPositionTop = this.searchHeight + this.searchLetterTop + 88;
		this.scrollViewPositionTop = this.searchHeight + 88;
		//#endif
		//#ifndef H5
		this.letterPositionTop = this.searchHeight + this.searchLetterTop;
		this.scrollViewPositionTop = this.searchHeight;
		//#endif
		// #ifdef APP-PLUS
		this.letterPositionTop = this.searchHeight + this.searchLetterTop;
		this.scrollViewPositionTop = this.searchHeight + 260;
		console.log(1);
		// #endif
		this.countLetterHeight();
	},
	methods: {
		//计算和记录右边每个搜索字母的高度和位置
		countLetterHeight() {
			const sysInfo = uni.getSystemInfoSync();
			//#ifdef H5
			const usableHeight = sysInfo.screenHeight - uni.upx2px(this.searchHeight) - this.tabHeight - this.navHeight;
			//#endif
			//#ifndef H5
			const usableHeight = sysInfo.screenHeight - uni.upx2px(this.searchHeight) - this.tabHeight - this.navHeight - sysInfo.statusBarHeight;
			//#endif
			this.screenWidth = sysInfo.screenWidth;
			const letter = this.letter;
			const itemH = (usableHeight - uni.upx2px(this.subtractiveSearchLetterHeight) - 20) / letter.length;
			const tempObj = [];
			for (let i = 0; i < letter.length; i++) {
				tempObj.push({
					name: letter[i],
					tHeight: i * itemH,
					bHeight: (i + 1) * itemH
				});
			}
			this.usableHeight = usableHeight;
			this.itemH = itemH;
			this.searchLetter = tempObj;
		},
		searchEnd(e) {
			const that = this;
			setTimeout(() => (that.isShowLetter = false), 500);
		},
		//点击选择
		searchStart(e) {
			console.log(e);
			const pageY = e.touches[0].pageY - this.badPageY;
			this.nowLetter(pageY);
		},
		//滑动选择
		searchMove(e) {
			const pageY = e.touches[0].pageY - this.badPageY;
			const pageX = e.touches[0].pageX;
			if (this.screenWidth - pageX > uni.upx2px(this.searchLetterWidth + this.paddingRight)) {
				this.isShowLetter = false;
				return;
			}
			if (pageY < 0 || pageY > this.itemH * this.searchLetter.length) {
				this.isShowLetter = false;
				return;
			}
			this.nowLetter(pageY);
		},
		//计算当前选中的字母并显示
		nowLetter(pageY) {
			const letterData = this.searchLetter;
			for (let i = 0; i < letterData.length; i++) {
				if (letterData[i].tHeight <= pageY && pageY <= letterData[i].bHeight) {
					this.showLetter = letterData[i].name;
					this.isShowLetter = true;
					break;
				}
			}
			this.scrollPage(this.showLetter);
		},
		//计算要滑动的距离
		countScrollTop(showLetter) {
			let scrollTop = 0;
			let cityCount = 0;
			let initialCount = 0;
			for (let item of Object.keys(this.bookList)) {
				if (showLetter == item) {
					scrollTop = initialCount * uni.upx2px(this.bookKeyHeight) + cityCount * uni.upx2px(this.bookItemHeight);
					break;
				} else {
					initialCount++;
					cityCount += this.bookList[item].length;
				}
			}
			return scrollTop;
		},
		//页面滑动到与选中字母对应的位置
		scrollPage(showLetter) {
			const scroll = this.countScrollTop(showLetter);
			if (scroll != 0 || showLetter == Object.keys(this.bookList)[0]) this.scrollTop = scroll;
		}
	}
};
</script>

<style lang="less" scoped>
.searchLetter {
	position: fixed;
	top: 126rpx;
	right: 0;
	text-align: center;
	display: flex;
	flex-direction: column;
	color: #666;
	z-index: 999;
}
.touchClass {
	color: #5a5a5a;
	font-size: 28upx;
}

.showSlectedLetter {
	background-color: rgba(0, 0, 0, 0.5);
	display: flex;
	justify-content: center;
	align-items: center;
	position: fixed;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	width: 100upx;
	height: 100upx;
	border-radius: 10upx;
	font-size: 44upx;
	z-index: 99;
	color: #fff;
}
scroll-view {
	// position: fixed;
	width: 100%;
}
</style>
