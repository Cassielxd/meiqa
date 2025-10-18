<template>
  <div class="wrapper">
    <!-- Left Brand Section -->
    <div class="login-container">
      <div class="brand-section">
        <div class="logo-container">
          <img src="https://mdn.alipayobjects.com/fecodex_image/afts/img/xOLTT5BfbTAAAAAAQLAAAAgAejH3AQBr/original" class="brand-logo" />
          <span class="brand-name">{{ $t('kefu.brandName') }}</span>
        </div>
      </div>
      <div class="tagline-container">
        <span class="tagline-line-1">{{ $t('kefu.tagline1') }}</span>
        <span class="tagline-line-2">{{ $t('kefu.tagline2') }}</span>
      </div>
      <div class="description-container">
        <p class="description-text">{{ $t('kefu.description') }}</p>
      </div>
    </div>

    <!-- Right Sign-in Form -->
    <div class="signin-form">
      <!-- QR Code Switch Icon (top-right) -->
      <div v-if="loginType === 0" class="switch-mode-btn" @click="toggleLoginType">
        <Icon type="ios-qr-scanner" class="switch-icon" />
      </div>
      <div v-else class="switch-mode-btn" @click="toggleLoginType">
        <Icon type="ios-contact" class="switch-icon" />
      </div>

      <!-- Account Login Form -->
      <template v-if="loginType === 0">
        <span class="signin-label">{{ $t('kefu.loginTitle') }}</span>
        <Form ref="formInline" :model="formInline" :rules="ruleInline" @keyup.enter="handleSubmit('formInline')">
          <FormItem prop="username">
            <Input
              type="text"
              v-model="formInline.username"
              :placeholder="$t('kefu.usernameRequired')"
              size="large"
              class="email-input"
            >
              <Icon type="ios-contact-outline" slot="prefix"></Icon>
            </Input>
          </FormItem>
          <FormItem prop="password">
            <Input
              type="password"
              v-model="formInline.password"
              :placeholder="$t('kefu.passwordRequired')"
              size="large"
              class="password-input"
            >
              <Icon type="ios-lock-outline" slot="prefix"></Icon>
            </Input>
          </FormItem>
          <FormItem>
            <Button
              type="primary"
              long
              :loading="loading"
              size="large"
              @click="handleSubmit('formInline')"
              class="signin-button"
            >
              {{ $t('kefu.login') }}
            </Button>
          </FormItem>
        </Form>
      </template>

      <!-- QR Code Login -->
      <template v-if="loginType === 1">
        <span class="signin-label">{{ $t('kefu.appScanLogin') }}</span>
        <div class="code-box">
          <div class="qrcode" ref="qrCodeUrl"></div>
          <div class="rxpired-box" v-show="rxpired">
            <p>{{ $t('kefu.qrcodeExpired') }}</p>
            <Button type="primary" @click="bindRefresh">{{ $t('kefu.refreshQrcode') }}</Button>
          </div>
        </div>
      </template>

      <!-- Footer -->
      <div class="foot-box">
        {{ $t('kefu.copyrightText') }} {{ version }} {{ $t('kefu.systemTitle') }}
      </div>
    </div>
  </div>
</template>

<script>
import { AccountLogin, loginInfoApi, getSanCodeKey, scanStatus, kefuConfig } from '@/api/kefu';
import mixins from '../account/mixins';
import Setting from '@/setting';
import QRCode from 'qrcodejs2';
import { getCookies, setCookies } from '@/libs/util';

export default {
  mixins: [mixins],
  data() {
    return {
      fullWidth: document.documentElement.clientWidth,
      loading: false,
      formInline: {
        username: '',
        password: '',
        code: ''
      },
      ruleInline: {
        username: [
          { required: true, message: this.$t('kefu.usernameRequired'), trigger: 'blur' }
        ],
        password: [
          { required: true, message: this.$t('kefu.passwordRequired'), trigger: 'blur' }
        ]
      },
      loginType: 0, // 0: account, 1: QR code
      codeKey: '',
      scanTime: '',
      rxpired: false,
      isMobile: false,
      version: '',
      isScan: false,
      timeNum: 0
    };
  },
  created() {
    kefuConfig().then(res => {
      this.version = res.data.version;
      if (res.data.site_name) {
        document.title = res.data.site_name;
      }
    });

    this.isMobile = this.$store.state.media.isMobile;
    var _this = this;
    top != window && (top.location.href = location.href);
    document.onkeydown = function(e) {
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
      if (!this.timer) {
        this.screenWidth = val;
        this.timer = true;
        let that = this;
        setTimeout(function() {
          that.timer = false;
        }, 400);
      }
    }
  },
  methods: {
    toggleLoginType() {
      if (this.loginType === 0) {
        // Switch to QR code
        if (!this.isScan) {
          this.isScan = true;
          this.getSanCodeKey();
        }
        this.loginType = 1;
      } else {
        // Switch to account
        this.loginType = 0;
      }
    },
    creatQrCode() {
      let url = `${window.location.protocol}//${window.location.host}/pages/users/scan_login/index?key=${this.codeKey}`;
      var qrcode = new QRCode(this.$refs.qrCodeUrl, {
        text: url,
        width: 180,
        height: 180,
        colorDark: '#000000',
        colorLight: '#ffffff',
        correctLevel: QRCode.CorrectLevel.H
      });
    },
    closeModel() {
      let msg = this.$Message.loading({
        content: this.$t('kefu.loginLoading'),
        duration: 0
      });
      this.loading = true;

      AccountLogin({
        account: this.formInline.username,
        password: this.formInline.password,
        imgcode: this.formInline.code
      }).then(res => {
        msg();

        if (!res.data.token) {
          this.$Message.error(this.$t('kefu.loginFailedNoToken'));
          return;
        }

        let expTime = res.data.exp_time || res.data.expires_time || Math.round(new Date() / 1000) + 7200;
        let expires = this.getExpiresTime(expTime);

        let kefuInfo = res.data.kefuInfo || res.data.kefu_info;
        let kefuUid = kefuInfo ? kefuInfo.uid : null;

        if (!kefuUid) {
          this.$Message.error(this.$t('kefu.loginFailedNoUserInfo'));
          return;
        }

        setCookies('kefu_uuid', kefuUid, expires);
        setCookies('kefu_token', res.data.token, expires);
        setCookies('kefu_expires_time', expTime, expires);
        setCookies('kefuInfo', kefuInfo, expires);

        this.$store.commit('kefu/setInfo', kefuInfo);

        return this.$router.replace({ path: this.$route.query.redirect || '/kefu/pc_list' });

      }).catch(rej => {
        msg();
        let data = rej === undefined ? {} : rej;
        this.$Message.error(data.msg || this.$t('kefu.loginFailed'));
      }).finally(() => {
        this.loading = false;
      });
    },
    getExpiresTime(expiresTime) {
      let nowTimeNum = Math.round(new Date() / 1000);
      let expiresTimeNum = expiresTime - nowTimeNum;
      return parseFloat(parseFloat(parseFloat(expiresTimeNum / 60) / 60) / 24);
    },
    handleResize(event) {
      this.fullWidth = document.documentElement.clientWidth;
    },
    handleSubmit(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          this.closeModel();
        }
      });
    },
    getSanCodeKey() {
      getSanCodeKey().then(res => {
        this.codeKey = res.data.key;
        this.creatQrCode();
        this.scanTime = setInterval(() => {
          this.timeNum++;
          if (this.timeNum >= 60) {
            this.timeNum = 0;
            window.clearInterval(this.scanTime);
            this.rxpired = true;
          } else {
            this.getScanStatus();
          }
        }, 1000);
      }).catch(error => {
        this.timeNum = 0;
        window.clearInterval(this.scanTime);
        this.rxpired = true;
        this.$Message.error(error.msg);
      });
    },
    getScanStatus() {
      scanStatus(this.codeKey).then(async res => {
        if (res.data.status == 0) {
          this.timeNum = 0;
          window.clearInterval(this.scanTime);
          this.rxpired = true;
        }
        if (res.data.status == 3) {
          window.clearInterval(this.scanTime);
          let expires = this.getExpiresTime(res.data.exp_time);
          setCookies('kefu_uuid', res.data.kefuInfo.uid, expires);
          setCookies('kefu_token', res.data.token, expires);
          setCookies('kefu_expires_time', res.data.exp_time, expires);
          setCookies('kefuInfo', res.data.kefuInfo, expires);
          this.$store.commit('kefu/setInfo', res.data.kefuInfo);

          if (this.$store.state.media.isMobile) {
            return this.$router.replace({ path: this.$route.query.redirect || '/kefu/mobile_list' });
          } else {
            return this.$router.replace({ path: this.$route.query.redirect || '/kefu/pc_list' });
          }
        }
      }).catch(error => {
        this.$Modal.error({
          title: this.$t('kefu.tip'),
          content: error.msg
        });
        this.timeNum = 0;
        window.clearInterval(this.scanTime);
        this.rxpired = true;
      });
    },
    bindRefresh() {
      this.$refs.qrCodeUrl.innerHTML = '';
      this.rxpired = false;
      this.getSanCodeKey();
    }
  },
  beforeDestroy: function() {
    this.timeNum = 0;
    if (this.$refs.qrCodeUrl) {
      this.$refs.qrCodeUrl.innerHTML = '';
    }
    window.clearInterval(this.scanTime);
    window.removeEventListener('resize', this.handleResize);
  }
};
</script>

<style scoped lang="stylus">
.wrapper {
  background: #2D4B8E;
  width: 100%;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 80px;
  padding: 60px 80px;
  min-height: 100vh;
  box-sizing: border-box;
  position: relative;
}

.login-container {
  display: flex;
  flex-direction: column;
  max-width: 600px;
  flex-shrink: 0;
}

.brand-section {
  margin-bottom: 40px;
}

.logo-container {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.brand-logo {
  border-radius: 8px;
  flex-shrink: 0;
  width: 56px;
  height: 56px;
}

.brand-name {
  color: #FFFFFF;
  font-size: 48px;
  font-weight: 700;
  margin-left: 16px;
  white-space: nowrap;
}

.tagline-container {
  display: flex;
  flex-direction: column;
  margin-bottom: 30px;
}

.tagline-line-1 {
  color: #FFFFFF;
  font-size: 48px;
  font-weight: 700;
  line-height: 1.2;
  white-space: nowrap;
}

.tagline-line-2 {
  color: #FFFFFF;
  font-size: 48px;
  font-weight: 700;
  line-height: 1.2;
  white-space: nowrap;
}

.description-container {
  max-width: 480px;
}

.description-text {
  color: rgba(255, 255, 255, 0.85);
  font-size: 16px;
  line-height: 1.6;
  margin: 0;
}

.signin-form {
  border-radius: 12px;
  background-color: #FFFFFF;
  border: none;
  display: flex;
  flex-direction: column;
  padding: 40px 36px;
  width: 380px;
  max-width: 380px;
  flex-shrink: 0;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  position: relative;
}

.switch-mode-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  cursor: pointer;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background-color: #2D6FF7;
  transition: all 0.3s ease;
}

.switch-mode-btn:hover {
  background-color: #1E5CE6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(45, 111, 247, 0.3);
}

.switch-icon {
  font-size: 22px;
  color: #FFFFFF;
}

.signin-label {
  color: #1A1A1A;
  font-size: 24px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 32px;
  margin-top: 0;
}

.email-input,
.password-input {
  width: 100%;
  margin-bottom: 16px;
}

.signin-button {
  background: #2D6FF7 !important;
  border: none !important;
  font-size: 15px;
  font-weight: 600;
  color: #FFFFFF !important;
  width: 100%;
  height: 44px !important;
  margin-top: 8px;
  border-radius: 6px !important;
  transition: all 0.3s ease;
}

.signin-button:hover {
  background: #1E5CE6 !important;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(45, 111, 247, 0.4);
}

.code-box {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 0;
  margin-bottom: 16px;
}

.qrcode {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 220px;
  height: 220px;
  border: 1px solid #E8E8E8;
  border-radius: 8px;
}

.rxpired-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 200px;
  height: 200px;
  background: rgba(0, 0, 0, 0.75);
  border-radius: 8px;
}

.rxpired-box p {
  margin-bottom: 16px;
  font-size: 15px;
  color: #FFFFFF;
}

.foot-box {
  padding: 24px 0 0;
  font-size: 12px;
  color: #999999;
  text-align: center;
  margin-top: auto;
  line-height: 1.5;
}

/* Mobile Responsive */
@media (max-width: 1024px) {
  .wrapper {
    flex-direction: column;
    padding: 40px 30px;
  }

  .login-container {
    padding-right: 0;
    margin-bottom: 40px;
    align-items: center;
    text-align: center;
  }

  .brand-section {
    align-items: center;
  }

  .logo-container {
    justify-content: center;
  }

  .tagline-container {
    align-items: center;
  }

  .description-container {
    max-width: 100%;
    text-align: center;
  }

  .signin-form {
    width: 100%;
    max-width: 420px;
    margin: 0 auto;
  }
}

@media (max-width: 768px) {
  .wrapper {
    padding: 30px 20px;
  }

  .brand-name {
    font-size: 36px;
  }

  .tagline-line-1,
  .tagline-line-2 {
    font-size: 36px;
  }

  .description-text {
    font-size: 14px;
  }

  .signin-form {
    padding: 32px 28px;
  }

  .signin-label {
    font-size: 22px;
    margin-bottom: 24px;
  }

  .qrcode {
    width: 180px;
    height: 180px;
  }

  .rxpired-box {
    width: 160px;
    height: 160px;
  }
}

@media (max-width: 480px) {
  .wrapper {
    padding: 20px 16px;
  }

  .brand-logo {
    width: 44px;
    height: 44px;
  }

  .brand-name {
    font-size: 28px;
    margin-left: 12px;
  }

  .tagline-line-1,
  .tagline-line-2 {
    font-size: 28px;
  }

  .description-text {
    font-size: 13px;
  }

  .signin-form {
    padding: 28px 24px;
  }

  .signin-label {
    font-size: 20px;
  }

  .signin-button {
    height: 42px !important;
  }

  .qrcode {
    width: 160px;
    height: 160px;
  }

  .rxpired-box {
    width: 140px;
    height: 140px;
  }
}
</style>
