<template>
	<view class="container">
		<lay-out id='layOut' ref="layOut" :scrollTop="scrollTop" :scrollWithAnimation="false" @goTop="goTop"
			@layoutScroll="scroll">
			<!-- #ifdef H5 -->
			<view slot="header" class="header">
				<view class="header_left" @click="goBackToMessageList"><span class="iconfont icon-fanhui"></span></view>
				<view class="header_center">
					{{ userData.remark_nickname ? userData.remark_nickname : userData.nickname }}
				</view>
				<view class="header_right" @click="selectCustomerServerOfTopRight(userData)"><span
						class="iconfont">&#xe6d3;</span></view>
			</view>
			<!-- #endif -->
			<view slot="content" class="content" id="contentMessage">
				<view class="content_list" v-for="(item, index) in messageList" :key="index"
					:class="{ right_box: customerServerData.user_ids && customerServerData.user_ids.indexOf(item.user_id) !== -1 }">
					<view class="content_list_avar" @click="selectCustomerServer(item)">
						<image :src="item.avatar" mode="widthFix"></image>
					</view>
					<view class="content_list_message">
						<view class="content_list_message_value" v-if="item.msn_type <= 2"><text selectable="true"
								v-html="replace_em(item.msn)"></text></view>
						<view class="content_list_message_product" v-if="item.msn_type == 5">
							<view class="content_list_message_orderId">
								<span class="content_list_message_orderId_label">商品名称:</span>
								<span class="content_list_message_orderId_value">{{ item.other.store_name }}</span>
							</view>

							<view class="content_list_message_detils">
								<view class="content_list_message_detils_image">
									<image :src="item.other.image" mode="widthFix"></image>
								</view>
								<view class="content_list_message_detils_value">
									<view class="content_list_message_detils_value_shopMessag">
										<view class="content_list_message_detils_value_shopMessag_item">
											<view class="content_list_message_detils_value_shopMessag_item_label">
												<span>库存:</span>
											</view>
											<view class="content_list_message_detils_value_shopMessag_item_value">
												<span>{{ item.other.stock }}</span>
											</view>
										</view>
										<view class="content_list_message_detils_value_shopMessag_item">
											<view class="content_list_message_detils_value_shopMessag_item_label">
												<span>销量:</span>
											</view>
											<view class="content_list_message_detils_value_shopMessag_item_value">
												<span>{{ parseInt(item.other.sales) + parseInt(item.other.ficti ? item.other.ficti : 0) }}</span>
											</view>
										</view>
									</view>
									<view class="content_list_message_detils_value_shopPrice">
										<span>￥{{ item.other.price }}</span>
									</view>
								</view>
							</view>
						</view>

						<view class="content_list_message_image" v-if="item.msn_type == 3">
							<image @load="imageLoad" :src="item.msn" mode="widthFix" @click="showImage(item.msn)">
							</image>
						</view>
					</view>

					<view class="chat_result" v-if="item.user_id == customerServerData.user_id">
						<view class="chat_result_ing" v-if="item.is_send == 0">
							<!-- 正在发送 -->
							<view></view>
						</view>
						<view class="chat_result_faile" v-if="item.is_send == -1">
							<!-- 发送失败 -->
							<view>!</view>
						</view>
					</view>
				</view>
			</view>

			<view slot="bottom" class="footer" :class="{ translateY0: selectModel }">
				<view class="footer_input">
					<view class="footer_input_icon mr18" @click="selectScriptLibary"><span
							class="iconfont">&#xe6b8;</span></view>
					<view class="footer_input_content mr18">
						<textarea v-model="sendMessage" @input="textareaChange($event)" @focus="textareaFocus"
							class="font" value="" @confirm="sendText" auto-height confirm-type="send"
							:focus="sendMessageFocus" placeholder="" />
						<!-- <p v-html="pCont" class="font"></p> -->
						<!-- <span class="iconfont" :class="{ canSend: sendMessage }" @click="sendText">&#xe6bb;</span> -->
					</view>
					<view class="footer_input_icon">
						<span class="iconfont mr18" @click="selectOption(1)">&#xe6cb;</span>
						<span class="sendMessage" v-if="sendMessage" @click.stop="sendText">发送</span>
						<span class="iconfont" @click="selectOption(2)" v-if="!sendMessage">&#xe6c2;</span>
					</view>
				</view>

				<view class="footer_option" :style="{ height: selectModel ? '440rpx' : '0' }">
					<view class="option" v-if="selectModel == 2">
						<view @click="uploadImage">
							<image src="~static/image/messageList/picture.png" mode="widthFix"></image>
							<view>图片</view>
						</view>
						<view @click="transfer">
							<image src="~static/image/messageList/connection.png" mode="widthFix"></image>
							<view>转接</view>
						</view>
						<view @click="authReply">
							<image src="~static/images/auth-reply.png" mode="widthFix"></image>
							<view>自动回复</view>
						</view>
					</view>

					<scroll-view class="emoji" v-if="selectModel == 1" scroll-y>
						<view class="emoji-item" v-for="(item, index) in emoji" :key="index" @click="sendEmoji(item)"><i
								class="em" :class="item"></i></view>
					</scroll-view>
				</view>
			</view>
		</lay-out>

		<uni-popup ref="transferModel" animation type="bottom">
			<transfer @close="transferModelClose" :userInfo="userData" @handleSubmit="handleSubmitTransfer"></transfer>
		</uni-popup>

		<uni-popup ref="scriptLibaryModel" animation type="bottom">
			<view class="scriptLibary">
				<view class="scriptLibary_header">
					<view class="scriptLibary_header_title">
						<view class="tit" v-for="(item, index) in speechArtTypeList" :key="index" @click="selectSpeechArtType(item)"
							:class="{ speechArtSelectEd: speechArtType == item.id }">
							<span>{{ item.label }}</span>
						</view>
					</view>

					<view class="scriptLibary_header_icon">
						<view @click="editScript"><span class="iconfont icon_edit">&#xe6b4;</span></view>
						<view @click="closeScript"><span class="iconfont">&#xe6b5;</span></view>
					</view>
				</view>
				<view class="scriptLibary_search">
					<view class="search">
						<span @click="handleSetTitle" class="iconfont icon_search">&#xe6d8;</span>
						<input v-model="scriptTitle" type="text" placeholder="搜索快捷回复" />
					</view>
				</view>
				<view class="scriptLibary_content">
					<scripts-libary :tagList="speechTagList" :speechArtType="speechArtType" maxHeight="1000rpx"
						height="900rpx" :title="scriptTitle" @sendSpeechMessage="sendSpeechMessage"></scripts-libary>
				</view>
			</view>
		</uni-popup>

		<uni-popup ref="showImage" animation type="center">
			<view class="image_content">
				<span class="iconfont" @click="closeImage">&#xe6b5;</span>
				<image :src="selectMessageImage" mode="widthFix"></image>
			</view>
		</uni-popup>
	</view>
</template>

<script>
	import emoji from 'pages/utils/emoji.js';
	import {
		navigateBack,
		navigateTo,
		serialize,
		chooseImage,
		Toast,
		Modal
	} from 'pages/utils/uniApi.js';
	import transfer from './component/transfer.vue';
	import scriptsLibary from './component/scriptLibary.vue';
	import http from 'pages/api/index';
	import api from 'pages/api/api.js';
	import {
		mapGetters
	} from 'vuex';
	const app = getApp();
	export default {
		components: {
			transfer, // 转接客服
			scriptsLibary // 话术库
		},
		computed: mapGetters(['chatList', 'token']),
		data() {
			return {
				selectMessageImage: '', // 选中的图片
				sendMessageFocus: false,
				pCont: '',
				sendMessage: '', // 发送的消息
				selectModel: 0, // 1:选择表情 2: 功能选择（转接，上传图片）
				emoji: [],
				messageList: [],
				userData: {},
				pageData: {
					limit: 20,
					upperId: 0
				},
				speechArtType: 1, // 0 全局 1 私有
				speechTagList: [], // 话术分类
				scriptTitle: '', // 搜索输入的内容
				propstitle: '',
				speechArtTypeList: [{
						id: 1,
						label: '个人库'
					},
					{
						id: 0,
						label: '公共库'
					}
				],
				customerServerData: {
					user_ids: []
				},
				scrollTop: 0, // 滚动位置
				refresherTriggered: true, // 下拉刷新装状态
				audioFun: '', // 音频对象
				old: {
					scrollTop: 0
				},
				kefuInfo: {}, //当前客服信息
				userId: 0, //当前用户user_id
				toChatSend: false //是否发送过to_chat
			};
		},
		watch: {
			messageList: {
				deep: true,
				handler() {
					this.messageList.map((item, index) => {
						if (index) {
							if (item.add_time - this.messageList[index - 1].add_time >= 300) {
								item.show = true;
							} else {
								item.show = false;
							}
						} else {
							item.show = true;
						}
						return item;
					});
				}
			}
		},
		onLoad(opt) {
			this.emoji = emoji;
			this.userId = opt.to_user_id;
			if (!this.userId) {
				return navigateBack(1);
			}
			this.customerServerData = this.$store.getters.kefuInfo;
			console.log(this.customerServerData)
			this.kefuInfo = this.$store.getters.kefuInfo;
			this.messageList = this.$store.getters.chatLog(this.userId);
			this.userData = this.$store.getters.userInfo(this.userId);
			this.getUserInfo();
			this.getKefuInfo();
			this.initData();
			this.sockEventListenr();
			this.$store.dispatch('updataUserCount').then(res => {
				this.scoket.setBadgeNumber(res.count);
			});
		},
		onUnload() {
			console.log('聊天界面卸载');
			uni.$off('chat', this.chatEvent);
			uni.$off('chat_auth', this.chatAuthEvent);
			uni.$off('reply', this.replyEvent);
			uni.$off('success', this.successTochat);
			uni.$off('close', this.closeEvent);
			uni.$off('rm_transfer', this.rmTransferEvent);
			this.toChatSend = false;
			this.scoket.send({
				data: {
					id: 0
				},
				type: 'to_chat'
			});
		},
		// 监听，点击界面右上角的按钮
		onNavigationBarButtonTap(e) {
			this.selectCustomerServerOfTopRight();
		},

		methods: {
			authReply() {
				navigateTo(1, '/pages/view/authReply/index');
			},
			getKefuInfo() {
				this.$store.dispatch('getKeufuInfo').then(res => {
					this.kefuInfo = res;
				});
			},
			//获取用户信息
			getUserInfo() {
				this.$store
					.dispatch('getUserInfo', this.userId)
					.then(res => {
						this.userData = res; // 获取用户信息
						uni.setNavigationBarTitle({
							title: this.userData.remark_nickname ? this.userData.remark_nickname : this
								.userData.nickname
						});
					})
					.catch(err => {
						uni.setNavigationBarTitle({
							title: this.userData.remark_nickname ? this.userData.remark_nickname : this
								.userData.nickname
						});
					});
			},
			scroll(value) {
				this.old.scrollTop = value;
			},
			successTochat() {
				console.log(this.toChatSend);
				//没发过在发送to_chat
				if (!this.toChatSend) {
					this.scoket.send({
						data: {
							id: this.userId
						},
						type: 'to_chat'
					});
					this.toChatSend = true;
				}
			},
			replyEvent(data) {
				this.pushMessageToList(data);
			},
			chatAuthEvent(data) {
				if (data.length) {
					data.map(item => {
						item.msn = this.replace_em(item.msn);
						this.pushMessageToList(item);
					})
				}
			},
			chatEvent(data) {
				this.messageList.map(item => {
					if (item.guid === data.guid) {
						item.is_send = 1;
					}
				});
				this.$store.commit('delChatList', {
					id: this.userId,
					guid: data.guid
				});
			},
			closeEvent() {
				this.toChatSend = false;
			},
			rmTransferEvent(data) {
				if (data.recored.to_user_id == this.userId) {
					navigateBack(1);
				}
			},
			// socket监听
			sockEventListenr() {
				if (!this.toChatSend && this.scoket.connectStatus) {
					this.scoket.send({
						data: {
							id: this.userId
						},
						type: 'to_chat'
					});
					this.toChatSend = true;
				}
				uni.$on('success', this.successTochat);
				uni.$on('chat', this.chatEvent);
				uni.$on('chat_auth', this.chatAuthEvent);
				uni.$on('reply', this.replyEvent);
				uni.$on('close', this.closeEvent);
				uni.$on('rm_transfer', this.rmTransferEvent);
			},

			// 获取聊天列表
			initData() {
				this.$store
					.dispatch('getChatLogList', {
						user_id: this.userId,
						pageData: this.pageData
					})
					.then(res => {
						let upperId = this.pageData.upperId;
						this.messageList = upperId === 0 ? res : res.concat(this.messageList);
						this.pageData.upperId = this.messageList.length ? this.messageList[0].id : 0;
						if (this.chatList[this.userId]) {
							this.chatList[this.userId].map(item => {
								this.messageList.push(item);
							});
						}
						if (upperId === 0) {
							this.$nextTick(() => {
								this.goBottom();
							});
						}
					})
					.catch(err => {
						if (this.chatList[this.userId]) {
							this.chatList[this.userId].map(item => {
								this.messageList.push(item);
							});
						}
						if (upperId === 0) {
							this.$nextTick(() => {
								this.goBottom();
							});
						}
					});
			},
			// 滑动到顶部
			goTop() {
				this.refresherTriggered = true;
				this.initData();
			},

			// 发送文本消息
			sendText(e) {
				if (this.sendMessage) {
					// this.sendMessageFocus = true;
					this.sendMsg(this.sendMessage, 1);
					this.sendMessage = '';
				} else {
					return;
					// Toast('请输入要发送的消息');
				}
			},
			// 选择表情
			sendEmoji(item) {
				this.sendMessage += `[${item}]`;
			},
			// 发送图片
			uploadImage() {
				let that = this;
				chooseImage(1).then(res => {
					//#ifndef H5
					uni.compressImage({
						src: res.tempFilePaths[0],
						width: 'auto',
						height: 'auto',
						success(res) {
							that.upload(res.tempFilePath);
						},
						fail(e) {
							console.log(e);
							that.upload(res.tempFilePaths[0]);
						}
					});
					//#endif
					//#ifdef H5
					that.upload(res.tempFilePaths[0]);
					//#endif
				});
			},
			upload(tempFilePath) {
				uni.uploadFile({
					url: app.globalData.http + api.kefuUploadAvatar.url,
					filePath: tempFilePath,
					name: 'file',
					formData: {
						// file: res.tempFilePaths[0],
						filename: 'file'
					},
					header: {
						// #ifdef MP || APP-PLUS
						'Content-Type': 'multipart/form-data',
						// #endif
						'Authori-zation': 'Bearer ' + this.$store.state.token
					},
					success: uploadFileRes => {
						let unloadResponse = JSON.parse(uploadFileRes.data);
						console.log(unloadResponse);
						if (unloadResponse.status == 200) {
							this.sendMsg(unloadResponse.data.url, 3);
						}
					},
					fail: res => {
						Toast('发送图片失败');
					},
					complete: res => {
						console.log(res);
					}
				});
			},
			pushMessageToList(data) {
				this.messageList.push(data);
				this.$nextTick(() => {
					this.goBottom();
				});
			},
			imageLoad() {

			},
			goBottom() {
				this.$nextTick(() => {
					const query = uni.createSelectorQuery().in(this);
					query
						.select('#contentMessage')
						.boundingClientRect(data => {
							this.scrollTop = this.old.scrollTop;
							this.scrollTop = data.height + 50;
							console.log(data, this.scrollTop);

						})
						.exec();
				});
			},
			// textarea获取焦点
			textareaFocus() {
				this.selectModel = 0;
				this.$nextTick(() => {
					this.goBottom();
				});
			},
			chatOptinos(guid, msn, type, other) {
				return {
					msn,
					msn_type: type,
					to_user_id: this.userId,
					is_send: 0,
					is_tourist: 0,
					avatar: this.kefuInfo.avatar,
					user_id: this.kefuInfo.user_id,
					appid: this.kefuInfo.appid,
					other: other || {},
					type: 0,
					guid
				};
			},
			// 发送消息统一处理
			sendMsg(msn, type) {
				let guid = this.scoket.guid();
				let chat = this.chatOptinos(guid, msn, type);
				this.$store.commit('setChatList', {
					id: this.userId,
					list: chat
				});
				chat.msn = this.replace_em(msn);
				this.pushMessageToList(chat);
				http(api.sendMessage, {
					guid,
					msn,
					msn_type: type,
					to_user_id: this.userId,
				}).then(data => {
					this.chatEvent({
						guid: data.guid
					});
				})

			},
			selectScriptLibary() {
				this.$refs.scriptLibaryModel.open();
			},
			// 选择话术库，个人，工共
			selectSpeechArtType(item) {
				this.speechArtType = item.id;
				this.selectScriptLibary();
			},
			// 选中话术
			sendSpeechMessage(item) {
				this.sendMessage = item.message;
				this.$refs.scriptLibaryModel.close(); // 关闭话术库弹框
			},
			// 关闭话术弹框
			closeScript() {
				this.$refs.scriptLibaryModel.close();
			},
			// 修改话术库
			editScript() {
				this.$refs.scriptLibaryModel.close();
				navigateTo(1, '/pages/view/customerServer/script', {
					speechArtType: this.speechArtType
				});
			},
			// 话术搜索
			handleSetTitle() {
				this.propstitle = serialize(this.scriptTitle);
			},
			// 转接
			transfer() {
				this.selectModel = 0;
				this.$refs.transferModel.open();
			},
			// 确认转接
			handleSubmitTransfer() {
				this.$refs.transferModel.close();
				navigateTo(4, '/pages/view/messageList/index');
			},
			// 关闭转接弹框
			transferModelClose() {
				this.$refs.transferModel.close();
			},
			// 头部方法，回到聊天列表页
			goBackToMessageList() {
				navigateTo(4, '/pages/view/messageList/index');
			},
			// 查看用户详情
			selectCustomerServer(item) {
				if (this.customerServerData.user_ids && this.customerServerData.user_ids.indexOf(item.user_id) === -1) {
					navigateTo(1, '/pages/view/customerServer/customerMessage', item);
				}
			},
			selectCustomerServerOfTopRight() {
				navigateTo(1, '/pages/view/customerServer/customerMessage', {
					user_id: this.userId
				});
			},
			// 选择上传图片，转接
			selectOption(item) {
				this.selectModel = item;
			},
			// 聊天表情转换
			replace_em(str) {
				str = str.replace(/\[em-([\s\S]*)\]/g, "<span class='em em-$1'/></span>");
				return str;
			},
			textareaChange(e) {
				let strCont = e.target.value.replace(/\n\s(\s)*/gi, '\n'); // 将多个回车换行合并为 1个
				strCont = strCont.replace(/^\s*/gi, ''); // 清除首行的 空格与换行
				let strHtml = strCont.replace(/</gi, '&lt;'); // 将所有的 < 转义为 &lt; 防止html标签被转义
				strHtml = strCont.replace(/\n(\n)*/gi, '<br>'); // 回车换行替换为 <br>
				strHtml = strHtml.replace(/\s+/gi, '&nbsp;'); // 一个或过个空格 替换为 &nbsp;
				strCont = strHtml.replace(/&nbsp;/gi, ' '); // 逆向处理
				strCont = strCont.replace(/<br>/gi, '\n'); // 逆向处理
				strCont = strCont.replace(/&lt;/gi, '<');
				/** 如果 p 标签最后的字符为 <br> 并不会单独另起一行, 会导致与 textarea 的高度相差一行,
				 * 所以在最后添加一个字符, 这样就能保证 P 标签的高度与 textarea 的高度一致
				 */
				this.pCont = strHtml + '.';
			},
			// 点击聊天内容
			// 点击图片
			showImage(item) {
				console.log(item);
				this.selectMessageImage = item;
				this.$refs.showImage.open();
			},
			closeImage() {
				this.$refs.showImage.close();
			}
		}
	};
</script>

<style scoped lang="less">
	@import './less/customerServer.less';
	@import './less/scriptLibary.less';
</style>
