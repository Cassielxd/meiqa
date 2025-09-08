<template>
    <div class="record">
        <div class="i-layout-page-header">
            <div class="i-layout-page-header">
                <span class="ivu-page-header-title">{{ $route.meta.title }}</span>
            </div>
        </div>

        <cards-data v-if="cardLists.length" :cardLists="cardLists"></cards-data>

        <Card :bordered="false" dis-hover class="ivu-mt">
            <Form ref="formValidate" :model="formValidate" :label-width="labelWidth" :label-position="labelPosition" class="tabform" @submit.native.prevent>
                <Row :gutter="24">
                    <Col span="24" class="ivu-text-left">
                        <FormItem label="时间：">
                            <RadioGroup v-model="formValidate.time" type="button" @on-change="selectChange(formValidate.time)" class="mr">
                                <Radio :label=item.val v-for="(item,i) in fromList.fromTxt" :key="i">{{item.text}}</Radio>
                            </RadioGroup>
                            <DatePicker :editable="false" @on-change="onchangeTime" :value="timeVal" format="yyyy/MM/dd" type="daterange" placement="bottom-end" placeholder="自定义时间" style="width: 200px;"></DatePicker>
                        </FormItem>
                    </Col>
                    <Col span="10" class="ivu-text-left">
                        <FormItem label="搜索：">
                            <Input v-model="searchValue" placeholder="请输入" clearable search enter-button @on-search="selChange"></Input>
                        </FormItem>
                    </Col>
                </Row>
            </Form>
            <Table :columns="columns" :data="tableData" :loading="loading" highlight-row no-userFrom-text="暂无数据" class="ivu-mt">
                <template slot-scope="{ row, index }" slot="action">
                    <a @click="lock(row)">查看聊天记录</a>
                </template>
                <template slot-scope="{row,index}" slot="avatar">
                    <img class="avatar" :src="row.user.avatar" alt="">
                </template>
                <template slot-scope="{row,index}" slot="nickname">
                    <span>{{row.user.nickname}}</span>
                </template>
                <template slot-scope="{row,index}" slot="nickname1">
                   <span>{{row.dialogueUser.nickname}}</span>
                </template>
            </Table>
            <div class="acea-row row-right page">
                <Page :total="total" :page-size="formValidate.limit" show-elevator show-total @on-change="onChange" />
            </div>
        </Card>

        <Modal
                v-model="modals3"
                footer-hide
                scrollable
                closable
                :title="modalTitle"
                width="1000"
                @on-cancel="modals3Cancel"
        >
            <div class="modelBox">
                <Form ref="formValidate" :model="formValidate3" :label-width="labelWidth" :label-position="labelPosition" class="tabform" @submit.native.prevent>
                    <Row :gutter="24">
                        <Col span="24" class="ivu-text-left">
                            <FormItem label="时间：">
                                <RadioGroup v-model="formValidate3.time" type="button" @on-change="onchangeMsgTime" class="mr">
                                    <Radio :label=item.val v-for="(item,i) in fromList.fromTxt" :key="i">{{item.text}}</Radio>
                                </RadioGroup>
                                <DatePicker :editable="false" @on-change="onchangeMsgTime" :value="formValidate3.time" format="yyyy/MM/dd" type="daterange" placement="bottom-end" placeholder="自定义时间" style="width: 200px;"></DatePicker>
                            </FormItem>
                        </Col>
                        <Col span="10" class="ivu-text-left">
                            <FormItem label="搜索：">
                                <Input v-model="formValidate3.msn" placeholder="请输入聊天内容" clearable search enter-button @on-search="onchangeMsgTime"></Input>
                            </FormItem>
                        </Col>
                    </Row>
                </Form>
                <Table
                        :loading="loading3"
                        highlight-row
                        no-userFrom-text="暂无数据"
                        no-filtered-userFrom-text="暂无筛选结果"
                        :columns="columns3"
                        :data="tableList3"
                >
                    <template slot-scope="{ row, index }" slot="avatar">
                        <viewer>
                            <div class="tabBox_img">
                                <img v-lazy="row.userThis.avatar" />
                            </div>
                        </viewer>
                    </template>
                    <template slot-scope="{row,index}" slot="nickname">
                        <span>{{row.userThis.nickname}}</span>
                    </template>
                    <template slot-scope="{row,index}" slot="to_nickname">
                        <span>{{row.user.nickname}}</span>
                    </template>
                    <template slot-scope="{row,index}" slot="msn">
                        <span>{{row.msn}}</span>
                    </template>
                    <template slot-scope="{ row, index }" slot="action">
                        <a @click="look(row)">查看对话</a>
                    </template>
                </Table>
                <div class="acea-row row-right page">
                    <Page
                            :total="total3"
                            show-elevator
                            show-total
                            @on-change="pageChange3"
                            :page-size="formValidate3.limit"
                    />
                </div>
            </div>
        </Modal>

    </div>
</template>

<script>
import { chatRecord, recordKefuApi,recordUserListApi } from '@/api/setting';
import { mapState } from 'vuex'
import cardsData from "@/components/cards/cards";
export default {
    name: 'kefu_record',
    components:{cardsData},
    data() {
        return {
            modals3:false,
            isChat:true,
            loading3:false,
            total3:0,
            modalTitle:'',
            lockUserId:0,
            cardLists:[],
            columns3: [
                {
                    title: "用户名称",
                    slot: "nickname",
                    width: 200,
                },
                {
                    title: "用户头像",
                    slot: "avatar",
                },
                {
                    title: "对话人昵称",
                    slot: "to_nickname",
                },
                {
                    title: "聊天内容",
                    slot: "msn",
                },
            ],
            tableList3:[],
            formValidate3:{
                page: 1,
                limit: 15,
                user_id:0,
                time:'',
                msn:'',
            },
            formValidate: {
                nickname:'',
                time: '',
                page: 1,
                limit: 15,
            },
            searchType: '',
            searchValue: '',
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
            timeVal: [],
            columns: [
                {
                    title: 'ID',
                    key: 'id',
                    width: 80
                },
                {
                    title: '用户昵称',
                    slot: 'nickname',
                    minWidth: 120
                },
                {
                    title: '用户头像',
                    slot: 'avatar',
                    minWidth: 150,
                },
                {
                    title: '对话人昵称',
                    slot: 'nickname1',
                },
                {
                    title: '时间',
                    key: '_add_time',
                    minWidth: 120
                },
                {
                    title: '操作',
                    slot: 'action',
                    minWidth: 150,
                    fixed: 'right'
                }
            ],
            tableData: [],
            loading: false,
            total: 0,
            kefuList:[],
            search:{
                kefu_id:0,
            },
            statistics:{},
        };
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
        this.recordUserList();
    },
    methods: {
        pageChange3(value){
            this.formValidate3.page = value;
            this.getChatRecordList()
        },
        modals3Cancel(){
            this.formValidate3.page = 1;
            this.formValidate3.msn = '';
            this.formValidate3.time = '';
            this.formValidate3.user_id = 0;
        },
        onchangeMsgTime(){
            this.formValidate3.page = 1;
            this.getChatRecordList()
        },
        getChatRecordList(){
            this.loading3 = true
            chatRecord(this.formValidate3).then(res=>{
                this.loading3 = false
                this.tableList3 = res.data.list;
                this.total3 = res.data.count;
            }).catch(()=>{
                this.loading3 = false
            })
        },
        lock(row){
            this.modals3 = true;
            this.modalTitle = row.nickname+' - 聊天记录';
            this.formValidate3.user_id = row.user_id;
            this.formValidate3.page = 1;
            this.getChatRecordList()
        },
        recordUserList(){
            recordUserListApi(this.formValidate).then(res=>{
                this.tableData = res.data.list;
                this.total = res.data.count;
                this.cardLists = [
                    {
                        className:'ios-contact',
                        col:6,
                        count:res.data.data.user_count,
                        name:'用户总数'
                    },
                    {
                        className:'ios-contact',
                        col:6,
                        count:res.data.data.tourist_count,
                        name:'游客总数'
                    },
                    {
                        className:'ios-contact',
                        col:6,
                        count:res.data.data.dialogue_count,
                        name:'聊天记录总数'
                    },
                    {
                        className: '',
                        col: 6,
                        count: res.data.data.recode_count,
                        name: '聊天用户总数'
                    }
                ];
            })
        },
        onChange(index) {
            this.formValidate.page = index;
            this.recordUserList()
        },
        selChange() {
            this.formValidate.page = 1;
            this.recordUserList()
        },
        // 具体日期
        onchangeTime(e) {
            this.timeVal = e
            this.formValidate.time = this.timeVal.join('-')
            this.formValidate.page = 1
            this.recordUserList()
        },
        // 选择时间
        selectChange(tab) {
            console.log(tab);
            this.formValidate.time = tab
            this.timeVal = []
            this.formValidate.page = 1
            this.recordUserList()
        },
    }
}
</script>

<style scoped>
.ivu-input-group-append .ivu-btn {
    border-color: #2d8cf0;
    border-top-left-radius: 0;
    border-bottom-left-radius: 0;
    background-color: #2d8cf0;
    color: #fff;
}

        .record .avatar{
            max-width: 100px;
            max-height: 100px;
        }

</style>
