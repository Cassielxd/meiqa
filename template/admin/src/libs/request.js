import axios from 'axios'
import { Message } from 'iview'
import { getCookies, removeCookies, setCookies, getLoc } from '@/libs/util'
import Setting from '@/setting'
import router from '@/router'

const service = axios.create({
    baseURL: Setting.apiBaseURL,
    timeout: 10000
})

axios.defaults.withCredentials = true

service.interceptors.request.use(
    config => {
        let baseUrl
        if (config.kefu) {
            baseUrl = Setting.apiBaseURL.replace('api/admin', 'api/kefu')
            config.baseURL = baseUrl
        } else if (config.mobile) {
            baseUrl = Setting.apiBaseURL.replace('api/admin', 'api/mobile')
            config.baseURL = baseUrl
        } else {
            config.baseURL = Setting.apiBaseURL
        }

        const token = getCookies('token')
        const kefuToken = getCookies('kefu_token')
        const mobileToken = getLoc('mobile_token')

        if (token || kefuToken || mobileToken) {
            config.headers['Authori-zation'] = config.mobile
                ? 'Bearer ' + mobileToken
                : config.kefu
                    ? 'Bearer ' + kefuToken
                    : 'Bearer ' + token
        }
        return config
    },
    error => Promise.reject(error)
)

service.interceptors.response.use(
    response => {
        const status = response.data ? response.data.status : 0
        switch (status) {
            case 200:
                return response.data
            case 400:
            case 400011:
            case 400012:
                return Promise.reject(response.data || { msg: '未知错误' })
            case 410000:
            case 410001:
            case 410002:
                clearAdminAuth()
                router.replace('/admin/login')
                return Promise.reject({ msg: 'Token过期，请重新登录' })
            case 410003:
                if (isKefuRequest(response.config) && !isRefreshRequest(response.config)) {
                    return handleKefuTokenExpiry(response.config)
                }
                clearKefuAuth()
                redirectToKefuLogin()
                return Promise.reject({ msg: 'Kefu token过期' })
            default:
                return response.data
        }
    },
    error => {
        const { response } = error || {}
        const status = response ? response.status : null
        const data = response && response.data ? response.data : {}
        const message = data.msg || error.message || '请求失败'
        const config = response ? (response.config || {}) : {}
        const isKefu = isKefuRequest(config)

        if (status === 401) {
            if (isKefu && !isRefreshRequest(config)) {
                return handleKefuTokenExpiry(config)
            }
            if (!isKefu) {
                clearAdminAuth()
                if (router.currentRoute.path !== '/admin/login') {
                    router.replace({
                        path: '/admin/login',
                        query: { redirect: router.currentRoute.fullPath }
                    })
                }
                Message.error(message || '登录状态已失效，请重新登录')
            } else {
                clearKefuAuth()
                redirectToKefuLogin(router.currentRoute.fullPath, message)
            }
        } else {
            Message.error(message)
        }

        return Promise.reject({ ...error, msg: message })
    }
)

export default service

function isKefuRequest(config) {
    if (!config) return false
    if (config.kefu) return true
    if (!config.baseURL) return false
    return config.baseURL.indexOf('/api/kefu') !== -1
}

function isRefreshRequest(config) {
    if (!config || !config.url) return false
    return config.url.indexOf('/token/refresh') !== -1
}

function clearAdminAuth() {
    localStorage.clear()
    removeCookies('token')
    removeCookies('expires_time')
    removeCookies('uuid')
}

function clearKefuAuth() {
    removeCookies('kefuInfo')
    removeCookies('kefu_token')
    removeCookies('kefu_expires_time')
    removeCookies('kefu_uuid')
}

function redirectToKefuLogin(redirectTarget, msg) {
    const currentRoute = router.currentRoute || {}
    const target = redirectTarget || currentRoute.fullPath || '/kefu'
    if (currentRoute.path !== '/kefu') {
        router.replace({
            path: '/kefu',
            query: { redirect: target }
        })
    }
    Message.error(msg || '客服登录状态已失效，请重新登录')
}

let refreshTokenPromise = null

function handleKefuTokenExpiry(originalConfig) {
    if (!originalConfig) {
        clearKefuAuth()
        redirectToKefuLogin()
        return Promise.reject({ msg: 'Kefu token过期' })
    }

    if (originalConfig.__isRetryRequest) {
        clearKefuAuth()
        redirectToKefuLogin()
        return Promise.reject({ msg: 'Kefu token过期' })
    }

    if (!refreshTokenPromise) {
        refreshTokenPromise = requestKefuTokenRefresh()
            .then(data => {
                applyKefuTokenData(data)
            })
            .catch(err => {
                clearKefuAuth()
                redirectToKefuLogin(null, extractErrorMessage(err, '刷新登录状态失败，请重新登录'))
                throw err
            })
            .finally(() => {
                refreshTokenPromise = null
            })
    }

    return refreshTokenPromise.then(() => {
        const retryConfig = {
            ...originalConfig,
            __isRetryRequest: true
        }
        return service(retryConfig)
    })
}

function requestKefuTokenRefresh() {
    const token = getCookies('kefu_token')
    if (!token || token === 'undefined') {
        return Promise.reject({ msg: '缺少登录凭证' })
    }
    const baseURL = buildKefuBaseURL()
    return axios({
        baseURL,
        url: '/token/refresh',
        method: 'post',
        headers: {
            'Authori-zation': 'Bearer ' + token
        },
        withCredentials: true
    }).then(res => {
        const payload = res.data || {}
        if (payload.status !== 200 || !payload.data) {
            return Promise.reject({ msg: payload.msg || '刷新登录状态失败' })
        }
        return payload.data
    }).catch(err => {
        const message = extractErrorMessage(err, '刷新登录状态失败')
        return Promise.reject({ msg: message })
    })
}

function applyKefuTokenData(data) {
    if (!data || !data.token) {
        throw { msg: '刷新未返回有效 token' }
    }
    const expTime = data.exp_time || data.expires_time || Math.round(Date.now() / 1000) + 7200
    const expiresDays = computeCookieDays(expTime)
    setCookies('kefu_token', data.token, expiresDays)
    setCookies('kefu_expires_time', expTime, expiresDays)
    if (data.kefu_info) {
        setCookies('kefuInfo', data.kefu_info, expiresDays)
        if (data.kefu_info.uid) {
            setCookies('kefu_uuid', data.kefu_info.uid, expiresDays)
        }
    }
    if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('kefu-token-updated', {
            detail: { token: data.token }
        }))
    }
}

function buildKefuBaseURL() {
    const baseUrl = Setting.apiBaseURL || ''
    if (baseUrl.includes('api/admin')) {
        return baseUrl.replace('api/admin', 'api/kefu')
    }
    if (baseUrl.includes('/adminapi')) {
        return baseUrl.replace('/adminapi', '/kefuapi')
    }
    if (baseUrl.endsWith('/admin')) {
        return baseUrl.replace(/\/admin$/, '/kefu')
    }
    return baseUrl
}

function computeCookieDays(expTime) {
    const now = Math.round(Date.now() / 1000)
    const diff = expTime - now
    return diff > 0 ? diff / 86400 : 1
}

function extractErrorMessage(err, fallback) {
    if (!err) {
        return fallback
    }
    if (err.response && err.response.data && err.response.data.msg) {
        return err.response.data.msg
    }
    if (err.msg) {
        return err.msg
    }
    if (err.message) {
        return err.message
    }
    return fallback
}
