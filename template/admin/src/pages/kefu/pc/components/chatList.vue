<template>
  <div class="chatList">
    <div class="search_box">
      <Input :placeholder="$t('kefu.search')" @on-enter="bindSearch" @on-change="inputChange" />
    </div>
    <div class="tab-head">
      <div class="item" :class="{active:item.key == hdTabCur}" v-for="(item, index) in hdTab" :key="index" @click="changeTab(item)">{{item.title}}</div>
    </div>
    <div class="scroll-box">

      <vue-scroll :ops="ops" @handle-scroll="handleScroll" v-if="userList.length>0">
        <div class="chat-item" v-for="(item,index) in userList" :key="index" :class="{active:curId == item.id}" @click="selectUser(item,index)">
          <div class="avatar">
            <img v-lazy="item.avatar" alt="">
            <div class="status" :class="{off:item.online == 0}"></div>
          </div>
          <div class="user-info">
            <div class="hd">
              <span class="name line1">{{ item.nickname }}</span>
              <template >
                <span class="label pc">default</span>
              </template>
            </div>
            <div class="bd line1">
              <template v-if="item.message_type <=2">{{item.message}}</template>
              <template v-if="item.message_type ==3">[ Image]</template>
              <template v-if="item.message_type ==5">[ Product]</template>
              <template v-if="item.message_type ==6">[ Order]</template>
            </div>
          </div>
          <div class="right-box">
            <div class="time">{{item.update_time | toDay}}</div>
            <div class="num">
              <Badge :count="item.mssage_num">
                <a href="#" class="demo-badge"></a>
              </Badge>
            </div>
          </div>
        </div>
      </vue-scroll>
      <empty v-else msg="No user list" status="1"></empty>
    </div>

  </div>
</template>

<script>
import { Socket } from '@/libs/socket';
import dayjs from 'dayjs'
import { record, userLabel, userGroupApi } from '@/api/kefu'
import { HappyScroll } from 'vue-happy-scroll'
import empty from "../../components/empty";
import { forEach } from "../../../../libs/tools";
export default {
  name: "chatList",
  props: {
    userOnline: {
      type: Object,
      default: function() {
        return {}
      }
    },
    newRecored: {
      type: Object,
      default: function() {
        return {}
      }
    },
    searchData: {
      type: String,
      default: ''
    },
    isShow:{
      type: Boolean,
      default: false
    }
  },
  components: {
    HappyScroll,
    empty
  },
  watch: {
    userOnline: {
      handler(nVal, oVal) {
        if(!nVal || typeof nVal !== 'object' || !Object.prototype.hasOwnProperty.call(nVal, 'user_id')) {
          return;
        }
        const incomingId = Number(nVal.user_id);
        const matched = this.applyOnlineUpdate(incomingId, nVal);
        if(!matched && nVal.online == 1) {
          this.refreshRecordList();
        }
      },
      deep: true
    },
    searchData: {
      handler(nVal, oVal) {
        if(nVal != oVal) {
          this.nickname = nVal
          this.page = 1
          this.isScroll = true
          this.userList = []
          this.isSearch = true
          this.getList()
        }
      },
      deep: true
    },
    isShow: {
      handler(nVal, oVal) {
        console.log('isShow',nVal)
        if(nVal) {
          this.wsStart()
        }
      },
      deep: true
    }
  },
  data() {
    return {
      hdTabCur: 1,
      hdTab: [],
      userList: [],
      curId: '',
      page: 1,
      limit: 15,
      isScroll: true,
      nickname: '',
      labelId: '',
      groupId: '',
      isSearch: false,
      ops: {
        vuescroll: {
          mode: 'native',
          enable: false,
          tips: {
            deactive: 'Push to Load',
            active: 'Release to Load',
            start: 'Loading...',
            beforeDeactive: 'Load Successfully!'
          },
          auto: false,
          autoLoadDistance: 0,
          pullRefresh: {
            enable: false
          },
          pushLoad: {
            enable: true,
            auto: true,
            autoLoadDistance: 10
          }
        },
        bar: {
          background: '#393232',
          opacity: '.5',
          size: '5px'
        }
      },
      visible: false,
    //   labelOn: -1,
    //   groupOn: -1,
      labelList: [],
      userGroupList: [],
      tabOn: '1',
      refreshingOnline: false
    }
  },
  filters: {
    toDay: function(value) {
      if(!value) return ''
      return dayjs.unix(value).format('MM-DD HH:mm')

    }
  },
  created() {
    this.initI18nData();
  },
  mounted() {

    this.bus.$on('change', data => {
    //   this.nickname = data
    for (const key in data) {
        if (Object.hasOwnProperty.call(data, key)) {
            this[key] = data[key];
        }
    }
    })
    this.getList();
    // this.userLabel();
    // this.wsStart();
    userLabel().then(res => {
        let labelData = Array.isArray(res.data) ? res.data : (res.data && res.data.list ? res.data.list : []);
        labelData.forEach(item => {
            item.labelOn = -1;
        });
        this.labelList = labelData;
    });
    userGroupApi().then(res => {
        let groupData = Array.isArray(res.data) ? res.data : (res.data && res.data.list ? res.data.list : []);
        groupData.forEach(item => {
            item.groupOn = false;
        });
        this.userGroupList = groupData;
    });
  },
  methods: {
      initI18nData() {
        this.hdTab = [
          {
            key: 1,
            title: this.$t('kefu.chatRecord')
          },
          {
            key: 0,
            title: this.$t('kefu.visitor')
          }
        ];
      },
      onPopperShow() {
          this.labelId = '';
          this.groupId = '';
      },
      onFilter() {
          if (this.tabOn == '1') {
            this.labelList.forEach(item => {
                if (item.labelOn != -1) {
                    this.labelId += this.labelId ? `,${item.labelOn}` : item.labelOn;
                }
            });
            // if (!this.labelId) {
            //     return this.$Message.info('请选择标签筛选条件');
            // }
          } else {
            this.userGroupList.forEach(item => {
                if (item.groupOn) {
                    this.groupId += this.groupId ? `,${item.id}` : item.id;
                }
            });
            // if (!this.groupId) {
            //   return this.$Message.info('请选择分组筛选条件');
            // }
          }
          this.nickname = '';
          this.page = 1;
          this.isScroll = true
          this.userList = []
          this.isSearch = true
          this.getList();
          this.visible = false;
      },
    // 搜索
    bindSearch(e) {
      this.$emit('search', e.target.value);
    },
    // inputChange
    inputChange(e) {
      console.log(e.target.value)
      this.bus.$emit('change', { nickname: e.target.value })
    },
    deleteUserList(item){
      this.userList.forEach((el, index, arr) => {
        if(el.id == item.id){
          this.userList.splice(index,1)
        }
      })
      if(this.userList.length){
        this.selectUser(this.userList[0],0)
      }
    },
    updateUserList(data,op){
      let ids = [];
      this.userList.map(item=>{
        ids.push(item.id)
        if (item.id === data.id) {
          item.message = data.message
          item._update_time = data._update_time
        }
      })
      if(ids.indexOf(data.id) === -1 && op) {
        this.userList.unshift(data);
      }
    },
    wsStart() {
      let that = this
      this.bus.pageWs.then(ws => {
        // 用户转接
        ws.$on('transfer', data => {
          let status = false
          that.userList.forEach((el, index, arr) => {
            if(data.recored.id == el.id) {
              status = true
              let oldVal = data.recored
              arr.splice(index, 1)
              if(index == 0) {
                oldVal.index = index
                this.$emit('setDataId', oldVal)
                oldVal.mssage_num = 0
              }
              arr.unshift(oldVal)

              this.$Notice.info({
                title: this.$t('kefu.transferSuccess')
              });
            }
          })
          if(!status) {
            if(data.recored.is_tourist == this.hdTabCur) { this.userList.unshift(data.recored) }
          }
        })
        //已被转接走
        ws.$on('rm_transfer',data=>{
          let rmIndex = -1;
          that.userList.forEach((value, index) => {
            if(value.id == data.recored.id){
              rmIndex = index
            }
          })
          if(rmIndex !== -1){
            this.userList.splice(rmIndex,1)
            if(this.userList.length){
              this.$emit('setDataId', this.userList[0])
            }
          }
        })
        ws.$on('mssage_num', data => {
          // console.log('mssage_num',data)
          if(data.recored.id) {
            let status = false
            that.userList.forEach((el, index, arr) => {
              if(data.recored.id == el.id) {
                status = true
                let oldVal = data.recored
                arr.splice(index, 1)
                arr.unshift(oldVal)
              }
            })
            if(!status) {
              if(data.recored.is_tourist == this.hdTabCur) { this.userList.unshift(data.recored) }
            }
          }


          if(data.recored.is_tourist != this.hdTabCur && data.recored.id) {
            this.$Notice.info({
              title: this.$t('kefu.sendMessage')
            });
          }

        })
      });
    },
    //切换
    changeTab(item) {
      if(this.hdTabCur == item.key) return
      this.hdTabCur = item.key
      this.isScroll = true
      this.page = 1
      this.userList = []
      this.$emit('changeType', item.key)
      this.getList()
    },
    getList() {
      if(!this.isScroll) return Promise.resolve();
      return record({
        nickname: this.nickname,
        labelId: this.labelId,
        groupId: this.groupId,
        page: this.page,
        limit: this.limit,
        is_tourist: this.hdTabCur === 1 ? '' : 1
      }).then(res => {
        // 兼容Java后端返回格式：可能是数组或对象{list: [], total: 0}
        let dataList = Array.isArray(res.data) ? res.data : (res.data && res.data.list ? res.data.list : []);

        if(dataList.length > 0) {
          dataList[0].mssage_num = 0
          this.isScroll = dataList.length >= this.limit

          this.userList = this.userList.concat(dataList)

          if(this.page == 1 && dataList.length > 0 && !this.isSearch) {
            this.curId = dataList[0].id
            dataList[0].index = 0
            this.$emit('setDataId', dataList[0])
          }
          this.page++
        } else {
          this.$emit('setDataId', 0)
        }

      })
    },
    refreshRecordList() {
      if(this.refreshingOnline) {
        return;
      }
      this.refreshingOnline = true;
      const params = {
        nickname: this.nickname,
        labelId: this.labelId,
        groupId: this.groupId,
        page: 1,
        limit: this.limit,
        is_tourist: this.hdTabCur === 1 ? '' : 1
      };
      record(params)
        .then(res => {
          let dataList = Array.isArray(res.data) ? res.data : (res.data && res.data.list ? res.data.list : []);
          if(dataList.length > 0) {
            dataList[0].mssage_num = dataList[0].mssage_num || 0;
            const merged = [...dataList, ...this.userList];
            const seen = new Set();
            const makeKey = (item) => {
              if(!item) return `empty-${Math.random()}`;
              if(item.id) return `id-${item.id}`;
              if(item.user_id) return `user-${item.user_id}`;
              return `hash-${Math.random()}`;
            };
            this.userList = merged.filter(item => {
              const key = makeKey(item);
              if(seen.has(key)) {
                const existing = this.userList.find(u => makeKey(u) === key);
                if(existing && item.online != null) {
                  existing.online = item.online;
                }
                if(existing && item.nickname) {
                  existing.nickname = item.nickname;
                }
                if(existing && item.avatar) {
                  existing.avatar = item.avatar;
                }
                return false;
              }
              seen.add(key);
              return true;
            });
          }
        })
        .finally(() => {
          this.refreshingOnline = false;
        });
    },
    chartReachBottom() {
      this.getList()
    },
    // 选择用户
    selectUser(item,index) {
      if(this.curId == item.id) return
      item.mssage_num = 0
      this.curId = item.id
      item.index = index;
      this.$emit('setDataId', item)
    },
    handleScroll(vertical, horizontal, nativeEvent) {
      if(vertical.process == 1) {
        this.getList()
      }
    }
  }
}
</script>

<style lang="stylus" scoped>
.chatList {
  display: flex;
  flex-direction: column;
  width: 320px;
  height: 742px;
  border-right: 1px solid #E5E7EB;
  background: #FAFBFC;

  .tab-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 52px;
    flex-shrink: 0;
    padding: 0 52px;
    font-size: 14px;
    color: #374151;
    background: #F5F6F8;
    border-bottom: 1px solid #E5E7EB;

    .item {
      position: relative;
      cursor: pointer;
      padding: 6px 12px;
      border-radius: 8px;
      transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
      font-weight: 500;

      &:hover {
        color: #4F46E5;
        background: #F3F4F6;
        transform: translateY(-1px);
      }

      &:after {
        display: none;
        content: ' ';
        position: absolute;
        left: 50%;
        bottom: -16px;
        transform: translateX(-50%);
        height: 3px;
        width: 100%;
        background: linear-gradient(90deg, #4F46E5 0%, #8B5CF6 100%);
        border-radius: 3px 3px 0 0;
      }

      &.active {
        color: #4F46E5;

        &:after {
          display: block;
        }
      }
    }
  }

  .scroll-box {
    flex: 1;
    height: 500px;
    overflow: hidden;
  }

  .chat-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 12px;
    height: 78px;
    box-sizing: border-box;
    border-left: 3px solid transparent;
    cursor: pointer;
    transition: all 0.2s ease;
    background: #F5F6F8;
    margin: 2px 8px;
    border-radius: 8px;

    &:hover {
      background: linear-gradient(90deg, #FAFBFC 0%, #F9FAFB 100%);
      transform: translateX(3px);
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.06), 0 2px 4px rgba(0, 0, 0, 0.04);
    }

    &.active {
      background: linear-gradient(90deg, #EEF2FF 0%, #F9FAFB 100%);
      border-left-color: #4F46E5;
      box-shadow: 0 4px 12px rgba(79, 70, 229, 0.15), 0 2px 6px rgba(79, 70, 229, 0.1);
      transform: translateX(5px);

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 3px;
        background: linear-gradient(180deg, #4F46E5 0%, #8B5CF6 100%);
        border-radius: 0 4px 4px 0;
        box-shadow: 0 0 10px rgba(79, 70, 229, 0.4);
      }
    }

    .avatar {
      position: relative;
      width: 44px;
      height: 44px;
      flex-shrink: 0;

      img {
        display: block;
        width: 100%;
        height: 100%;
        border-radius: 50%;
        object-fit: cover;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1), 0 0 0 2px rgba(255, 255, 255, 0.8);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      }

      &:hover img {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 0 0 0 3px rgba(255, 255, 255, 0.9), 0 0 0 5px rgba(79, 70, 229, 0.1);
        transform: scale(1.05);
      }

      .status {
        position: absolute;
        right: 0;
        bottom: 0;
        width: 10px;
        height: 10px;
        background: #10B981;
        border: 2px solid #fff;
        border-radius: 50%;
        box-shadow: 0 0 0 2px white, 0 0 8px rgba(16, 185, 129, 0.4);
        animation: status-pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;

        &.off {
          background: #9CA3AF;
          box-shadow: 0 0 0 2px white;
          animation: none;
        }
      }

      @keyframes status-pulse {
        0%, 100% {
          opacity: 1;
        }
        50% {
          opacity: 0.7;
        }
      }
    }

    .user-info {
      width: 155px;
      margin-left: 12px;
      margin-top: 5px;
      font-size: 16px;

      .hd {
        display: flex;
        align-items: center;
        color: rgba(0, 0, 0, 0.65);

        .name {
          max-width: 67%;
        }

        .label {
          margin-left: 5px;
          color: #3875EA;
          font-size: 12px;
          background: #D8E5FF;
          border-radius: 10px;
          padding: 2px 8px;
          font-weight: 500;
          letter-spacing: 0.3px;

          &.H5 {
            background: #FAF1D0;
            color: #DC9A04;
          }

          &.wechat {
            background: rgba(64, 194, 73, 0.16);
            color: #40C249;
          }

          &.pc {
            background: rgba(100, 64, 194, 0.16);
            color: #6440C2;
          }
        }
      }

      .bd {
        margin-top: 3px;
        font-size: 12px;
        color: #8E959E;
      }
    }

    .right-box {
      position: relative;
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      color: #9CA3AF;
      font-size: 12px;

      .time {
        font-weight: 500;
      }

      .num {
        margin-top: 4px;

        /deep/ .ivu-badge-count {
          background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%);
          box-shadow: 0 2px 4px rgba(239, 68, 68, 0.3);
          animation: badge-pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
          font-weight: 600;
          font-size: 11px;
          min-width: 18px;
          height: 18px;
          line-height: 18px;
          padding: 0 5px;
        }

        @keyframes badge-pulse {
          0%, 100% {
            transform: scale(1);
          }
          50% {
            transform: scale(1.05);
          }
        }
      }
    }
  }
}

.chart-scroll {
  margin-top: -10px;
}

.search_box {
  margin: 12px 12px 8px 12px;

  /deep/ .ivu-input-wrapper {
    .ivu-input {
      border-radius: 10px;
      background: #F3F4F6;
      border: 2px solid transparent;
      transition: all 0.2s ease;
      padding: 8px 12px;
      font-size: 14px;

      &:focus {
        background: #FAFBFC;
        border-color: #4F46E5;
        box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
      }

      &::placeholder {
        color: #9CA3AF;
      }
    }

    .ivu-input-prefix, .ivu-input-suffix {
      i {
        color: #6B7280;
        font-size: 16px;
      }
    }
  }
}

.ivu-input-wrapper {
    /deep/ .ivu-poptip-body {
        padding: 0;
    }

    /deep/ .ivu-tabs-nav {
        float: none;
        display: inline-block;

        .ivu-tabs-ink-bar {
            background-color: #1890FF;
        }
    }

    /deep/ .ivu-tabs-tab {
        height: 50px;
        padding: 0 16px;
        line-height: 50px;

        &.ivu-tabs-tab-focused {
            color: #1890FF;
        }
    }

    .ivu-tabs-tabpane {
        display: flex;
        flex-direction: column;
        min-height: 300px;
        padding: 14px;

        .item-group {
            flex: 1;
            min-height: 0;
        }

        .item {
            ~ .item {
                margin-top: 14px;
            }
        }

        .item-title {
            font-size: 13px;
            line-height: 18px;
            text-align: left;
            color: #333333;
        }

        .cell-group {
            margin: 12px -8px 0 0;
            text-align: left;
            white-space: normal;
        }

        .cell {
            display: inline-block;
            height: 28px;
            padding: 0 12px;
            border-radius: 2px;
            margin: 0 8px 8px 0;
            background-color: #EEEEEE;
            font-size: 13px;
            line-height: 28px;
            color: #333333;
            cursor pointer;

            &.on {
                background-color: #1890FF;
                color: #FFFFFF;
            }
        }

        .button-group {
            text-align: right;
        }

        .ivu-btn-primary {
            width: 76px;
            height: 28px;
            padding: 0;
            margin: 0;
            background-color: #1890FF;
            font-size: 13px;
            line-height: 26px;
            color: #FFFFFF;

            &.ivu-btn-ghost {
                margin-right: 10px;
                border-color: #1890FF;
                background-color: transparent;
                color: #1890FF;
            }
        }
    }
}
</style>
    applyOnlineUpdate(userId, payload) {
      let matched = false;
      this.userList.forEach(el => {
        if(Number(el.user_id) === userId) {
          matched = true;
          if(Object.prototype.hasOwnProperty.call(payload, 'online')) {
            el.online = payload.online;
            if(payload.online == 1) {
              this.$Notice.info({
                title: this.$t('kefu.online'),
                desc: `${payload.nickname || el.nickname || ''} ${this.$t('kefu.online')}`
              });
            } else if(payload.online == 0) {
              el.online = 0;
            }
          }
          if(payload.nickname) {
            el.nickname = payload.nickname;
          }
          if(payload.avatar) {
            el.avatar = payload.avatar;
          }
        }
      });
      return matched;
    },
