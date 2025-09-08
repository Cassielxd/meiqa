<?php

namespace crmeb\listeners;


use crmeb\interfaces\ListenerInterface;
use think\facade\Log;

class TestListen implements ListenerInterface
{

    public function handle($event): void
    {
        Log::error('我執行了');
    }
}
