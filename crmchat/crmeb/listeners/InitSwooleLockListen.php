<?php



namespace crmeb\listeners;


use crmeb\interfaces\ListenerInterface;
use Swoole\Lock;

class InitSwooleLockListen implements ListenerInterface
{

    public function handle($event): void
    {
        $GLOBALS['_swoole_order_lock'] = [];
        $locks = array_merge(['default'], config('swoole.locks', []));
        foreach ($locks as $lock) {
            $GLOBALS['_swoole_order_lock'][$lock] = new Lock(SWOOLE_MUTEX);
        }
    }
}
