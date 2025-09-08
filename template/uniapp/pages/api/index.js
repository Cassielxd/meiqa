import {
	Loading,
	hideLoading,
	Toast,
	navigateTo,
	cache
} from 'pages/utils/uniApi.js';
import apiHttp from 'pages/api/api.js';
import store from '../../store/index.js';

//检测是否去登录
async function toLogin() {
	if (!store.state.token) {
		if (cache.get('accountData')) {
			let res = await http(apiHttp.login, cache.get('accountData'));
			if (res.token) {
				cache.set('userData', res);
				cache.remove('pages_login');
				store.commit('setToken', res.token);
				return false;
			}
		}
		if (!cache.get('pages_login')) {
			navigateTo(3, '/pages/view/login/index');
		}
		return true;
	}
	return false;
}


function getHttpUrl() {
	let requestFile = cache.get('requestFile');
	let dominName = cache.get('dominName');
	return `${cache.get('requestFile')}://${cache.get('dominName')}`;
}

//检测并执行去设置域名
function checkHttpUrl() {
	let requestFile = cache.get('requestFile');
	let dominName = cache.get('dominName');
	let res = !!requestFile && !!dominName;
	if (!res) {
		cache.remove('pages_dominName');
		if (!cache.get('pages_dominName')) {
			navigateTo(1, '/pages/view/dominName/index');
		}
	}
	return res;
}

function http(api, data, base) {
	let baseUrl = getHttpUrl();
	let url = (base ? base : baseUrl) + api.url;
	// 是否在url上挂载单独参数时，也可以传输数据 eg: /api/{12}/ false 代表可传输，true 代表不可传输
	let canNotInputQuery = api.canNotInputQuery ? api.canNotInputQuery : false
	if (api.queryKey instanceof Array && api.queryKey.length) {
		url += api.queryKey.map(item => `/${data[item]}`).join('')
	}

	let post_data = {
		url: url,
		data: api.canNotInputQuery ? '' : data,
		header: {
			'content-type': api.form ? 'application/x-www-form-urlencoded' : 'application/json',
			'Authori-zation': 'Bearer ' + store.state.token
		},
		method: api.method.toUpperCase(),
		timeout: api.timeout || 3000,
	}

	return new Promise(async (reslove, reject) => {
		//检测域名是否存在
		if (!checkHttpUrl()) {
			return reject({
				msg: '域名填写错误， 请核实并修改域名'
			})
		}
		if (api.loading) {
			Loading();
		}
		//检测是否登录
		if (api.noAuth === undefined && await toLogin()) {
			return reject({
				msg: '请登录'
			})
		}
		uni.request({
			...post_data,
			success: (data) => {
				switch (data.data.status) {
					case 200:
						reslove(data.data.data);
						break;
					case 400:
						Toast(data.data.msg)
						reject(data.data);
						break;
					case 410003:
						// token失效后，自动登录
						if (cache.get('accountData')) {
							http(apiHttp.login, cache.get('accountData')).then(res => {
								cache.set('userData', res);
								store.commit('setToken', res.token);
							}).catch(res => {
								if (!cache.get('pages_login')) {
									navigateTo(2, '/pages/view/login/index');
								}
							})
						} else {
							if (!cache.get('pages_login')) {
								navigateTo(2, '/pages/view/login/index');
							}
						}
						break;
					default:
						Toast(data.data.msg)
						reject(data.data);
						break;
				}
			},
			fail: (rej) => {
				reject(rej);
			},
			complete: () => {
				if (api.loading) {
					hideLoading();
				}
			}
		});
	})

}


export default http;
