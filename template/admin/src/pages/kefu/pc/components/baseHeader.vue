<template>
  <div class="base-header">

    <div class="left-wrapper">
      <!-- <Input class="search_box" prefix="ios-search" placeholder="搜索用户名称" @on-enter="bindSearch" @on-change="inputChange" /> -->

      <div class="user_info">
        <img v-lazy="kefuInfo.avatar" alt="">
        <span>{{kefuInfo.nickname}}</span>
        <div class="status-box">
          <div class="status" :class="online ? 'on':'off'" @click.stop="setOnline">
            <span class="dot"></span>
            {{online ? $t('kefu.online'): $t('kefu.offline')}}
          </div>

          <div class="online-down" v-show="isOnline">
            <div class="item" @click.stop="changeOnline(1)"><span class="iconfont iconduihao" v-if="online == 1"></span><i class="green"></i>{{$t('kefu.online')}}</div>
            <div class="item" @click.stop="changeOnline(0)"><span class="iconfont iconduihao" v-if="online == 0"></span><i></i>{{$t('kefu.offline')}}</div>
            <div class="item" @click.stop="changeOnline(3)"><span class="iconfont iconduihao" v-if="online == 3"></span><i class="orange"></i>{{$t('kefu.close')}}</div>
          </div>
        </div>

      </div>
      <!-- <div class="out-btn" @click.stop="outLogin">退出登录</div> -->
    </div>

    <!-- <div class="right-menu">
      <div class="menu-item" :class="{on:index == curIndex }" v-for="(item,index) in menuList" :key="index" @click.stop="selectTab(item)">{{item.title}}</div>
    </div> -->
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import bus from '@/utils/bus'
export default {
  name: "baseHeader",
  props: {
    kefuInfo: {
      type: Object,
      default: function() {
        return {}
      }
    },
    online: {
      type: Boolean | Number,
      default: true
    }
  },
  computed: {
  },
  data() {
    return {
      menuList: [],
      curIndex: 0,
      isOnline: false
    }
  },
  created() {
    this.initI18nData();
  },
  mounted() {
    document.addEventListener('click', () => {
      this.isOnline = false
    })
  },
  methods: {
    ...mapActions('kefu/', [
      'logout',
      'logoutKefu'
    ]),
    initI18nData() {
      this.menuList = [
        {
          key: 0,
          title: this.$t('kefu.customerInfo'),
        },
        {
          key: 1,
          title: this.$t('kefu.viewOrder'),
        },
        {
          key: 2,
          title: this.$t('kefu.viewGoods'),
        },
      ];
    },
    selectTab(item) {
      this.curIndex = item.key
      this.bus.$emit('selectRightMenu', this.curIndex)
    },
    setOnline() {
      this.isOnline = !this.isOnline

    },
    changeOnline(type) {
      if(type == 3) {
        this.outLogin();
        return;
      }
      this.$emit('setOnline', type);
      this.isOnline = false
    },
    // 退出登录
    outLogin() {
      let self = this
      this.$Modal.confirm({
        title: this.$t('kefu.confirm'),
        content: this.$t('kefu.leaveConfirm'),
        onOk: () => {
          self.logoutKefu({
            confirm: false,
            vm: self
          });
        },
        onCancel: () => {

        }
      });

    },
    // 搜索
    bindSearch(e) {
      this.$emit('search', e.target.value);
    },
    // inputChange
    inputChange(e) {
      console.log(e.target.value)
      this.bus.$emit('change', e.target.value)
    }
  }
}
</script>

<style lang="stylus" scoped>
.base-header {
  z-index: 99;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
  padding: 0 24px 0 20px;
  background: linear-gradient(135deg, #F5F6F8 0%, #EBEDEF 100%);
  color: #1F2937;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.12);

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 2px;
    background: linear-gradient(90deg, transparent, rgba(0, 0, 0, 0.12), transparent);
  }

  .left-wrapper {
    position: relative;
    display: flex;
    flex: 1;
    align-items: center;
    justify-content: space-between;
    padding-right: 15px;

    .search_box {
      width: 295px;
      border-radius: 17px;
      overflow: hidden;
    }

    .user_info {
      display: flex;
      align-items: center;

      // margin-left: 30px;
      img {
        width: 44px;
        height: 44px;
        margin-right: 12px;
        border-radius: 50%;
        box-shadow: 0 0 0 3px white, 0 4px 12px rgba(0, 0, 0, 0.15);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        cursor: pointer;
        position: relative;

        &:hover {
          transform: scale(1.08);
          box-shadow: 0 0 0 4px white, 0 6px 16px rgba(0, 0, 0, 0.2), 0 0 0 6px rgba(79, 70, 229, 0.1);
        }
      }

      span {
        font-size: 16px;
        font-weight: 500;
        letter-spacing: 0.3px;
        color: #1F2937;
      }

      .status-box {
        position: relative;
        cursor: pointer;
      }

      .status {
        display: flex;
        align-items: center;
        padding: 6px 12px;
        margin-left: 8px;
        background: white;
        backdrop-filter: blur(10px);
        color: #1F2937;
        border-radius: 20px;
        font-size: 14px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s ease;
        border: 1px solid #E5E7EB;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);

        &:hover {
          background: #F9FAFB;
          transform: translateY(-1px);
          box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        }

        .dot {
          width: 8px;
          height: 8px;
          margin-right: 6px;
          border-radius: 50%;
          background: #10B981;
          box-shadow: 0 0 8px #10B981, 0 0 0 2px rgba(255, 255, 255, 0.3);
          animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
        }

        &.off {
          background: #F3F4F6;

          .dot {
            background: #9CA3AF;
            box-shadow: 0 0 0 2px white;
            animation: none;
          }
        }
      }

      @keyframes pulse {
        0%, 100% {
          opacity: 1;
        }
        50% {
          opacity: 0.7;
        }
      }

      .online-down {
        z-index: 50;
        position: absolute;
        left: 5px;
        bottom: -85px;
        width: 140px;
        background: white;
        backdrop-filter: blur(20px);
        color: #374151;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(0, 0, 0, 0.08);
        border-radius: 12px;
        border: 1px solid #E5E7EB;
        overflow: hidden;
        animation: dropdown-slide 0.2s ease-out;

        .item {
          position: relative;
          display: flex;
          align-items: center;
          padding: 10px 12px 10px 32px;
          cursor: pointer;
          transition: all 0.15s ease;
          font-size: 14px;

          &:hover {
            background: #F3F4F6;
          }

          &:active {
            background: #E5E7EB;
          }

          i {
            width: 10px;
            height: 10px;
            margin-right: 8px;
            border-radius: 50%;
            background: #9CA3AF;
            box-shadow: 0 0 0 2px rgba(156, 163, 175, 0.2);

            &.green {
              background: #10B981;
              box-shadow: 0 0 8px rgba(16, 185, 129, 0.4), 0 0 0 2px rgba(16, 185, 129, 0.2);
            }
          }

          .iconfont {
            position: absolute;
            left: 10px;
            top: 50%;
            transform: translateY(-50%);
            font-size: 14px;
            color: #4F46E5;
          }
        }
      }

      @keyframes dropdown-slide {
        from {
          opacity: 0;
          transform: translateY(-10px);
        }
        to {
          opacity: 1;
          transform: translateY(0);
        }
      }
    }

    .out-btn {
      position: absolute;
      right: 30px;
      top: 50%;
      transform: translateY(-50%);
      width: 86px;
      height: 26px;
      line-height: 28px;
      text-align: center;
      background: #FFFFFF;
      border-radius: 16px;
      color: #3875EA;
      font-size: 13px;
      cursor: pointer;
    }
  }

  .right-menu {
    display: flex;
    align-items: center;

    .menu-item {
      position: relative;
      margin-right: 30px;
      font-size: 14px;
      font-weight: 400;
      cursor: pointer;

      &.on {
        font-weight: 600;

        &::after {
          position: absolute;
          left: 0;
          bottom: -22px;
          content: '';
          width: 100%;
          height: 2px;
          background: #fff;
        }
      }
    }
  }
}

.orange {
  background: #ff6700 !important;
}
</style>
