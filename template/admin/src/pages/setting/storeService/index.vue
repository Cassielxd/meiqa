<template>
  <div>

    <div class="i-layout-page-header">
      <div class="i-layout-page-header">
        <span class="ivu-page-header-title">{{$route.meta.title}}</span>
      </div>
    </div>

    <Row class="ivu-mt box-wrapper">
      <Col span="3" class="left-wrapper">
        <Menu :theme="theme3" :active-name="sortName" width="auto">
          <MenuGroup>
            <MenuItem
                    :name="item.id"
                    class="menu-item"
                    :class="index === current ? 'showOn' : ''"
                    v-for="(item, index) in groupList"
                    :key="index"
                    @click.native="bindMenuItem(item, index)"
            >
              {{ item.name }}
              <div class="icon-box" v-if="index != 0">
                <Icon type="ios-more" size="24" @click.stop="showMenu(item,index)" />
              </div>
              <div class="right-menu ivu-poptip-inner" v-show="item.status">
                <div class="ivu-poptip-body" @click="editGroup(item)">
                  <div class="ivu-poptip-body-content">
                    <div class="ivu-poptip-body-content-inner">编辑</div>
                  </div>
                </div>
                <div class="ivu-poptip-body" @click="deleteGroup(item, '删除分组', index)">
                  <div class="ivu-poptip-body-content">
                    <div class="ivu-poptip-body-content-inner">删除</div>
                  </div>
                </div>
                <div class="ivu-poptip-body" @click="lockGroup(item)">
                  <div class="ivu-poptip-body-content">
                    <div class="ivu-poptip-body-content-inner">查看二维码</div>
                  </div>
                </div>
              </div>
            </MenuItem>
          </MenuGroup>
        </Menu>
      </Col>
      <Col span="21" ref="rightBox">
        <Card :bordered="false" dis-hover class="ivu-mt" style="margin-top: 0!important;">

          <Row type="flex" class="mb20">
            <Col span="24">
              <Button  type="primary" style="margin-right: 10px" icon="md-add" @click="editGroup({id:0})">添加标签</Button>
              <Button v-auth="['setting-store_service-add']" type="success" icon="md-add" @click="add" class="mr10">添加客服</Button>
            </Col>
          </Row>

          <Table :columns="columns1" :data="tableList" :loading="loading" highlight-row no-userFrom-text="暂无数据" no-filtered-userFrom-text="暂无筛选结果">
            <template slot-scope="{ row, index }" slot="avatar">
              <div class="tabBox_img" v-viewer>
                <img v-lazy="row.avatar">
              </div>
            </template>
            <template slot-scope="{ row, index }" slot="status">
              <i-switch v-model="row.status" :value="row.status" :true-value="1" :false-value="0" @on-change="onchangeIsShow(row)" size="large">
                <span slot="open">开启</span>
                <span slot="close">关闭</span>
              </i-switch>
            </template>
            <template slot-scope="{ row, index }" slot="online">
              <Tag color="success" v-if="row.online">在线</Tag>
              <Tag color="default" v-else>下线</Tag>
            </template>
            <template slot-scope="{row,index}" slot="group_name">
                <span>{{row.chatgroup.name}}</span>
            </template>
            <template slot-scope="{ row, index }" slot="action">
              <a @click="edit(row)">编辑</a>
              <Divider type="vertical" v-if="row.status" />
              <a @click="goChat(row)" v-if="row.status">进入工作台</a>
              <Divider type="vertical"  />
              <Dropdown @on-click="changeMenu(row,$event,index)">
                <a href="javascript:void(0)">
                  更多
                  <Icon type="ios-arrow-down"></Icon>
                </a>
                <DropdownMenu slot="list">
                  <DropdownItem name="1">查看二维码</DropdownItem>
                  <DropdownItem name="2">自动回复</DropdownItem>
                  <DropdownItem name="3">删除客服</DropdownItem>
                </DropdownMenu>
              </Dropdown>

            </template>
          </Table>

          <div class="acea-row row-right page">
            <Page :total="total" show-elevator show-total @on-change="pageChange" :page-size="tableFrom.limit" />
          </div>
        </Card>
      </Col>
    </Row>


    <auto-reply ref="AutoReply" :userId="userId" :appId="appId"></auto-reply>
    <Modal v-model="modal" :title="modalTitle" footer-hide>
        <div ref="qrcode" class="qrcode-wrap"></div>
        <div class="qrcode-text">{{ qrcodeText }}</div>
        <div class="button-wrap">
            <Button type="primary" v-clipboard="{ value: qrcodeText, success: handleCopySuccess }">点击复制连接</Button>
        </div>
    </Modal>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { setCookies } from '@/libs/util'
import {
  kefuListApi, kefucreateApi, kefuaddApi, kefuAddApi,
  kefusetStatusApi, kefuEditApi, kefuRecordApi, kefuChatlistApi,
  kefuLogin,kefuGroupListApi,kefuCreateGroupApi
} from '@/api/setting'
import { adminAppCustomer } from '@/api/kefu';
import AutoReply from "./compoents/AutoReply";
import QRCode from 'qrcodejs2';
export default {
  name: 'index',
  filters: {
    typeFilter(status) {
      const statusMap = {
        'wechat': '微信用户',
        'routine': '小程序用户'
      }
      return statusMap[status]
    }
  },
  components:{
    AutoReply
  },
  computed: {
    ...mapState('media', [
      'isMobile'
    ]),
    ...mapState('userLevel', [
      'categoryId'
    ]),
    labelWidth() {
      return this.isMobile ? undefined : 80
    },
    labelPosition() {
      return this.isMobile ? 'top' : 'left'
    }
  },
  data() {
    return {
      userId:0,
      appId:'',
      isChat: true,
      formValidate3: {
        page: 1,
        limit: 15
      },
      groupList:[],
      theme3: "light",
      total3: 0,
      sortName: 0,
      loading3: false,
      modals3: false,
      tableList3: [],
      columns3: [
        {
          title: '用户名称',
          key: 'nickname',
          width: 200
        },
        {
          title: '客服头像',
          slot: 'headimgurl'
        },
        {
          title: '操作',
          slot: 'action'
        }
      ],
      formValidate5: {
        page: 1,
        limit: 15,
        uid: 0,
        to_uid: 0,
        id: 0
      },
      total5: 0,
      loading5: false,
      tableList5: [],
      columns5: [
        {
          title: '用户名称',
          key: 'nickname',
          width: 200
        },
        {
          title: '用户头像',
          slot: 'avatar'
        },
        {
          title: '发送消息',
          key: 'msn',
          width: 250
        },
        {
          title: '发送时间',
          key: 'add_time'
        }
      ],
      FromData: null,
      formValidate: {
        page: 1,
        limit: 15,
        data: '',
        type: '',
        nickname: ''
      },
      tableList2: [],
      modals: false,
      total: 0,
      tableFrom: {
        page: 1,
        limit: 15,
        group_id:''
      },
      timeVal: [],
      fromList: {
        title: '选择时间',
        custom: true,
        fromTxt: [
          { text: '全部', val: '' },
          { text: '今天', val: 'today' },
          { text: '昨天', val: 'yesterday' },
          { text: '最近7天', val: 'lately7' },
          { text: '最近30天', val: 'lately30' },
          { text: '本月', val: 'month' },
          { text: '本年', val: 'year' }
        ]
      },
      loading: false,
      tableList: [],
      columns1: [
        {
          title: 'ID',
          key: 'id',
          width: 80
        },
        {
          title: '客服名称',
          key: 'nickname',
          minWidth: 60
        },
        {
          title: '客服账号',
          key: 'account',
          minWidth: 60
        },
        {
          title: '客服分组',
          slot: 'group_name',
        },
        {
          title: '客服状态',
          slot: 'status',
          minWidth: 60
        },
        {
          title: '是否上线',
          slot: 'online',
          minWidth: 120
        },
        {
          title: '添加时间',
          key: 'add_time',
          minWidth: 120
        },
        {
          title: '操作',
          slot: 'action',
          fixed: 'right',
          minWidth: 150
        }
      ],
      columns4: [
        {
          type: 'selection',
          width: 60,
          align: 'center'
        },
        {
          title: 'ID',
          key: 'uid',
          width: 80
        },
        {
          title: '微信用户名称',
          key: 'nickname',
          minWidth: 160
        },
        {
          title: '客服头像',
          slot: 'headimgurl',
          minWidth: 60
        },
        {
          title: '用户类型',
          slot: 'user_type',
          width: 100
        },
        {
          title: '性别',
          slot: 'sex',
          minWidth: 60
        },
        {
          title: '地区',
          slot: 'country',
          minWidth: 120
        },
        {
          title: '是否关注公众号',
          slot: 'subscribe',
          minWidth: 120
        }
      ],
      loading2: false,
      total2: 0,
      addFrom: {
        uids: []
      },
      selections: [],
      rows: {},
      rowRecord: {},
      modal: false,
      modalTitle: '',
      qrcodeTextStart: `${window.location.origin}/chat/index?noCanClose=1`,
      qrcodeText: '',
      current:0
    }
  },
  async created() {
    let res = await adminAppCustomer();
    if (res.status == 200 && res.data.list.length) {
      this.qrcodeTextStart += `&token=${res.data.list[0].token_md5}`;
    }
    this.getGroupList();
    this.getList();
    window.addEventListener('click',()=>{
      this.groupList.forEach((el) => el.status = false)
    })
  },
  methods: {
    lockGroup(row){
      this.modalTitle = row.name+' 组二维码';
      this.qrcodeText = `${this.qrcodeTextStart}&group_id=${row.id}`;
      if (this.qrcode) {
        this.qrcode.makeCode(this.qrcodeText);
      } else {
        this.qrcode = new QRCode(this.$refs.qrcode, {
          text: this.qrcodeText,
          width: 128,
          height: 128,
          colorDark : "#000000",
          colorLight : "#ffffff",
          correctLevel : QRCode.CorrectLevel.L
        });
      }
      this.modal = true;
    },
    // 显示标签小菜单
    showMenu(item,index) {
      this.groupList.forEach((el) => {
        if (el.id == item.id) {
          el.status = item.status ? false : true;
        } else {
          el.status = false;
        }
      });
      // item.status = true;
    },
    editGroup(row){
      this.$modalForm(kefuCreateGroupApi(row.id)).then(() => this.getGroupList())
    },
    deleteGroup(row,tt,index){
      if(!row.id){
        return
      }
      let delfromData = {
        title: tt,
        num: index,
        url: `chat/group/${row.id}`,
        method: 'DELETE',
        ids: '' // 此参数为传递给后端得数据，若无可传空
      }
      this.$modalSure(delfromData).then((res) => {
        this.$Message.success(res.msg)
        this.groupList.splice(index, 1)
      }).catch(res => {
        this.$Message.error(res.msg)
      })
    },
    getGroupList(){
      kefuGroupListApi().then(res=>{
        let obj = {
          name: "全部",
          id: "",
        };
        res.data.unshift(obj);
        res.data.forEach((el) => {
          el.status = false;
        });
        this.groupList = res.data;
        this.sortName = res.data[0].id;
      })
    },
    bindMenuItem(name, index) {
      this.current = index;
      this.groupList.forEach((el) => {
        el.status = false;
      });
      this.tableFrom.group_id = name.id;
      this.getList();
    },
    // 操作
    changeMenu(row, name, index) {
      let uid = [];
      uid.push(row.id);
      let uids = { uids: uid };
      this.activeUserInfo = row;
      switch(name) {
        case '1':
          this.handleCopy(row)
          break;
        case '2':
         this.auth(row);
          break;
        case '3':
          this.del(row,'删除客服',index);
          break
      }
    },
    // 复制成功
    handleCopySuccess() {
        this.$Message.success({
            content: '复制成功',
            onClose: () => {
                this.modal = false;
            }
        });
    },
    // 点击列表的复制
    handleCopy(row) {
        this.modalTitle = '客服'+row.nickname+'个人二维码';
        this.qrcodeText = `${this.qrcodeTextStart}&kefu_id=${row.id}`;
        if (this.qrcode) {
            this.qrcode.makeCode(this.qrcodeText);
        } else {
            this.qrcode = new QRCode(this.$refs.qrcode, {
                text: this.qrcodeText,
                width: 128,
                height: 128,
                colorDark : "#000000",
                colorLight : "#ffffff",
                correctLevel : QRCode.CorrectLevel.L
            });
        }
        this.modal = true;
    },
    auth(item){
      this.userId = item.user_id
      this.appId = item.appid
      this.$refs.AutoReply.open()
    },
    // 进入工作台
    goChat(item) {
      kefuLogin(item.id).then(res => {
        var url = ''
        if(res.data.token) {
          let expires = this.getExpiresTime(res.data.exp_time);
          setCookies('kefu_token', res.data.token, expires);
          setCookies('kefu_uuid', res.data.kefuInfo.uid, expires);
          setCookies('kefu_expires_time', res.data.exp_time, expires);
          setCookies('kefuInfo', res.data.kefuInfo, expires);

          if(this.$store.state.media.isMobile) {
            url = window.location.protocol + "//" + window.location.host + '/kefu/mobile_list';
          } else {
            url = window.location.protocol + "//" + window.location.host + '/kefu/pc_list';
          }


          window.open(url, '_blank');
        }
      }).catch(error => {
        this.$Message.error(error.msg)
      })
    },
    getExpiresTime(expiresTime) {
      let nowTimeNum = Math.round(new Date() / 1000);
      let expiresTimeNum = expiresTime - nowTimeNum;
      return parseFloat(parseFloat(parseFloat(expiresTimeNum / 60) / 60) / 24);
    },
    cancel() {
      this.formValidate = {
        page: 1,
        limit: 10,
        data: '',
        type: '',
        nickname: ''
      }
    },
    handleReachBottom() {
      return new Promise(resolve => {
        this.formValidate.page = this.formValidate.page + 1
        setTimeout(() => {
          // this.loading2 = true;
          kefucreateApi(this.formValidate).then(async res => {
            let data = res.data
            // this.tableList2 = data.list;
            if(data.list.length > 0) {
              for(let i = 0; i < data.list.length; i++) {
                this.tableList2.push(data.list[i])
              }
            }
            this.total2 = data.count
            this.loading2 = false
          }).catch(res => {
            this.loading2 = false
            this.$Message.error(res.msg)
          })
          resolve()
        }, 2000)
      })
    },
    // 查看对话
    look(row) {
      this.isChat = false
      this.rowRecord = row
      this.getChatlist()
    },
    // 查看对话列表
    getChatlist() {
      this.loading5 = true
      this.formValidate5.uid = this.rows.uid
      this.formValidate5.to_uid = this.rowRecord.uid
      this.formValidate5.id = this.rows.id
      kefuChatlistApi(this.formValidate5).then(async res => {
        let data = res.data
        this.tableList5 = data.list
        this.total5 = data.count
        this.loading5 = false
      }).catch(res => {
        this.loading5 = false
        this.$Message.error(res.msg)
      })
    },
    pageChange5(index) {
      this.formValidate5.page = index
      this.getChatlist()
    },
    // 修改成功
    submitFail() {
      this.getList()
    },
    // 聊天记录
    record(row) {
      this.rows = row
      this.modals3 = true
      this.isChat = true
      this.getListRecord()
    },
    // 聊天记录列表
    getListRecord() {
      this.loading3 = true
      kefuRecordApi(this.formValidate3, this.rows.id).then(async res => {
        let data = res.data
        this.tableList3 = data.list ? data.list : []
        this.total3 = data.count
        this.loading3 = false
      }).catch(res => {
        this.loading3 = false
        this.$Message.error(res.msg)
      })
    },
    pageChange3(index) {
      this.formValidate3.page = index
      this.getListRecord()
    },
    // 编辑
    edit(row) {
      this.$modalForm(kefuEditApi(row.id)).then(() => this.getList())
    },
    // 添加
    add() {
      // this.modals = true;
      // this.formValidate.data = '';
      // this.getListService();
      this.$modalForm(kefuaddApi()).then(() => {
        this.getList();
        console.log(1223);
      })
    },
    // 全选
    onSelectTab(selection) {
      this.selections = selection
      let data = []
      this.selections.map((item) => {
        data.push(item.uid)
      })
      this.addFrom.uids = data
    },
    // 具体日期
    onchangeTime(e) {
      this.timeVal = e
      this.formValidate.data = this.timeVal.join('-')
      this.formValidate.page = 1
      this.getListService()
    },
    // 选择时间
    selectChange(tab) {
      this.formValidate.data = tab
      this.timeVal = []
      this.formValidate.page = 1
      this.getListService()
    },
    // 客服列表
    getListService() {
      this.loading2 = true
      kefucreateApi(this.formValidate).then(async res => {
        let data = res.data
        this.tableList2 = data.list
        this.total2 = data.count
        this.tableList2.map((item) => {
          item._isChecked = false
        })
        this.loading2 = false
      }).catch(res => {
        this.loading2 = false
        this.$Message.error(res.msg)
      })
    },
    pageChange2(pageIndex) {
      this.formValidate.page = pageIndex
      this.getListService()
      this.addFrom.uids = []
    },
    // 搜索
    userSearchs() {
      this.formValidate.page = 1
      this.getListService()
    },
    // 删除客服
    del(row, tit, num) {
      let delfromData = {
        title: tit,
        num: num,
        url: `chat/kefu/${row.id}`,
        method: 'DELETE',
        ids: '' // 此参数为传递给后端得数据，若无可传空
      }
      this.$modalSure(delfromData).then((res) => {
        this.$Message.success(res.msg)
        this.tableList.splice(num, 1)
      }).catch(res => {
        this.$Message.error(res.msg)
      })
    },
    // 列表
    getList() {
      this.loading = true
      kefuListApi(this.tableFrom).then(async res => {
        let data = res.data
        this.tableList = data.list
        this.total = res.data.count
        this.loading = false
      }).catch(res => {
        this.loading = false
        this.$Message.error(res.msg)
      })
    },
    pageChange(index) {
      this.tableFrom.page = index
      this.getList()
    },
    // 修改是否显示
    onchangeIsShow(row) {
      let data = {
        id: row.id,
        status: row.status
      }
      kefusetStatusApi(data).then(async res => {
        this.$Message.success(res.msg);
        this.getList();
      }).catch(res => {
        this.$Message.error(res.msg)
      })
    },
    // 添加客服
    putRemark() {
      if(this.addFrom.uids.length === 0) {
        return this.$Message.warning('请选择要添加的客服')
      }
      kefuAddApi(this.addFrom).then(async res => {
        this.$Message.success(res.msg)
        this.modals = false
        this.getList()
      }).catch(res => {
        this.loading = false
        this.$Message.error(res.msg)
      })
    }
  }
}
</script>

<style scoped lang="less">
.tabBox_img {
  width: 36px;
  height: 36px;
  border-radius: 4px;
  cursor: pointer;

  img {
    width: 100%;
    height: 100%;
  }
}

.modelBox {
  /deep/ .ivu-table-header {
    width: 100% !important;
  }
}

.trees-coadd {
  width: 100%;
  height: 385px;

  .scollhide {
    width: 100%;
    height: 100%;
    overflow-x: hidden;
    overflow-y: scroll;
  }
}

// margin-left: 18px;
.scollhide::-webkit-scrollbar {
  display: none;
}

.qrcode-wrap /deep/ img {
    margin: 0 auto;
}

.qrcode-text {
    margin-top: 16px;
  display: none;
    word-break: break-all;
}

.button-wrap {
    margin-top: 16px;
    text-align: center;
}

/deep/ .ivu-menu-vertical .ivu-menu-item-group-title {
  display: none;
}

.menu-item {
  position: relative;
  display: flex;
  justify-content: space-between;
  word-break: break-all;

  .icon-box {
    z-index: 3;
    position: absolute;
    right: 20px;
    top: 50%;
    transform: translateY(-50%);
    display: none;
  }

  &:hover .icon-box {
    display: block;
  }

  .right-menu {
    z-index: 10000;
    position: absolute;
    right: -106px;
    top: -11px;
    width: auto;
    min-width: 121px;
  }
}
</style>
