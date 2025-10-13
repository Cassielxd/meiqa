<template>
  <div class="register-page">
    <div class="register-container">
      <!-- Logo区域 -->
      <div class="logo-section">
        <img :src="logo" alt="Logo" class="logo-img" />
        <h1 class="welcome-title">Create Your Account</h1>
        <p class="welcome-subtitle">Join us and start your journey</p>
      </div>

      <!-- 注册表单 -->
      <div class="form-card">
        <Form ref="registerForm" :model="formData" :rules="formRules" @submit.native.prevent>
          <!-- 邮箱 -->
          <FormItem prop="account">
            <div class="input-wrapper">
              <Icon type="md-mail" class="input-icon" />
              <Input
                v-model="formData.account"
                type="email"
                size="large"
                placeholder="Email Address"
                class="modern-input"
              />
            </div>
          </FormItem>

          <!-- 邮件验证码 -->
          <FormItem prop="captcha">
            <div class="captcha-row">
              <div class="input-wrapper flex-1">
                <Icon type="md-keypad" class="input-icon" />
                <Input
                  v-model="formData.captcha"
                  size="large"
                  placeholder="Email Verification Code"
                  class="modern-input"
                />
              </div>
              <Button
                type="primary"
                size="large"
                :disabled="sending || countdown > 0"
                :loading="sending"
                @click="handleSendCode"
                class="send-code-btn"
              >
                {{ sending ? 'Sending...' : countdown > 0 ? `${countdown}s` : 'Send Code' }}
              </Button>
            </div>
          </FormItem>

          <!-- 图形验证码 -->
          <FormItem prop="imgcode">
            <div class="captcha-row">
              <div class="input-wrapper flex-1">
                <Icon type="md-lock" class="input-icon" />
                <Input
                  v-model="formData.imgcode"
                  size="large"
                  placeholder="Image Captcha (4 digits)"
                  class="modern-input"
                  :maxlength="4"
                />
              </div>
              <div class="captcha-img-wrapper" @click="loadCaptcha">
                <img v-if="captchaImg" :src="captchaImg" alt="Captcha" class="captcha-img" />
                <div v-else class="captcha-loading">Loading...</div>
              </div>
            </div>
          </FormItem>

          <!-- 租户名称 -->
          <FormItem prop="tenantName">
            <div class="input-wrapper">
              <Icon type="md-business" class="input-icon" />
              <Input
                v-model="formData.tenantName"
                size="large"
                placeholder="Company Name (Optional)"
                class="modern-input"
              />
            </div>
          </FormItem>

          <!-- 联系人 -->
          <FormItem prop="contactName">
            <div class="input-wrapper">
              <Icon type="md-person" class="input-icon" />
              <Input
                v-model="formData.contactName"
                size="large"
                placeholder="Contact Name (Optional)"
                class="modern-input"
              />
            </div>
          </FormItem>

          <!-- 联系电话 -->
          <FormItem prop="contactPhone">
            <div class="input-wrapper">
              <Icon type="md-phone-portrait" class="input-icon" />
              <Input
                v-model="formData.contactPhone"
                size="large"
                placeholder="Phone Number"
                class="modern-input"
              />
            </div>
          </FormItem>

          <!-- 密码 -->
          <FormItem prop="pwd">
            <div class="input-wrapper">
              <Icon type="md-lock" class="input-icon" />
              <Input
                v-model="formData.pwd"
                type="password"
                size="large"
                placeholder="Password (6-32 characters)"
                class="modern-input"
              />
            </div>
          </FormItem>

          <!-- 确认密码 -->
          <FormItem prop="confirmPwd">
            <div class="input-wrapper">
              <Icon type="md-lock" class="input-icon" />
              <Input
                v-model="formData.confirmPwd"
                type="password"
                size="large"
                placeholder="Confirm Password"
                class="modern-input"
              />
            </div>
          </FormItem>

          <!-- 提交按钮 -->
          <FormItem>
            <Button
              type="primary"
              size="large"
              long
              :loading="loading"
              @click="handleRegister"
              class="register-btn"
            >
              {{ loading ? 'Creating Account...' : 'Create Account' }}
            </Button>
          </FormItem>

          <!-- 登录链接 -->
          <div class="footer-links">
            <span class="link-text">Already have an account?</span>
            <router-link to="/tenant/login" class="link-primary">Sign In</router-link>
          </div>
        </Form>
      </div>

      <!-- 页脚说明 -->
      <div class="page-footer">
        <p>By creating an account, you agree to our Terms of Service and Privacy Policy</p>
      </div>
    </div>
  </div>
</template>

<script>
import { AccountRegister, sendRegisterCaptcha, getSimpleCaptcha } from '@/api/account';

export default {
  name: 'TenantRegister',
  data() {
    const validateConfirmPwd = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('Please confirm your password'));
      } else if (value !== this.formData.pwd) {
        callback(new Error('Passwords do not match'));
      } else {
        callback();
      }
    };

    return {
      logo: require('@/assets/images/logo.png'),
      loading: false,
      countdown: 0,
      sending: false,
      countdownTimer: null,
      formData: {
        account: '',
        tenantName: '',
        contactName: '',
        contactPhone: '',
        pwd: '',
        confirmPwd: '',
        captcha: '',
        imgcode: '',
        key: ''
      },
      captchaImg: '',
      formRules: {
        account: [
          { required: true, message: 'Please enter your email address', trigger: 'blur' },
          { type: 'email', message: 'Please enter a valid email address', trigger: 'blur' }
        ],
        captcha: [
          { required: true, message: 'Please enter verification code', trigger: 'blur' },
          { len: 6, message: 'Verification code must be 6 characters', trigger: 'blur' }
        ],
        imgcode: [
          { required: true, message: 'Please enter image captcha', trigger: 'blur' },
          { len: 4, message: 'Image captcha must be 4 characters', trigger: 'blur' }
        ],
        contactPhone: [
          { required: true, message: 'Please enter your phone number', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: 'Please enter a valid phone number', trigger: 'blur' }
        ],
        pwd: [
          { required: true, message: 'Please enter your password', trigger: 'blur' },
          { min: 6, max: 32, message: 'Password must be 6-32 characters', trigger: 'blur' }
        ],
        confirmPwd: [
          { required: true, message: 'Please confirm your password', trigger: 'blur' },
          { validator: validateConfirmPwd, trigger: 'blur' }
        ]
      }
    };
  },
  mounted() {
    this.loadCaptcha();
  },
  beforeDestroy() {
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer);
    }
  },
  methods: {
    async loadCaptcha() {
      try {
        const res = await getSimpleCaptcha();
        this.captchaImg = res.data.img;
        this.formData.key = res.data.key;
      } catch (err) {
        this.$Message.error('Failed to load captcha image');
      }
    },
    async handleSendCode() {
      if (this.sending) {
        return;
      }

      // 验证邮箱
      if (!this.formData.account) {
        this.$Message.error('Please enter your email address first');
        return;
      }

      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(this.formData.account)) {
        this.$Message.error('Please enter a valid email address');
        return;
      }

      this.sending = true;
      let closeLoading = this.$Message.loading({
        content: 'Sending verification code...',
        duration: 0
      });

      try {
        await sendRegisterCaptcha(this.formData.account);
        if (typeof closeLoading === 'function') {
          closeLoading();
          closeLoading = null;
        }

        this.$Message.success('Verification code sent successfully! Please check your email.');

        // 开始倒计时
        if (this.countdownTimer) {
          clearInterval(this.countdownTimer);
          this.countdownTimer = null;
        }
        this.countdown = 60;
        this.countdownTimer = setInterval(() => {
          this.countdown--;
          if (this.countdown <= 0) {
            clearInterval(this.countdownTimer);
            this.countdownTimer = null;
          }
        }, 1000);
      } catch (err) {
        if (typeof closeLoading === 'function') {
          closeLoading();
          closeLoading = null;
        }

        this.$Message.error(err.msg || 'Failed to send verification code');
      } finally {
        if (typeof closeLoading === 'function') {
          closeLoading();
          closeLoading = null;
        }
        this.sending = false;
      }
    },

    handleRegister() {
      this.$refs.registerForm.validate(async (valid) => {
        if (!valid) {
          return;
        }

        this.loading = true;
        const msg = this.$Message.loading({
          content: 'Creating your account...',
          duration: 0
        });

        try {
          const registerData = {
            account: this.formData.account,
            tenant_name: this.formData.tenantName,
            contact_name: this.formData.contactName,
            contact_phone: this.formData.contactPhone,
            pwd: this.formData.pwd,
            confirm_pwd: this.formData.confirmPwd,
            captcha: this.formData.captcha,
            imgcode: this.formData.imgcode,
            key: this.formData.key
          };

          await AccountRegister(registerData);
          msg();

          this.$Message.success({
            content: 'Registration successful! Please wait for admin approval.',
            duration: 5
          });

          // 3秒后跳转到登录页
          setTimeout(() => {
            this.$router.push('/tenant/login');
          }, 3000);
        } catch (err) {
          msg();
          this.formData.captcha = '';
          this.formData.imgcode = '';
          this.loadCaptcha(); // 重新加载图形验证码
          this.$Message.error(err.msg || 'Registration failed, please try again');
        } finally {
          this.loading = false;
        }
      });
    }
  }
};
</script>

<style scoped lang="stylus">
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #86efac 0%, #10b981 45%, #3b82f6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  position: relative;
  overflow: hidden;
}

.register-page::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.2) 0%, transparent 70%);
  animation: float 20s infinite ease-in-out;
}

.register-page::after {
  content: '';
  position: absolute;
  bottom: -50%;
  left: -50%;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(251, 191, 36, 0.2) 0%, transparent 70%);
  animation: float 25s infinite ease-in-out reverse;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(30px, -30px) scale(1.1);
  }
}

.register-container {
  max-width: 500px;
  width: 100%;
  animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  z-index: 1;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.logo-section {
  text-align: center;
  margin-bottom: 48px;
  color: #fff;
}

.logo-img {
  height: 70px;
  margin-bottom: 28px;
  filter: drop-shadow(0 8px 16px rgba(0, 0, 0, 0.12));
  transition: transform 0.3s ease;
}

.logo-img:hover {
  transform: scale(1.05);
}

.welcome-title {
  font-size: 36px;
  font-weight: 800;
  margin: 0 0 12px 0;
  letter-spacing: -0.8px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background: linear-gradient(135deg, #fff 0%, rgba(255, 255, 255, 0.9) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-subtitle {
  font-size: 17px;
  margin: 0;
  opacity: 0.95;
  font-weight: 400;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.form-card {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 52px 44px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.15), 0 0 1px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 18px;
  font-size: 19px;
  color: #9ca3af;
  z-index: 1;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.input-wrapper:focus-within .input-icon {
  color: #10b981;
  transform: scale(1.1);
}

>>> .modern-input {
  padding-left: 52px !important;
  border: 2px solid #e5e7eb;
  border-radius: 14px;
  font-size: 15px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fafafa;
}

>>> .modern-input input {
  padding-left: 52px;
  background: transparent;
}

>>> .modern-input:hover {
  border-color: #d1d5db;
  background: #fff;
}

>>> .modern-input:focus,
>>> .ivu-input-focused .modern-input {
  border-color: #10b981;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.1);
  transform: translateY(-1px);
}

.captcha-row {
  display: flex;
  gap: 14px;
}

.flex-1 {
  flex: 1;
}

.send-code-btn {
  height: 46px;
  min-width: 130px;
  border-radius: 14px;
  font-weight: 700;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
  position: relative;
  overflow: hidden;
}

.send-code-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.send-code-btn:hover:not(:disabled)::before {
  left: 100%;
}

.send-code-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.4);
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
}

.send-code-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: #9ca3af;
  box-shadow: none;
}

.captcha-img-wrapper {
  width: 130px;
  height: 46px;
  border: 2px solid #e5e7eb;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  position: relative;
}

.captcha-img-wrapper::after {
  content: '点击刷新';
  position: absolute;
  bottom: 2px;
  right: 4px;
  font-size: 9px;
  color: #6b7280;
  opacity: 0;
  transition: opacity 0.3s;
}

.captcha-img-wrapper:hover {
  border-color: #10b981;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.1);
  transform: translateY(-1px);
}

.captcha-img-wrapper:hover::after {
  opacity: 1;
}

.captcha-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-loading {
  font-size: 12px;
  color: #9ca3af;
}

.register-btn {
  height: 56px;
  margin-top: 16px;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  border-radius: 14px;
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.5px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.35);
  position: relative;
  overflow: hidden;
}

.register-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s;
}

.register-btn:hover::before {
  left: 100%;
}

.register-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(16, 185, 129, 0.45);
  background: linear-gradient(135deg, #059669 0%, #047857 100%);
}

.register-btn:active {
  transform: translateY(-1px);
}

>>> .ivu-form-item {
  margin-bottom: 22px;
}

>>> .ivu-form-item-error-tip {
  font-size: 13px;
  padding-top: 8px;
  color: #ef4444;
  font-weight: 500;
}

.footer-links {
  text-align: center;
  margin-top: 28px;
  font-size: 14px;
  color: #6b7280;
}

.link-text {
  margin-right: 6px;
}

.link-primary {
  color: #10b981;
  font-weight: 700;
  text-decoration: none;
  transition: all 0.3s;
  position: relative;
}

.link-primary::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: #10b981;
  transition: width 0.3s;
}

.link-primary:hover::after {
  width: 100%;
}

.link-primary:hover {
  color: #059669;
}

.page-footer {
  text-align: center;
  margin-top: 36px;
  color: rgba(255, 255, 255, 0.95);
  font-size: 13px;
  line-height: 1.6;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (max-width: 640px) {
  .form-card {
    padding: 36px 28px;
    border-radius: 20px;
  }

  .welcome-title {
    font-size: 30px;
  }

  .logo-img {
    height: 60px;
  }

  .register-btn {
    height: 52px;
  }

  .send-code-btn {
    min-width: 110px;
    font-size: 13px;
  }

  .captcha-img-wrapper {
    width: 110px;
  }
}
</style>
