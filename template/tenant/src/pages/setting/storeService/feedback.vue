<template>
  <div>
    <div class="i-layout-page-header">
      <div class="i-layout-page-header">
        <span class="ivu-page-header-title">{{ $t($route.meta.title)}}</span>
      </div>
    </div>
    <Card :bordered="false" dis-hover class="ivu-mt">
      <Form ref="formValidate" :model="formValidate" :label-width="labelWidth" :label-position="labelPosition" class="tabform" @submit.native.prevent>
        <Row :gutter="24" type="flex" justify="end">
          <Col span="24" class="ivu-text-left">
          <FormItem :label="$t('kefu.messageInfo') + '：'">
            <Input search enter-button @on-search="selChange" :placeholder="$t('kefu.enterUserNicknamePhoneMessageSearch')" element-id="name" v-model="formValidate.title" style="width: 30%;display: inline-table;" class="mr" />
          </FormItem>
          </Col>
          <Col span="24" class="ivu-text-left">
          <FormItem :label="$t('kefu.messageTime') + '：'">
            <RadioGroup v-model="formValidate.time" type="button" @on-change="selectChange(formValidate.time)" class="mr">
              <Radio :label=item.val v-for="(item,i) in fromList.fromTxt" :key="i">{{item.text}}</Radio>
            </RadioGroup>
            <DatePicker :editable="false" @on-change="onchangeTime" :value="timeVal" format="yyyy/MM/dd" type="daterange" placement="bottom-end" :placeholder="$t('kefu.customTime')" style="width: 200px;"></DatePicker>
          </FormItem>
          </Col>
        </Row>
      </Form>
      <Table :columns="columns1" :data="list" :loading="loading" :no-userFrom-text="$t('kefu.noData')" :no-filtered-userFrom-text="$t('kefu.noFilteredData')">>
        <template slot-scope="{ row, index }" slot="status">
          <div>{{row.status===1 ? $t('kefu.processed') : $t('kefu.unprocessed')}}</div>
        </template>
        <template slot-scope="{ row, index }" slot="action">
          <a @click="remarks(row.id)">{{row.status===1 ? $t('kefu.remark') : $t('kefu.process')}}</a>
          <Divider type="vertical" />
          <a @click="del(row,$t('kefu.deleteFeedback'),index)">{{ $t('kefu.delete') }}</a>
        </template>
      </Table>
      <div class="acea-row row-right page">
        <Page :total="count" show-elevator show-total @on-change="pageChange" :page-size="limit" />
      </div>
    </Card>
  </div>
</template>

<script>
import { kefuFeedBack, kefuFeedBackEdit } from '@/api/setting'
import { mapState } from 'vuex'
export default {
  name: "feedback",
  data() {
    return {
      loading: false,
      list: [],
      page: 1,
      limit: 15,
      formValidate: {
        time: '',
        title: ''
      },
      fromList: {
        title: this.$t('kefu.selectTime'),
        custom: true,
        fromTxt: [
          { text: this.$t('kefu.all'), val: '' },
          { text: this.$t('kefu.today'), val: 'today' },
          { text: this.$t('kefu.yesterday'), val: 'yesterday' },
          { text: this.$t('kefu.latest7Days'), val: 'lately7' },
          { text: this.$t('kefu.latest30Days'), val: 'lately30' },
          { text: this.$t('kefu.thisMonth'), val: 'month' },
          { text: this.$t('kefu.thisYear'), val: 'year' }
        ]
      },
      timeVal: [],
      count: 0,
      columns1: [
        {
          title: 'ID',
          key: 'id',
          width: 80
        },
        {
          title: this.$t('kefu.nickname'),
          key: 'rela_name',
          minWidth: 120
        },
        {
          title: this.$t('kefu.phone'),
          key: 'phone',
          minWidth: 120
        },
        {
          title: this.$t('kefu.content'),
          key: 'content',
          minWidth: 320
        },
        {
          title: this.$t('kefu.status'),
          slot: 'status',
          minWidth: 120
        },
        {
          title: this.$t('kefu.time'),
          key: 'add_time',
          minWidth: 120
        },
        {
          title: this.$t('kefu.actions'),
          slot: 'action',
          fixed: 'right',
          minWidth: 150
        }
      ],
    }
  },
  computed: {
    ...mapState('media', [
      'isMobile'
    ]),
    labelWidth() {
      return this.isMobile ? undefined : 80
    },
    labelPosition() {
      return this.isMobile ? 'top' : 'right'
    }
  },
  created() {
    this.getList()
  },
  methods: {
    //备注；
    remarks(id) {
      this.$modalForm(kefuFeedBackEdit(id)).then(() => this.getList());
    },
    // 选择
    selChange() {
      this.page = 1;
      this.getList()
    },
    // 选择时间
    selectChange(tab) {
      this.formValidate.time = tab
      this.timeVal = []
      this.page = 1
      this.getList()
    },
    // 具体日期
    onchangeTime(e) {
      this.timeVal = e
      this.formValidate.time = this.timeVal.join('-')
      this.page = 1
      this.getList()
    },
    getList() {
      kefuFeedBack({
        page: this.page,
        limit: this.limit,
        time: this.formValidate.time,
        title: this.formValidate.title
      }).then(res => {
        this.list = res.data.data
        this.count = parseInt(res.data.count) || 0
      })
    },
    // 删除
    del(row, tit, num) {
      let delfromData = {
        title: tit,
        num: num,
        url: `/chat/feedback/${row.id}`,
        method: 'DELETE',
        ids: ''
      };
      this.$modalSure(delfromData).then((res) => {
        this.$Message.success(res.msg);
        this.list.splice(num, 1);
      }).catch(res => {
        this.$Message.error(res.msg);
      });
    },
    pageChange(index) {
      this.page = index;
      this.getList();
    },
  }
}
</script>

<style scoped>
</style>
