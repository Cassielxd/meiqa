

import request from '@/libs/request'

/**
 * 客服详细信息
 * @constructor
 */
export function serviceInfo() {
    return request({
        url: 'service/info',
        method: 'get',
        kefu: true
    });
}

/**
 * 用户端发送消息
 * @param data
 */
export function sendMessageMobile(data) {
    return request({
        url: 'service/send_message',
        method: 'post',
        mobile: true,
        data
    });
}
