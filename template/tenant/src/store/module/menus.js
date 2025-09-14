

/**
 * 布局菜单配置
 * */
import { getStaticMenusData, staticMenusData, transformedMenus } from '@/data/static-menus'

function getMenusName () {
    // 直接使用static-menus.js中已经构建好的树形菜单数据
    // 避免重复构建
    return transformedMenus
}
export default {
    namespaced: true,
    state: {
        menusName: getMenusName(),
        openMenus: []
    },
    mutations: {
        getmenusNav (state, menuList) {
            state.menusName = menuList
        },
        // getopenMenus (state, openList) {
        //   state.openMenus = openList
        // }
        setopenMenus (state, openList) {
            state.openMenus = openList
        }
    },
    actions: {
        getMenusNavList ({ commit }) {
            return new Promise((resolve, reject) => {
                getStaticMenusData().then(async res => {
                    resolve(res)
                    commit('getmenusNav', res.data)
                }).catch(res => {
                    reject(res)
                })
            })
        }
    }
}
