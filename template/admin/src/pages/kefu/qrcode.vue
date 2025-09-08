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
                        <FormItem label="二维码搜索：" label-for="name">
                            <Input search enter-button @on-search="onSearch" placeholder="请输入二维码名称搜索" element-id="name" v-model="formValidate.name" style="width: 30%;display: inline-table;" class="mr" />
                        </FormItem>
                    </Col>
                    <Col span="24" class="ivu-text-left">
                        <Button type="primary" icon="md-add" @click="add" class="mr10">添加客服二维码</Button>
                    </Col>
                </Row>
            </Form>
            <Table :columns="columns" :data="tableData" :loading="loading" highlight-row no-userFrom-text="暂无数据" class="ivu-mt">
                <template slot-scope="{ row, index }" slot="picture">
                    <img :src="row.qrcode" width="61" height="61" v-viewer>
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
import { adminAppCustomer } from '@/api/kefu';
import { chatQrcode, chatQrcodeForm } from '@/api/setting';
import { mapState } from 'vuex';
import QRCode from 'qrcodejs2';

export default {
    name: 'kefu_qrcode',
    data() {
        return {
            formValidate: {
                name: ''
            },
            page: 1,
            limit: 15,
            total: 0,
            columns: [
                {
                    title: 'ID',
                    key: 'id',
                    width: 80
                },
                {
                    title: '二维码名称',
                    key: 'name',
                    minWidth: 80
                },
                {
                    title: '客服',
                    key: 'user_account',
                    minWidth: 120,
                    render: (h, params) => {
                        return h('span', params.row.user_account.join('，'));
                    }
                },
                {
                    title: '二维码图片',
                    slot: 'picture',
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
            id: 0,
            qrcodeText: `${window.location.origin}/chat/index?noCanClose=1`,
            token: ''
        };
    },
    computed: {
        ...mapState('media', [
            'isMobile'
        ]),
        labelWidth() {
            return this.isMobile ? undefined : 100;
        },
        labelPosition() {
            return this.isMobile ? 'top' : 'right';
        }
    },
    created() {
        adminAppCustomer().then(res => {
            if (res.status == 200 && res.data.list.length) {
                this.qrcodeText += `&token=${res.data.list[0].token_md5}`;
            }
        });
        this.chatQrcode();
    },
    methods: {
        chatQrcode() {
            chatQrcode({
                page: this.page,
                limit: this.limit,
                name: this.formValidate.name
            }).then(res => {
                let list = res.data.list;
                list.forEach(item => {
                    this.qrcode = new QRCode(document.createElement('div'), `${this.qrcodeText}&kefu_rand=${item.id}`);
                    item.qrcode =  this.qrcode._el.children[0].toDataURL();
                });
                this.tableData = list;
                this.total = res.data.count;
            });
        },
        // 搜索
        onSearch() {
            this.page = 1;
            this.chatQrcode();
        },
        // 分页
        onChange(index) {
            this.page = index;
            this.chatQrcode();
        },
        // 编辑
        onEdit(row) {
            this.$modalForm(chatQrcodeForm(row.id)).then(() => this.chatQrcode());
        },
        // 删除
        onDelete(row, title, num) {
            let delfromData = {
                title: title,
                num: num,
                url: `/chat/qrcode/${row.id}`,
                method: 'DELETE',
                ids: ''
            };
            this.$modalSure(delfromData).then((res) => {
                this.$Message.success(res.msg);
                this.tableData.splice(num, 1);
            }).catch(res => {
                this.$Message.error(res.msg);
            });
        },
        // 添加客服二维码
        add() {
            this.$modalForm(chatQrcodeForm(this.id)).then(() => this.chatQrcode());
        }
    }
}
</script>

<style scoped>

</style>
