import request from '@/libs/request'

/**
 * @description 租户列表
 * @param {Object} data 查询参数
 */
export function tenantListApi(data) {
    return request({
        url: 'tenant/list',
        method: 'get',
        params: data
    })
}

/**
 * @description 租户详情
 * @param {Number} id 租户ID
 */
export function tenantInfoApi(id) {
    return request({
        url: `tenant/info/${id}`,
        method: 'get'
    })
}

/**
 * @description 创建租户
 * @param {Object} data 租户数据
 */
export function tenantCreateApi(data) {
    return request({
        url: 'tenant/save',
        method: 'post',
        data
    })
}

/**
 * @description 更新租户信息
 * @param {Number} id 租户ID
 * @param {Object} data 租户数据
 */
export function tenantUpdateApi(id, data) {
    return request({
        url: `tenant/update/${id}`,
        method: 'put',
        data
    })
}

/**
 * @description 删除租户
 * @param {Number} id 租户ID
 */
export function tenantDeleteApi(id) {
    return request({
        url: `tenant/delete/${id}`,
        method: 'delete'
    })
}

/**
 * @description 更新租户状态
 * @param {Number} id 租户ID
 * @param {Number} status 状态值 (0:禁用, 1:启用)
 */
export function tenantUpdateStatusApi(id, status) {
    return request({
        url: `tenant/status/${id}`,
        method: 'put',
        data: { status }
    })
}

/**
 * @description 批量更新租户状态
 * @param {Array} ids 租户ID数组
 * @param {Number} status 状态值 (0:禁用, 1:启用)
 */
export function tenantBatchUpdateStatusApi(ids, status) {
    return request({
        url: 'tenant/batch_status',
        method: 'put',
        data: { ids, status }
    })
}

/**
 * @description 获取租户统计信息
 */
export function tenantStatisticsApi() {
    return request({
        url: 'tenant/statistics',
        method: 'get'
    })
}

/**
 * @description 获取即将过期的租户
 * @param {Object} data 查询参数
 */
export function tenantExpiringApi(data) {
    return request({
        url: 'tenant/expiring',
        method: 'get',
        params: data
    })
}

/**
 * @description 重置租户密码
 * @param {Number} id 租户ID
 * @param {Object} data 密码数据
 */
export function tenantResetPasswordApi(id, data) {
    return request({
        url: `tenant/reset_password/${id}`,
        method: 'put',
        data
    })
}