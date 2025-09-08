<?php

namespace app\models\chat\user;


use crmeb\basic\BaseModel;
use think\Model;

class ChatUserLabelAssist extends BaseModel
{

    /**
     * 表名
     * @var string
     */
    protected $name = 'chat_user_label_assist';

    /**
     * 主键
     * @var string
     */
    protected $key = 'id';

    /**
     * @param Model $query
     * @param $value
     */
    public function searchUserIdAttr($query, $value)
    {
        $query->where('user_id', $value);
    }
}
