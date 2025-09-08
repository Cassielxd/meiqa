<?php

namespace crmeb\listeners;


use crmeb\interfaces\ListenerInterface;
use Swoole\Timer;

class SwooleShutdownListen implements ListenerInterface
{

    public function handle($event): void
    {
        Timer::clearAll();
    }
}
