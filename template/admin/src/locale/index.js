import Vue from 'vue'
import VueI18n from 'vue-i18n'
import { localRead } from '@/libs/util'
import customZhCn from './lang/zh-CN'
import customZhTw from './lang/zh-TW'
import customEnUs from './lang/en-US'
import zhCnLocale from 'iview/src/locale/lang/zh-CN'
import enUsLocale from 'iview/src/locale/lang/en-US'
import zhTwLocale from 'iview/src/locale/lang/zh-TW'

Vue.use(VueI18n);

// 从URL参数获取语言设置
const getUrlLang = () => {
  const urlParams = new URLSearchParams(window.location.search);
  const urlLang = urlParams.get('lang') || urlParams.get('language');
  if (urlLang) {
    // 支持的语言列表
    const supportedLangs = ['zh-CN', 'en-US', 'zh-TW'];
    // 语言映射，支持简写
    const langMap = {
      'zh': 'zh-CN',
      'cn': 'zh-CN',
      'en': 'en-US',
      'tw': 'zh-TW',
      'hk': 'zh-TW'
    };

    const normalizedLang = langMap[urlLang.toLowerCase()] || urlLang;
    return supportedLangs.includes(normalizedLang) ? normalizedLang : null;
  }
  return null;
};

// 语言优先级: URL参数 > 本地存储 > 浏览器语言 > 默认中文
const urlLang = getUrlLang();
const navLang = navigator.language;
const localLang = (navLang === 'zh-CN' || navLang === 'en-US' || navLang === 'zh-TW') ? navLang : false;
let lang = urlLang || localRead('local') || localLang || 'zh-CN';

// 如果从URL获取了语言，保存到本地存储
if (urlLang) {
  localStorage.setItem('local', urlLang);
}

Vue.config.lang = lang;

// vue-i18n 6.x+写法
Vue.locale = () => {};
const messages = {
  'zh-CN': Object.assign(zhCnLocale, customZhCn),
  'zh-TW': Object.assign(zhTwLocale, customZhTw),
  'en-US': Object.assign(enUsLocale, customEnUs)
};
const i18n = new VueI18n({
  locale: lang,
  messages
});

export default i18n

// vue-i18n 5.x写法
// Vue.locale('zh-CN', Object.assign(zhCnLocale, customZhCn))
// Vue.locale('en-US', Object.assign(zhTwLocale, customZhTw))
// Vue.locale('zh-TW', Object.assign(enUsLocale, customEnUs))
