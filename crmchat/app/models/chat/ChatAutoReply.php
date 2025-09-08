<?php

namespace app\models\chat;


use crmeb\basic\BaseModel;

/**
 * Class ChatAutoReply
 * @package app\models\chat
 */
class ChatAutoReply extends BaseModel
{

    /**
     * @var string
     */
    protected $name = 'chat_auto_reply';

    /**
     * @var string
     */
    protected $key = 'id';

    /**
     * @param $query
     * @param $value
     */
    public function searchAppidAttr($query, $value)
    {
        $query->where('appid', $value);
    }

    /**
     * @param $query
     * @param $value
     */
    public function searchUserIdAttr($query, $value)
    {
        $query->where('user_id', $value);
    }
}
