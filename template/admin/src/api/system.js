// +----------------------------------------------------------------------
// | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2016~2021 https://www.crmeb.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
// +----------------------------------------------------------------------
// | Author: CRMEB Team <admin@crmeb.com>
// +----------------------------------------------------------------------

import request from '@/libs/request'

/**
 * @description 配置分类--列表
 * @param {Object} param data {Object} 传值参数
 */
export function classListApi(data) {
    return request({
        url: 'setting/config_class',
        method: 'get',
        params: data
    })
}

/**
 * @description 配置分类--新增表单
 * @param {Object} param data {Object} 传值参数
 */
export function classAddApi(data) {
    return request({
        url: 'setting/config_class/create',
        method: 'get'
    })
}

/**
 * @description 配置分类--编辑表单
 * @param {Number} param id {Number} 配置分类id
 */
export function classEditApi(id) {
    return request({
        url: `setting/config_class/${id}/edit`,
        method: 'get'
    })
}

/**
 * @description 配置分类--修改状态
 * @param {Number} param id {Number} 文章id
 */
export function setStatusApi(data) {
    return request({
        url: `setting/config_class/set_status/${data.id}/${data.status}`,
        method: 'PUT'
    })
}

/**
 * @description 配置--列表
 * @param {Object} param data {Object} 传值参数
 */
export function configTabListApi(data) {
    return request({
        url: 'setting/config',
        method: 'get',
        params: data
    })
}

/**
 * @description 配置--新增表单
 * @param {Object} param data {Object} 传值参数
 */
export function configTabAddApi(data) {
    return request({
        url: 'setting/config/create',
        method: 'get',
        params: data
    })
}

/**
 * @description 配置--编辑表单
 * @param {Number} param id {Number} 配置id
 */
export function configTabEditApi(id) {
    return request({
        url: `/setting/config/${id}/edit`,
        method: 'get'
    })
}

/**
 * @description 配置--修改状态
 * @param {Number} param id {Number} 文章id
 */
export function configSetStatusApi(id, status) {
    return request({
        url: `setting/config/set_status/${id}/${status}`,
        method: 'PUT'
    })
}

/**
 * @description 组合数据--列表
 * @param {Object} param data {Object} 传值参数
 */
export function groupListApi(data) {
    return request({
        url: 'setting/group',
        method: 'get',
        params: data
    })
}

/**
 * @description 组合数据--新增
 * @param {Object} param data {Object} 传值参数
 */
export function groupAddApi(data) {
    return request({
        url: data.url,
        method: data.method,
        data: data.datas
    })
}

/**
 * @description 组合数据--详情
 * @param {Number} param id {Number} 组合数据id
 */
export function groupInfoApi(id) {
    return request({
        url: `setting/group/${id}`,
        method: 'get'
    })
}

/**
 * @description 系统日志 -- 搜索条件
 */
export function searchAdminApi(data) {
    return request({
        url: `system/log/search_admin`,
        method: 'GET'
    })
}

/**
 * @description 系统日志 -- 搜索条件
 */
export function systemListApi(params) {
    return request({
        url: `system/log`,
        method: 'GET',
        params
    })
}

/**
 * @description 获取客服页面广告
 * @param data
 */
export function getKfAdv() {
    return request({
        url: 'setting/get_kf_adv',
        method: 'get',
    })
}

/**
 * @description 设置客服页面广告
 * @param data
 */
export function setKfAdv(data) {
    return request({
        url: 'setting/set_kf_adv',
        method: 'post',
        data
    })
}

/**
 * @description 数据配置
 * @param data
 */
export function getUserAgreementApi() {
    return request({
        url: 'setting/get_user_agreement',
        method: 'get'
    })
}


export function saveUserAgreementApi(data) {
    return request({
        url: 'setting/set_user_agreement',
        method: 'post',
        data
    })
}
/**
 * 获取客服图标上传配置/保存客服图标配置
 * @param {*} method
 * @param {*} data
 * @returns
 */
export function kfIcon(method, data) {
    return request({
        url: 'setting/config/kefu',
        method: method || 'get',
        data: data || {}
    })
}
