<template>
  <div class="content">
    <p class="font-w">{{ $t('system.usageInstructions') }}</p>
    <p class="text-i">{{ $t('system.webEmbedDescription') }}</p>
    <Divider />
    <p class="typetitle">{{ $t('system.getCode') }}</p>

    <div class="fenlei">
      <div class="code-content-wrap">
        <textarea id="NormalCodeTextarea1" class="code" rows="22">
<script>
  (function() {
  _s = document.createElement('script');
  _s.src="{{siteUrl}}/customerServer.js"
  _s.onload = function(){
  var option = {
        "authInit":true,
        openUrl: '{{siteUrl}}',
        token: '{{tokeninfo.tokenMd5}}',
        kefuid:'',//默认为空自动对接客服，可填写指定客服ID
        isShowTip: true, // 初始化成功后，界面右下角会自动创建 “联系客服按钮”， 如无需默认展示，则填写false即可,默认为true
        mobileIcon: '', //  手机端悬浮客服图片
        pcIcon: '', // pc端悬浮客服图片
        windowStyle:'center',//默认空 右下角小弹窗， center 普通中间弹窗样式
      };
      var canCustomerServer = new initCustomerServer(option);
      canCustomerServer.init();
  }
  document.head.appendChild(_s)
  })();
</script>
            </textarea>
        <div class="other-wrap">
          <a class="btn btn-blue btn-large mr10" @click="jiazai" href="javascript:void(0);">{{ $t('system.clickToExperience') }}</a>
          <a @click="getCopy('NormalCodeTextarea1')" class="btn btn-blue btn-large" href="javascript:void(0);"><span>{{ $t('system.copyCode') }}</span></a>
        </div>
      </div>

    </div>


    <div class="fenlei">
      <p class="font-w">{{ $t('system.tips') }}</p>
      <p class="text-i">{{ $t('system.experienceTip') }}</p>
      <p class="text-i">{{ $t('system.tokenChangeTip') }}</p>
    </div>
  </div>
</template>
<script>
import { mapState } from 'vuex';
import { adminAppCustomer, appReset } from '@/api/kefu';
import initCustomerServer from '@/libs/customerServer';
export default {
  name: 'wangye',
  props: {
    tokeninfo: {},
    siteUrl: '',
  },
  mounted() {

  },
  methods: {
      jiazai(){
          window.open(`${location.origin}/chat/index?token=${this.tokeninfo.tokenMd5}&noCanClose=1`)
      },
    getCopy(id) {
      this.$emit('cgetCopy', id);
    },
  }
}
</script>
<style lang="less" scoped>
.a {
  display: none;
  display: block;
  border-radius: 8px;
  overflow: hidden;
}
</style>

