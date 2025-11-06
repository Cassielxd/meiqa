<template>
  <div class="login-page">
    <div class="login-card">
<!--      <div class="login-logo">
        <img :src="login_logo" alt="logo" />
      </div>-->
      <div class="login-title">{{ $t('login.title') }}</div>
      <Form ref="formInline" :model="formInline" :rules="ruleInline" @keyup.enter="handleSubmit('formInline')">
        <FormItem prop="username">
          <Input
            type="text"
            v-model="formInline.username"
            :placeholder="$t('login.usernamePlaceholder')"
            size="large"
          >
            <Icon type="ios-contact-outline" slot="prefix"></Icon>
          </Input>
        </FormItem>
        <FormItem prop="password">
          <Input
            type="password"
            v-model="formInline.password"
            :placeholder="$t('login.passwordPlaceholder')"
            size="large"
          >
            <Icon type="ios-lock-outline" slot="prefix"></Icon>
          </Input>
        </FormItem>
        <FormItem>
          <Button type="primary" long :loading="loading" size="large" @click="handleSubmit('formInline')" class="login-button">
            {{$t('login.loginBtn')}}
          </Button>
        </FormItem>
      </Form>
    </div>
  </div>
</template>
<script>
import { AccountLogin, loginInfoApi } from '@/api/account';
import { getWorkermanUrl } from '@/api/kefu';
import Setting from '@/setting';
import { setCookies } from '@/libs/util';
import '../../../assets/js/canvas-nest.min';

export default {
  components: {},
  data() {
    return {
      fullWidth: document.documentElement.clientWidth,
      loading: false,
      formInline: {
        username: '',
        password: '',
      },
      errorNum: 0,
      login_logo: '',
      key: '',
    };
  },
  computed: {
    ruleInline() {
      return {
        username: [{ required: true, message: this.$t('login.usernamePlaceholder'), trigger: 'blur' }],
        password: [{ required: true, message: this.$t('login.passwordPlaceholder'), trigger: 'blur' }],
      };
    },
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
  mounted: function () {
    this.$nextTick(() => {
      this.loginInfo();
    });
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
    closeModel() {
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
          msg();
          let data = res.data;
          let expires = this.getExpiresTime(data.expires_time);

          setCookies('uuid', data.user_info.id, expires);
          setCookies('token', data.token, expires);
          setCookies('expires_time', data.expires_time, expires);

          this.$store.commit('userInfo/uniqueAuth', data.unique_auth);
          this.$store.commit('userInfo/userInfo', data.user_info);
          this.$store.commit('menus/setopenMenus', []);
          this.$store.commit('menus/getmenusNav', data.menus);
          this.$store.commit('userInfo/name', data.user_info.account);
          this.$store.commit('userInfo/avatar', data.user_info.head_pic);
          this.$store.commit('userInfo/access', data.unique_auth);
          this.$store.commit('userInfo/logo', data.logo);
          this.$store.commit('userInfo/logoSmall', data.logo_square);
          this.$store.commit('userInfo/version', data.version);
          this.$store.commit('userInfo/newOrderAudioLink', data.newOrderAudioLink);

          this.$router.replace({ path: '/admin/home/' }).catch(err => {
            if (err.name !== 'NavigationDuplicated') {
              console.error('Navigation error:', err);
            }
          });
        })
        .catch((res) => {
          msg();
          let data = res === undefined ? {} : res;
          this.errorNum++;
          this.$Message.error(data.msg || '登录失败');
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
    handleSubmit(name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          this.closeModel();
        }
      });
    },
  },
  beforeDestroy: function () {
    window.removeEventListener('resize', this.handleResize);
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
  max-width: 400px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.2);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  text-align: center;
}

.login-logo {
  margin-bottom: 20px;
  img {
    max-width: 150px;
    height: auto;
  }
}

.login-title {
  font-size: 26px;
  font-weight: 600;
  color: #333;
  margin-bottom: 35px;
}

.login-button {
  background: linear-gradient(90deg, #4b79ff, #3d6ae6) !important;
  border: none;
  font-size: 16px;
  height: 45px !important;
  &:hover {
    background: linear-gradient(90deg, #3d6ae6, #4b79ff) !important;
  }
}

// iview overrides
.login-card >>> .ivu-input-large {
  font-size: 14px !important;
  height: 45px !important;
}

.login-card >>> .ivu-form-item {
  margin-bottom: 25px;
}

.login-card >>> .ivu-input-prefix i {
    font-size: 18px !important;
    line-height: 45px !important;
}
</style>
