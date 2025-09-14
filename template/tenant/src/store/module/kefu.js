

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
         * */
        logoutKefu({ commit, dispatch }, { confirm = false, vm } = {}) {
            async function logout() {
                AccountLogoutKefu().then(() => {
                    console.log(Socket);
                    Socket(false).then(ws => {
                        ws.send({
                            type: 'logout',
                            data: { uid: getCookies('kefu_uuid') }
                        });
                        ws.onClose();
                    });
                    // localStorage.clear();
                    removeCookies('kefu_token')
                    removeCookies('kefu_expires_time')
                    removeCookies('kefuInfo')
                    removeCookies('kefu_uuid')
                    // 删除localStorage
                    // 清空 vuex 用户信息
                    // 跳转路由
                    router.push({
                        name: 'kefu_index'
                    });
                })
            }
            logout();
        },
    }
}
