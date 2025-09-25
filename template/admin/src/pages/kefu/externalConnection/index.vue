<template>
  <div class="redirect_container">

  </div>
</template>
<script>
import { mapState } from 'vuex';
import Cookies from "js-cookie";
import { setLoc, getLoc } from '@/libs/util'

export default {
  created() {
    this.redirect();
  },
  computed: {
    ...mapState('media', ['isMobile']),
  },
  methods: {
    // 重定向方法，判断是否是移动端环境，跳转对应界面
    redirect() {
      //      console.log('index');
      let tokenName = this.$route.query.tokenName || 'token';
      const token = this.$route.query[tokenName];
      setLoc('mobile_token', token);

      // 保留所有查询参数，包括语言参数
      const queryParams = { ...this.$route.query };

      if(this.$route.query.deviceType == "pc" || this.$route.query.deviceType == "Desktop") {
        this.$router.replace({ name: 'customerServerPc', query: queryParams });
        return;
      }

      if(this.$route.query.deviceType == "Mobile") {
        this.$router.replace({ name: 'customerServerMobile', query: queryParams });
        return;
      }

      if(this.isMobile) {
        this.$router.replace({ name: 'customerServerMobile', query: queryParams });
      } else {
        this.$router.replace({ name: 'customerServerPc', query: queryParams });
      }
    }
  }
}
</script>
