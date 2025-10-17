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
              <span class="name">{{ item.nickname }}</span>
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
        if(nVal.hasOwnProperty('user_id')) {
          this.userList.forEach((el, index) => {
            if(el.to_user_id == nVal.user_id) {
              el.online = nVal.online
              if(nVal.online == 1) {
                this.$Notice.info({
                  title: this.$t('kefu.online'),
                  desc: `${el.nickname} ${this.$t('kefu.online')}`
                });
              }

            }
          })
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
      tabOn: '1'
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
      console.log('[updateUserList] 收到更新:', { data, op });

      // 查找用户在列表中的索引
      const userIndex = this.userList.findIndex(item => item.id === data.id);

      if (userIndex !== -1) {
        // 用户已在列表中：移除旧位置，更新后插入顶部
        const existingUser = this.userList[userIndex];

        // 合并所有字段，新数据优先，但使用fallback保留旧值
        const updatedUser = {
          ...existingUser,  // 保留所有现有字段
          ...data,          // 用新数据覆盖
          // 特殊处理：确保关键字段存在
          message: data.message !== undefined ? data.message : existingUser.message,
          update_time: data.update_time || data._update_time || existingUser.update_time,
          message_type: data.message_type !== undefined ? data.message_type : existingUser.message_type,
          nickname: data.nickname || existingUser.nickname,
          avatar: data.avatar || existingUser.avatar,
          mssage_num: data.mssage_num !== undefined ? data.mssage_num : existingUser.mssage_num,
          online: data.online !== undefined ? data.online : existingUser.online,
          is_tourist: data.is_tourist !== undefined ? data.is_tourist : existingUser.is_tourist
        };

        // 使用 Vue.set 或 splice 确保响应式更新
        this.userList.splice(userIndex, 1);  // 移除旧位置
        this.userList.unshift(updatedUser);  // 插入顶部

        console.log('[updateUserList] 已更新并移至顶部:', updatedUser);
      } else if (op) {
        // 用户不在列表中且 op=true：添加到顶部
        console.log('[updateUserList] 新用户添加到列表，data内容:', data);
        this.userList.unshift(data);
        console.log('[updateUserList] 新用户添加到列表:', data);
      } else {
        console.log('[updateUserList] 用户不在列表且 op=false，跳过添加');
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
      if(!this.isScroll) return
      record({
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
    background: #FFFFFF;
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
    background: #FFFFFF;
    margin: 2px 8px;
    border-radius: 4px;
    border: 1px solid #E5E7EB;

    &:hover {
      background: #F9FAFB;
      border-color: #D1D5DB;
    }

    &.active {
      background: #F3F4F6;
      border-left-color: #4F46E5;
      border-left-width: 3px;
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
        border: 2px solid #E5E7EB;
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

        &.off {
          background: #9CA3AF;
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
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
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
          background: #EF4444;
          font-weight: 500;
          font-size: 11px;
          min-width: 18px;
          height: 18px;
          line-height: 18px;
          padding: 0 5px;
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
      border-radius: 4px;
      background: #FFFFFF;
      border: 1px solid #E5E7EB;
      transition: all 0.2s ease;
      padding: 8px 12px;
      font-size: 14px;

      &:focus {
        border-color: #4F46E5;
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

