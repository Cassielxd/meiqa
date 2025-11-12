
import {wss, getCookies} from '@/libs/util';
import {netWorkPing} from '@/api/kefu';
import Setting from '@/setting';
import Vue from 'vue';


let reconneTimer = {};
let reconneCount = {};
let connectGuid = {};
let NetWork = null;
const socketRegistry = {};

class wsSocket {
    constructor(opt) {
        this.vm = new Vue;
        this.ws = null;
        this.opt = opt || {};
        this.networkStatus = true;
        this.reconneMax = 100;
        this.connectLing = false;
        this.destroyed = false;
        this.boundTimeoutHandler = this.timeoutEvent.bind(this);
        reconneTimer[this.opt.key] = null;
        reconneCount[this.opt.key] = 0;
        this.init(opt);
        this.networkWath();
        this.defaultEvenv();
        this.handleTokenUpdate = this.handleTokenUpdate.bind(this);
        window.addEventListener('kefu-token-updated', this.handleTokenUpdate);
    }

    defaultEvenv() {
        this.vm.$on('timeout', this.boundTimeoutHandler);
    }

    handleTokenUpdate(event) {
        if (this.destroyed || !this.opt || this.opt.key !== 2) return;
        const newToken = event && event.detail ? event.detail.token : null;
        if (!newToken || newToken === this.opt.token) return;
        this.opt.token = newToken;

        if (this.socketStatus && this.ws) {
            this.socketStatus = false;
            this.connectLing = false;
            try {
                this.ws.close();
            } catch (e) {
                console.warn('[WebSocket] Failed to close during token refresh:', e);
            }
            setTimeout(() => {
                if (!this.destroyed) {
                    this.init(this.opt);
                }
            }, 0);
            return;
        }

        if (!this.socketStatus && !this.connectLing) {
            this.init(this.opt);
        }
    }

    destroy(forceClose = true) {
        if (this.destroyed) return;
        this.destroyed = true;
        this.vm.$off('timeout', this.boundTimeoutHandler);
        window.removeEventListener('kefu-token-updated', this.handleTokenUpdate);
        if (this.timer) {
            clearInterval(this.timer);
            this.timer = null;
        }
        if (reconneTimer[this.opt.key]) {
            clearInterval(reconneTimer[this.opt.key]);
            reconneTimer[this.opt.key] = null;
        }
        reconneCount[this.opt.key] = 0;
        this.socketStatus = false;
        this.connectLing = false;
        if (forceClose && this.ws) {
            try {
                this.ws.close();
            } catch (e) {
                console.warn('[WebSocket] Error while closing connection:', e);
            }
        }
        this.ws = null;
        this.opt.onDestroy && this.opt.onDestroy();
    }

    timeoutEvent() {
        this.reconne();
    }

    guid() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            var r = Math.random() * 16 | 0,
                v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }

    addHandler(element, type, handler) {
        if (element.addEventListener) {
            element.addEventListener(type, handler, false);
        } else if (element.attachEvent) {
            element.attachEvent("on" + type, handler);
        } else {
            element["on" + type] = handler;
        }
    }

    networkStatusFn(onlineFun, offlineFun) {
        this.addHandler(window, 'online', () => {
            onlineFun()
        })
        this.addHandler(window, 'offline', () => {
            offlineFun()
        });
    }

    networkStatusFnv2(onlineFun, offlineFun) {
        if (NetWork) {
            clearInterval(NetWork);
            NetWork = null;
        }
        let online = null,
            offline = null;
        NetWork = setInterval(() => {
            netWorkPing().then(res => {
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
        }, 1000)
    }

    networkWath() {
        this.networkStatusFn(() => {
            this.networkStatus = true;
            console.log('联网了')
            this.vm.$on('timeout', this.timeoutEvent);
        }, () => {
            this.networkStatus = false;
            this.socketStatus = false;
            this.timer && clearInterval(this.timer);
            this.timer = null;
            this.ws.close();
            console.log('断网了')
        });
    }

    reconne() {

        if (reconneCount[this.opt.key] > this.reconneMax) {
            //重连次数超过限制不再重连
            if (reconneTimer[this.opt.key]) {
                clearInterval(reconneTimer[this.opt.key]);
            }
            return;
        }
        if (reconneTimer[this.opt.key] || this.socketStatus) {
            return;
        }
        reconneTimer[this.opt.key] = setInterval(() => {
            //断线连接中发现状态为真就不用再连接
            if (this.socketStatus) {
                return;
            }
            //正在连接中也不需要在连接了
            if (!this.connectLing) {
                console.log('重新连接')
                this.init(this.opt);
                reconneCount[this.opt.key]++;
            }
        }, 2000)
    }

    onOpen(key = false) {
        //关闭断线重连定时器
        clearInterval(reconneTimer[this.opt.key]);
        reconneTimer[this.opt.key] = null;

        this.connectLing = false;
        this.opt.open && this.opt.open();
        reconneCount[this.opt.key] = 0
        this.socketStatus = true;
        this.ping();
    }

    init(opt) {
        if (this.socketStatus) {
            return;
        }
        let wsUrl = ''
        let hostUrl = wss(Setting.wsSocketUrl);

        hostUrl = hostUrl + '/ws';

        if (opt.key == 1) {
            wsUrl = hostUrl + '?type=admin' + '&token=' + (getCookies("token") || "")
        }
        if (opt.key == 2) {
            wsUrl = hostUrl + `?type=kefu` + '&token=' + `${opt.token}`;
        }
        if (opt.key == 3) {
            wsUrl = `${hostUrl}?type=user&form=${opt.form}&token=${opt.token}`;
        }
        if (opt.tourist_uid) {
            wsUrl += '&tourist_uid=' + opt.tourist_uid
        }
        if (wsUrl) {
            this.connectLing = true;
            // connectGuid[opt.key] = this.guid();
            this.ws = new WebSocket(wsUrl);
            this.ws.onopen = this.onOpen.bind(this);
            this.ws.onerror = this.onError.bind(this);
            this.ws.onmessage = this.onMessage.bind(this);
            this.ws.onclose = this.onClose.bind(this);
        }

    }

    ping() {
        var that = this;
        this.timer = setInterval(() => {
            that.send({type: 'ping'});
        }, 10000);
    }

    send(data) {
        if (!this.socketStatus || this.ws.readyState === 0 || !this.networkStatus) {
            this.reconne();
        }
        return new Promise((resolve, reject) => {
            try {
                this.ws.send(JSON.stringify(data));
                resolve({status: true});
            } catch (e) {
                console.log(e)
                reject({status: false, socketStatus: this.socketStatus, networkStatus: this.networkStatus})
            }
        });
    }

    onMessage(res) {
        this.opt.message && this.opt.message(res);
    }

    onClose() {
        if (this.destroyed) {
            return;
        }
        this.connectLing = false;
        this.timer && clearInterval(this.timer);
        this.timer = null;
        this.opt.close && this.opt.close();
        this.socketStatus = false;
        this.reconne();
    }

    onError(e) {
        if (this.destroyed) {
            return;
        }
        this.connectLing = false;
        this.timer && clearInterval(this.timer);
        this.timer = null;
        this.opt.error && this.opt.error(e);
        this.socketStatus = false;
        this.reconne();
    }

    $on(...args) {
        this.vm.$on(...args);
    }

    $off(...args) {
        this.vm.$off(...args);
    }
}

let promises = {};

function createSocket(key, flag, token, tourist_uid, type, form) {
    if (flag) {
        if (socketRegistry[key]) {
            socketRegistry[key].destroy(true);
            delete socketRegistry[key];
        }
        promises[key] = null;
    }
    if (!promises[key])
        promises[key] = new Promise((resolve, reject) => {
            const ws = new wsSocket({
                key,
                token,
                tourist_uid,
                type,
                form,
                open() {
                    resolve(ws);
                },
                error(e) {
                    reject(e)
                },
                message(res) {
                    const {type, data = {}} = JSON.parse(res.data);
                    ws.vm.$emit(type, data);
                },
                close(e) {
                    ws.vm.$emit('close', e);
                },
                onDestroy() {
                    if (socketRegistry[key] === ws) {
                        delete socketRegistry[key];
                        promises[key] = null;
                    }
                }
            })
            socketRegistry[key] = ws;
        });

    return promises[key];
}


export const adminSocket = (flag, token) => createSocket(1, flag, token);
export const Socket = (flag, token, tourist_uid, type) => createSocket(2, flag, token, tourist_uid, type);
export const mobileScoket = (flag, token, form, tourist_uid, type,) => createSocket(3, flag, token, tourist_uid, type, form);
