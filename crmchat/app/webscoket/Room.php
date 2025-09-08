<?php
// +----------------------------------------------------------------------
// | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2016~2020 https://www.crmeb.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
// +----------------------------------------------------------------------
// | Author: CRMEB Team <admin@crmeb.com>
// +----------------------------------------------------------------------

namespace app\webscoket;


use Swoole\Table as SwooleTable;
use think\swoole\Table;

/**
 * 房间管理
 * Class Room
 * @package app\webscoket
 */
class Room
{

    /**
     * 类型 只有kefu和admin有区别
     * @var string
     */
    protected $type = '';

    /**
     * fd前缀
     * @var string
     */
    protected $tableFdPrefix = 'ws_fd_';

    /**
     *
     * @var array
     */
    protected $room = [];

    /**
     * @var \Redis
     */
    protected $cache;

    /**
     *
     */
    const USER_INFO_FD_PRE = 'socket_user_list';

    const TYPE_NAME = 'socket_user_type';

    /**
     * 设置缓存
     * @param $cache
     * @return $this
     */
    public function setCache($cache)
    {
        $this->cache = $cache;
        return $this;
    }

    /**
     * 设置表
     * @param string $type
     * @return $this
     */
    public function type(string $type)
    {
        $this->type = $type;
        return $this;
    }

    /**
     * 获取表实例
     * @return SwooleTable
     */
    public function getTable(string $tableName = 'user')
    {
        return app()->make(Table::class)->get($tableName);
    }

    /**
     * 添加fd
     * @param string $key fd
     * @param string $appid
     * @param int $userId
     * @param int $toUserId
     * @param int $tourist 是否为游客
     * @return mixed
     */
    public function add(string $key, string $appid, int $userId = 0, int $toUserId = 0, int $tourist = 0)
    {
        $nowkey = $this->tableFdPrefix . $key;
        $data = [
            'fd' => $key,
            'is_open' => 1,
            'appid' => $appid,
            'type' => $this->type ?: 'user',
            'user_id' => $userId,
            'to_user_id' => $toUserId,
            'tourist' => $tourist
        ];
        return $this->getTable()->set($nowkey, $data);
    }

    /**
     * 修改数据
     * @param string $key
     * @param null $field
     * @param null $value
     * @return bool|mixed
     */
    public function update(string $key, $field = null, $value = null)
    {
        $nowkey = $this->tableFdPrefix . $key;
        $res = true;
        if (is_array($field)) {
            $this->getTable()->del($nowkey);
            $res = $this->getTable()->set($nowkey, $field);
        } else if (!is_array($field) && $value !== null) {
            $data = $this->getTable()->get($nowkey);
            if (!$data) {
                return false;
            }
            $data[$field] = $value;
            $this->getTable()->del($nowkey);
            $res = $this->getTable()->set($nowkey, $data);
        }
        return $res;
    }

    /**
     * 重置
     * @return $this
     */
    public function reset()
    {
        $this->type = $this->typeReset;
        return $this;
    }

    /**
     * 删除
     * @param string $key
     * @return mixed
     */
    public function del(string $key)
    {
        return $this->getTable()->del($this->tableFdPrefix . $key);
    }

    /**
     * 是否存在
     * @param string $key
     * @return mixed
     */
    public function exist(string $key)
    {
        return $this->getTable()->exist($this->tableFdPrefix . $key);
    }

    /**
     * 获取fd的所有信息
     * @param string $key
     * @return array|bool|mixed
     */
    public function get(string $key, string $field = null)
    {
        return $this->getTable()->get($this->tableFdPrefix . $key, $field);
    }

    /**
     * 获取客服所有用户
     * @return array
     */
    public function getKefuRoomAll()
    {
        return $this->cache->sMembers('_ws_kefu');
    }
}
