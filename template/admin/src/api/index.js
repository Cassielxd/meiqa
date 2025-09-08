

import request from '@/libs/request'

/**
 * @description 首页订单图表
 */
export function orderApi (params) {
    return request({
        url: 'home/order',
        method: 'get',
        params
    })
}

/**
 * @description 首页订单图表
 */
export function userApi () {
    return request({
        url: 'home/user',
        method: 'get'
    })
}

/**
 * @description 首页统计
 */
export function sumApi () {
    return request({
        url: 'chart/sum',
        method: 'get'
    })
}

/**
 * @description 首页统计图表
 */
export function charApi (params) {
    return request({
        url: 'chart',
        method: 'get',
        params
    })
}

/**
 * @description 首页商品交易额排行
 */
export function rankApi () {
    return request({
        url: 'home/rank',
        method: 'get'
    })
}

export function checkAuth () {
    return request({
        url: 'check_auth',
        method: 'get'
    })
}
