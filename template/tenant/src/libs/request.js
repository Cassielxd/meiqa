
import axios from 'axios'
import { Message } from 'iview'
import { getCookies, removeCookies, getSen, getLoc } from '@/libs/util'

import Setting from '@/setting'
import router from '@/router';
const service = axios.create({
    baseURL: Setting.apiBaseURL,
    timeout: 10000 // 请求超时时间
})

axios.defaults.withCredentials = true// 携带cookie

// 请求拦截器
service.interceptors.request.use(
    config => {
        config.baseURL = Setting.apiBaseURL
        const token = getCookies('tenant_token')
        if(token) {
            config.headers['Authori-zation'] = 'Bearer ' + token;
        }

        // 添加默认语言参数到请求头
        const getUrlParam = (name) => {
            const urlParams = new URLSearchParams(window.location.search);
            return urlParams.get(name);
        };

        const urlLang = getUrlParam('lang');
        const lang = urlLang && ['zh-CN', 'en-US'].includes(urlLang)
            ? urlLang
            : localStorage.getItem('local') || 'zh-CN';

        // 添加语言参数到请求头（使用标准的Accept-Language头）
        config.headers = config.headers || {};
        config.headers['Accept-Language'] = lang;
        console.log('Request headers:', config.headers);

        return config
    },
    error => {
        // do something with request error
        return Promise.reject(error)
    }
)

// response interceptor
service.interceptors.response.use(

    response => {
        let status = response.data ? response.data.status : 0
        const code = status
        switch(code) {
            case 200:
                return response.data
            case 400: case 400011: case 400012:
                return Promise.reject(response.data || { msg: '未知错误' })
            case 410000:
            case 410001:
            case 410002:
                console.log(code);
                localStorage.clear()
                removeCookies('tenant_token')
                removeCookies('expires_time')
                removeCookies('uuid')
                router.replace({ path: '/tenant/login' })
                break
            case 410003:
                removeCookies('kefuInfo')
                removeCookies('kefu_token')
                removeCookies('kefu_expires_time')
                removeCookies('kefu_uuid')
                router.replace({ path: '/kefu' })
            default:
                break
        }
    },
    error => {
        // 【安全增强】处理HTTP 401未授权响应
        if (error.response && error.response.status === 401) {
            console.warn('[认证失败] HTTP 401 - 清除会话并跳转登录页');
            localStorage.clear();
            removeCookies('tenant_token');
            removeCookies('expires_time');
            removeCookies('uuid');
            router.replace({ path: '/tenant/login' });
            return Promise.reject(error);
        }

        Message.error(error.msg);
        return Promise.reject(error);
    }
)

export default service
