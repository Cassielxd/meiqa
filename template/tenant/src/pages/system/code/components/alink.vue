<template>
    <div class="content">
        <p class="font-w">{{ $t('system.usageInstructions') }}</p>
        <p class="text-i">{{ $t('system.hyperlinkDescription') }}</p>
        <p class="font-w">{{ $t('system.hyperlinkParameters') }}</p>
                <p class="text-i">
                    <br>{{ $t('system.paramToken') }}
                    <br>{{ $t('system.paramNoCanClose') }}
                <br>{{ $t('system.paramDeviceType') }}
                <br>{{ $t('system.paramUid') }}
                <br>{{ $t('system.paramNickName') }}
                <br>{{ $t('system.paramPhone') }}
                <br>{{ $t('system.paramSex') }}
                <br>{{ $t('system.paramAvatar') }}
                <br>{{ $t('system.paramOpenid') }}
                <br>{{ $t('system.paramKefuId') }}
                </p>
        <Divider />
        <p class="typetitle">{{ $t('system.autoRecognizeBrowser') }}</p>

        <div class="fenlei">
            <div class="code-content-wrap">
        <textarea id="NormalCodeTextarealink1" class="code" rows="1">{{siteUrl}}/chat/index?noCanClose=1&token={{tokeninfo.token_md5}}</textarea>
                <div class="other-wrap">
                    <a class="btn btn-blue btn-large mr10" :href="linkUrl3" target="_blank">{{ $t('system.clickToExperience') }}</a>
                    <a @click="getCopy('NormalCodeTextarealink1')" class="btn btn-blue btn-large" href="javascript:void(0);"><span>{{ $t('system.copyCode') }}</span></a>
                </div>
            </div>

        </div>
        <p class="typetitle">{{ $t('system.pcLinkUsage') }}</p>
        <div  class="fenlei">
            <div class="code-content-wrap">
        <textarea id="NormalCodeTextarealink2" class="code" rows="1">{{siteUrl}}/chat/pc?noCanClose=1&token={{tokeninfo.token_md5}}</textarea>
                <div class="other-wrap">
                    <a class="btn btn-blue btn-large mr10" :href="linkUrl1" target="_blank">{{ $t('system.clickToExperience') }}</a>
                    <a @click="getCopy('NormalCodeTextarealink2')" class="btn btn-blue btn-large" href="javascript:void(0);"><span>{{ $t('system.copyCode') }}</span></a>
                </div>
            </div>

        </div>
        <p class="typetitle">{{ $t('system.mobileLinkUsage') }}</p>
        <div  class="fenlei">
            <div class="code-content-wrap">
        <textarea id="NormalCodeTextarealink3" class="code" rows="1">{{siteUrl}}/chat/mobile?noCanClose=1&token={{tokeninfo.token_md5}}</textarea>
            <div class="other-wrap">
                <a class="btn btn-blue btn-large mr10" :href="linkUrl2" target="_blank">{{ $t('system.clickToExperience') }}</a>
                <a @click="getCopy('NormalCodeTextarealink3')" class="btn btn-blue btn-large" href="javascript:void(0);"><span>{{ $t('system.copyCode') }}</span></a>
            </div>
            </div>

        </div>
        <div  class="fenlei">
            <p class="font-w">{{ $t('system.tips') }}</p>
            <p class="text-i">{{ $t('system.experienceTip') }}</p>
            <p class="text-i">{{ $t('system.tokenChangeTip') }}</p>

        </div>
    </div>
</template>
<script>

export default{
    name: 'alink',

    props: {
        tokeninfo:{},
        siteUrl:'',
    },
    computed: {
        linkUrl1() {
            return `${location.origin}/chat/index?token=${this.tokeninfo.token_md5}&noCanClose=1`;
        },
        linkUrl2() {
            return `${location.origin}/chat/index?token=${this.tokeninfo.token_md5}&noCanClose=1&deviceType=Mobile`;
        },
        linkUrl3() {
            return `${location.origin}/chat/index?token=${this.tokeninfo.token_md5}&noCanClose=1`;
        }
    },
    mounted() {

    },
    methods: {
        getCopy(id) {
            this.$emit('cgetCopy',id);
        },
    }
}
</script>

