<template>
    <div class="content">
        <p class="font-w">{{ $t('system.usageInstructions') }}</p>
        <p class="text-i">{{ $t('system.customDevelopmentDescription') }}</p>
        <Divider />
        <p class="typetitle">{{ $t('system.stepOneImportJs') }}</p>
        <div class="fenlei">
                <div class="fenlei">
                <p class="font-w">{{ $t('system.generalWebpageImport') }}</p>
                <div class="code-content-wrap">
                    <textarea id="NormalCodeTextareakaifa1" class="code" rows="1">
<script src="{{siteUrl}}/customerServer.js"></script>
                    </textarea>
                    <div class="other-wrap">
                        <a @click="getCopy('NormalCodeTextareakaifa1')" class="btn btn-blue btn-large" href="javascript:void(0);"><span>{{ $t('system.copyCode') }}</span></a>
                    </div>
                </div>
                </div>

            <div class="fenlei">
            <p class="font-w">{{ $t('system.webpackProjectImport') }}</p>
                <div class="code-content-wrap">
                    <textarea id="NormalCodeTextarea2" class="code" rows="9">
<script>
    (function() {
        var hm = document.createElement("script");
        hm.src = "{{siteUrl}}/customerServer.js";
        var s = document.getElementsByTagName("script")[0];
        s.parentNode.insertBefore(hm, s);
    })()
</script>
                    </textarea>
                    <div class="other-wrap">
                    <a @click="getCopy('NormalCodeTextarea2')" class="btn btn-blue btn-large" href="javascript:void(0);"><span>{{ $t('system.copyCode') }}</span></a>
                    </div>
                </div>
            </div>
        </div>
        <p class="typetitle">{{ $t('system.stepTwoSetParams') }}</p>
        <div  class="fenlei">
            <p class="font-w">{{ $t('system.initializeDescription') }}</p>
            <div class="code-content-wrap">
                <textarea id="NormalCodeTextareakaifa2" class="code" rows="45">
var option = {
    openUrl: "{{siteUrl}}", // 打开客服聊天框的地址，即：部署后台管理系统的地址，若未填写，则自动获取当前服务器的地址
    token: {{tokeninfo.tokenMd5}}, // token,与后台交互的凭证
    kefuid:'',//默认为空自动对接客服，可填写指定客服ID
    isShowTip: true, // 初始化成功后，界面右下角会自动创建 “联系客服按钮”， 如无需默认展示，则填写false即可,默认为true
    mobileIcon: '', //  手机端悬浮客服图片
    pcIcon: '', // pc端悬浮客服图片
    windowStyle:'center',//默认空 右下角小弹窗， center 普通中间弹窗样式
    domId: 'customerServerTip',//展示在页面右下角联系客服的dom的id，可根据id获取到dom后自行修改样式, 默认为customerServerTip
    insertDomNode: '.getCode_container', // SPA应用必填，html文件单独引入选填，表示插入客服弹窗的 dom节点，一般为当前界面的根节点，默认为body
    //设置客户信息
    sendUserData: {
        uid: '1', // 用户id
        nickName: '', // 用户昵称
        phone: '', // 用户联系方式
        sex: '1', // 用户性别
        avatar: '', // 用户头像 URL地址
        openid: ''//微信openid
    }，
    //设置默认打开推送产品
    productInfo: {
        store_name: '蒙奇 D 路飞',
        stock: '库存',
        sales: '122', // 销量
        ficti: '10', // 赠送
        price: '100',
        image: ''//产品图片
        proUrl:''//产品链接
    }
};

var canCustomerServer = new initCustomerServer(option);
//实例化加载客服组建
canCustomerServer.init();

//样式设置说明
canCustomerServer.setStyleOfCustomerServer(this.canCustomerServer.connentServerDom,{
            bottom:'300px',
            left:'50%'
        });
// 调用打开客服弹窗的方法，如果isShowTip为false，就使用这个函数，当然也可以使用A链接
canCustomerServer.getCustomeServer();

                </textarea>


                <div class="other-wrap">
                    <a class="btn btn-blue btn-large mr10" @click="jiazai" href="javascript:void(0);">{{ $t('system.loadCustomerServiceWindow') }}</a>
                    <a class="btn btn-blue btn-large mr10" @click="tanchuang" href="javascript:void(0);">{{ $t('system.openChatWindow') }}</a>
                    <a @click="getCopy('NormalCodeTextareakaifa2')" class="btn btn-blue btn-large" href="javascript:void(0);"><span>{{ $t('system.copyCode') }}</span></a>
                </div>
            </div>

        </div>
        <p class="typetitle">{{ $t('system.eventDescription') }}</p>
        <div  class="fenlei">
            <p class="font-w">{{ $t('system.jsEventsOnly') }}</p>
            <div class="code-content-wrap">
                <textarea id="NormalCodeTextareakaifa3" class="code" rows="45">

<script src="{{siteUrl}}/customerServer.js" id="chat" option='{"authInit":true,"openUrl":"{{siteUrl}}","isShowTip":true,"token":"{{tokeninfo.tokenMd5}}"}'></script>
<script>
    //自动实例化对象
    var canCustomerServer
     //authInit = true 后监听事件可以拿到实例化后的客服对象
     window.$chat.on('chatAuthAfter',function (instance) {
         canCustomerServer = instance;
     });

    //触发事件
    // window.$chat.emit('postMessage',['touchend',{uid:10}]);

    //关闭弹窗事件
    window.$chat.on('closeWindow',function () {
        console.log('关闭弹窗')
    })
    //消息回执事件
    window.$chat.on('onMessageChat',function (data) {

    });
    //消息数量更新事件
    window.$chat.on('onMessageNum',function (data) {

    });
    //转接事件
    window.$chat.on('onMessageTransfer',function (data) {

    });
    //登录成功回执事件
    window.$chat.on('onMessageSuccess',function () {

    })
    //发送图片后的事件
    window.$chat.on('sendImage',function (data) {

    })
    //发送图文消息后的事件
    window.$chat.on('sendImageText',function (data) {

    })

</script>
                </textarea>
            </div>
        </div>
        <div class="fenlei">
            <p class="font-w">{{ $t('system.tips') }}</p>
            <p class="text-i">{{ $t('system.experienceTip') }}</p>
            <p class="text-i">{{ $t('system.tokenChangeTip') }}</p>
            <p class="text-i">{{ $t('system.moreSettingsTip') }}</p>
        </div>
    </div>
</template>
<script>

import { mapState } from 'vuex';
import { adminAppCustomer, appReset } from '@/api/kefu';
import initCustomerServer from '@/libs/customerServer';
    export default{
        name: 'kaifa',
        props: {
            tokeninfo:{},
            siteUrl:'',
        },
        mounted() {

        },
        methods: {
            //加载客服
            jiazai() {
                let option = {

                    openUrl: this.siteUrl,
                    deviceType: '', //pc, Mobile
                    // domId: 'customerServerTip',
                    insertDomNode: '.getCode_container',
                    token: this.tokeninfo.tokenMd5,
                    isShowTip: true, // true 展示 false 不展示
                    windowStyle: 'center', // center 仅仅pc端有效，在页面中间弹出
                    // sendUserData: {
                    // uid: '',
                    //   nickName: '',
                    //   phone: '',
                    //   type: '',
                    //   sex: '1',
                    //   avatar: ''
                    // },
                    // productInfo: {
                    //   store_name: '蒙奇 D 路飞',
                    //   stock: '库存',
                    //   sales: '122', // 销量
                    //   ficti: '10', // 赠送
                    //   price: '100',
                    //   image: 'https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fhbimg.b0.upaiyun.com%2F4495e731345f73cb023b1d70197d50e7f451dbc91a88e-UU7MfN_fw658&refer=http%3A%2F%2Fhbimg.b0.upaiyun.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1629276024&t=9d1c5b297dc857ddd2d18c9580dde427'
                    // }
                };
                this.canCustomerServer = new initCustomerServer(option);
                this.canCustomerServer.init();
            },
            //弹窗体验
            tanchuang() {
            this.canCustomerServer.getCustomeServer(); // 点击调取客服弹框
            },
            getCopy(id) {
//                this.cgetCopy(id);
                this.$emit('cgetCopy',id);
            }

        }
    }
</script>

