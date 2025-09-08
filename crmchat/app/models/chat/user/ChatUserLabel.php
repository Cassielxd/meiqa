<?php

namespace app\models\chat\user;


use app\models\chat\ChatUser;
use crmeb\basic\BaseModel;
use think\Model;
use think\model\relation\HasManyThrough;

/**
 * 设置用户标签
 * Class ChatUserLabel
 * @package app\models\chat\user
 */
class ChatUserLabel extends BaseModel
{

    /**
     * @var string
     */
    protected $name = 'chat_user_label';

    /**
     * @var string
     */
    protected $key = 'id';

    /**
     * @return HasManyThrough
     */
    public function user()
    {
        return $this->hasManyThrough(
            ChatUser::class,
            ChatUserLabelAssist::class,
            'label_id',
            'id',
            'id',
            'user_id'
        );
    }

    /**
     * @return \think\model\relation\HasOne
     */
    public function userone()
    {
        return $this->hasOne(ChatUserLabelAssist::class,'label_id','id');
    }

    /**
     * @param Model $query
     * @param $value
     */
    public function searchCateIdAttr($query, $value)
    {
        if ($value != '') {
            $query->where('cate_id', $value);
        }
    }

}
