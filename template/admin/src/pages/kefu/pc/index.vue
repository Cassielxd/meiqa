<template>
  <div class="kefu-layouts">
    <div class="content-wrapper">
      <baseHeader :kefuInfo="kefuInfo" :online="online" @setOnline="setOnline"></baseHeader>
      <div class="container">
        <chatList ref="chatList" @setDataId="setDataId" @search="bindSearch" @changeType="changeType" :isShow="isShow" :userOnline="userOnline" :newRecored="newRecored" :searchData="searchData"></chatList>
        <div class="chat-content">
          <!-- ⭐ Referer 来源显示区域 -->
          <div class="referer-bar" v-if="userActive && userActive.referer">
            <span class="referer-label">{{$t('kefu.referer')}}:</span>
            <span class="referer-value" :title="userActive.referer">
              {{ userActive.referer }}
            </span>
          </div>

          <!-- ⭐ Request URL 显示区域 -->
          <div class="referer-bar" v-if="userActive && userActive.request_url">
            <span class="referer-label">{{$t('kefu.requestUrl')}}:</span>
            <span class="referer-value" :title="userActive.request_url">
              {{ userActive.request_url}}
            </span>
          </div>

          <div class="chat-body">

            <happy-scroll size="5" resize hide-horizontal :scroll-top="scrollTop" @vertical-start="scrollHandler">
              <div style="width: 570px; padding:20px;" id="chat_scroll" ref="scrollBox">
                <Spin v-show="isLoad">
                  <Icon type="ios-loading" size=18 class="demo-spin-icon-load"></Icon>
                  <div>{{$t('kefu.loading')}}</div>
                </Spin>
                <div class="chat-item" v-for="(item,index) in records" :key="index" :class="[{'right-box':isKefuMessage(item)},{'gary':item.msn_type==5}]" :id="`chat_${item.id}`">
                  <div class="time" v-show="item.show">{{item.time }}</div>
                  <div class="flex-box">
                    <div class="avatar">
                      <img v-lazy="item.avatar" alt="">
                    </div>
                    <div class="msg-content-wrapper">
                      <!-- ⭐ 客服名称显示在右下角 -->
                      <div class="kefu-name-tag" v-if="isKefuMessage(item)">
                        {{ item.nickname || '客服' }}
                      </div>
                      <div class="msg-wrapper">
                        <!-- ⭐ 新增：显示其他客服的昵称 -->
                        <div class="sender-name" v-if="isOtherKefu(item)">
                          {{ item.nickname || '客服' }}
                        </div>
                        <!-- 文档 -->
                        <template v-if="item.msn_type<=2">
                          <div class="txt-wrapper pad16" :class="{'has-translate': !isKefuMessage(item)}" v-html="item.msn"></div>
                          <!-- ⭐ 翻译按钮 - 只对游客消息显示，绝对定位在消息右上角 -->
                          <button
                            v-if="!isKefuMessage(item)"
                            class="translate-btn-icon"
                            @click.stop="handleTranslate(item)"
                            :disabled="item.translating"
                            :title="item.translated ? (item.showTranslation ? '显示原文' : '显示译文') : '翻译'"
                          >
                            <Icon v-if="item.translating" type="ios-loading" class="translate-loading-icon" />
                            <span v-else-if="item.translated && item.showTranslation">🔄</span>
                            <span v-else>🌐</span>
                          </button>
                        </template>
                        <!-- 图片 -->
                        <template v-if="item.msn_type==3">
                          <div class="img-wraper" v-viewer>
                            <img v-lazy="item.msn" alt="">
                          </div>
                        </template>
                        <!-- 商品 -->

                        <template v-if="item.msn_type==5">
                          <div class="order-wrapper pad16">
                            <div class="img-box"><img :src="item.other.image" alt=""></div>
                            <div class="order-info">
                              <div class="name line1">{{item.other.store_name}}</div>
                              <div class="sku">{{$t('kefu.inventory')}}：{{item.other.stock}} {{$t('kefu.sales')}}：{{parseInt(item.other.sales) + parseInt(item.other.ficti?item.other.ficti:0)}}</div>
                              <div class="price-box">
                                <div class="num">¥ {{item.other.price}}</div>
                                <!-- <a herf="javascript:;" class="more" @click.stop="lookGoods(item)">View goods ></a> -->
                              </div>
                            </div>

                          </div>
                        </template>
                        <!-- 订单 -->
                        <template v-if="item.msn_type==6 && (item.orderInfo.length>0||item.orderInfo.id)">
                          <div class="order-wrapper pad16">
                            <div class="img-box"><img :src="item.orderInfo.cartInfo[0].productInfo.image" alt=""></div>
                            <div class="order-info">
                              <div class="name line1">{{item.orderInfo.order_id}}</div>
                              <div class="sku">{{$t('kefu.goodsQuantity')}}：{{item.orderInfo.total_num}}</div>
                              <div class="price-box">
                                <div class="num">¥ {{item.orderInfo.pay_price}}</div>
                                <a href="javascript:;" class="more" @click.stop="lookOrder(item)">{{$t('kefu.viewOrder')}} ></a>
                              </div>
                            </div>

                          </div>
                        </template>

                      </div>

                      <!-- ⭐ 翻译结果显示区域 - 只对游客消息显示 -->
                      <transition name="translate-fade">
                        <div class="translate-result" v-if="!isKefuMessage(item) && item.translated && item.showTranslation">
                          <div class="translate-result-content" v-html="item.translatedText"></div>
                        </div>
                      </transition>
                    </div>

                  </div>
                </div>
              </div>
            </happy-scroll>
          </div>

          <div class="chat-textarea">
            <div class="chat-btn-wrapper">
              <div class="left-wrapper">
                <div class="icon-item" @click.stop="isEmoji = !isEmoji"><span class="iconfont iconbiaoqing1"></span></div>
                <div class="icon-item">
                  <Upload :show-upload-list="false" :headers="header" :data="uploadData" :on-success="handleSuccess" :format="['jpg','jpeg','png','gif']" :on-format-error="handleFormatError" :action="upload">
                    <span class="iconfont icontupian1"></span>
                  </Upload>
                </div>
                <div class="icon-item" @click.stop.stop="isMsg = true"><span class="iconfont iconliaotian"></span></div>
                <div class="icon-item" @click.stop.stop="authMsg = true"><Icon style="font-weight: bold" size="22" color="#515a6e" type="ios-chatboxes-outline" /></div>
                <!-- ⭐ 翻译设置按钮 -->
                <div class="icon-item translate-setting-btn" @click.stop="isTranslateSetting = !isTranslateSetting" :title="$t('kefu.translateSetting')">
                  <Icon type="ios-globe" size="22" color="#515a6e" />
                </div>
              </div>
              <div class="right-wrapper">
                <div class="icon-item" @click.stop="isTransfer = !isTransfer">
                  <span class="iconfont iconzhuanjie"></span>
                  <span>{{$t('kefu.transfer')}}</span>
                </div>
                <div class="transfer-box" v-if="isTransfer">
                  <transfer ref="transfer" @transferSuccess="transferSuccess" @close="msgClose" @transferPeople="transferPeople" :userUid="userActive.user_id"></transfer>
                </div>
                <div class="transfer-bg" v-if="isTransfer" @click.stop="isTransfer = false"></div>
              </div>
              <!-- 表情 -->
              <div class="emoji-box" v-show="isEmoji">
                <div class="emoji-item" v-for="(emoji, index) in emojiList" :key="index">
                  <i class="em" :class="emoji" @click.stop="select(emoji)"></i>
                </div>
              </div>
              <!-- ⭐ 翻译设置弹窗 -->
              <div class="translate-setting-box" v-show="isTranslateSetting" @click.stop>
                <div class="translate-setting-header">
                  <span>{{$t('kefu.translateSetting')}}</span>
                </div>
                <div class="translate-setting-content">
                  <div class="setting-item">
                    <label>{{$t('kefu.sourceLanguage')}}:</label>
                    <Select v-model="translateConfig.sourceLang" style="width: 150px" size="small" @click.stop>
                      <Option value="auto">{{$t('kefu.autoDetect')}}</Option>
                      <Option value="en-US">{{$t('kefu.english')}}</Option>
                      <Option value="zh-CN">{{$t('kefu.chineseSimplified')}}</Option>
                      <Option value="zh-TW">{{$t('kefu.chineseTraditional')}}</Option>
                      <Option value="ja-JP">{{$t('kefu.japanese')}}</Option>
                      <Option value="ko-KR">{{$t('kefu.korean')}}</Option>
                    </Select>
                  </div>
                  <div class="setting-item">
                    <label>{{$t('kefu.targetLanguage')}}:</label>
                    <Select v-model="translateConfig.targetLang" style="width: 150px" size="small" @click.stop>
                      <Option value="zh-CN">{{$t('kefu.chineseSimplified')}}</Option>
                      <Option value="en-US">{{$t('kefu.english')}}</Option>
                      <Option value="zh-TW">{{$t('kefu.chineseTraditional')}}</Option>
                      <Option value="ja-JP">{{$t('kefu.japanese')}}</Option>
                      <Option value="ko-KR">{{$t('kefu.korean')}}</Option>
                    </Select>
                  </div>
                  <div class="setting-item">
                    <Button type="primary" size="small" @click.stop="saveTranslateSetting">{{$t('kefu.save')}}</Button>
                  </div>
                </div>
              </div>
            </div>
            <div class="textarea-box" style="position:relative;">
              <!-- <Input v-model="chatCon" type="textarea" :rows="4" @keydown.enter="sendText" placeholder="Please enter text content" @on-enter="sendText" style="font-size:14px" /> -->
              <div ref="editable" class="editable" contenteditable="true" :data-placeholder="'输入消息，按 Enter 发送'" @keydown.enter="sendText" @keydown="handleInput" @paste="handlePaste" @input="handleInput"></div>
              <div class="send-btn">
                <Button class="btns" type="primary" :disabled="disabled" @click.stop="sendText">{{$t('kefu.send')}}</Button>
              </div>
            </div>
          </div>
        </div>
        <div class="right_menu">
          <rightMenu :isTourist="tourist" :uid="userActive.user_id" :webType="userActive.type" @bindPush="bindPush"></rightMenu>
<!--          <div class="crmchat_link" @click="tolink">
            <span>{{$t('kefu.openSourceCustomerService')}}</span>
          </div>-->
        </div>
      </div>
      <!-- 用户标签 -->
      <Modal v-model="isMsg" :mask="true" class="none-radius isMsgbox" width="600" :footer-hide="true">
        <msg-window v-if="isMsg" @close="msgWinClose" @activeTxt="activeTxt"></msg-window>
      </Modal>
      <!-- 自动回复 -->
      <Modal v-model="authMsg" :mask="true" class="none-radius isMsgbox" width="600" :footer-hide="true">
        <auth-reply v-if="authMsg" @close="msgAuthClose" @activeTxt="activeTxt"></auth-reply>
      </Modal>
    </div>
  </div>

</template>

<script>

var mp3 = require('../../../assets/video/notice.wav');
var mp3 = new Audio(mp3);
mp3.muted = false;
import Setting from '@/setting';
import { HappyScroll } from 'vue-happy-scroll'
import baseHeader from './components/baseHeader';
import chatList from './components/chatList'
import rightMenu from "./components/rightMenu";
import emojiList from "@/utils/emoji";
import { Socket } from '@/libs/socket';
import msgWindow from "./components/msgWindow";
import authReply from "./components/authReply";
import transfer from './components/transfer'
import { serviceList, sendMessage } from '@/api/kefu'
import { mapState } from 'vuex'
import { getCookies, getGuid } from '@/libs/util'
import { serviceInfo } from '@/api/kefu_mobile'
import request from '@/libs/request'

// 将所得数组，按照 num 数量进行分组
const chunk = function(arr, num) {
  num = num * 1 || 1;
  var ret = [];
  arr.forEach(function(item, i) {
    if(i % num === 0) {
      ret.push([]);
    }
    ret[ret.length - 1].push(item);
  });

  return ret;
};


export default {
  name: 'index',
  components: {
    baseHeader,
    chatList,
    rightMenu,
    msgWindow,
    transfer,
    HappyScroll,
    authReply
    // goodsDetail,
    // orderDetail
  },
  directives: {

  },
  data() {
    return {
      wsOpen:false,
      authMsg:false,
      isEmoji: false, // 是否显示表情弹框
      chatCon: '', // 输入框输入的聊天内容
      emojiGroup: chunk(emojiList, 20), // 表情列表 已20个一组进行分组
      emojiList: emojiList, // 表情总数据
      html: '',
      userActive: {}, //左侧用户列表选中信息
      kefuInfo: {}, //客服信息
      isMsg: false,
      isTransfer: false,
      // ⭐ 翻译设置
      isTranslateSetting: false, // 是否显示翻译设置弹窗
      translateConfig: {
        sourceLang: 'en-US', // 源语言，默认英文
        targetLang: 'zh-CN'  // 目标语言，默认中文简体
      },
      activeMsg: '', // 选中的话术
      chatList: [],
      text: '',
      limit: 20,
      upperId: 0,
      online: true,//当前客服在线状态
      scrollTop: 0,
      isScroll: true,
      oldHeight: 0,
      isLoad: false,
      isProductBox: false,
      goodsId: "",
      isOrder: false,
      orderId: '',
      upload: '',
      header: {},
      uploadData: {
        filename: 'file'
      },
      userOnline: {},
      newRecored: {}, //新对话信息
      searchData: '', // 搜索文字
      scrollNum: 0, //滚动次数
      transferId: '', //转接id
      bodyClose: false,
      tourist: 0,
      isShow:false,
      toChat:false,
    }
  },
  computed: {
    ...mapState({
      socketStatus: state => state.admin.kefu.socketStatus
    }),
    disabled() {
      if(this.chatCon.length == 0) {
        return true
      } else {
        return false
      }
    },
    records() {
      return this.chatList.map((item, index) => {
        item.time = this.$moment(item.add_time * 1000).format('MMMDo H:mm')
        if(index) {
          if(
            item.add_time -
            this.chatList[index - 1].add_time >=
            300
          ) {
            item.show = true;
          } else {
            item.show = false;
          }
        } else {
          item.show = true;
        }

        // ⭐ 初始化翻译相关属性
        if (!item.hasOwnProperty('translated')) {
          this.$set(item, 'translated', false);
          this.$set(item, 'translating', false);
          this.$set(item, 'translatedText', '');
          this.$set(item, 'showTranslation', false);
          this.$set(item, 'targetLang', 'zh'); // 默认翻译为中文
        }

        return item;
      });
    },
  },
  watch: {
    // socketStatus:{
    //     handler(nVal,Val){
    //         if(nVal){
    //             Socket.send({
    //                 data: util.cookies.kefuGet('token'),
    //                 type: "kefu_login"
    //             });
    //         }
    //     },
    //     deep:true
    // }
  },
  created() {
    const baseUrl = Setting.apiBaseURL || '';
    let uploadBase = baseUrl;

    try {
      const parsed = new URL(baseUrl, window.location.origin);
      if (parsed.pathname.includes('adminapi')) {
        parsed.pathname = parsed.pathname.replace('adminapi', 'kefuapi');
      } else if (parsed.pathname.includes('/api/admin')) {
        parsed.pathname = parsed.pathname.replace('/api/admin', '/api/kefu');
      } else if (parsed.pathname.endsWith('/admin')) {
        parsed.pathname = parsed.pathname.replace(/\/admin$/, '/kefu');
      }
      uploadBase = parsed.origin + parsed.pathname;
    } catch (error) {
      if (uploadBase.includes('adminapi')) {
        uploadBase = uploadBase.replace('adminapi', 'kefuapi');
      } else if (uploadBase.includes('/api/admin')) {
        uploadBase = uploadBase.replace('/api/admin', '/api/kefu');
      } else if (uploadBase.endsWith('/admin')) {
        uploadBase = uploadBase.replace(/admin$/, 'kefu');
      }
    }

    this.upload = uploadBase.replace(/\/$/, '') + '/upload';
    console.log(Setting.apiBaseURL, this.upload);
    serviceInfo().then(res => {
      this.kefuInfo = res.data;
      // this.online = !!this.kefuInfo.online
      if(this.kefuInfo.site_name) {
        document.title = this.kefuInfo.site_name;
      } else {
        this.kefuInfo.site_name = '';
      }
    })
  },
  mounted() {
    let self = this
    window.addEventListener('click', function() {
      self.isEmoji = false
      self.isTranslateSetting = false
    });
    this.bus.pageWs = Socket(true, getCookies('kefu_token'));
    this.wsAgain();
    this.header['Authori-zation'] = 'Bearer ' + getCookies('kefu_token');
    this.text = this.replace_em('[em-smiling_imp]');

    // ⭐ 从 cookie 读取翻译配置
    this.loadTranslateSetting();

    console.log(this.$route);


    window.onbeforeunload = (e) => {
      if(this.$route.name == "kefu_pc_list") {
        e = e || window.event;
        // 兼容IE8和Firefox 4之前的版本
        if(e) {
          e.returnValue = 'Are you sure you want to leave?';
        }
        // Chrome, Safari, Firefox 4+, Opera 12+ , IE 9+

        return 'Are you sure you want to leave?';
      } else {
        window.onbeforeunload = null
      }
    };


  },
  methods: {
      handleInput(event) {
          // Prevent Enter key from being processed twice
          if (event && event.key === 'Enter') {
              return;
          }

          // Use nextTick to ensure DOM has updated
          this.$nextTick(() => {
              let chatCon = this.$refs.editable.innerText.replace(/[\r\n]/g, '');
              console.log('handleInput - raw text:', chatCon);
              this.chatCon = chatCon.trim();
              console.log('handleInput - this.chatCon:', this.chatCon);
              console.log('handleInput - send button disabled?', this.disabled);
          });
      },
      handlePaste(event) {
        let clipboardDataItem = event.clipboardData.items[0];
        if (clipboardDataItem.type.indexOf('image/') != -1) {
            let file = clipboardDataItem.getAsFile();
            let formData = new FormData();
            formData.append('filename', 'file');
            formData.append('file', file);
            console.log(this.upload);
            request({
                url: this.upload,
                method: 'post',
                data: formData,
                kefu: true
            }).then(res => {
                this.sendMsg(res.data.url, 3);
                this.$refs.editable.innerText = '';
            }).catch(err => {
                this.$Message.error(err.msg);
            });
        }
      },
    // 建立scoket 连接
    wsAgain() {
      this.bus.pageWs.then((ws) => {
        ws.$on('close',()=>{
          this.toChat = false;
        })
        ws.$on('success',(data)=>{

          this.isShow = true;
          let toChat = this.userActive ? this.userActive.user_id : this.userActive;
          if(!this.toChat && toChat){
            ws.send({
              data: {
                id: toChat,
                test:1
              },
              type: "to_chat",
            });
            this.toChat = true;
            this.online = !!data.online
          }
        });


        ws.$on('chat_auth',(data)=>{
          if(data.length){
            data.map(item=>{
              item.msn = this.replace_em(item.msn);
              this.pushMessageToList(item);
            });
          }
        });

        ws.$on(["reply", "chat"], (data) => {
          console.log('[WebSocket] 收到 reply/chat 消息:', data);
          console.log('[WebSocket] data.recored:', data.recored);

          if(data.msn_type == 1) {
            data.msn = this.replace_em(data.msn);
          }
          if(data.msn_type == 2) {
            if(data.msn.indexOf("[") == -1) {
              data.msn = this.replace_em(`[${data.msn}]`);
            }
          }

          // ⭐ FIX: 只有消息属于当前选中用户时，才添加到聊天窗口
          // 判断逻辑：消息的发送者或接收者是当前选中的用户
          const currentUserId = this.userActive ? this.userActive.user_id : null;
          const belongsToCurrentChat = currentUserId && (
            data.user_id === currentUserId ||
            data.to_user_id === currentUserId
          );

          if (belongsToCurrentChat) {
            this.chatList.push(data);
            this.$nextTick(()=>{
              this.scrollTop = document.querySelector(
                "#chat_scroll"
              ).offsetHeight;
            });
            console.log('[消息归属] 消息属于当前用户，已添加到聊天窗口:', {
              currentUserId,
              messageFrom: data.user_id,
              messageTo: data.to_user_id
            });
          } else {
            console.log('[消息归属] 消息不属于当前用户，仅更新列表:', {
              currentUserId,
              messageFrom: data.user_id,
              messageTo: data.to_user_id
            });
          }

          // 无论消息是否属于当前用户，都要更新左侧用户列表
          // ⭐ FIX: op=true 确保新用户会被添加到列表
          if (data.recored) {
            console.log('[WebSocket] 调用 updateUserList, recored:', data.recored);
            this.$refs.chatList.updateUserList(data.recored, true);
          } else {
            console.warn('[WebSocket] data.recored 为空，无法更新用户列表');
          }
        });

        ws.$on('recored',(data)=>{
          console.log(data)
          this.$refs.chatList.updateUserList(data,true);
        });
        ws.$on("reply", (data) => {
          mp3.play();
        });
        ws.$on("socket_error", () => {
          this.$Message.error(this.$t('kefu.connectionFailed'));
        });
        ws.$on("err_tip", (data) => {
          this.$Message.error(data.msg);
        });
        // 用户上线提醒广播
        ws.$on("user_online", (data) => {
          console.log(data);
          this.userOnline = data;
        });
        // 用户未读消息条数更改
        ws.$on("mssage_num", (data) => {
          if(data.num > 0) {
            mp3.play();
          }
          this.chatList.forEach((item) => {
            if(item.to_uid == data.user_id) {
              item.mssage_num = data.num;
            }
          });
          if(data.recored.id) {
            mp3.play();
            this.newRecored = data.recored;
          }

        });

      })
    },
    wsRestart() {
        debugger
      this.bus.pageWs = Socket(true);
      this.wsOpen = true
      this.wsAgain();
    },

    handleFormatError(file) {
      this.$Message.error(this.$t('kefu.uploadImageFormats'));
    },

    // 上传成功
    handleSuccess(res, file, fileList) {
      if(res.status === 200) {
        this.$Message.success(res.msg);
        this.sendMsg(res.data.url, 3)
      } else {
        this.$Message.error(res.msg);
      }
    },
    setOnline(data) {

      this.bus.pageWs.then(ws => {
        ws.send({
          data: {
            online: data
          },
          type: "online"
        })
      })
      this.online = data;
    },
    // 输入框选择表情
    select(data) {
      let val = `[${data}]`
      this.$refs.editable.innerText += val
      this.chatCon +=val;
      this.isEmoji = false
    },
    // 聊天表情转换
    replace_em(str) {
      str = str.replace(/\[em-([\s\S]*)\]/g, "<span class='em em-$1'/></span>");
      return str;
    },
    // 获取是否游客 获取会话列表
    changeType(data) {
      this.tourist = data;
      // console.log(this.tourist);
    },
    // 获取列表用户信息
    setDataId(data) {
      this.userActive = data
      this.chatList = []
      this.upperId = 0
      this.oldHeight = 0
      this.isScroll = true
      if(data) {
        // 翻译中文为英文
        window.document.title = data.nickname ? `Talking with ${data.nickname} - ${this.kefuInfo.site_name}` : 'Talking with visitor - ' + this.kefuInfo.site_name

        this.bus.pageWs.then((ws) => {
          ws.send({
            data: {
              id: this.userActive ? this.userActive.user_id : this.userActive,
            },
            type: "to_chat",
          });
          this.toChat = true
        });
        this.getChatList()
      } else {
        window.document.title = this.kefuInfo.site_name
        this.bus.pageWs.then((ws) => {
          ws.send({
            data: {
              id: this.userActive ? this.userActive.user_id : this.userActive,
            },
            type: "to_chat",
          });
        });
      }


    },
    msgClose(e) {
      this.isTransfer = false
    },
    transferSuccess(e){
      // ⭐ 转接成功后不删除用户（所有客服共享游客列表）
      // 只需要关闭转接弹窗即可，用户仍然在列表中
      this.isTransfer = false;
      this.$Message.success('转接成功');

      // WebSocket 会自动推送用户信息更新，无需手动刷新
    },
    msgWinClose() {
      this.isMsg = false
    },
    msgAuthClose() {
      this.authMsg = false
    },
    // 话术选中
    activeTxt(data) {
      this.$refs.editable.innerText += `${data}`
      this.chatCon +=data;
      this.isMsg = false
    },
    // 文本发送
    sendText() {
    let chatCon = this.$refs.editable.innerText.replace(/[\r\n]/g, '');
    if (!chatCon) {
        return this.$Message.error('Please enter content');
    }
    this.sendMsg(chatCon, 1);
    this.$refs.editable.innerText = '';
      this.chatCon = '';
    },

    // 统一发送处理
    sendMsg(msn, type) {
      console.log('sendMsg - starting with:', { msn, type });
      let guid = getGuid();
      let chat = this.chatOptinos(guid, msn, type);
      console.log('sendMsg - message object before send:', chat);

      sendMessage(chat).then(res => {
          console.log('sendMsg - API response:', res);
          // Add required fields for display
          chat.add_time = Date.parse(new Date()) / 1000;
          chat.id = res.data && res.data.id ? res.data.id : Date.now(); // Use server ID if available
          chat.msn = this.replace_em(chat.msn);
          console.log('sendMsg - message object after enrichment:', chat);
          this.pushMessageToList(chat);
          console.log('sendMsg - message added to chatList, total messages:', this.chatList.length);
      }).catch(err => {
          console.error('sendMsg - API error:', err);
          this.$Message.error(err.msg || 'Failed to send message');
      })
    },
    pushMessageToList(data) {
      this.chatList.push(data);
      this.setPageScrollTo();
    },
    chatOptinos(guid, msn, type, other) {
      return {
        msn,
        msn_type: type,
        to_user_id: this.userActive ? this.userActive.user_id : this.userActive,
        is_send: 0,
        is_tourist: 0,
        avatar: this.kefuInfo.avatar,
        user_id: this.kefuInfo.user_id,
        appid: this.kefuInfo.appid,
        other: other || {},
        type: 0,
          guid: guid
      };
    },
    send(type, data) {
      Socket.send({
        data,
        type
      });
    },
    // 获取聊天列表
    getChatList() {

      serviceList({
        limit: this.limit,
        user_id: this.userActive.user_id,
        upperId: this.upperId,
        is_tourist: this.tourist
      }).then(res => {
        // 兼容Java后端返回格式：可能是数组或对象{list: [], total: 0}
        console.log('getChatList response:', res);
        console.log('res.data type:', Array.isArray(res.data) ? 'array' : typeof res.data);
        console.log('res.data content:', res.data);
        let dataList = Array.isArray(res.data) ? res.data : (res.data && res.data.list ? res.data.list : []);
        console.log('dataList after processing:', dataList);

        dataList.forEach(el => {
          if(el.msn_type == 1) {
            el.msn = this.replace_em(el.msn)
          } else if(el.msn_type == 2) {
            el.msn = this.replace_em(`[${el.msn}]`)
          }
        })
        let selector = ''
        if(this.upperId == 0) {
          selector = '';

        } else {
          selector = `chat_${this.chatList[0].id}`;
        }

        // this.chatList = dataList.concat(this.chatList)
        this.chatList = [...dataList, ...this.chatList];
        this.upperId = dataList.length > 0 ? dataList[0].id : 0
        this.isLoad = false
        this.$nextTick(() => {
          // this.scrollToTop()
          this.isScroll = dataList.length >= this.limit
          this.setPageScrollTo(selector)
        })
      })
    },
    // 设置页面滚动位置
    setPageScrollTo(selector) {
      this.$nextTick(() => {
        if(selector) {
          setTimeout(() => {
            let num = parseFloat(document.getElementById(selector).offsetTop) - 60
            this.scrollTop = num
          }, 0)
        } else {
          var container = document.querySelector("#chat_scroll");
          this.scrollTop = container.offsetHeight + 0.01
          setTimeout(res => {
            if(this.scrollTop != this.$refs.scrollBox.offsetHeight) {
              this.scrollTop = document.querySelector("#chat_scroll").offsetHeight
            }
          }, 300)
        }
      })

    },
    //滚动到顶部
    scrollHandler() {
      let self = this
      if(this.isScroll && this.upperId) {
        this.isLoad = true
        this.getChatList()
      }
    },
    // 滚动条动画
    scrollToTop(duration) {
      var container = document.querySelector("#chat_scroll");
      this.scrollTop = container.offsetHeight - this.oldHeight
      setTimeout(res => {
        console.log(this.$refs.scrollBox.offsetHeight)
        this.scrollTop = this.$refs.scrollBox.offsetHeight - this.oldHeight
      }, 300)

    },
    // 商品推送
    bindPush(data) {
      this.sendMsg(data, 5)
    },
    // 商品详情
    lookGoods(item) {
      this.goodsId = item.msn
      this.isProductBox = true
    },
    // 搜索用户
    bindSearch(data) {
      this.searchData = data
      this.oldHeight = 0
      this.upperId = 0
      this.isScroll = false

    },
    // 客服转接
    transferPeople(data) {
      this.transferId = data.id
      this.isTransfer = false
      this.$Message.success('Transfer successful')
      Socket.then(ws => {
        ws.send({
          type: 'to_chat',
          data: { id: data.uid }
        })
      })
    },
    // 客服转接确定
    transferOk() {

    },

    tolink() {
      window.open('http://github.cassie.net/u/CRMChat');
    },

    // ⭐ 新增：判断消息是否是客服发送的（包括当前客服和其他客服）
    isKefuMessage(item) {
      // 方法1：检查 user_id 是否在 kefuInfo.user_ids 中（当前客服）
      if (this.kefuInfo.user_ids && this.kefuInfo.user_ids.indexOf(item.user_id) !== -1) {
        return true;
      }

      // 方法2：检查 is_kefu 字段（其他客服）
      if (item.is_kefu === 1) {
        return true;
      }

      return false;
    },

    // ⭐ 新增：判断消息是否是其他客服发送的
    isOtherKefu(item) {
      // 不是当前客服，但是客服
      return item.is_kefu === 1 && (!this.kefuInfo.user_ids || this.kefuInfo.user_ids.indexOf(item.user_id) === -1);
    },

    // ⭐ 翻译设置：保存配置到 cookie
    saveTranslateSetting() {
      try {
        const config = JSON.stringify(this.translateConfig);
        document.cookie = `translate_config=${config}; path=/; max-age=31536000`; // 保存1年
        this.$Message.success(this.$t('kefu.translateSettingSaved'));
        this.isTranslateSetting = false;
      } catch (error) {
        console.error('保存翻译配置失败:', error);
        this.$Message.error('保存失败');
      }
    },

    // ⭐ 翻译设置：从 cookie 加载配置
    loadTranslateSetting() {
      try {
        const cookies = document.cookie.split(';');
        for (let cookie of cookies) {
          const [name, value] = cookie.trim().split('=');
          if (name === 'translate_config') {
            const config = JSON.parse(decodeURIComponent(value));
            this.translateConfig = {
              sourceLang: config.sourceLang || 'en-US',
              targetLang: config.targetLang || 'zh-CN'
            };
            break;
          }
        }
      } catch (error) {
        console.error('加载翻译配置失败:', error);
        // 使用默认配置
        this.translateConfig = {
          sourceLang: 'en-US',
          targetLang: 'zh-CN'
        };
      }
    },

    // ⭐ 翻译功能：处理翻译请求
    async handleTranslate(item) {
      if (item.translating) return;

      // 如果已经翻译过，直接切换显示
      if (item.translated) {
        this.$set(item, 'showTranslation', !item.showTranslation);
        return;
      }

      // 开始翻译
      this.$set(item, 'translating', true);

      try {
        // 使用配置的目标语言
        this.$set(item, 'targetLang', this.translateConfig.targetLang);

        // 调用真实的翻译API
        await this.callTranslateAPI(item);
        this.$Message.success('翻译完成');
      } catch (error) {
        console.error('翻译失败:', error);
        this.$Message.error('翻译失败，请稍后重试');
        this.$set(item, 'translating', false);
      }
    },

    // ⭐ 翻译功能：切换显示原文/译文
    toggleTranslation(item) {
      this.$set(item, 'showTranslation', !item.showTranslation);
    },

    // ⭐ 翻译功能：获取语言名称
    getLanguageName(langCode) {
      const langMap = {
        'zh': '中文',
        'en': 'English',
        'ja': '日本語',
        'ko': '한국어',
        'es': 'Español',
        'fr': 'Français',
        'de': 'Deutsch',
        'ru': 'Русский',
        'ar': 'العربية',
        'pt': 'Português'
      };
      return langMap[langCode] || langCode;
    },

    // ⭐ 翻译功能：模拟翻译（临时使用，后续替换为真实API）
    getMockTranslation(text, targetLang) {
      // 移除HTML标签获取纯文本
      const tempDiv = document.createElement('div');
      tempDiv.innerHTML = text;
      const plainText = tempDiv.textContent || tempDiv.innerText || '';

      // 模拟翻译结果
      const mockTranslations = {
        'zh': `[中文翻译] ${plainText}`,
        'en': `[English Translation] ${plainText}`,
        'ja': `[日本語翻訳] ${plainText}`,
        'ko': `[한국어 번역] ${plainText}`,
        'es': `[Traducción al español] ${plainText}`,
        'fr': `[Traduction française] ${plainText}`
      };

      return mockTranslations[targetLang] || `[Translation to ${targetLang}] ${plainText}`;
    },

    // ⭐ 翻译功能：调用真实翻译API
    async callTranslateAPI(item) {
      try {
        this.$set(item, 'translating', true);

        // 动态导入翻译工具
        const { translateText, stripHtmlTags } = await import('@/utils/zhipuTranslator.js');

        // 移除HTML标签获取纯文本
        const plainText = stripHtmlTags(item.msn);

        if (!plainText || plainText.trim() === '') {
          this.$Message.warning('消息内容为空，无需翻译');
          return;
        }

        // 获取目标语言（默认中文简体）
        const targetLang = item.targetLang || 'zh-CN';

        // 调用智谱AI翻译API
        const translatedText = await translateText(plainText, targetLang);

        if (!translatedText || translatedText.trim() === '') {
          this.$Message.warning('翻译结果为空');
          return;
        }

        // 设置翻译结果
        this.$set(item, 'translatedText', translatedText);
        this.$set(item, 'translated', true);
        this.$set(item, 'showTranslation', true);
      } catch (error) {
        console.error('翻译API调用失败:', error);
        this.$Message.error('翻译失败: ' + (error.message || '未知错误'));
        throw error;
      } finally {
        this.$set(item, 'translating', false);
      }
    }


  }
}
</script>

<style lang="stylus" scoped>
@import '../../../styles/emoji-awesome/css/google.min.css';

textarea.ivu-input {
  border: none;
  resize: none;
}

.kefu-layouts {
  padding-top: 30px;
  height: 100%;
  display: flex;
  background: #F3F4F6;
  overflow: scroll;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  width: 1200px;
  height: 850px;
  margin: 0 auto;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  overflow: hidden;

  .container {
    flex: 1;
    min-height: 0; /* 允许flex子元素缩小 */
    display: flex;
    overflow: hidden; /* 防止内容溢出 */

    /deep/ .chatList {
      height: 100%;
      flex-shrink: 0; /* 防止chatList被压缩 */
    }

    .chat-content {
      width: 600px;
      height: 100%;
      min-height: 0;
      border-right: 1px solid #E5E7EB;
      display: flex;
      flex-direction: column;
      background: #FAFBFC;
      overflow: hidden; /* 防止内容溢出 */

      /* ⭐ Referer 来源显示区域 */
      .referer-bar {
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 8px 16px;
        font-size: 12px;
        color: #6B7280;
        border-bottom: 1px solid #E5E7EB;
        flex-shrink: 0; /* 防止被压缩 */

        .referer-label {
          font-weight: 500;
          margin-right: 6px;
          white-space: nowrap;
        }

        .referer-value {
          max-width: 400px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .chat-body {
        flex: 1;
        min-height: 0; /* 允许flex子元素缩小 */
        background: #F3F4F6;
        padding: 16px;
        overflow: hidden; /* 防止内容溢出 */
        display: flex;
        flex-direction: column;

        /deep/ .happy-scroll {
          flex: 1;
          min-height: 0;
        }

        .chat-item {
          margin-bottom: 16px;
          animation: message-slide-in 0.3s ease-out;

          @keyframes message-slide-in {
            from {
              opacity: 0;
              transform: translateY(10px);
            }
            to {
              opacity: 1;
              transform: translateY(0);
            }
          }

          .time {
            display: inline-block;
            text-align: center;
            color: #6B7280;
            font-size: 12px;
            margin: 16px 0;
            font-weight: 500;
            padding: 5px 12px;
            background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(249, 250, 251, 0.9) 100%);
            border: 1px solid rgba(0,0,0,0.06);
            border-radius: 999px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
            backdrop-filter: blur(4px);
          }

          .flex-box {
            display: flex;
          }

          .avatar {
            width: 40px;
            height: 40px;
            margin-right: 12px;
            flex-shrink: 0;

            img {
              display: block;
              width: 100%;
              height: 100%;
              border-radius: 50%;
              object-fit: cover;
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1), 0 0 0 2px rgba(255, 255, 255, 0.6);
              transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            }

            &:hover img {
              transform: scale(1.08);
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 0 0 0 3px rgba(255, 255, 255, 0.8), 0 0 0 5px rgba(79, 70, 229, 0.1);
            }
          }

          .msg-content-wrapper {
            display: flex;
            flex-direction: column;
            max-width: 70%;
            position: relative;
          }

          /* ⭐ 客服名称标签 - 显示在右下角 */
          .kefu-name-tag {
            align-self: flex-end;
            margin-top: 4px;
            padding: 2px 8px;
            font-size: 11px;
            color: #9CA3AF;
            background: rgba(0, 0, 0, 0.03);
            border-radius: 4px;
            white-space: nowrap;
          }

          .msg-wrapper {
            max-width: 360px;
            background: #F5F6F8;
            border-radius: 18px 18px 18px 4px;
            color: #1F2937;
            font-size: 14px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06), 0 1px 3px rgba(0, 0, 0, 0.03);
            transition: all 0.2s ease;
            border: 1px solid rgba(0, 0, 0, 0.04);
            position: relative;

            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12), 0 2px 6px rgba(0, 0, 0, 0.08);
            }

            /* ⭐ 新增：发送者昵称样式 */
            .sender-name {
              font-size: 12px;
              color: #6B7280;
              padding: 6px 14px 0;
              font-weight: 500;
            }

            .txt-wrapper {
              word-break: break-all;

              /* ⭐ 有翻译按钮的消息，右侧留出空间 */
              &.has-translate {
                padding-right: 32px !important;
              }
            }

            .pad16 {
              padding: 12px 14px;
            }

            /* ⭐ 翻译按钮 - 绝对定位在消息右上角 */
            .translate-btn-icon {
              position: absolute;
              top: 4px;
              right: 4px;
              display: flex;
              align-items: center;
              justify-content: center;
              width: 22px;
              height: 22px;
              padding: 0;
              font-size: 13px;
              background: rgba(255, 255, 255, 0.9);
              border: none;
              border-radius: 50%;
              cursor: pointer;
              transition: all 0.2s ease;
              outline: none;
              opacity: 0.5;
              z-index: 10;

              &:hover:not(:disabled) {
                opacity: 1;
                background: rgba(79, 70, 229, 0.15);
                transform: scale(1.2);
              }

              &:active:not(:disabled) {
                transform: scale(0.9);
              }

              &:disabled {
                cursor: not-allowed;
                opacity: 0.3;
              }

              .translate-loading-icon {
                animation: rotate 1s linear infinite;
                font-size: 12px;
              }

              span {
                line-height: 1;
                display: inline-block;
              }
            }

            /* ⭐ 翻译结果样式 */
            .translate-result {
              margin-top: 6px;
              padding: 8px 12px;
              background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
              border-left: 3px solid #F59E0B;
              border-radius: 6px;
              font-size: 13px;
              color: #78350F;
              line-height: 1.6;
              word-break: break-word;
            }

            .translate-result-content {
              font-size: 13px;
              color: #78350F;
              line-height: 1.6;
              word-break: break-word;
            }

            /* 翻译结果展开/收起动画 */
            .translate-fade-enter-active,
            .translate-fade-leave-active {
              transition: all 0.3s ease;
            }

            .translate-fade-enter,
            .translate-fade-leave-to {
              opacity: 0;
              transform: translateY(-8px);
            }

            /* 旋转动画 */
            @keyframes rotate {
              from {
                transform: rotate(0deg);
              }
              to {
                transform: rotate(360deg);
              }
            }

            .img-wraper img {
              max-width: 100%;
              height: auto;
              display: block;
              border-radius: 12px;
            }

            &::after {
              content: '';
              position: absolute;
              left: -6px;
              bottom: 10px;
              width: 12px;
              height: 12px;
              background: inherit;
              transform: rotate(45deg);
              border-bottom: 1px solid rgba(0,0,0,0.04);
              border-left: 1px solid rgba(0,0,0,0.04);
            }

            .order-wrapper {
              display: flex;
              width: 320px;
              background: #F9FAFB;
              border-radius: 12px;
              padding: 10px;

              .img-box {
                width: 64px;
                height: 64px;

                img {
                  width: 100%;
                  height: 100%;
                  border-radius: 10px;
                  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                }
              }

              .order-info {
                display: flex;
                flex-direction: column;
                justify-content: space-between;
                width: 224px;
                margin-left: 10px;
                font-size: 12px;

                .price-box {
                  display: flex;
                  align-items: center;
                  justify-content: space-between;
                  font-size: 14px;
                  color: #FF0000;

                  .more {
                    font-size: 12px;
                    color: #1890FF;
                  }
                }

                .name {
                  font-size: 14px;
                }

                .sku {
                  margin: 1px 0;
                  color: #999999;
                }
              }
            }
          }

          &.right-box {
            .flex-box {
              flex-direction: row-reverse;

              .avatar {
                margin-right: 0;
                margin-left: 12px;
              }

              .msg-wrapper {
                background: linear-gradient(135deg, #E0E7FF 0%, #D4D9F7 100%);
                border-radius: 18px 18px 4px 18px;
                color: #312E81;
                box-shadow: 0 2px 12px rgba(79, 70, 229, 0.15), 0 1px 3px rgba(79, 70, 229, 0.08);
                border: 1px solid rgba(79, 70, 229, 0.1);

                &:hover {
                  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.2), 0 2px 6px rgba(79, 70, 229, 0.12);
                }

                &::after {
                  left: auto;
                  right: -6px;
                  border-left: none;
                  border-right: 1px solid rgba(79, 70, 229, 0.12);
                }

                /* ⭐ 新增：右侧消息的发送者昵称样式 */
                .sender-name {
                  color: #4C1D95;
                  text-align: right;
                }
              }
            }

            &.gary .msg-wrapper {
              background: #F3F4F6;
              color: #6B7280;
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
            }
          }
        }
      }

      .chat-textarea {
          display: flex;
          flex-direction: column;
          flex-shrink: 0; /* 防止被压缩，保持固定高度 */
          height: 214px;
          min-height: 214px; /* 确保最小高度 */
          border-top: none;
          background: #F9FAFB;
          border-radius: 0 0 16px 0;

        .chat-btn-wrapper {
          position: relative;
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 12px 16px;
          background: #F5F6F8;
          border-bottom: 1px solid #E5E7EB;
          border-radius: 12px;
          margin: 10px 16px 8px 16px;
          box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);

          .left-wrapper {
            display: flex;
            align-items: center;

            .icon-item {
              display: flex;
              align-items: center;
              justify-content: center;
              width: 38px;
              height: 38px;
              margin-left: 6px;
              border-radius: 10px;
              cursor: pointer;
              transition: all 0.2s ease;
              background: #F9FAFB;

              &:hover {
                background: #E5E7EB;
                transform: translateY(-2px);
                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
              }

              &:active {
                transform: translateY(0);
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
              }

              .iconfont {
                font-size: 20px;
                color: #6B7280;
              }
            }
          }

          .right-wrapper {
            position: relative;
            padding-right: 20px;

            .icon-item {
              display: flex;
              align-items: center;
              font-size: 15px;
              color: #333;
              cursor: pointer;

              span {
                margin-left: 10px;
              }
            }

            .transfer-box {
              z-index: 60;
              position: absolute;
              right: 1px;
              bottom: 50px;
              width: 160px;
              background: #F5F6F8;
              box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15), 0 4px 12px rgba(0, 0, 0, 0.1);
              padding: 16px;
              border-radius: 14px;
              border: 1px solid #E5E7EB;
            }

            .transfer-bg {
              z-index: 50;
              position: fixed;
              left: 0;
              top: 0;
              width: 100%;
              height: 100%;
              background: transparent;
            }
          }

          .emoji-box {
            position: absolute;
            left: 16px;
            bottom: 60px;
            display: flex;
            flex-wrap: wrap;
            width: 400px;
            padding: 16px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(0, 0, 0, 0.08);
            background: #F5F6F8;
            border-radius: 16px;
            border: 1px solid #E5E7EB;

            .emoji-item {
              margin-right: 8px;
              margin-bottom: 8px;
              cursor: pointer;
              padding: 6px;
              border-radius: 8px;
              transition: all 0.15s ease;

              &:hover {
                background: #F3F4F6;
                transform: scale(1.1);
              }

              &:active {
                transform: scale(0.95);
              }

              &:nth-child(10n) {
                margin-right: 0;
              }
            }
          }

          /* ⭐ 翻译设置弹窗样式 */
          .translate-setting-box {
            position: absolute;
            left: 200px;
            bottom: 60px;
            width: 320px;
            padding: 0;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(0, 0, 0, 0.08);
            background: #fff;
            border-radius: 12px;
            border: 1px solid #E5E7EB;
            overflow: hidden;

            .translate-setting-header {
              padding: 12px 16px;
              background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
              color: #fff;
              font-size: 14px;
              font-weight: 600;
            }

            .translate-setting-content {
              padding: 16px;

              .setting-item {
                margin-bottom: 16px;
                display: flex;
                align-items: center;
                justify-content: space-between;

                &:last-child {
                  margin-bottom: 0;
                  justify-content: flex-end;
                }

                label {
                  font-size: 13px;
                  color: #374151;
                  font-weight: 500;
                  white-space: nowrap;
                  margin-right: 12px;
                }
              }
            }
          }
        }

        .textarea-box {
            flex: 1;
            display: flex;
            flex-direction: column;
            min-height: 0;
        }

        .editable {
            flex: 1;
            padding: 14px 18px;
            overflow-x: hidden;
            overflow-y: auto;
            font-size: 14px;
            line-height: 1.6;
            color: #374151;
            border-radius: 14px;
            margin: 0 16px 10px 16px;
            background: #F3F4F6;
            border: 2px solid transparent;
            transition: all 0.2s ease;
            box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.04);

            // contenteditable 占位符
            &:empty:before {
                content: attr(data-placeholder);
                color: #9CA3AF;
            }

            &:focus-visible {
                outline: 0;
                background: #FAFBFC;
                border-color: #4F46E5;
                box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1), inset 0 2px 4px rgba(0, 0, 0, 0.02);
            }

            &::placeholder {
                color: #9CA3AF;
            }

            /deep/ img {
                max-width: 100%;
                max-height: 100%;
                vertical-align: bottom;
            }
        }
      }
    }
  }
}

.send-btn {
  position: absolute;
  right: 24px;
  bottom: 18px;
  display: flex;
  justify-content: flex-end;

  .btns {
    padding: 12px 32px;
    background: linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%);
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(79, 70, 229, 0.35), 0 2px 6px rgba(79, 70, 229, 0.2);
    transition: all 0.2s ease;
    font-weight: 600;
    font-size: 15px;
    letter-spacing: 0.3px;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 44px;

    &:hover {
      box-shadow: 0 8px 20px rgba(79, 70, 229, 0.45), 0 4px 10px rgba(79, 70, 229, 0.25);
      transform: translateY(-2px);
      background: linear-gradient(135deg, #4338CA 0%, #6D28D9 100%);
    }

    &:active {
      transform: translateY(0);
      box-shadow: 0 2px 8px rgba(79, 70, 229, 0.3);
    }

    &[disabled] {
      background: #D1D5DB;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
      color: #9CA3AF;
      cursor: not-allowed;
      transform: none;
    }
  }
}

.bg {
  z-index: 100;
  position: fixed;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
}

/deep/.happy-scroll-content {
  width: 100%;

  .demo-spin-icon-load {
    animation: ani-demo-spin 1s linear infinite;
  }

  @keyframes ani-demo-spin {
    from {
      transform: rotate(0deg);
    }

    50% {
      transform: rotate(180deg);
    }

    to {
      transform: rotate(360deg);
    }
  }

  .demo-spin-col {
    height: 100px;
    position: relative;
    border: 1px solid #eee;
  }
}

.isMsgbox {
  >>> .ivu-modal-body {
    padding: 0;
  }
}

.right_menu {
  position: relative;
  background: #FAFBFC;

  .crmchat_link {
    position: absolute;
    bottom: 10px;
    left: 0;
    right: 0;
    margin: auto;
    text-align: center;
    transition: 0.3s;
    cursor: pointer;

    span {
      color: #ccc;
    }

    span:hover {
      color: #007aff;
    }
  }
}
</style>
