<template>
  <div class="page-account">
    <div class="container" :class="[fullWidth > 768 ? 'containerSamll' : 'containerBig']">
      <swiper :options="swiperOption" class="swiperPross" v-if="fullWidth > 768">
        <swiper-slide class="swiperPic" v-for="(item, index) in swiperList" :key="index">
          <img :src="item.slide" />
        </swiper-slide>
        <div class="swiper-pagination" slot="pagination"></div>
      </swiper>
      <div class="index_from page-account-container from-wh">
        <div class="page-account-top">
          <div class="page-account-top-logo">
            <img :src="login_logo" alt="logo" style="width: 100%; height: 74px" />
          </div>
        </div>
        <Form ref="formInline" :model="formInline" :rules="ruleInline" @keyup.enter="handleSubmit('formInline')">
          <FormItem prop="username">
            <Input
              type="text"
              v-model="formInline.username"
              prefix="ios-contact-outline"
              placeholder="请输入用户名"
              size="large"
            />
          </FormItem>
          <FormItem prop="password">
            <Input
              type="password"
              v-model="formInline.password"
              prefix="ios-lock-outline"
              placeholder="请输入密码"
              size="large"
            />
          </FormItem>
          <FormItem>
            <Button type="primary" long :loading="loading" size="large" @click="handleSubmit('formInline')" class="btn"
              >登录</Button
            >
          </FormItem>
        </Form>
      </div>
    </div>
    
    <!-- 验证码已移除，只需用户名密码登录 -->
  </div>
</template>
<script>
import { AccountLogin, loginInfoApi } from '@/api/account';
import { getWorkermanUrl } from '@/api/kefu';
// import mixins from '../mixins'
import Setting from '@/setting';
import { setCookies } from '@/libs/util';
import '../../../assets/js/canvas-nest.min';
// import '../../../assets/js/jigsaw.js';
// 验证码已移除
export default {
  // mixins: [mixins],
  components: {
  },
  data() {
    return {
      fullWidth: document.documentElement.clientWidth,
      swiperOption: {
        pagination: '.swiper-pagination',
        autoplay: true,
      },
      loading: false,
      isShow: false,
      autoLogin: true,
      formInline: {
        username: '',
        password: '',
      },
      ruleInline: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
      },
      errorNum: 0,
      // jigsaw: null,
      login_logo: '',
      swiperList: [],
      defaultSwiperList: require('@/assets/images/sw.jpg'),
      key: '',
    };
  },
  created() {
    var _this = this;
    top != window && (top.location.href = location.href);
    document.onkeydown = function (e) {
      if (_this.$route.name === 'login') {
        let key = window.event.keyCode;
        if (key === 13) {
          _this.handleSubmit('formInline');
        }
      }
    };
    window.addEventListener('resize', this.handleResize);
  },
  watch: {
    fullWidth(val) {
      // 为了避免频繁触发resize函数导致页面卡顿，使用定时器
      if (!this.timer) {
        // 一旦监听到的screenWidth值改变，就将其重新赋给data里的screenWidth
        this.screenWidth = val;
        this.timer = true;
        let that = this;
        setTimeout(function () {
          // 打印screenWidth变化的值
          that.timer = false;
        }, 400);
      }
    },
    $route(n) {
      // 数字验证码已移除，只使用滑块验证码
    },
  },
  mounted: function () {
    this.$nextTick(() => {
      // /* eslint-disable */
      let that = this;
      // this.jigsaw = jigsaw.init({
      //   el: this.$refs.captcha,
      //   onSuccess() {
      //     that.modals = false;
      //     that.closeModel();
      //   },
      //   onFail: this.closefail,
      //   onRefresh() {},
      // });
      if (this.screenWidth < 768) {
        document.getElementsByTagName('canvas')[0].removeAttribute('class', 'index_bg');
      } else {
        document.getElementsByTagName('canvas')[0].className = 'index_bg';
      }
      this.swiperData();
    });
    // 数字验证码已移除，只使用滑块验证码
  },
  methods: {
    swiperData() {
      loginInfoApi()
        .then((res) => {
          localStorage.setItem('ADMIN_TITLE', res.data.site_name);
          let data = res.data || {};
          this.login_logo = data.login_logo ? data.login_logo : require('@/assets/images/logo.png');
          this.swiperList = data.slide.length ? data.slide : [{ slide: this.defaultSwiperList }];
          this.key = data.key;
        })
        .catch((err) => {
          this.$Message.error(err);
          this.login_logo = require('@/assets/images/logo.png');
          this.swiperList = [{ slide: this.defaultSwiperList }];
        });
    },
    // 登录处理（无验证码）
    closeModel() {
      this.isShow = false;
      let msg = this.$Message.loading({
        content: '登录中...',
        duration: 0,
      });
      this.loading = true;
      AccountLogin({
        account: this.formInline.username,
        pwd: this.formInline.password,
      })
        .then(async (res) => {
          console.log('[LOGIN] Response received:', res);
          msg();
          let data = res.data;
          console.log('[LOGIN] Data:', data);
          console.log('[LOGIN] user_info:', data.user_info);
          console.log('[LOGIN] token:', data.token);
          console.log('[LOGIN] expires_time:', data.expires_time);

          let expires = this.getExpiresTime(data.expires_time);
          console.log('[LOGIN] Calculated expires:', expires);

          // 记录用户登陆信息
          setCookies('uuid', data.user_info.id, expires);
          setCookies('token', data.token, expires);
          setCookies('expires_time', data.expires_time, expires);
          console.log('[LOGIN] Cookies set, checking:', document.cookie);

          this.$store.commit('userInfo/uniqueAuth', data.unique_auth);
          this.$store.commit('userInfo/userInfo', data.user_info);
          // 保存菜单信息
          this.$store.commit('menus/setopenMenus', []);
          this.$store.commit('menus/getmenusNav', data.menus);

          // 记录用户信息
          this.$store.commit('userInfo/name', data.user_info.account);
          this.$store.commit('userInfo/avatar', data.user_info.head_pic);
          this.$store.commit('userInfo/access', data.unique_auth);
          this.$store.commit('userInfo/logo', data.logo);
          this.$store.commit('userInfo/logoSmall', data.logo_square);
          this.$store.commit('userInfo/version', data.version);
          this.$store.commit('userInfo/newOrderAudioLink', data.newOrderAudioLink);

          // if (this.jigsaw) this.jigsaw.reset();

          // 登录成功，跳转到首页
          this.$router.replace({ path: '/admin/home/' }).catch(err => {
            // 忽略导航重复错误
            if (err.name !== 'NavigationDuplicated') {
              console.error('Navigation error:', err);
            }
          });
        })
        .catch((res) => {
          console.log('[LOGIN] Error caught:', res);
          msg();
          let data = res === undefined ? {} : res;
          this.errorNum++;
          this.$Message.error(data.msg || '登录失败');
          // if (this.jigsaw) this.jigsaw.reset();
        });
      setTimeout((e) => {
        this.loading = false;
      }, 1000);
    },
    getExpiresTime(expiresTime) {
      let nowTimeNum = Math.round(new Date() / 1000);
      let expiresTimeNum = expiresTime - nowTimeNum;
      return parseFloat(parseFloat(parseFloat(expiresTimeNum / 60) / 60) / 24);
    },
    closefail() {
      // if (this.jigsaw) this.jigsaw.reset();
      this.$Message.error('校验错误');
    },
    handleResize(event) {
      this.fullWidth = document.documentElement.clientWidth;
      if (this.fullWidth < 768) {
        document.getElementsByTagName('canvas')[0].removeAttribute('class', 'index_bg');
      } else {
        document.getElementsByTagName('canvas')[0].className = 'index_bg';
      }
    },
    handleSubmit(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          // 直接登录，不需要验证码
          this.closeModel();
        }
      });
    },
  },
  beforeCreate() {
    if (this.fullWidth < 768) {
      document.getElementsByTagName('canvas')[0].removeAttribute('class', 'index_bg');
    } else {
      document.getElementsByTagName('canvas')[0].className = 'index_bg';
    }
  },
  beforeDestroy: function () {
    window.removeEventListener('resize', this.handleResize);
    document.getElementsByTagName('canvas')[0].removeAttribute('class', 'index_bg');
  },
};
</script>
<style scoped lang="stylus">
.page-account {
  display: flex;
  width: 100%;
  background-image: url('../../../assets/images/bg.jpg');
  background-size: cover;
  background-position: center;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  overflow: auto;
}

.page-account .code {
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-account .code .pictrue {
  height: 40px;
}

.swiperPross {
  border-radius: 12px 0px 0px 12px;
}

.swiperPross, .swiperPic, .swiperPic img {
  width: 510px;
  height: 100%;
}

.swiperPic img {
  width: 100%;
  height: 100%;
}

.container {
  height: 400px !important;
  padding: 0 !important;
  border-radius: 12px;
  z-index: 1;
  display: flex;
}

.containerSamll {
  /* width: 56% !important; */
  background: #fff !important;
}

.containerBig {
  width: auto !important;
  background: #f7f7f7 !important;
}

.index_from {
  padding: 0 40px 32px 40px;
  height: 400px;
  box-sizing: border-box;
}

.page-account-top {
  padding: 20px 0 !important;
  box-sizing: border-box !important;
  display: flex;
  justify-content: center;
}

.page-account-container {
  border-radius: 0px 6px 6px 0px;
}

.btn {
  background: linear-gradient(90deg, rgba(25, 180, 241, 1) 0%, rgba(14, 115, 232, 1) 100%) !important;
}

.captchaBox {
  width: 310px;
}

input {
  display: block;
  width: 290px;
  line-height: 40px;
  margin: 10px 0;
  padding: 0 10px;
  outline: none;
  border: 1px solid #c8cccf;
  border-radius: 4px;
  color: #6a6f77;
}

#msg {
  width: 100%;
  line-height: 40px;
  font-size: 14px;
  text-align: center;
}

a:link, a:visited, a:hover, a:active {
  margin-left: 100px;
  color: #0366D6;
}

.index_from >>> .ivu-input-large {
  font-size: 14px !important;
}

.from-wh {
  width: 400px;
}

.pull-right {
    float: right!important;
}
.footer{
  position: fixed;
  bottom: 0;
  width: 100%;
  left: 0;
  margin: 0;
  background: rgba(255,255,255,.8);
  border-top: 1px solid #e7eaec;
  overflow: hidden;
  padding: 10px 20px;
  height: 36px;
}
</style>
