

import { AccountLogoutKefu } from '@/api/kefu';
import { getCookies, removeCookies, setCookies } from '@/libs/util'
import router from '@/router';
import { Modal } from 'view-design';
import { Socket } from '@/libs/socket';
export default {
    namespaced: true,
    state: {
        kefuInfo: null,
    },
    mutations: {
        setInfo(state, val) {
            state.kefuInfo = val
        },
    },
    actions: {
        /**
         * @description 退出登录
         * @param {Object} vm - Vue实例，用于调用组件方法（如清理Token定时器）
         * */
        logoutKefu({ commit, dispatch }, { confirm = false, vm } = {}) {
            async function logout() {
                // 1. 先发送logout消息到服务器，标记离线
                Socket(false).then(ws => {
                    ws.send({
                        type: 'logout',
                        data: { uid: getCookies('kefu_uuid') }
                    }).then(() => {
                        console.log('[退出登录] 已发送logout消息到服务器');
                    }).catch(err => {
                        console.warn('[退出登录] 发送logout消息失败:', err);
                    });

                    // 2. 等待100ms让服务器处理logout消息，然后手动关闭WebSocket（不重连）
                    setTimeout(() => {
                        ws.manuallyClose();
                    }, 100);
                });

                // 3. 调用后端API退出登录
                AccountLogoutKefu().then(() => {
                    console.log('[退出登录] 后端API退出成功');
                }).catch(err => {
                    console.warn('[退出登录] 后端API退出失败:', err);
                });

                // 4. 清理Token检查定时器
                if (vm && typeof vm.cleanupTokenTimer === 'function') {
                    vm.cleanupTokenTimer();
                }

                // 5. 清理所有本地认证信息
                localStorage.clear();

                // 清除所有Cookie（与Token过期处理保持一致）
                document.cookie.split(";").forEach(c => {
                    document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
                });

                // 6. 清空 vuex 用户信息
                commit('setInfo', null);

                // 7. 跳转到登录页（使用replace避免返回）
                router.replace({
                    path: '/kefu'
                });
            }
            logout();
        },
    }
}
