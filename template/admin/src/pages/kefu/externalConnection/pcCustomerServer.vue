<template>

    <div class="pc_customerServer">
        <div class="fixed" v-if="upperData.noCanClose == 1"></div>
        <div class="pc_customerServer_container max-width_con"
             :class="{'max-width_advertisement': upperData.noCanClose == 1 || upperData.windowStyle == `center`}">
            <!-- 客服头部开始 -->
            <div class="pc_customerServer_container_header">
                <div class="pc_customerServer_container_header_title">

                    <img :src="chatServerData.to_user_avatar" alt="">
                    <span>{{chatServerData.to_user_nickname}}</span>
                </div>
                <div class="pc_customerServer_container_header_handle" @click="closeIframe"
                     v-if="upperData.noCanClose != '1'">
                    <span class="iconfont">&#xe6c5;</span>
                </div>
            </div>
            <!-- 客服头部结束 -->

            <div class="layout_content">

                <div class="layout_customerServer_content">
                    <!-- 聊天内容开始 -->
                    <div class="pc_customerServer_container_content">
                        <div class="productMessage_container" v-if="isShowProductModel">
                            <div class="productMessage_container_image">
                                <img :src="productMessage.image" alt="">
                            </div>
                            <div class="productMessage_container_content">
                                <div class="productMessage_container_content_title">{{productMessage.store_name}}</div>
                                <div class="productMessage_container_content_priceOrHandle">
                                    <div>￥{{productMessage.price}}</div>
                                    <div @click="sendProduct"> {{$t('chat.sendCustomer')}}</div>
                                </div>
                            </div>
                        </div>
                        <happy-scroll size="1" resize hide-horizontal :scroll-top="scrollTop"
                                      @vertical-start="scrollHandler">
                            <div class="scroll_content" id="chat_scroll"
                                 :class="{ 'pt140': isShowProductModel || inputConType == 2 }">
                                <!-- 滑动到容器顶部时，动画加载 -->
                                <Spin v-show="isLoad">
                                    <Icon type="ios-loading" size=18 class="demo-spin-icon-load"></Icon>
                                    <div>{{$t('kefu.loading')}}</div>
                                </Spin>
                                <!-- 动画结束 -->

                                <!-- 聊天内容列表 -->
                                <div class="chart_list">

                                    <div class="chart_list_item" v-for="(item, index) in records" :key="index">

                                        <div class="chart_list_item_time" v-show="item.show">{{item.time}}</div>
                                        <div class="chart_list_item_content"
                                             :class="{'right-box': item.user_id == chatServerData.user_id}">
                                            <div class="chart_list_item_avatar">
                                                <img :src="item.avatar" alt="">
                                            </div>
                                            <!-- 文字及表情信息 -->
                                            <div class="chart_list_item_text" v-if="item.msn_type <= 2">
                                                <span v-html="replace_em(item.msn)"></span>
                                            </div>
                                            <!-- 图片信息 -->
                                            <div class="chart_list_item_img" v-if="item.msn_type == 3">
                                                <img v-lazy="item.msn" @load="imageLoad"/>
                                            </div>

                                            <!-- 图文信息 -->
                                            <div class="chart_list_item_imgOrText" v-if="item.msn_type == 5">
                                                <div class="order-wrapper">
                                                    <div class="img-box">
                                                        <img :src="item.other.image" alt="">
                                                    </div>
                                                    <div class="order-info">
                                                        <div class="price-box">
                                                            <div class="num">¥ {{item.other.price}}</div>
                                                            <!-- <a herf="javascript:;" class="more" @click.stop="lookGoods(item)">查看商品 ></a> -->
                                                        </div>
                                                        <div class="name">{{item.other.store_name}}</div>
                                                    </div>

                                                </div>
                                            </div>

                                        </div>

                                    </div>
                                </div>
                                <!-- 聊天内容列表结束 -->
                            </div>
                        </happy-scroll>

                    </div>
                    <!-- 聊天内容结束 -->

                    <!-- 内容输入开始 -->
                    <div class="pc_customerServer_container_footer">
                        <div class="pc_customerServer_container_footer_header">
                            <!-- 表情及图片容器 -->
                            <div class="pc_customerServer_container_footer_emoji" v-if="inputConType == 2">
                                <div class="emoji-item" v-for="(emoji, index) in emojiList" :key="index">
                                    <i class="em" :class="emoji" @click.stop="select(emoji)"></i>
                                </div>
                            </div>
                            <div class="pc_customerServer_container_footer_header_handle">
                                <div @click="inputConType = 2;goPageBottom()">
                                    <img src="@/assets/images/customerServer/face.png" alt="">
                                </div>
                                <div>
                                    <img src="@/assets/images/customerServer/picture.png" alt="">
                                    <input type="file" accept=".jp2,.jpe,.jpeg,.jpg,.png,.svf,.tif,.tiff"
                                           class="type_file" @change="uploadFile">
                                </div>
                            </div>
                        </div>

                        <!-- 输入框容器 -->
                        <div class="pc_customerServer_container_footer_input" @click="inputConType = 1">
                            <!-- <textarea v-model="userMessage" @keyup.enter="sendText" class="pc_customerServer_container_footer_input-textarea opacity0" rows="5" placeholder="请输入文字"></textarea> -->
                            <div v-paste="handleParse" ref="inputDiv" @keyup.enter="sendText" contenteditable
                                 class="pc_customerServer_container_footer_input-textarea"
                                 :class="{'readyEmojiHeight': inputConType == 2}"
                                 placeholder="输入消息，按 Enter 发送">
                            </div>
                            <!-- 发送按钮内嵌在输入框内 -->
                            <div class="pc_customerServer_container_footer_handle_send" @click="sendText">
                                <span>{{$t('kefu.send')}}</span>
                            </div>
                        </div>
                        <!-- 输入框容器结束 -->
                        <div class="pc_customerServer_container_footer_copyright" @click="tolink"
                             v-if="upperData.noCanClose != '1' && upperData.windowStyle != `center`">
                            <span>CRMChat</span>
                        </div>
                        <!-- 相关操作结束 -->

                    </div>
                    <!-- 内容输入结束 -->
                </div>

                <div class="pc_customerServer_container_advertisement"
                     v-if="upperData.noCanClose == '1' || upperData.windowStyle == `center`">
                    <div class="advertisement">
                        <div v-html="advertisement"></div>
                        <div class="copyright" @click="tolink">
                            <span>CRMChat</span>
                        </div>
                    </div>
                </div>
            </div>

        </div>

    </div>
</template>
<script>
    import {HappyScroll} from 'vue-happy-scroll'
    import emojiList from "@/utils/emoji";
    import socketServer from './minix/socketServer';

    export default {
        components: {
            HappyScroll
        },
        mixins: [socketServer],
        data() {
            return {
                happyScroll: false,
                isLoad: false,
                scrollTop: 0,
                emojiList: emojiList,
                inputConType: 1,
                deviceType: 'pc',
            }
        },
        computed: {
            records() {
                return this.chatServerData.serviceList.map((item, index) => {
                    item.time = this.$moment(item.add_time * 1000).format('MMMDo H:mm')
                    if (index) {
                        if (
                            item.add_time -
                            this.chatServerData.serviceList[index - 1].add_time >=
                            300
                        ) {
                            item.show = true;
                        } else {
                            item.show = false;
                        }
                    } else {
                        item.show = true;
                    }
                    return item;
                });
            }
        },
        methods: {

            getScrollTop() {
                console.log(123);
            },
            getScrollEnd() {
                console.log(321);
            },
            scrollHandler(e) {
                console.log('Scrolled to top');
                this.isLoad = true;
                setTimeout(() => {
                    this.isLoad = false;
                }, 2000)
            },
            // 聊天表情转换
            replace_em(str) {
                str = str.replace(/\[em-([\s\S]*)\]/g, "<span class='em em-$1'/></span>");
                return str;
            },
        }
    }
</script>
<style lang="less" scoped>
    .max-width_con {
        max-width: 600px;
    }

    .max-width_advertisement {
        max-width: 840px;
    }

    .pc_customerServer_container {
        width: 100%;
        height: 100%;
        max-height: 720px;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        background: #F9FAFB;
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
        border-radius: 16px;
        overflow: hidden;

        &_header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%);
            padding: 14px 14px;
            box-sizing: border-box;
            height: 56px;
            font-size: 16px;
            color: #fff;

            &_title {
                display: flex;
                align-items: center;

                img {
                    width: 40px;
                    height: 40px;
                    border-radius: 50%;
                    margin-right: 10px;
                    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1), 0 0 0 2px rgba(255, 255, 255, 0.6);
                    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

                    &:hover {
                        transform: scale(1.08);
                        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 0 0 0 3px rgba(255, 255, 255, 0.8);
                    }
                }
            }

            &_handle {
                cursor: pointer;
                transition: all 0.2s ease;

                &:hover {
                    transform: scale(1.1);
                }
            }
        }

        &_content {
            flex: 1;
            overflow: hidden;
            background: #F3F4F6;

            .scroll_content {
                width: 100%;
                height: 100%;
                overflow-y: auto;
                padding: 20px 16px;
                box-sizing: border-box;
                position: relative;

                .chart_list {
                    &_item {
                        margin-bottom: 16px;
                        animation: message-slide-in 0.3s ease-out;

                        @keyframes message-slide-in {
                            from {
                                opacity: 0;
                                transform: translateY(10px);
                            }
                            to {
                                opacity: 1;
                                transform: translateY(0);
                            }
                        }

                        &_time {
                            text-align: center;
                            color: #6B7280;
                            font-size: 12px;
                            margin: 16px 0;
                            font-weight: 500;
                            padding: 5px 12px;
                            background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(249, 250, 251, 0.9) 100%);
                            border: 1px solid rgba(0,0,0,0.06);
                            border-radius: 999px;
                            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
                            backdrop-filter: blur(4px);
                            display: inline-block;
                        }

                        &_content {
                            display: flex;
                            align-items: flex-start;
                            padding: 0;
                            box-sizing: border-box;
                        }

                        &_avatar {
                            width: 40px;
                            height: 40px;
                            border-radius: 50%;
                            overflow: hidden;
                            margin-right: 12px;
                            align-self: flex-start;
                            flex-shrink: 0;

                            img {
                                width: 100%;
                                height: 100%;
                                object-fit: cover;
                                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1), 0 0 0 2px rgba(255, 255, 255, 0.6);
                                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                            }
                        }

                        &_text {
                            max-width: 60%;
                            word-wrap: break-word;
                            background: #F5F6F8;
                            padding: 12px 14px;
                            font-size: 14px;
                            border-radius: 18px 18px 18px 4px;
                            color: #1F2937;
                            overflow: hidden;
                            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06), 0 1px 3px rgba(0, 0, 0, 0.03);
                            transition: all 0.2s ease;
                            border: 1px solid rgba(0, 0, 0, 0.04);
                            position: relative;

                            &:hover {
                                transform: translateY(-2px);
                                box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12), 0 2px 6px rgba(0, 0, 0, 0.08);
                            }

                            &::after {
                                content: '';
                                position: absolute;
                                left: -6px;
                                bottom: 10px;
                                width: 12px;
                                height: 12px;
                                background: inherit;
                                transform: rotate(45deg);
                                border-bottom: 1px solid rgba(0,0,0,0.04);
                                border-left: 1px solid rgba(0,0,0,0.04);
                            }
                        }

                        &_img {
                            max-width: 60%;
                            border-radius: 12px;
                            overflow: hidden;
                            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

                            img {
                                width: 100%;
                                height: auto;
                                display: block;
                            }
                        }

                        .chart_list_item_imgOrText {
                            background: #F9FAFB;
                            padding: 10px;
                            border-radius: 12px;
                            width: 320px;
                            box-sizing: border-box;
                            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

                            .order-wrapper {
                                display: flex;

                                .img-box {
                                    width: 64px;
                                    height: 64px;
                                    flex-shrink: 0;

                                    img {
                                        width: 100%;
                                        height: 100%;
                                        border-radius: 10px;
                                        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                                    }
                                }

                                .order-info {
                                    display: flex;
                                    flex-direction: column;
                                    justify-content: space-between;
                                    width: 224px;
                                    margin-left: 10px;
                                    font-size: 12px;

                                    .price-box {
                                        display: flex;
                                        align-items: center;
                                        justify-content: space-between;
                                        font-size: 14px;
                                        color: #FF0000;
                                    }

                                    .name {
                                        font-size: 14px;
                                    }
                                }
                            }
                        }

                        .right-box {
                            flex-direction: row-reverse;

                            .chart_list_item_avatar {
                                margin-left: 12px;
                                margin-right: 0;
                            }

                            .chart_list_item_text {
                                text-align: left;
                                background: linear-gradient(135deg, #E0E7FF 0%, #D4D9F7 100%);
                                border-radius: 18px 18px 4px 18px;
                                color: #312E81;
                                box-shadow: 0 2px 12px rgba(79, 70, 229, 0.15), 0 1px 3px rgba(79, 70, 229, 0.08);
                                border: 1px solid rgba(79, 70, 229, 0.1);

                                &:hover {
                                    box-shadow: 0 8px 20px rgba(79, 70, 229, 0.2), 0 2px 6px rgba(79, 70, 229, 0.12);
                                }

                                &::after {
                                    left: auto;
                                    right: -6px;
                                    border-left: none;
                                    border-right: 1px solid rgba(79, 70, 229, 0.12);
                                    border-bottom: 1px solid rgba(79, 70, 229, 0.12);
                                }
                            }

                            .chart_list_item_img {
                                text-align: right;

                                img {
                                    width: 100%;
                                    height: auto;
                                }
                            }

                            .chart_list_item_imgOrText {
                                background: #F9FAFB;
                                padding: 10px;
                                border-radius: 12px;
                                width: 320px;
                                box-sizing: border-box;

                                .order-wrapper {
                                    .img-box {
                                        width: 64px;
                                        height: 64px;

                                        img {
                                            width: 100%;
                                            height: 100%;
                                            border-radius: 10px;
                                        }
                                    }

                                    .order-info {
                                        .price-box {
                                            color: #FF0000;
                                            font-size: 14px;
                                        }

                                        .name {
                                            font-size: 14px;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        &_footer {
            background: #F9FAFB;
            padding: 0 0 12px 0;
            min-height: 190px;
            border-top: 1px solid #E5E7EB;
            display: flex;
            flex-direction: column;

            &_header {
                position: relative;
                padding: 12px 16px;
                background: #F5F6F8;
                border-bottom: 1px solid #E5E7EB;
                border-radius: 12px;
                margin: 10px 16px 8px 16px;
                box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);

                &_handle {
                    display: flex;
                    align-items: center;

                    > div {
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        width: 38px;
                        height: 38px;
                        margin-right: 6px;
                        border-radius: 10px;
                        cursor: pointer;
                        transition: all 0.2s ease;
                        background: #F9FAFB;
                        position: relative;

                        &:hover {
                            background: #E5E7EB;
                            transform: translateY(-2px);
                            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
                        }

                        &:active {
                            transform: translateY(0);
                            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
                        }

                        img {
                            width: 20px;
                            height: 20px;
                        }

                        .type_file {
                            position: absolute;
                            width: 100%;
                            height: 100%;
                            top: 0;
                            left: 0;
                            opacity: 0;
                            cursor: pointer;
                        }
                    }
                }
            }

            &_input {
                flex: 1;
                display: flex;
                flex-direction: column;
                padding: 0 16px 18px 16px;
                min-height: 0;
                position: relative;

                &-textarea {
                    width: 100%;
                    flex: 1;
                    border: none;
                    outline: none;
                    padding: 14px 18px 56px 18px;
                    font-size: 14px;
                    line-height: 1.6;
                    color: #374151;
                    border-radius: 14px;
                    background: #F3F4F6;
                    border: 2px solid transparent;
                    transition: all 0.2s ease;
                    box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.04);
                    overflow-y: auto;
                    resize: none;

                    &:focus {
                        background: #FAFBFC;
                        border-color: #4F46E5;
                        box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1), inset 0 2px 4px rgba(0, 0, 0, 0.02);
                    }

                    /* contenteditable placeholder */
                    &:empty:before {
                        content: attr(placeholder);
                        color: #9CA3AF;
                    }
                }

                .readyEmojiHeight {
                    min-height: 90px;
                }
            }

            &_emoji {
                position: absolute;
                left: 16px;
                bottom: 210px;
                display: flex;
                flex-wrap: wrap;
                width: 400px;
                padding: 16px;
                box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(0, 0, 0, 0.08);
                background: #F5F6F8;
                border-radius: 16px;
                border: 1px solid #E5E7EB;
                z-index: 100;
                max-height: 200px;
                overflow-y: auto;

                .emoji-item {
                    margin-right: 8px;
                    margin-bottom: 8px;
                    cursor: pointer;
                    padding: 6px;
                    border-radius: 8px;
                    transition: all 0.15s ease;

                    &:hover {
                        background: #F3F4F6;
                        transform: scale(1.1);
                    }

                    &:active {
                        transform: scale(0.95);
                    }

                    &:nth-child(10n) {
                        margin-right: 0;
                    }
                }
            }

            &_handle_send {
                position: absolute;
                right: 24px;
                bottom: 50px;
                padding: 12px 32px;
                background: linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%);
                border-radius: 12px;
                box-shadow: 0 4px 12px rgba(79, 70, 229, 0.35), 0 2px 6px rgba(79, 70, 229, 0.2);
                transition: all 0.2s ease;
                font-weight: 600;
                font-size: 15px;
                letter-spacing: 0.3px;
                border: none;
                color: #fff;
                display: flex;
                align-items: center;
                justify-content: center;
                min-height: 44px;
                cursor: pointer;
                z-index: 10;

                &:hover {
                    box-shadow: 0 8px 20px rgba(79, 70, 229, 0.45), 0 4px 10px rgba(79, 70, 229, 0.25);
                    transform: translateY(-2px);
                    background: linear-gradient(135deg, #4338CA 0%, #6D28D9 100%);
                }

                &:active {
                    transform: translateY(0);
                    box-shadow: 0 2px 8px rgba(79, 70, 229, 0.3);
                }
            }

            &_copyright {
                position: absolute;
                left: 0;
                width: 100%;
                display: block;
                text-align: center;
                bottom: 4px;
                color: #bbb;
                padding: 2px 10px;
                font-size: 11px;
                /*background-color: #eee;*/
            }
        }
    }

    .layout_content {
        flex: 1;
        display: flex;
        // justify-content: space-between;
        .layout_customerServer_content {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            border-right: 1px solid #ececec;
        }

        .pc_customerServer_container_advertisement {
            width: 260px;
            background: #fff;

            .advertisement {
                padding: 5px;
                box-sizing: border-box;
                height: 550px;
                overflow-y: auto;

                img {
                    max-width: 100% !important;
                }
            }

            .copyright {
                position: fixed;
                bottom: 20px;
                text-align: center;
                width: 230px;
                transition: 0.3s;
                z-index: 99;
                cursor: pointer;

                span {
                    color: #ccc;
                }

                span:hover {
                    color: #007aff;
                }
            }
        }
    }

    .demo-spin-icon-load {
        animation: ani-demo-spin 1s linear infinite;
    }

    .productMessage_container {
        height: 94px;
        width: 100%;
        padding: 12px;
        box-sizing: border-box;
        background: #fff;
        display: flex;
        justify-content: space-between;

        &_image {
            margin-right: 12px;

            img {
                width: 77px;
                height: 77px;
            }
        }

        &_content {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: space-between;

            &_title {
                font-size: 14px;
                color: #333;
                height: 42px;
                font-weight: 800;
                overflow: hidden;
                text-overflow: ellipsis;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                -webkit-box-orient: vertical;
                text-align: left !important;
            }

            &_priceOrHandle {
                display: flex;
                justify-content: space-between;

                > div:nth-child(1) {
                    font-size: 18px;
                    color: #e93323;
                    text-align: left;
                }

                > div:nth-child(2) {
                    width: 65px;
                    height: 25px;
                    background: #e83323;
                    opacity: 1;
                    border-radius: 62px;
                    color: #fff;
                    font-size: 12px;
                    text-align: center;
                    line-height: 25px;
                    cursor: pointer;
                }
            }
        }
    }

    .fixed {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.7);
    }

    .pt140 {
        padding-bottom: 140px !important;
    }

    @keyframes ani-demo-spin {
        from {
            transform: rotate(0deg);
        }

        50% {
            transform: rotate(180deg);
        }

        to {
            transform: rotate(360deg);
        }
    }

    /deep/ .happy-scroll-content {
        width: 100% !important;
        box-sizing: border-box;
    }
</style>
<style lang="less">
    .advertisement {
        img,
        p,
        div,
        span {
            max-width: 100%;
        }
    }

    .happy-scroll-container {
        width: 100% !important;
    }

    .advertisement {
        overflow: auto !important;
    }

    .advertisement::-webkit-scrollbar {
        /*滚动条整体样式*/
        width: 1px; /*高宽分别对应横竖滚动条的尺寸*/
        height: 1px;
    }

    .advertisement::-webkit-scrollbar-thumb {
        /*滚动条里面小方块*/
        border-radius: 10px;
        background-color: skyblue;
        background-image: -webkit-linear-gradient(45deg,
        rgba(255, 255, 255, 0.2) 25%,
        transparent 25%,
        transparent 50%,
        rgba(255, 255, 255, 0.2) 50%,
        rgba(255, 255, 255, 0.2) 75%,
        transparent 75%,
        transparent);
    }

    .advertisement::-webkit-scrollbar-track {
        /*滚动条里面轨道*/
        box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.2);
        background: #ededed;
        border-radius: 10px;
    }
</style>
