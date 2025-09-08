import { mobileScoket } from '@/libs/socket';
import { userRecord, serviceUpload, serviceAdv, userStatistics } from '@/api/kefu';
import { sendMessageMobile } from '@/api/kefu_mobile';
import { setLoc, getLoc, getGuid } from '@/libs/util'
import { mapState } from 'vuex';
import Cookies from "js-cookie";

var mp3 = require('@/assets/video/notice.wav');
export default {
  data() {
    return {
      userAgentType:0,
      mp3: new Audio(mp3),
      inputConType: 1,
      userMessage: '',
      chatServerData: {
        avatar: '',
        nickname: '',
        site_name: '',
        user_id: '',
        to_user_avatar: '',
        to_user_id: '',
        to_user_nickname: '',
        uid: '',
        serviceList: [] // 聊天记录
      },
      chatStatus:false,
      toChat:false,
      upperStrataData: {},
      upperData: {}, // 外部链接携带进来的参数
      unreadMessages: '',
      userKey: '',
      productMessage: {},
      isShowProductModel: false, // 是否显示携带商品
      copyFile: '', // 粘贴在输入框中的file文件
      unReadMesage: 0, // 未读消息数
      advertisement: '' // 广告
    }
  },
  computed: {
    ...mapState('media', ['isMobile']),
  },
  // 指令粘贴指令定义
  directives: {
    paste: {
      bind(el, binding, vnode) {
        el.addEventListener("paste", function(event) {
          //这里直接监听元素的粘贴事件
          binding.value(event);
        });
      },
    },
  },
  created() {
    this.redirect();
    this.loadJS();

    // 获取url参数
    this.upperData = this.$route.query;
    // 更新token
    let tokenName = this.$route.query.tokenName || 'token';
    const token = this.$route.query[tokenName];
    if(token != getLoc('mobile_token')) {
      setLoc('mobile_token', token);
    }
    // 将url参数存入缓存
    Object.keys(this.upperData).forEach(item => {
      if(this.upperData[item]) {
        setLoc(item, this.upperData[item]);
      }
    });
    this.getUserRecord(); // 查看当前是否有客服在线, 建立socket连接
    // 获取从父页面传递过来的数据
    window.addEventListener("message", e => {
      // 获取图文数据
      switch (e.data.type) {
        case 'getImgOrText'://发送商品信息
          this.userKey = e.data.key;
          if(e.data.productInfo) {
            this.productMessage = e.data.productInfo;
          }
          break;
        case 'openCustomeServer'://打开窗口
          this.bus.pageWs.then((ws) => {
            ws.send({ type: 'to_chat', data: { id: this.chatServerData.to_user_id } });
            this.toChat = true;
            if(this.unReadMesage) {
              this.getUserRecord()
            }

          })
          break;
        case 'closeCustomeServer'://关闭窗口
          this.bus.pageWs.then((ws) => {
            ws.send({ type: 'to_chat', data: { id: 0 } });
            this.toChat = true;
          })
          break;
        case 'chat'://自定义发送消息
          if(e.data.data.type && e.data.data){
            this.bus.pageWs.then((ws) => {
              ws.send({type:e.data.data.type,data:e.data.data.data});
            })
          }
          break;
      }
    });

    this.getServiceAdv();

    this.userStatistics();

  },
  watch: {
    productMessage: {
      handler(val, oldVal) {
        if(JSON.stringify(val) != JSON.stringify(oldVal)) {
          this.isShowProductModel = true;
          this.goPageBottom(); // 滑动到页面底部
        }
      },
      deep: true
    },
    isMobile(n){
      this.redirect();
    },
  },
  methods: {
    loadJS(){
      var userAgent = navigator.userAgent;
      if (userAgent.indexOf('AlipayClient') > -1) {
        this.userAgentType = 1;
        // 支付宝小程序的 JS-SDK 防止 404 需要动态加载，如果不需要兼容支付宝小程序，则无需引用此 JS 文件。
        document.writeln('<script src="https://appx/web-view.min.js"' + '>' + '<' + '/' + 'script>');
      } else if (/QQ/i.test(userAgent) && /miniProgram/i.test(userAgent)) {
        this.userAgentType = 2;
        // QQ 小程序
        document.write(
            '<script type="text/javascript" src="https://qqq.gtimg.cn/miniprogram/webview_jssdk/qqjssdk-1.0.0.js"><\/script>'
        );
      } else if (/miniProgram/i.test(userAgent) && /micromessenger/i.test(userAgent)) {
        this.userAgentType = 3;
        // 微信小程序 JS-SDK 如果不需要兼容微信小程序，则无需引用此 JS 文件。
        document.write('<script type="text/javascript" src="https://res.wx.qq.com/open/js/jweixin-1.4.0.js"><\/script>');
      } else if (/toutiaomicroapp/i.test(userAgent)) {
        this.userAgentType = 4;
        // 头条小程序 JS-SDK 如果不需要兼容头条小程序，则无需引用此 JS 文件。
        document.write(
            '<script type="text/javascript" src="https://s3.pstatp.com/toutiao/tmajssdk/jssdk-1.0.1.js"><\/script>');
      } else if (/swan/i.test(userAgent)) {
        this.userAgentType = 5;
        // 百度小程序 JS-SDK 如果不需要兼容百度小程序，则无需引用此 JS 文件。
        document.write(
            '<script type="text/javascript" src="https://b.bdstatic.com/searchbox/icms/searchbox/js/swan-2.0.18.js"><\/script>'
        );
      } else if (/quickapp/i.test(userAgent)) {
        this.userAgentType = 6;
        // quickapp
        document.write('<script type="text/javascript" src="https://quickapp/jssdk.webview.min.js"><\/script>');
      }
    },
    userStatistics() {
        let ua = window.navigator.userAgent;
        let browser = '';
        if (ua.indexOf('MSIE') != -1 && (!!window.ActiveXObject || 'ActiveXObject' in window)) {
            browser = 'IE';
        } else if (ua.indexOf('Firefox') != -1) {
            browser = 'Firefox';
        } else if (ua.indexOf('Chrome') != -1) {
            browser = 'Chrome';
        } else if (ua.indexOf('Opera') != -1) {
            browser = 'Opera';
        } else if (ua.indexOf('Safari') != -1) {
            browser = 'Safari';
        } else if (ua.indexOf('Netscape') != -1) {
            browser = 'Netscape';
        }
        userStatistics({
            ip: window.returnCitySN ? window.returnCitySN.cip : '',
            path: window.location.href,
            source: window.parent.location.href,
            browser: browser
        });
      },
    redirect(){
      if (this.isMobile && this.deviceType == 'mobile'){

      } else if(this.isMobile && this.deviceType == 'pc'){
        this.$router.push({ name: 'customerServerMobile', query: this.$route.query })
      } else if(!this.isMobile && this.deviceType == 'pc'){

      } else if(!this.isMobile && this.deviceType == 'mobile'){
        this.$router.push({ name: 'customerServerPc', query: this.$route.query });
      }
    },
    // 获取客服广告
    getServiceAdv() {
      serviceAdv().then(res => {
        if(res.status == 200) {
          this.advertisement = res.data.content;
        }
      })
    },

    // 查看当前是否有客服在线, 若不在线，跳转到反馈界面
    getUserRecord() {
      let postData = {
        uid: this.upperData.uid || getLoc('uid') || 0,
        limit: 20,
        nickname: this.upperData.nickName,
        phone: this.upperData.phone,
        sex: this.upperData.sex,
        avatar: this.upperData.avatar,
        openid: this.upperData.openid,
        kefu_id: this.upperData.kefu_id || 0,
        toUserId:getLoc('to_user_id') || 0,
        type: this.upperData.deviceType == 'Mobile' ? '3' : '0'
      }

      userRecord(postData).then(res => {
          this.chatServerData = res.data;
          this.$nextTick(() => {
            this.happyScroll = !this.happyScroll;
          })

          this.unReadMesage = 0;
          this.goPageBottom();
          let cookieData = {
            nickname: '',
            uid: '',
            avatar: '',
            to_user_id:0,
            to_user_nickname:''
          };
            Object.keys(cookieData).forEach(item => {
              setLoc(item, getLoc(item) ? getLoc(item) : res.data[item]);
            })

          if (res.data.welcome) {
            this.pushMessageToList(res.data.welcome);
          }
          this.goPageBottom(); // 滑动到页面底部
          document.title = res.data.to_user_nickname ? `正在和${res.data.to_user_nickname}对话中 - ${this.chatServerData.site_name}` : '正在和游客对话中 - ' + this.chatServerData.site_name;
          this.connentServer(); // 建立socket 链接

      }).catch(rej => {
        if(rej.status == 400) {
          this.$router.replace({ name: 'customerOutLine', query: this.$route.query });
        }
      })
    },
    imageLoad(){
      this.goPageBottom(); // 滑动到页面底部
    },
    // 建立连接
    connentServer() {
      let number = 0;
      let token = getLoc('mobile_token');
      let formTerminal = this.upperData.deviceType == 'Mobile' ? 'h5' : 'pc'
      this.bus.pageWs = mobileScoket(true, token, formTerminal);
      this.bus.pageWs.then((ws) => {

        ws.$on('close',()=>{
          this.toChat = false;
          this.chatStatus = false;
        })

        ws.$on('kefu_logout',data=>{
          if(data.online == 0){
            this.$router.replace({
              name: 'customerOutLine',
              query: this.$route.query
            });
          }
        })

        // 发送消息监听函数
        ws.$on(["reply", "chat"], data => {
          this.chatServerData.serviceList.push(data);
          // this.userMessage = '';
          this.goPageBottom(); // 滑动到页面底部

        });

        ws.$on("reply", (data) => {
          try {
            this.mp3.play();
          }catch (e) {

          }

          parent.postMessage({ type: 'onMessageChat', data: data }, "*");
        });

        ws.$on('success', data => {
          this.chatStatus = true;
          let to_user_id = this.upperData.isShowTip && this.upperData.isShowTip !='undefined' ? 0 : this.chatServerData.to_user_id
            ws.send({
              type: 'user',
              data: {
                to_user_id: to_user_id,
                uid: this.chatServerData.uid,
                nickname: this.chatServerData.nickname,
                avatar: this.chatServerData.avatar,
                phone: this.userMessage.phone ? this.userMessage.phone : this.chatServerData.phone,
                openid: this.upperData.openid,
                type: this.upperData.deviceType == 'Mobile' ? '3' : '0' // 0 = pc , 1 = 微信 ，2 = 小程序 ，3 = H5, 4 = APP
              }
            })
          if(!to_user_id){
            if(!this.toChat && this.chatServerData.to_user_id){
              ws.send({
                data: {
                  id: this.chatServerData.to_user_id,
                  test:1
                },
                type: "to_chat",
              });
              this.toChat = true;
            }
          }
          parent.postMessage({ type: 'onMessageSuccess', data: {} }, "*");
        })

        ws.$on('mssage_num', data => {
          if(data.num > 0) {
            this.mp3.play();
            this.unReadMesage = data.num;
          }
          parent.postMessage({ type: 'onMessageNum', data: {num:data.num} }, "*");
        })


        ws.$on('to_transfer',data=>{
          let to_user_id = this.chatServerData.to_user_id;
          this.chatServerData.to_user_id = data.toUid
          this.chatServerData.to_user_nickname = data.nickname
          this.chatServerData.to_user_avatar = data.avatar
          this.chatServerData.serviceList.map(item=>{
            if(to_user_id == item.user_id){
              item.avatar = data.avatar
            }
          })
          ws.send({ type: 'to_chat', data: { id: data.toUid } });
          this.toChat = true;
          document.title =  `正在和${data.nickname}对话中 - ${this.chatServerData.site_name}`

          parent.postMessage({ type: 'onMessageTransfer', data: data }, "*");
        })

      })
    },

    // 前往页面底部，用于接收到聊天记录后查看到最新消息
    goPageBottom() {
      this.$nextTick(() => {
        this.scrollTop = document.querySelector(
          "#chat_scroll"
        ).offsetHeight;
      });
    },
    // 发送商品给客服
    sendProduct() {
      this.bus.pageWs.then((ws) => {
        ws.send({
          type: 'chat',
          data: {
            to_user_id: this.chatServerData.to_user_id,
            uid: this.chatServerData.uid,
            type: 5,
            other: this.productMessage
          }
        })
        parent.postMessage({ type: 'sendImageText', data: this.productMessage }, "*");
      })
      this.isShowProductModel = false;
      this.goPageBottom();
    },
    //微信截图上传图片时触发
    handleParse(e) {
      console.log(e);
      let file = null;
      if(
        e.clipboardData &&
        e.clipboardData.items[0] &&
        e.clipboardData.items[0].type &&
        e.clipboardData.items[0].type.indexOf("image") > -1
      ) {
        //这里就是判断是否有粘贴进来的文件且文件为图片格式
        file = e.clipboardData.items[0].getAsFile();
      } else {
        this.$message({
          type: "warning",
          message:
            "上传的文件必须为图片且无法复制本地图片且无法同时复制多张图片",
        });
        return;
      }
      this.update(file);
    },
    update(e) {
      // 上传照片
      let file = e;
      let param = new FormData(); // 创建form对象
      param.append("filename", "file"); // 通过append向form对象添加数据进去
      param.append("file", file); // 通过append向form对象添加数据进去
      // 添加请求头
      serviceUpload(param).then(res => {
        if(res.status == 200) {
          this.sendMsg(res.data.url, 3);
          parent.postMessage({ type: 'sendImage', data: {url:res.data.url} }, "*");
          this.$refs['inputDiv'].innerText = '';
        }
      })

    },
    // 选择表情
    select(item) {
      if(this.$route.query.deviceType == 'Mobile' || !this.$refs['inputDiv']) {
        this.userMessage += `[${item}]`
      } else {
        this.inputConType = 1;
        this.$refs['inputDiv'].innerText += `[${item}]`
        // this.$refs['inputDiv'].innerHTML += `<span class="em ${item}"></span>`
      }

    },

    // 文本发送
    sendText() {
      let sendMessage;
      if(!this.$refs['inputDiv']) {
        sendMessage = this.userMessage.replace(/[\r\n]/g, '');
      } else {
        sendMessage = this.$refs['inputDiv'].innerText.replace(/[\r\n]/g, '');
      }

      if(sendMessage) {
        this.sendMsg(sendMessage, 1);
        this.$refs['inputDiv'] ? this.$refs['inputDiv'].innerText = '' : this.userMessage = '';
      } else {
        this.$Message.error('请先输入信息，在进行发送');
        this.$refs['inputDiv'] ? this.$refs['inputDiv'].innerText = '' : this.userMessage = '';
      }



    },
    // type: 1 普通文本 2 图片
    sendMsg(msn, type, id) {
      if(!this.chatStatus){
        return this.$Message.error('正在连接中');
      }
      let guid = getGuid();
      let chat = this.chatOptinos(guid, msn, type);
      sendMessageMobile(chat).then( res => {
        chat.add_time = Date.parse(new Date()) / 1000;
        this.pushMessageToList(chat);
        if (res.data.autoReply === true) {
          this.pushMessageToList(res.data.autoReplyData);
        }
        this.goPageBottom();
      }).catch(()=>{
      })
    },
    pushMessageToList(data) {
      this.chatServerData.serviceList.push(data);
    },
    chatOptinos(guid, msn, type, other) {
      return {
        msn,
        msn_type: type,
        to_user_id: this.chatServerData.to_user_id,
        is_send: 0,
        is_tourist: 0,
        avatar: this.chatServerData.avatar,
        user_id: this.chatServerData.user_id,
        appid: this.chatServerData.appid,
        other: other || {},
        type: 0,
        guid:guid
      };
    },
    // 滑动到顶部
    scrollHandler(e) {
      this.isLoad = true;
      userRecord({
        limit: 20,
        uid: this.chatServerData.uid,
        idTo: this.chatServerData.serviceList ? this.chatServerData.serviceList[0].id : '',
        toUserId: this.chatServerData.to_user_id
      }).then(res => {
        if(res.status == 200) {
          res.data.serviceList.reverse().forEach(item => {
            this.chatServerData.serviceList.unshift(item);
          })
        }
        this.isLoad = false;
      })
    },
    closeIframe() {
      // 通知外部容器，收起iframe
      parent.postMessage({ type: 'closeWindow' }, "*");
      // 通知服务器，客户收起了聊天框
      this.bus.pageWs.then((ws) => {
        ws.send({ type: 'to_chat', data: { id: 0 } });
        this.toChat = true;
      })
    },
    // 聊天输入框获取焦点
    textareaInput() {
      this.inputConType = 1;
    },
    // 打开选择表情弹框
    selectEmoji() {
      this.inputConType = 2;
      this.goPageBottom();
    },
    dataURLtoFile(dataurl,f) {
      let arr = dataurl.split(','), mime = arr[0].match(/:(.*?);/)[1],
          bstr = atob(arr[1]), n = bstr.length, u8arr = new Uint8Array(n);
      while(n--){
        u8arr[n] = bstr.charCodeAt(n);
      }
      return new File([u8arr],f.name, {type:mime})
    },
    compressImg(file){
      var src;
      var files;
      var fileSize = parseFloat(parseInt(file['size'])/1024/1024).toFixed(2);
      var read = new FileReader();
      let that=this;
      read.readAsDataURL(file);
      return new Promise((resolve, reject)=>{
        read.onload = function (e) {
          var img = new Image();
          img.src = e.target.result;
          img.onload = function(){
            //默认按比例压缩
            var w = this.width,
                h = this.height;
            //生成canvas
            var canvas = document.createElement('canvas');
            var ctx = canvas.getContext('2d');
            var base64;
            // 创建属性节点
            canvas.setAttribute("width", w);
            canvas.setAttribute("height", h);
            ctx.drawImage(this, 0, 0, w, h);

            if(fileSize<1){
              //如果图片小于一兆 那么不执行压缩操作
              base64 = canvas.toDataURL(file['type'], 1);
            }else if(fileSize>1&&fileSize<2){

              //如果图片大于1M并且小于2M 那么压缩0.5
              base64 = canvas.toDataURL(file['type'], 0.5);
            }else{
              //如果图片超过2m 那么压缩0.2
              base64 = canvas.toDataURL(file['type'], 0.2);
            }
            // 回调函数返回file的值（将base64编码转成file）
            files = that.dataURLtoFile(base64,file); //如果后台接收类型为base64的话这一步可以省略

            resolve(files)
          };
        };
      })
    },
    // 上传图片
    uploadFile(e) {
      this.compressImg(e.target.files[0]).then(file=>{
        let formData = new FormData();
        formData.append('filename', 'file');
        formData.append('file', file);
        serviceUpload(formData).then(res => {
          if(res.status == 200) {
            this.sendMsg(res.data.url, 3);
          }
        }).catch(rej => {
          this.$Message.error(rej.msg);
        })
      })
    },

    // 使用 window.opener 更改session
    setSession(name, value) {
      if(window.opener && Object.getOwnPropertyNames(window.opener).length > 0) {
        window.opener.sessionStorage.setItem(name, value)
      } else {
        sessionStorage.setItem(name, value)
      }
    },
    getSession(name) {
      if(window.opener && Object.getOwnPropertyNames(window.opener).length > 0) {
        return window.opener.sessionStorage.getItem(name)
      } else {
        return sessionStorage.getItem(name)
      }
    },
    tolink() {
      window.open('http://github.crmeb.net/u/CRMChat');
    }
  }
}
