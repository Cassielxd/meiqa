<?php

namespace app\dao\chat;


use app\models\chat\ChatServiceGroup;
use crmeb\basic\BaseDao;

class ChatServiceGroupDao extends BaseDao
{

    /**
     * @return string
     */
    protected function setModel(): string
    {
        return ChatServiceGroup::class;
    }
}
