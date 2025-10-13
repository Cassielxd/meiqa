<template>
    <div class="content">
        <p class="font-w">{{ $t('system.usageInstructions') }}</p>
        <p class="text-i">{{ $t('system.tokenResetDescription') }}</p>
        <div class="fenlei">
            <div class="code-content-wrap">
               <textarea id="NormalCodeTextareatoken3" class="code textarea" rows="5">
{{tokeninfo.tokenMd5}}
      </textarea>
                <div class="other-wrap">
                    <a @click="getCopy('NormalCodeTextareatoken3')" class="btn btn-blue btn-large" href="javascript:void(0);"><span>{{ $t('system.copyCode') }}</span></a>
                    &nbsp;
                    <div @click="resetToken()" class="btn btn-blue btn-large" href="javascript:void(0);"><span>{{ $t('system.resetToken') }}</span></div>
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

