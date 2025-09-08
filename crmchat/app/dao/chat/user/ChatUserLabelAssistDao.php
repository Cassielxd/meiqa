<?php

namespace app\dao\chat\user;


use app\models\chat\user\ChatUserLabelAssist;
use crmeb\basic\BaseDao;

/**
 * Class ChatUserLabelAssistDao
 * @package app\dao\chat\user
 */
class ChatUserLabelAssistDao extends BaseDao
{

    /**
     * 获取当前模型
     * @return string
     */
    protected function setModel(): string
    {
        return ChatUserLabelAssist::class;
    }

    public function setUserLabel()
    {

    }
}
