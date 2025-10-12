import request from '@/libs/request'

/*
 * 登录
 * */
export function AccountLogin(data) {
    return request({
        url: '/login',
        method: 'post',
        data
    })
}

/**
 * 退出登陆
 * @constructor
 */
export function AccountLogout() {
    return request({
        url: '/system/admin/logout',
        method: 'post'
    })
}

/*
* 获取验证码图片
*/
export function captcha_pro() {
    return request({
        url: '/captcha_pro',
        method: 'get'
    })
}

/**
 * 获取轮播图和logo
 */
export function loginInfoApi() {
    return request({
        url: '/login/info',
        method: 'get'
    })
}

/**
 * 获取菜单数据
 */
export function menusApi() {
    return request({
        url: '/menus',
        method: 'get'
    })
}

/**
 * 搜索菜单数据
 */
export function menusListApi() {
    return request({
        url: '/menusList',
        method: 'get'
    })
}

/**
 * 租户注册
 */
export function AccountRegister(data) {
    return request({
        url: '/register',
        method: 'post',
        data
    })
}
