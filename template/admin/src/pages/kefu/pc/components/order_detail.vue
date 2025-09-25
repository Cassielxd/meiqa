<template>
    <div class="order_detail" v-if="orderDetail.userInfo">
        <div class="msg-box">
            <div class="box-title">{{$t('kefu.receiptInfo')}}</div>
            <div class="msg-wrapper">
                <div class="msg-item">
                    <div class="item">
                        <span>{{$t('kefu.userNickname')}}</span>{{orderDetail.userInfo.nickname}}
                    </div>
                    <div class="item">
                        <span>{{$t('kefu.recipient')}}</span>{{orderDetail.orderInfo.real_name}}
                    </div>
                </div>
                <div class="msg-item">
                    <div class="item">
                        <span>{{$t('kefu.contactPhone')}}</span>{{orderDetail.orderInfo.user_phone}}
                    </div>
                    <div class="item">
                        <span>{{$t('kefu.receiptAddress')}}</span>{{orderDetail.orderInfo.user_address}}
                    </div>
                </div>
            </div>
        </div>
        <div class="msg-box" style="border: none;">
            <div class="box-title">{{$t('kefu.orderInfo')}}</div>
            <div class="msg-wrapper">
                <div class="msg-item">
                    <div class="item">
                        <span>{{$t('kefu.orderID')}}:</span>{{orderDetail.orderInfo.order_id}}
                    </div>
                    <div class="item" style="color: red">
                        <span style="color: red">{{$t('kefu.orderStatus')}}</span>{{orderDetail.orderInfo._status._title}}
                    </div>
                </div>
                <div class="msg-item">
                    <div class="item">
                        <span>{{$t('kefu.totalQuantity')}}</span>{{orderDetail.orderInfo.total_num}}
                    </div>
                    <div class="item">
                        <span>{{$t('kefu.totalPrice')}}</span>{{parseFloat(orderDetail.orderInfo.total_price)+parseFloat(orderDetail.orderInfo.vip_true_price || 0)}}
                    </div>
                </div>
                <div class="msg-item">
                    <div class="item">
                        <span>{{$t('kefu.postage')}}</span>{{orderDetail.orderInfo.pay_postage}}
                    </div>
                    <div class="item">
                        <span>{{$t('kefu.couponAmount')}}</span>{{orderDetail.orderInfo.coupon_price}}
                    </div>
                </div>
                <div class="msg-item">
                    <div class="item">
                        <span>实际支付：</span>{{orderDetail.orderInfo.pay_price}}
                    </div>
                    <div class="item">
                        <span>创建时间：</span>{{orderDetail.orderInfo.add_time }}
                    </div>
                </div>
                <div class="msg-item">
                    <div class="item">
                        <span>支付方式：</span>{{orderDetail.orderInfo._status._payType}}
                    </div>
                    <div class="item">
                        <span>推广人：</span>{{orderDetail.userInfo.spread_name}}
                    </div>
                </div>
                <div class="msg-item">
                    <div class="item">
                        <span>商家备注：</span>{{orderDetail.orderInfo.mark}}
                    </div>
                </div>
            </div>
        </div>
        <div class="goods-box">
            <Table :columns="columns1" :data="orderList">
                <template slot-scope="{ row, index }" slot="id">
                    {{row.productInfo.id}}
                </template>
                <template slot-scope="{ row, index }" slot="name">
                    <div class="product_info">
                        <img :src="row.productInfo.image" alt="">
                        <p>{{row.productInfo.store_name}}</p>
                    </div>
                </template>
                <template slot-scope="{ row, index }" slot="className">
                    {{row.class_name}}
                </template>
                <template slot-scope="{ row, index }" slot="price">
                    {{row.productInfo.attrInfo.price}}
                </template>
                <template slot-scope="{ row, index }" slot="total_num">
                    {{row.cart_num}}
                </template>
            </Table>
        </div>
    </div>
</template>

<script>
    import { orderInfo } from '@/api/kefu'
    export default {
        name: "order_detail",
        props:{
            orderId:{
                type:String | Number,
                default:''
            },
        },
        data(){
            return {
                orderDetail:{},
                orderList:[],
                columns1: [
                    {
                        title: '商品ID',
                        slot: 'id',
                        maxWidth:80
                    },
                    {
                        title: '商品名称',
                        slot: 'name',
                        minWidth: 160
                    },
                    {
                        title: '商品分类',
                        slot: 'className'
                    },
                    {
                        title: '商品售价',
                        slot: 'price'
                    },
                    {
                        title: '商品数量',
                        slot: 'total_num'
                    }
                ],
            }
        },
        mounted() {
            this.getOrderInfo()
        },
        methods:{
            getOrderInfo(){
                orderInfo(this.orderId).then(res=>{
                    res.data.orderInfo.add_time =  this.$moment(parseInt(res.data.orderInfo.add_time)*1000).format('YYYY-MM-DD')
                    this.orderDetail = res.data
                    this.orderList = res.data.orderInfo.cartInfo
                })
            }
        }
    }
</script>

<style lang="stylus" scoped>
.order_detail
    .msg-box
        border-bottom 1px solid #E8EAED
        .box-title
            padding-top 20px
            font-size 16px
            color #333
        .msg-wrapper
            margin-top 15px
            padding-bottom 10px
            .msg-item
                display flex
                .item
                    flex 1
                    margin-bottom 15px
                    span
                        color #333
        &:first-child .box-title
            padding-top 0
    .product_info
        display flex
        align-items center
        img
            width 36px
            height 36px
            border-radius 4px
            margin-right 10px


</style>
