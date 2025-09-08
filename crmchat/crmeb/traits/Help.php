<?php

namespace crmeb\traits;

/**
 * Trait Help
 * @package crmeb\traits
 */
trait Help
{

    /**
     * 成功返回
     * @param string $msg
     * @param array|null $data
     * @return mixed
     */
    protected function success($msg = 'ok', ?array $data = [])
    {
        return app('json')->success($msg, $data);
    }

    /**
     * 错误返回
     * @param string $msg
     * @param array|null $data
     * @return mixed
     */
    protected function fail($msg = 'error', ?array $data = [])
    {
        return app('json')->fail($msg, $data);
    }

}
