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
      <div class="fenlei">
             <div class="code-content-wrap">
               <textarea id="NormalCodeTextareatoken3" class="code textarea" rows="5">
             {{tokeninfo.domain}}/#/kefu
           </textarea>
          <div class="other-wrap">
            <div @click="resetDomain()" class="btn btn-blue btn-large" href="javascript:void(0);"><span>{{ $t('system.resetDomain') }}</span></div>
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
        data() {
            return {
                domainList: []
            }
        },
        mounted() {
            this.loadDomainList();

        },
        methods: {
            async loadDomainList() {
                const domainUrl = `${location.origin}/domain.json?ts=${Date.now()}`;
                try {
                    const response = await fetch(domainUrl, { cache: 'no-cache' });
                    if(!response.ok) {
                        throw new Error(`domain.json request failed with status ${response.status}`);
                    }
                    const data = await response.json();
                    this.domainList = Array.isArray(data) ? data : [];
                    if(!Array.isArray(data)) {
                        console.warn('domain.json is expected to be an array.');
                    }
                } catch (error) {
                    console.error('Failed to load domain.json', error);
                    this.domainList = [];
                }
            },
            pickDomainFromPool() {
                if(!this.domainList.length) {
                    return '';
                }
                const candidate = this.domainList[Math.floor(Math.random() * this.domainList.length)];
                if(typeof candidate === 'string') {
                    return candidate;
                }
                if(candidate && typeof candidate === 'object') {
                    return candidate.domain || candidate.value || candidate.path || '';
                }
                return '';
            },
            getCopy(id) {
//                this.cgetCopy(id);
                this.$emit('cgetCopy',id);

            },
            //充值token
            resetToken() {
//                this.cresetToken();
                this.$emit('cresetToken');

            },
            async resetDomain(){
              if(!this.domainList.length) {
                await this.loadDomainList();
              }
              const domain = this.pickDomainFromPool();
              if(!domain) {
                console.warn('No domain available from domain.json for resetDomain action.');
                return;
              }
              this.$emit('confirmeDomain', domain);
            }
        }
    }
</script>

