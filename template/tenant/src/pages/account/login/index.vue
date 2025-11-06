<template>
  <div class="login-page">
    <div class="login-card">
      <!-- Login Form -->
      <template v-if="!isRegister">
        <div class="login-title">{{ $t('login.loginBtn') }}</div>
        <Form ref="formInline" :model="formInline" :rules="ruleInline" @keyup.enter="handleSubmit('formInline')">
          <FormItem prop="username">
            <Input type="text" v-model="formInline.username" :placeholder="$t('login.username')" size="large">
              <Icon type="ios-contact-outline" slot="prefix"></Icon>
            </Input>
          </FormItem>
          <FormItem prop="password">
            <Input type="password" v-model="formInline.password" :placeholder="$t('login.password')" size="large">
               <Icon type="ios-lock-outline" slot="prefix"></Icon>
            </Input>
          </FormItem>
          <FormItem>
            <Button type="primary" long :loading="loading" size="large" @click="handleSubmit('formInline')" class="login-button">
              {{ $t('login.loginBtn') }}
            </Button>
          </FormItem>
          <div class="switch-mode">
            <span>{{ $t('login.noAccount') }}</span>
            <a @click="switchToRegister">{{ $t('login.goRegister') }}</a>
          </div>
        </Form>
      </template>

      <!-- Register Form -->
      <template v-if="isRegister">
        <div class="login-title">{{ $t('login.registerBtn') }}</div>
        <Form ref="registerForm" :model="registerForm" :rules="registerRules" @keyup.enter="handleRegister('registerForm')">
          <FormItem prop="email">
            <Input type="text" v-model="registerForm.email" :placeholder="$t('login.email')" size="large">
              <Icon type="ios-mail-outline" slot="prefix"></Icon>
            </Input>
          </FormItem>
          <FormItem prop="contact_phone">
            <Input type="text" v-model="registerForm.contact_phone" :placeholder="$t('login.phone')" size="large">
              <Icon type="ios-call-outline" slot="prefix"></Icon>
            </Input>
          </FormItem>
          <FormItem prop="password">
            <Input type="password" v-model="registerForm.password" :placeholder="$t('login.password')" size="large">
              <Icon type="ios-lock-outline" slot="prefix"></Icon>
            </Input>
          </FormItem>
           <FormItem prop="confirm_pwd">
            <Input type="password" v-model="registerForm.confirm_pwd" :placeholder="$t('login.confirmPassword')" size="large">
               <Icon type="ios-lock-outline" slot="prefix"></Icon>
            </Input>
          </FormItem>
          <FormItem prop="captcha">
            <div class="captcha-line">
              <Input type="text" v-model="registerForm.captcha" :placeholder="$t('login.emailCaptcha')" size="large" class="captcha-input"/>
              <Button type="primary" size="large" :disabled="sendingCode || countdown > 0" :loading="sendingCode" @click="handleSendEmailCode" class="captcha-btn">
                {{ sendingCode ? $t('login.sending') : countdown > 0 ? `${countdown}${$t('login.seconds')}` : $t('login.sendCaptcha') }}
              </Button>
            </div>
          </FormItem>
           <FormItem prop="code">
             <div class="captcha-line">
              <Input type="text" v-model="registerForm.code" :placeholder="$t('login.captcha')" size="large" class="captcha-input"/>
              <img :src="imgcode" class="captcha-img" @click="captchas" />
             </div>
          </FormItem>
          <FormItem>
            <Button type="primary" long :loading="registerLoading" size="large" @click="handleRegister('registerForm')" class="login-button">
              {{ $t('login.registerBtn') }}
            </Button>
          </FormItem>
          <div class="switch-mode">
            <span>{{ $t('login.hasAccount') }}</span>
            <a @click="switchToLogin">{{ $t('login.goLogin') }}</a>
          </div>
        </Form>
      </template>
    </div>
  </div>
</template>

<script>
import { AccountLogin, AccountRegister, loginInfoApi, captcha_pro, sendRegisterCaptcha } from '@/api/account';
import { getWorkermanUrl } from '@/api/kefu';
import { getStaticMenusAPI, getTransformedMenus } from '@/data/static-menus';
import Setting from '@/setting';
import { setCookies } from '@/libs/util';
import '../../../assets/js/canvas-nest.min';
import Verify from "@/components/verifition/Verify";
import { validateGlobalPhone } from '@/utils/validate';

export default {
  components: {
    Verify,
  },
  data() {
    return {
      fullWidth: document.documentElement.clientWidth,
      loading: false,
      registerLoading: false,
      isRegister: false, // 控制登录/注册页面切换
      imgcode: '',
      sendingCode: false, // 发送邮件验证码状态
      countdown: 0, // 倒计时秒数
      countdownTimer: null, // 倒计时定时器
      formInline: {
        username: '',
        password: '',
        code: '',
        key: '',
      },
      // 注册表单数据
      registerForm: {
        email: '',
        contact_phone:"",
        password: '',
        confirm_pwd: '',
        captcha: '', // 邮件验证码
        code: '',
        key: '',
      },
      errorNum: 0,
      login_logo: '',
      key: '',
    };
  },
  computed: {
    ruleInline() {
      return {
        username: [{ required: true, message: this.$t('login.username'), trigger: 'blur' }],
        password: [{ required: true, message: this.$t('login.password'), trigger: 'blur' }],
      }
    },
    registerRules() {
      return {
        email: [
          { required: true, message: this.$t('login.email'), trigger: 'blur' },
          { type: 'email', message: this.$t('login.emailFormatError'), trigger: 'blur' }
        ],
        contact_phone: [
          { required: true, message: this.$t('login.phone'), trigger: 'blur' },
          { validator: validateGlobalPhone, trigger: 'blur', message: this.$t('login.phoneFormatError') }
        ],
        password: [
          { required: true, message: this.$t('login.password'), trigger: 'blur' },
          { min: 6, message: this.$t('login.passwordMinLength'), trigger: 'blur' }
        ],
        confirm_pwd: [
          { required: true, message: this.$t('login.confirmPassword'), trigger: 'blur' },
          { validator: this.validateConfirmPassword, trigger: 'blur' }
        ],
        captcha: [
          { required: true, message: this.$t('login.emailCaptcha'), trigger: 'blur' },
          { len: 6, message: this.$t('login.emailCaptchaLength'), trigger: 'blur' }
        ],
        code: [{ required: true, message: this.$t('login.captcha'), trigger: 'blur' }],
      }
    }
  },
  created() {
    var _this = this;
    top != window && (top.location.href = location.href);
    document.onkeydown = function (e) {
      if (_this.$route.name === 'login') {
        let key = window.event.keyCode;
        if (key === 13) {
          if (!_this.isRegister) {
            _this.handleSubmit('formInline');
          } else {
            _this.handleRegister('registerForm');
          }
        }
      }
    };
    window.addEventListener('resize', this.handleResize);
  },
  mounted: function () {
    this.$nextTick(() => {
      this.loginInfo();
    });
    this.captchas();
  },
  methods: {
    loginInfo() {
      loginInfoApi()
        .then((res) => {
          localStorage.setItem('ADMIN_TITLE', res.data.site_name);
          let data = res.data || {};
          this.login_logo = data.login_logo ? data.login_logo : require('@/assets/images/logo.png');
          this.key = data.key;
        })
        .catch((err) => {
          this.$Message.error(err);
          this.login_logo = require('@/assets/images/logo.png');
        });
    },
    success(params){
      if (this.isRegister) {
        this.closeRegisterModel(params);
      } else {
        this.closeModel(params);
      }
    },
    closeModel(params) {
      let msg = this.$Message.loading({
        content: this.$t('login.loggingIn'),
        duration: 0,
      });
      this.loading = true;

      const loginData = {
        account: this.formInline.username,
        pwd: this.formInline.password,
        imgcode: this.formInline.code,
        key: this.formInline.key,
        captchaType: 'blockPuzzle',
        captchaVerification: params.captchaVerification,
      };

      AccountLogin(loginData)
        .then(async (res) => {
          msg();
          let data = res.data;
          let expires = this.getExpiresTime(data.expires_time);
          setCookies('uuid', data.tenant_info.id, expires);
          setCookies('tenant_token', data.token, expires);
          setCookies('expires_time', data.expires_time, expires);
          this.$store.commit('userInfo/uniqueAuth', data.unique_auth||"");
          this.$store.commit('userInfo/userInfo', data.tenant_info);
          const menuData = getTransformedMenus();
          this.$store.commit('menus/setopenMenus', []);
          this.$store.commit('menus/getmenusNav', menuData);
          localStorage.setItem('menuList', JSON.stringify(menuData));
          this.$store.commit('userInfo/name', data.tenant_info.account);
          this.$store.commit('userInfo/avatar', data.tenant_info.head_pic);
          this.$store.commit('userInfo/access', data.unique_auth||"");
          this.$store.commit('userInfo/logo', data.logo||"");
          this.$store.commit('userInfo/logoSmall', data.logo_square||"");
          this.$store.commit('userInfo/version', data.version||"1.0.0");
          this.$store.commit('userInfo/newOrderAudioLink', data.newOrderAudioLink||"");
          return this.$router.replace({ path: '/tenant/home/' || '/tenant/' });
        })
        .catch((res) => {
          msg();
          this.formInline.code = '';
          let data = res === undefined ? {} : res;
          this.errorNum++;
          this.captchas();
          this.$Message.error(data.msg || this.$t('login.loginFailed'));
        });
      setTimeout(() => {
        this.loading = false;
      }, 1000);
    },
    getExpiresTime(expiresTime) {
      let nowTimeNum = Math.round(new Date() / 1000);
      let expiresTimeNum = expiresTime - nowTimeNum;
      return parseFloat(parseFloat(parseFloat(expiresTimeNum / 60) / 60) / 24);
    },
    handleResize(event) {
      this.fullWidth = document.documentElement.clientWidth;
    },
    captchas: function () {
      captcha_pro().then(res => {
        if(res.status == 200) {
          this.imgcode = res.data.img;
          this.formInline.key = res.data.key;
          this.registerForm.key = res.data.key;
        }
      })
    },
    handleSubmit(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          this.closeModel({ captchaVerification: '' });
        }
      });
    },
    handleRegister(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          this.closeRegisterModel({ captchaVerification: '' });
        }
      });
    },
    closeRegisterModel(params) {
      let msg = this.$Message.loading({
        content: this.$t('login.registering'),
        duration: 0,
      });
      this.registerLoading = true;

      AccountRegister({
        account: this.registerForm.email,
        pwd: this.registerForm.password,
        confirm_pwd: this.registerForm.confirm_pwd,
        contact_phone: this.registerForm.contact_phone ? this.registerForm.contact_phone.trim() : '',
        captcha: this.registerForm.captcha,
        imgcode: this.registerForm.code,
        key: this.registerForm.key,
        captchaType: 'blockPuzzle',
        captchaVerification: params.captchaVerification,
      })
        .then(async (res) => {
          msg();
          this.$Message.success(this.$t('login.registerSuccess'));
          this.switchToLogin();
          this.resetRegisterForm();
        })
        .catch((res) => {
          msg();
          this.registerForm.code = '';
          let data = res === undefined ? {} : res;
          this.captchas();
          this.$Message.error(data.msg || this.$t('login.registerFailed'));
        });
      setTimeout(() => {
        this.registerLoading = false;
      }, 1000);
    },
    switchToRegister() {
      this.isRegister = true;
      this.resetRegisterForm();
      this.captchas();
    },
    switchToLogin() {
      this.isRegister = false;
      this.formInline.code = '';
      this.captchas();
    },
    resetRegisterForm() {
      this.registerForm = {
        email: '',
        contact_phone: '',
        password: '',
        confirm_pwd: '',
        captcha: '',
        code: '',
        key: '',
      };
      if (this.$refs.registerForm) {
        this.$refs.registerForm.resetFields();
      }
      if (this.countdownTimer) {
        clearInterval(this.countdownTimer);
      }
      this.countdown = 0;
    },
    validateConfirmPassword(rule, value, callback) {
      if (value === '') {
        callback(new Error(this.$t('login.confirmPasswordRequired')));
      } else if (value !== this.registerForm.password) {
        callback(new Error(this.$t('login.passwordMismatch')));
      } else {
        callback();
      }
    },
    async handleSendEmailCode() {
      if (this.sendingCode || this.countdown > 0) return;

      if (!this.registerForm.email) {
        this.$Message.error(this.$t('login.enterEmailFirst'));
        return;
      }
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(this.registerForm.email)) {
        this.$Message.error(this.$t('login.emailFormatError'));
        return;
      }

      this.sendingCode = true;
      let closeLoading = this.$Message.loading({
        content: this.$t('login.sendingCaptcha'),
        duration: 0
      });

      try {
        await sendRegisterCaptcha(this.registerForm.email);
        if (typeof closeLoading === 'function') closeLoading();
        this.$Message.success(this.$t('login.captchaSent'));
        this.countdown = 60;
        this.countdownTimer = setInterval(() => {
          this.countdown--;
          if (this.countdown <= 0) {
            clearInterval(this.countdownTimer);
          }
        }, 1000);
      } catch (err) {
        if (typeof closeLoading === 'function') closeLoading();
        this.$Message.error(err.msg || this.$t('login.sendCaptchaFailed'));
      } finally {
        if (typeof closeLoading === 'function') closeLoading();
        this.sendingCode = false;
      }
    },
  },
  beforeDestroy: function () {
    window.removeEventListener('resize', this.handleResize);
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer);
    }
  },
};
</script>
<style scoped lang="stylus">
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background-image: url('../../../assets/images/bg.jpg');
  background-size: cover;
  background-position: center;
}

.login-card {
  width: 90%;
  max-width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.2);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  text-align: center;
  max-height: 90vh;
  overflow-y: auto;
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 25px;
  margin-top: 0;
}

.login-button {
  background: linear-gradient(90deg, #1890ff, #40a9ff) !important;
  border: none;
  font-size: 16px;
  height: 45px !important;
  &:hover {
    background: linear-gradient(90deg, #40a9ff, #1890ff) !important;
  }
}

.switch-mode {
  margin-top: 15px;
  font-size: 14px;
  color: #666;
  a {
    color: #1890ff;
    margin-left: 5px;
    cursor: pointer;
    &:hover {
      text-decoration: underline;
    }
  }
}

.captcha-line {
  display: flex;
  align-items: center;
  .captcha-input {
    flex: 1;
    margin-right: 10px;
  }
  .captcha-img {
    height: 45px;
    cursor: pointer;
    border-radius: 4px;
  }
  .captcha-btn {
    min-width: 120px;
    font-size: 14px;
  }
}

// iview overrides
.login-card >>> .ivu-input-large {
  font-size: 14px !important;
  height: 45px !important;
}

.login-card >>> .ivu-form-item {
  margin-bottom: 20px;
}

.login-card >>> .ivu-input-prefix i {
    font-size: 18px !important;
    line-height: 45px !important;
}
</style>
