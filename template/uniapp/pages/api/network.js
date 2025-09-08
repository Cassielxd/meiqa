import http from '@/pages/api/index.js'
import api from '@/pages/api/api.js'

export function onNetworkStatusChange(onlineFun, offlineFun) {

	//#ifdef H5
	function addHandler(element, type, handler) {
		if (element.addEventListener) {
			element.addEventListener(type, handler, false);
		} else if (element.attachEvent) {
			element.attachEvent("on" + type, handler);
		} else {
			element["on" + type] = handler;
		}
	}
	addHandler(window, 'online', () => {
		onlineFun()
	})
	addHandler(window, 'offline', () => {
		offlineFun()
	});
	//#endif
	//#ifdef APP-PLUS
	uni.onNetworkStatusChange(res => {
		if (res.networkType != 'none') {
			onlineFun(res);
		} else {
			offlineFun(res);
		}
	});
	//#endif

}

let NetWork = null;
export function onNetworkStatusChangeV2(onlineFun, offlineFun) {

	if (NetWork) {
		clearInterval(NetWork);
		NetWork = null;
	}
	let online = null,
		offline = null;
	NetWork = setInterval(() => {
		http(api.pingNetWork).then(() => {
			if (online === null) {
				onlineFun();
				online = true;
			}
			offline = null;
		}).catch(() => {
			if (offline === null) {
				offlineFun();
				offline = true;
			}
			online = null;
		})
	}, 1000);
}
