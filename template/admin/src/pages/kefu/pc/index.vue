<template>
  <div class="kefu-layouts">
    <div class="content-wrapper">
      <baseHeader :kefuInfo="kefuInfo" :online="online" @setOnline="setOnline"></baseHeader>
      <div class="container">
        <chatList ref="chatList" @setDataId="setDataId" @search="bindSearch" @changeType="changeType" :isShow="isShow" :userOnline="userOnline" :newRecored="newRecored" :searchData="searchData"></chatList>
        <div class="chat-content">
          <div class="chat-body">

            <happy-scroll size="5" resize hide-horizontal :scroll-top="scrollTop" @vertical-start="scrollHandler">
              <div style="width: 600px; padding:20px;" id="chat_scroll" ref="scrollBox">
                <Spin v-show="isLoad">
                  <Icon type="ios-loading" size=18 class="demo-spin-icon-load"></Icon>
                  <div>{{$t('kefu.loading')}}</div>
                </Spin>
                <div class="chat-item" v-for="(item,index) in records" :key="index" :class="[{'right-box':kefuInfo.user_ids.indexOf(item.user_id) !== -1},{'gary':item.msn_type==5}]" :id="`chat_${item.id}`">
                  <div class="time" v-show="item.show">{{item.time }}</div>
                  <div class="flex-box">
                    <div class="avatar">
                      <img v-lazy="item.avatar" alt="">
                    </div>
                    <div class="msg-wrapper">
                      <!-- 文档 -->
                      <template v-if="item.msn_type<=2">
                        <div class="txt-wrapper pad16" v-html="item.msn"></div>
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
          <div class="crmchat_link" @click="tolink">
            <span>{{$t('kefu.openSourceCustomerService')}}</span>
          </div>
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

    this.upload = Setting.apiBaseURL.replace('admin', 'kefu') + '/upload'
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
    });
    this.bus.pageWs = Socket(true, getCookies('kefu_token'));
    this.wsAgain();
    this.header['Authori-zation'] = 'Bearer ' + getCookies('kefu_token');
    this.text = this.replace_em('[em-smiling_imp]');

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
      this.$refs.chatList.deleteUserList(this.userActive)
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
  height: 820px;
  margin: 0 auto;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  overflow: hidden;

  .container {
    flex: 1;
    display: flex;

    .chat-content {
      width: 600px;
      height: 100%;
      border-right: 1px solid #E5E7EB;
      display: flex;
      flex-direction: column;
      background: #FAFBFC;

      .chat-body {
        max-height: 530px;
        flex: 1;
        background: #F3F4F6;
        padding: 16px;

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

            .txt-wrapper {
              word-break: break-all;
            }

            .pad16 {
              padding: 12px 14px;
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
        height: 214px;
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
