<template>
    <div>
        <div class="i-layout-page-header">
            <div class="i-layout-page-header">
                <span class="ivu-page-header-title">{{ $route.meta.title }}</span>
            </div>
        </div>
        <Card :bordered="false" dis-hover class="ivu-mt">
            <Row>
                <Col span="24">
                    <Button type="primary" icon="md-add" @click="callVersionForm(0)" class="mr10">添加升级包</Button>
                </Col>
            </Row>
            <Table :columns="columns" :data="tableData" :loading="loading" highlight-row no-userFrom-text="暂无数据" class="ivu-mt">
                <template slot-scope="{ row, index }" slot="action">
                    <a @click="callVersionForm(row.id)">编辑</a>
                    <Divider type="vertical" />
                    <a @click="deleteVersion(row.id, '删除反馈', index)">删除</a>
                </template>
            </Table>
            <div class="acea-row row-right page">
                <Page :total="total" :page-size="limit" show-elevator show-total @on-change="onChange" />
            </div>
        </Card>
    </div>
</template>

<script>
import { settingVerison, settingVerisonForm } from '@/api/setting';
import { mapState } from 'vuex';

export default {
    name: 'setting_version',
    data() {
        return {
            columns: [
                {
                    title: 'ID',
                    key: 'id',
                    width: 80
                },
                {
                    title: '摘要',
                    key: 'name',
                    minWidth: 120
                },
                {
                    title: '版本号',
                    key: 'verisons_num',
                    minWidth: 120
                },
                {
                    title: '详情',
                    key: 'info',
                    minWidth: 120
                },
                {
                    title: '时间',
                    key: 'update_time',
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
            limit: 15,
            page: 1
        };
    },
    computed: {
        ...mapState('media', [
            'isMobile'
        ])
    },
    created() {
        this.settingVerison();
    },
    methods: {
        // 列表
        settingVerison() {
            settingVerison({
                page: this.page,
                limit: this.limit
            }).then(res => {
                this.total = res.data.count;
                this.tableData = res.data.list;
            });
        },
        // 添加、编辑
        callVersionForm(id) {
            this.$modalForm(settingVerisonForm(id)).then(() => this.settingVerison());
        },
        // 删除
        deleteVersion(id, title, num) {
            let delfromData = {
                title,
                num,
                url: `setting/verison/${id}`,
                method: 'DELETE'
            };
            this.$modalSure(delfromData).then(res => {
                this.$Message.success(res.msg);
                this.tableData.splice(num, 1);
            }).catch(res => {
                this.$Message.error(res.msg);
            });
        },
        // 分页
        onChange(index) {
            this.page = index;
            this.settingVerison();
        }
    }
}
</script>

<style scoped>

</style>