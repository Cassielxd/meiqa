<?php

namespace app\models\chat;


use crmeb\basic\BaseModel;
use crmeb\traits\ModelTrait;
use think\Model;

/**
 * 客服留言反馈
 * Class StoreServiceFeedback
 * @package app\models\chat
 */
class ChatServiceFeedback extends BaseModel
{

    use ModelTrait;

    /**
     * @var string
     */
    protected $name = 'chat_service_feedback';

    /**
     * @var string
     */
    protected $pk = 'id';

    /**
     * @param $value
     * @return false|string
     */
    public function getAddTimeAttr($value)
    {
        return date('Y-m-d H:i:s', $value);
    }

    /**
     * 标题搜索
     * @param Model $query
     * @param $value
     */
    public function searchTitleAttr($query, $value)
    {
        $value && $query->whereLike('rela_name|phone|content|user_id', "%" . $value . "%");
    }

}
