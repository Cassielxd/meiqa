<?php

namespace app\dao\chat\user;


use app\models\chat\user\ChatUserLabel;
use crmeb\basic\BaseDao;

/**
 * 用户标签
 * Class ChatUserLabelDao
 * @package app\dao\chat\user
 */
class ChatUserLabelDao extends BaseDao
{


    /**
     * 获取当前模型
     * @return string
     */
    protected function setModel(): string
    {
        return ChatUserLabel::class;
    }
}
