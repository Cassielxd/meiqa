<?php



namespace crmeb\listeners;

use app\webscoket\Manager;
use crmeb\interfaces\ListenerInterface;
use Swoole\Server;
use Swoole\Server\Task;
use think\facade\Log;

class SwooleTaskListen implements ListenerInterface
{
    /**
     * @var Task
     */
    protected $task;

    public function handle($task): void
    {
        $this->task = $task;
        if (method_exists($this, $task->data['type'])) {
            $this->{$task->data['type']}($task->data['data']);
        } else {
            Log::error('任务执行失败,' . $task->data['type'] . '方法不存在');
        }
//        异步事件执行回调
//        $task->finish($task->data);
        return;
    }

    public function message(array $data)
    {
        /** @var Server $server */
        $server = app()->make(Server::class);
        $userId = is_array($data['user_id']) ? $data['user_id'] : [$data['user_id']];
        $except = $data['except'] ?? [];
        if (!count($userId) && $data['type'] != 'user') {
            $fds = Manager::userFd(0);
            foreach ($fds as $fd) {
                if (!in_array($fd, $except) && $server->isEstablished($fd))
                    $server->push((int)$fd, json_encode($data['data']));
            }
        } else {
            foreach ($userId as $id) {
                $fds = Manager::userFd($data['type'], $id);
                foreach ($fds as $fd) {
                    if (!in_array($fd, $except) && $server->isEstablished($fd))
                        $server->push((int)$fd, json_encode($data['data']));
                }
            }
        }
    }

}
