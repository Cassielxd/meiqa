<template>
    <div>
        <Card :bordered="false" dis-hover class="ivu-mt">
            <Form :label-width="labelWidth" :label-position="labelPosition">
                <FormItem label="客服图标：">
                    <div style="display: inline-block;">
                        <RadioGroup v-model="kefu_icon_type" size="large" @on-change="kefu_icon_url3 = ''">
                            <Radio label="1">默认图标</Radio>
                            <Radio label="2">悬浮球</Radio>
                            <Radio label="3">自定义</Radio>
                        </RadioGroup>
                        <div style="min-height: 76px;margin-top: 22px;">
                            <img v-show="kefu_icon_type == 1" src="@/assets/images/service1.png" alt="默认图标" width="114" height="40" v-viewer>
                            <img v-show="kefu_icon_type == 2" src="@/assets/images/service2.png" alt="悬浮球" width="52" height="52" v-viewer>
                            <div v-show="kefu_icon_type == 3 && kefu_icon_url3" class="custom-image">
                                <img ref="custom" :src="kefu_icon_url3" alt="自定义" v-viewer="{ movable: false }">
                                <div class="custom-cover">
                                    <Icon type="ios-eye-outline" @click.native="handleView" />
                                    <Icon type="ios-trash-outline" @click.native="kefu_icon_url3 = ''" />
                                </div>
                            </div>
                            <div v-show="kefu_icon_type == 3 && !kefu_icon_url3" class="custom-select" @click="modal = true">
                                <Icon type="ios-add" size="22" color="#999999" />
                            </div>
                        </div>
                    </div>
                </FormItem>
                <FormItem>
                    <Button type="primary" @click="handleSubmit">提交</Button>
                </FormItem>
            </Form>
        </Card>
        <Modal
            v-model="modal"
            :mask-closable="false"
            :z-index="888"
            width="950px"
            scrollable
            footer-hide
            closable
            title='上传客服图标'
        >
            <uploadPictures
                v-if="modal"
                :gridBtn="gridBtn"
                :gridPic="gridPic"
                isChoice="单选"
                @getPic="getPic"
            >
            </uploadPictures>
        </Modal>
    </div>
</template>

<script>
import { mapState } from "vuex";
import { kfIcon } from "@/api/system";
import uploadPictures from '@/components/uploadPictures';
export default {
    name: 'kfIcon',
    components: {
        uploadPictures
    },
    data() {
        return {
            kefu_icon_type: '0',
            kefu_icon_url3: '',
            modal: false,
            gridBtn: {
                xl: 4,
                lg: 8,
                md: 8,
                sm: 8,
                xs: 8
            },
            gridPic: {
                xl: 6,
                lg: 8,
                md: 12,
                sm: 12,
                xs: 12
            },
        };
    },
    computed: {
        ...mapState('media', [
            'isMobile'
        ]),
        labelWidth() {
            return this.isMobile ? undefined : 120
        },
        labelPosition() {
            return this.isMobile ? 'top' : 'right'
        }
    },
    created() {
        this.kfIcon();
    },
    methods: {
        // 获取/设置客服图标
        kfIcon(method, data) {
            kfIcon(method, data).then(res => {
                let data = res.data;
                if (method) {
                    return this.$Message.success(res.msg);
                }
                this.kefu_icon_type = data.kefu_icon_type;
                this.kefu_icon_url3 = data.kefu_icon_url3;
            }).catch(err => {
                this.$Message.error(err.msg);
            });
        },
        // 点击提交
        handleSubmit() {
            if (this.kefu_icon_type == 0) {
                return this.$Message.info('请选择客服图标类型');
            }
            if (this.kefu_icon_type == 3 && !this.kefu_icon_url3) {
                return this.$Message.info('请上传自定义客服图标');
            }
            this.kfIcon('post', {
                kefu_icon_type: this.kefu_icon_type,
                kefu_icon_url3: this.kefu_icon_url3
            });
        },
        // 选择的图片
        getPic(pic) {
            this.kefu_icon_url3 = pic.att_dir;
            this.modal = false;
        },
        // 预览
        handleView() {
            this.$refs.custom.$viewer.show();
        }
    }
}
</script>

<style scoped>
.ivu-form >>> .ivu-form-item-label {
    font-size: 13px !important;
}

.ivu-radio-wrapper {
    margin-right: 20px;
    font-size: 13px !important;
    color: #000000;
}

.ivu-radio-wrapper >>> .ivu-radio {
    margin-right: 8px;
}

.custom-image {
    position: relative;
    display: inline-block;
    width: 76px;
    height: 76px;
    border: 1px solid transparent;
    border-radius: 6px;
    overflow: hidden;
    line-height: 76px;
    text-align: center;
}

.custom-image img {
    width: 100%;
    height: 100%;
}

.custom-cover {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    display: none;
    background-color: rgba(0, 0, 0, .6);
}

.custom-image:hover .custom-cover {
    display: block;
}

.custom-cover i {
    font-size: 20px;
    color: #FFFFFF;
    cursor: pointer;
}

.custom-cover i ~ i {
    margin-left: 4px;
}

.custom-select {
    width: 76px;
    height: 76px;
    border: 1px dashed #999999;
    border-radius: 6px;
    line-height: 76px;
    text-align: center;
}
</style>