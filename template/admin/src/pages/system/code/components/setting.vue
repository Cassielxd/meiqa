<template>
    <div class="content">
        <p class="font-w">使用简介</p>
      <p class="text-i">网页内快速接入客服，让网页拥有客服窗口，请把一下代码复制到网页最底部。</p>
      <p class="text-i">体验客服：http://lxd.vv-xj.com/admin/#/kefu    kefu1 xjlidong123</p>
        <div class="fenlei">
            <div class="code-content-wrap">
               <textarea id="NormalCodeTextareatoken3" class="code textarea" rows="5">
{{tokeninfo.tokenMd5}}
      </textarea>
                <div class="other-wrap">
                    <a @click="getCopy('NormalCodeTextareatoken3')" class="btn btn-blue btn-large" href="javascript:void(0);"><span>复制代码</span></a>
                    &nbsp;

                </div>
            </div>
        </div>

    </div>
</template>
<script>
    export default{
        name: 'wangye',
        props: {
            tokeninfo:{},
            siteUrl:'',
//            cgetCopy:{},
//            cresetToken:{},

        },
        mounted() {

        },
        methods: {
            getCopy(id) {
//                this.cgetCopy(id);
                this.$emit('cgetCopy',id);

            },
            //充值token
            resetToken() {
//                this.cresetToken();
                this.$emit('cresetToken');

            },
        }
    }
</script>

