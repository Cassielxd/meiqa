<template>
    <div>
        <Form ref="formValidate" :model="formValidate" :rules="ruleInline" inline>
            <FormItem :label="$t('kefu.selectType')" class="form-item" label-position="right" :label-width="100">
                <RadioGroup v-model="formValidate.gender">
                    <Radio :label="item.key" v-for="(item,index) in radioList" :key="index">{{item.title}}</Radio>
                </RadioGroup>
            </FormItem>
            <FormItem v-if="formValidate.gender == 1" :label="$t('kefu.deliveryType')" class="form-item" label-position="right" :label-width="100" :key="'test0'">
                <RadioGroup v-model="formValidate.shipStatus">
                    <Radio :label="item.key" v-for="(item,index) in shipType" :key="index">{{item.title}}</Radio>
                </RadioGroup>
            </FormItem>
            <!--  发货手动填写  -->
            <div v-if="formValidate.gender == 1 && formValidate.shipStatus == 1" :key="'test1'">
                <FormItem :label="$t('kefu.expressCompany')" prop="logisticsCode" class="form-item" label-position="right" :label-width="100"  >
                    <Select v-model="formValidate.logisticsCode" filterable :placeholder="$t('kefu.pleaseSelect')" @on-change="bindChange" :label-in-value="true" style="width: 100%">
                        <Option :value="item.code" v-for="(item,index) in logisticsList" :key="index">{{item.value}}</Option>
                    </Select>
                </FormItem>
                <FormItem :label="$t('kefu.trackingNumber')" prop="number" class="form-item" label-position="right" :label-width="100">
                    <Input v-model="formValidate.number" :placeholder="$t('kefu.enterTrackingNumber')" style="width: 100%"></Input>
                </FormItem>
                <FormItem label="" class="form-item" label-position="right" :label-width="100">
                    <div style="color: #c4c4c4;">{{$t('kefu.sfNote1')}}</div>
                    <div style="color: #c4c4c4;">{{$t('kefu.sfNote2')}}</div>
                </FormItem>
            </div>
            <!--  电子面单打印  -->
            <div v-if="formValidate.gender == 1 && formValidate.shipStatus == 2" :key="'test2'">
                <FormItem :label="$t('kefu.expressCompany')" prop="logisticsCode" class="form-item" label-position="right" :label-width="100">
                    <Select v-model="formValidate.logisticsCode" :placeholder="$t('kefu.pleaseSelect')" style="width: 100%" @on-change="bindChange" filterable :label-in-value="true">
                        <Option :value="item.code" v-for="(item,index) in logisticsList" :key="index">{{item.value}}</Option>
                    </Select>
                </FormItem>
                <FormItem :label="$t('kefu.electronicWaybill')" class="form-item" label-position="right" :label-width="100" v-if="orderTempList.length>0">
                    <Select v-model="formValidate.electronic" :placeholder="$t('kefu.selectElectronicWaybill')" style="width: 80%">
                        <Option :value="item.temp_id" v-for="(item,index) in orderTempList" :key="index">{{item.title}}</Option>
                    </Select>
                    <Button style="flex: 1;margin-left:21px;" @click="lookImg">{{$t('kefu.preview')}}</Button>
                    <viewer :images="orderTempList"
                            class="viewer" ref="viewer"
                            @inited="inited"
                            style="display: none"
                    >
                        <img v-for="src in orderTempList" :src="src.pic" :key="src.id" class="image">
                    </viewer>
                </FormItem>
                <FormItem :label="$t('kefu.senderName')" prop="sendName" class="form-item" label-position="right" :label-width="100">
                    <Input v-model="formValidate.sendName" :placeholder="$t('kefu.enterSenderName')" style="width: 100%"></Input>
                </FormItem>
                <FormItem :label="$t('kefu.senderPhone')" prop="sendPhone" class="form-item" label-position="right" :label-width="100">
                    <Input v-model="formValidate.sendPhone" :placeholder="$t('kefu.enterSenderPhone')" style="width: 100%"></Input>
                </FormItem>
                <FormItem :label="$t('kefu.senderAddress')" prop="sendAddress" class="form-item" label-position="right" :label-width="100">
                    <Input v-model="formValidate.sendAddress" :placeholder="$t('kefu.enterSenderAddress')" style="width: 100%"></Input>
                </FormItem>
            </div>
            <!--  送货  -->
            <div v-if="formValidate.gender == 2" :key="'test3'">
                <FormItem :label="$t('kefu.selectDeliveryPerson')" class="form-item" label-position="right" :label-width="100">
                    <Select v-model="formValidate.postPeople" :placeholder="$t('kefu.selectDeliveryPersonPlaceholder')" style="width: 100%">
                        <Option :value="item.id" v-for="(item,index) in deliveryList" :key="index">{{item.nickname}}</Option>
                    </Select>
                </FormItem>
            </div>
            <div v-if="formValidate.gender == 3">
                <FormItem :label="$t('kefu.remarkColon')" props="msg" class="form-item" label-position="right" :label-width="100">
                    <Input :placeholder="$t('kefu.remarkPlaceholder')" v-model="formValidate.msg" />
                </FormItem>
            </div>
            <div class="mask-footer">
                <Button type="primary" @click="handleSubmit('formValidate')">{{$t('kefu.submit')}}</Button>
                <Button @click="close">{{$t('kefu.cancel')}}</Button>
            </div>
        </Form>

    </div>

</template>

<script>
    import { orderExport,orderTemp,orderDeliveryAll,orderDelivery,getSender } from '@/api/kefu'
    export default {
        name: "delivery",
        props:{
            isShow:{
                type:Boolean,
                default:false
            },
            orderId:{
                type:String | Number,
                default:''
            }
        },
        watch:{
            'formValidate.shipStatus':{
                handler(nVal,oVal){
                    if(nVal == 2 && !this.formValidate.sendName){
                        getSender().then(res=>{
                            this.formValidate.sendName = res.data.to_name
                            this.formValidate.sendPhone = res.data.to_tel
                            this.formValidate.sendAddress = res.data.to_add
                        })
                    }
                    this.$refs['formValidate'].resetFields()
                },
                deep: true
            },
            'formValidate.gender':{
                handler(nVal,oVal){

                    this.$refs['formValidate'].resetFields()
                },
                deep: true
            }
        },
        data(){
            return {
                shipType: [],
                radioList: [],
                ruleInline: {},
                formValidate:{
                    gender:1,
                    shipStatus:1,
                    logisticsCode:'', // 快递公司编号
                    logisticsName:'', // 快递公司名称
                    number:'', // 快递单号
                    electronic:'', //电子面单
                    sendName:'', //寄件人姓名
                    sendPhone:'', // 寄件人电话
                    sendAddress:'', //寄件人地址
                    postPeople:'',  // 配送员
                    msg:'' // 备注
                },
                logisticsList:[],
                orderTempList:[],
                deliveryList:[]
            }
        },
        mounted() {
            this.initI18nData();
            this.getOrderExport()
            this.getDelivery()
        },
        methods:{
            initI18nData() {
                this.shipType = [
                    {
                        key: 1,
                        title: this.$t('kefu.manualInput')
                    },
                    {
                        key: 2,
                        title: this.$t('kefu.electronicLabel')
                    },
                ];
                this.radioList = [
                    {
                        key: 1,
                        title: this.$t('kefu.delivery')
                    },
                    {
                        key: 2,
                        title: this.$t('kefu.homeDelivery')
                    },
                    {
                        key: 3,
                        title: this.$t('kefu.virtual')
                    }
                ];
                this.ruleInline = {
                    logisticsCode: [
                        { required: true, message: this.$t('kefu.selectExpressCompany'), trigger: 'change' }
                    ],
                    number: [
                        { required: true, message: this.$t('kefu.fillTrackingNumber'), trigger: 'change' }
                    ],
                    sendName: [
                        { required: true, message: this.$t('kefu.fillSenderName'), trigger: 'change' }
                    ],
                    sendPhone: [
                        { required: true, message: this.$t('kefu.fillSenderPhone'), trigger: 'change' },
                        { pattern: /^1[3456789]\d{9}$/, message: this.$t('kefu.phoneFormatError'), trigger: "blur" }
                    ],
                    sendAddress: [
                        { required: true, message: this.$t('kefu.fillSenderAddress'), trigger: 'change' }
                    ],
                    msg: [
                        { required: true, message: this.$t('kefu.fillRemarkInfo'), trigger: 'change' }
                    ],
                };
            },
            // 获取配送人
            getDelivery(){
                orderDeliveryAll().then(res=>{
                    this.deliveryList = res.data
                })
            },
            //查看大图
            inited (viewer) {
                this.$viewer = viewer
            },
            //物流公司
            getOrderExport(){
                orderExport().then(res=>{
                    this.logisticsList = res.data
                })
            },
            handleSubmit(name){
                if(this.formValidate.gender == 1){
                    this.$refs[name].validate((valid) => {
                        let paramsData = {}
                        paramsData.type = this.formValidate.gender
                        paramsData.express_record_type = parseFloat(this.formValidate.shipStatus)
                        paramsData.delivery_name = this.formValidate.logisticsName
                        paramsData.delivery_code = this.formValidate.logisticsCode
                        if (valid) {
                            // 手动
                            if(this.formValidate.gender == 1 && this.formValidate.shipStatus ==1){

                                paramsData.delivery_id = this.formValidate.number
                            }
                            // 电子
                            if(this.formValidate.gender == 1 && this.formValidate.shipStatus ==2){
                                paramsData.to_name = this.formValidate.sendName
                                paramsData.to_tel = this.formValidate.sendPhone
                                paramsData.to_addr = this.formValidate.sendAddress
                                paramsData.express_temp_id = this.formValidate.electronic
                            }
                            orderDelivery(this.orderId,paramsData).then(res=>{
                                this.$Message.success(res.msg)
                                this.$emit('ok')
                            }).catch(error=>{
                                this.$Message.error(error.msg)
                            })
                        } else {

                        }
                    })
                }
                if(this.formValidate.gender == 2){
                    let people = {}
                    this.deliveryList.forEach((el,index)=>{
                        if(el.id == this.formValidate.postPeople){
                            people = el
                        }
                    })
                    orderDelivery(this.orderId,{
                        type:this.formValidate.gender,
                        sh_delivery_name:people.wx_name,
                        sh_delivery_id:people.phone,
                        sh_delivery_uid:people.id
                    }).then(res=>{
                        this.$Message.success(res.msg)
                        this.$emit('ok')
                    }).catch(error=>{
                        this.$Message.error(error.msg)
                    })
                }
                if(this.formValidate.gender == 3){
                    orderDelivery(this.orderId,{
                        type:this.formValidate.gender,
                        remark:this.formValidate.msg
                    }).then(res=>{
                        this.$Message.success(res.msg)
                        this.$emit('ok')
                    }).catch(error=>{
                        this.$Message.error(error.msg)
                    })
                }
            },
            close(){
                this.$emit('close')
            },
            // 物流选中
            bindChange(val){
                console.log(val)
                this.formValidate.logisticsName = val.label
                if(this.formValidate.shipStatus == 2){
                    orderTemp({
                        com:val.value
                    }).then(res=>{
                        this.orderTempList = res.data.data
                    })
                }
            },
            lookImg(){
                if(this.formValidate.electronic){
                    this.orderTempList.forEach((el,index)=>{
                        if(el.temp_id == this.formValidate.electronic){
                            this.$viewer.view(index)
                        }
                    })

                }else{
                    this.$Message.error(this.$t('kefu.selectElectronicWaybillError'))
                }
            }
        }
    }
</script>

<style lang="stylus" scoped>
.form-item
    width 100%
</style>
