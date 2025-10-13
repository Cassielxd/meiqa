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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.register-container {
  max-width: 480px;
  width: 100%;
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.logo-section {
  text-align: center;
  margin-bottom: 40px;
  color: #fff;
}

.logo-img {
  height: 60px;
  margin-bottom: 24px;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.15));
}

.welcome-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  letter-spacing: -0.5px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.welcome-subtitle {
  font-size: 16px;
  margin: 0;
  opacity: 0.95;
  font-weight: 400;
}

.form-card {
  background: #fff;
  border-radius: 16px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  font-size: 18px;
  color: #a0aec0;
  z-index: 1;
  transition: color 0.3s;
}

.input-wrapper:focus-within .input-icon {
  color: #667eea;
}

>>> .modern-input {
  padding-left: 48px !important;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 15px;
  transition: all 0.3s;
}

>>> .modern-input input {
  padding-left: 48px;
}

>>> .modern-input:hover {
  border-color: #cbd5e0;
}

>>> .modern-input:focus,
>>> .ivu-input-focused .modern-input {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.captcha-row {
  display: flex;
  gap: 12px;
}

.flex-1 {
  flex: 1;
}

.send-code-btn {
  height: 46px;
  min-width: 120px;
  border-radius: 12px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
}

.send-code-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.send-code-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.captcha-img-wrapper {
  width: 120px;
  height: 46px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7fafc;
}

.captcha-img-wrapper:hover {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.captcha-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-loading {
  font-size: 12px;
  color: #a0aec0;
}

.register-btn {
  height: 52px;
  margin-top: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.register-btn:active {
  transform: translateY(0);
}

>>> .ivu-form-item {
  margin-bottom: 20px;
}

>>> .ivu-form-item-error-tip {
  font-size: 13px;
  padding-top: 6px;
}

.footer-links {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #718096;
}

.link-text {
  margin-right: 6px;
}

.link-primary {
  color: #667eea;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.3s;
}

.link-primary:hover {
  color: #764ba2;
  text-decoration: underline;
}

.page-footer {
  text-align: center;
  margin-top: 32px;
  color: #fff;
  font-size: 13px;
  opacity: 0.9;
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 640px) {
  .form-card {
    padding: 32px 24px;
    border-radius: 12px;
  }

  .welcome-title {
    font-size: 28px;
  }

  .logo-img {
    height: 50px;
  }

  .register-btn {
    height: 48px;
  }

  .send-code-btn {
    min-width: 100px;
    font-size: 13px;
  }
}
</style>
