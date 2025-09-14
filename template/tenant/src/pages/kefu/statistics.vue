<template>
    <div>
        <div class="i-layout-page-header">
            <div class="i-layout-page-header">
                <span class="ivu-page-header-title">{{ $route.meta.title }}</span>
            </div>
        </div>
        <Card :bordered="false" dis-hover class="ivu-mt">
            <Form ref="formValidate" :model="formValidate" :label-width="labelWidth" :label-position="labelPosition" class="tabform" @submit.native.prevent>
                <Row :gutter="24" type="flex" justify="end">
                    <Col span="24" class="ivu-text-left">
                        <FormItem label="地区：" label-for="province">
                            <Input search enter-button @on-search="onSearch" placeholder="请输入地区搜索" element-id="province" v-model="formValidate.province" style="width: 30%;display: inline-table;" class="mr" />
                        </FormItem>
                    </Col>
                    <Col span="24" class="ivu-text-left">
                        <FormItem label="时间：">
                            <RadioGroup v-model="formValidate.time" type="button" @on-change="selectChange(formValidate.time)" class="mr">
                                <Radio :label=item.val v-for="(item,i) in fromList.fromTxt" :key="i">{{item.text}}</Radio>
                            </RadioGroup>
                            <DatePicker :editable="false" @on-change="onchangeTime" :value="timeVal" format="yyyy/MM/dd" type="daterange" placement="bottom-end" placeholder="自定义时间" style="width: 200px;"></DatePicker>
                        </FormItem>
                    </Col>
                </Row>
            </Form>
            <Table :columns="columns" :data="tableData" :loading="loading" highlight-row no-userFrom-text="暂无数据" class="ivu-mt">
                <template slot-scope="{ row, index }" slot="picture">
                    <img src="" v-viewer>
                </template>
                <template slot-scope="{ row, index }" slot="action">
                    <a @click="onEdit(row)">编辑</a>
                    <Divider type="vertical" />
                    <a @click="onDelete(row, '删除反馈', index)">删除</a>
                </template>
            </Table>
            <div class="acea-row row-right page">
                <Page :total="total" :page-size="limit" show-elevator show-total @on-change="onChange" />
            </div>
        </Card>
    </div>
</template>

<script>
import { chatStatistics } from '@/api/setting';
import { mapState } from 'vuex';

export default {
    name: 'kefu_statistics',
    data() {
        return {
            formValidate: {
                province: '',
                time:'',
            },
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
            columns: [
                {
                    title: 'ID',
                    key: 'id',
                    width: 80
                },
                {
                    title: 'IP',
                    key: 'ip',
                    minWidth: 80
                },
                {
                    title: '网址',
                    key: 'path',
                    minWidth: 120
                },
                {
                    title: '浏览器',
                    key: 'browser',
                    minWidth: 80
                },
                {
                    title: '时间',
                    key: 'create_time',
                    minWidth: 80
                },
                {
                    title: '地区',
                    key: 'region',
                    minWidth: 80,
                    render: (h, params) => {
                        return h('span', `${params.row.province} ${params.row.region}`);
                    }
                },
            ],
            tableData: [],
            loading: false,
            total: 0,
            page: 1,
            limit: 15
        };
    },
    computed: {
        ...mapState('media', [
            'isMobile'
        ]),
        labelWidth() {
            return this.isMobile ? undefined : 80;
        },
        labelPosition() {
            return this.isMobile ? 'top' : 'right';
        }
    },
    created() {
        this.chatStatistics();
    },
    methods: {
        chatStatistics() {
            chatStatistics({
                page: this.page,
                limit: this.limit,
                province: this.formValidate.province,
                create_time: this.formValidate.time
            }).then(res => {
                this.tableData = res.data.data;
                this.total = res.data.count;
            });
        },
        onSearch() {
            this.page = 1;
            this.chatStatistics();
        },
        onChange(index) {
            this.page = index;
            this.chatStatistics();
        },
        // 具体日期
        onchangeTime(e) {
            this.timeVal = e
            this.formValidate.time = this.timeVal.join('-')
            this.page = 1
            this.chatStatistics()
        },
        // 选择时间
        selectChange(tab) {
            this.formValidate.time = tab
            this.timeVal = []
            this.page = 1
            this.chatStatistics()
        },
    }
}
</script>

<style scoped>

</style>
