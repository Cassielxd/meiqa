<?php

namespace app\controller\mobile;


class Comment
{
    /**
     * @return mixed
     */
    public function ping()
    {
        return app('json')->success(['time' => time()]);
    }
}
